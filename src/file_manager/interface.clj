(ns file-manager.interface
  (:gen-class)
  (:use [file-manager.tags :as tags]
        [file-manager.metadata :as metadata])
  (:require [clojure.java.io :as io])
  (:import file-manager.filetree-model
           java.io.File
           java.nio.file.Files
           [java.util
            Vector]
           [java.awt
            Color GridBagLayout BorderLayout GridBagConstraints
            GridLayout Insets Dimension FlowLayout Image]
           [java.awt.event
            MouseAdapter ActionEvent ActionListener]
           [java.awt.image
            BufferedImage]
           [javax.swing
            JLabel JButton JPanel JFrame JTree JToolBar
            JScrollPane JSplitPane JTable JFileChooser
            ImageIcon SwingWorker SwingUtilities]
           [javax.swing.table
            DefaultTableModel]
           [javax.imageio
            ImageIO]
           [org.jxmapviewer
            JXMapViewer OSMTileFactoryInfo]
           [org.jxmapviewer.input
            CenterMapListener PanKeyListener PanMouseInputListener
            ZoomMouseWheelListenerCursor]
           [org.jxmapviewer.viewer
            GeoPosition TileFactoryInfo DefaultTileFactory
            Waypoint WaypointPainter DefaultWaypoint]
           WrapLayout))

(declare open-folder)
(declare select-image)

;; Simplifies the use of GridBagLayout

(defmacro set-grid!
  "Given a GridbagConstraints variable, add a field that changes a parameter
  to a specified value"
  [constraint field value]
  `(set! (. ~constraint ~(symbol (name field)))
         ~(if (keyword? value)
            `(. java.awt.GridBagConstraints
                ~(symbol (name value)))
            value)))

(defmacro grid-bag-layout
  "Given a GridBagLayout, GridBagConstraint parameters and Java Swing
  objects, automatically generate a full GridBagConstraints variable and
  generate the Java Swing objects in the new GridBagLayout:

  i.e.
  (grid-bag-layout
    GridBagLayout-Variable
    :weight-1 :value-1 :weight-2 :value-2
    (Java-Swing-Object-1)
    :weight-3 :value-3 :weight-4 :value-4
    (Java-Swing-Object-2)
  ...                    )

  Outputs a spliced list of (set-grid!) calls"
  [container & body]
  (let [c (gensym "c")
        cntr (gensym "cntr")]
    `(let [~c (GridBagConstraints.)
           ~cntr ~container]
       ~@(loop [result '() body body]
           (if (empty? body)
             (reverse result)
             (let [expr (first body)]
               (if (keyword? expr)
                 (recur (cons `(set-grid! ~c ~expr ~(second body))
                              result)
                        (next (next body)))
                 (recur (cons `(.add ~cntr ~expr ~c)
                              result)
                        (next body)))))))))

(def app-frame (JFrame. "File Manager"))  ; Main frame of app
(def explorers (atom (vector)))            ; List of currently displayed file JPanels
(def current-explorer (atom 0))
(def selected-photo (atom (io/file "")))  ; File path to the image being observed currently
(def info (atom {:pane (JPanel.) :image-file (io/file "")}))
(def root-folder (atom (io/file "")))     ; Path to the root folder being displayed in the tree
(def map-viewer (atom {:viewer (JXMapViewer.) :waypoints (hash-set) :waypoint-painter (WaypointPainter.)}))
(def tool-bar (atom {:text (JLabel.)}))

;; Creates App Panes:
;; +---+-------------+---+
;; | A | B           | C |
;; |   |             |   |
;; |   |             +---+
;; |   |             | D |
;; |   |             |   |
;; +---+-------------+---+
;;
;; A - Tree Pane (Shows tree listing of files/directories, starting from a specified root)
;; B - File Explorer (Shows Windows-Explorer style listing of files/folders in grid)
;; C - Info Pane (Shows details of the selected file, i.e. EXIF data)
;; D - GPS Pane (Shows GPS info of current file and map of its location)

;; TODO: Link panes to functions and begin populating them

(defn tree-pane
  "Creates tree-style listing of files from root"
  []
  (doto (JScrollPane.
         (doto (JTree. (file-manager.filetree-model. @root-folder))
           (.setVisibleRowCount 10)))
    (.setVerticalScrollBarPolicy JScrollPane/VERTICAL_SCROLLBAR_ALWAYS)))

(def read-img
  (memoize #(. ImageIO read %)))

(defn is-image
  [file]
  (let [mime (. Files probeContentType (.toPath file))]
    (and (not (nil? mime)) (.startsWith mime "image/"))))


(defn add-to-file-history
  [n file]
  (swap! explorers update-in [n :history] conj file))

(defn load-img
  [file]
  (ImageIcon. (.getScaledInstance (.getImage (ImageIcon. (read-img file))) 150 150 Image/SCALE_FAST)))

(defn make-file
  "Creates a JPanel representing the icon/description of a file"
  [n file]
  (let [file-pane (doto (JPanel.)
                    (.setLayout (BorderLayout.))
                    (.add (JLabel. (.getName file)) BorderLayout/SOUTH)
                    (.setBackground Color/GRAY)
                    (.setPreferredSize (Dimension. 150 150))
                    (.addMouseListener
                     (proxy [MouseAdapter] []
                       (mouseClicked [e]
                         (cond
                           (.isDirectory file) (do
                                                 (add-to-file-history n (:folder (get @explorers n)))
                                                 (swap! explorers assoc-in [n :folder] file)
                                                 (open-folder n))
                           (is-image file) (select-image file))))))]
    (if (and (not (.isDirectory file)) (is-image file))
      (doto file-pane
        (.add (JLabel. (load-img file)) BorderLayout/CENTER))
      file-pane)))

(defn get-valid-files
  [n]
  (let [explorer (get @explorers n)]
    (->> (:folder explorer)
         .listFiles
         (filter #(or (:dotfiles (:flags explorer))
                      (not= (first (.getName %)) \.))))))

(defn open-folder
  "Reloads files present in file explorer based on the current directory"
  [n]
  (let [explorer (get @explorers n)]
    (.removeAll (:pane explorer))
    (.setText (:text @tool-bar) (str (.getAbsolutePath (:folder explorer)) ": Loading..."))
    (run! #(doto (:pane explorer)
                 (.add (make-file n %))
                 .revalidate
                 .repaint)
          (get-valid-files n))
    (.setText (:text @tool-bar) (str (.getAbsolutePath (:folder explorer)) ": Loaded."))))

(defn open-prev-folder
  [n]
  (when (not (empty? (:history (get @explorers n))))
    (swap! explorers assoc-in [n :folder] (last (:history (get @explorers n))))
    (swap! explorers update-in [n :history] pop)
    (open-folder n)))

(defn file-explorer
  "Creates grid layout of files in currently open directory"
  [folder]
  (do
    (swap! explorers
           conj {:pane (JPanel. (WrapLayout. WrapLayout/LEFT))
                 :folder folder
                 :history (vector)
                 :items (vector)
                 :flags {:dotfiles false}})
    (JScrollPane. (:pane (peek @explorers)))))

(defn select-image
  [file-path]
  (if (nil? file-path)
    nil
    (let [table-model (proxy [DefaultTableModel] [0 2] (isCellEditable [row col] false))
          gps-coordinates (gps-data file-path)]
      (when (not (nil? gps-coordinates))
        (swap! map-viewer
               update-in [:waypoints]
               conj (DefaultWaypoint. (GeoPosition.
                                       (.getLatitude gps-coordinates)
                                       (.getLongitude gps-coordinates))))
        (.setWaypoints (:waypoint-painter @map-viewer) (:waypoints @map-viewer))
        (.revalidate (:viewer @map-viewer))
        (.repaint (:viewer @map-viewer)))
      (run! #(.addRow table-model (Vector. %))
            (metadata/exif-info file-path))
      (doto (:pane @info)
        (.setLayout (BorderLayout.))
        .removeAll
        (.add (JTable. table-model))
        .revalidate)
      (swap! info assoc-in [:image-file] file-path))))

(defn info-pane
  "Displays info and metadata from the selected file"
  []
  (doto (JScrollPane. (:pane @info))))

(defn gps-pane
  "Creates pane with GPS Location"
  []
  (let [default-location (GeoPosition. 49.246292 -123.116226)
        mouse-input-listener (PanMouseInputListener. (:viewer @map-viewer))]
    (doto (:viewer @map-viewer)
      (.setTileFactory (DefaultTileFactory. (OSMTileFactoryInfo.)))
      (.setZoom 6)
      (.setAddressLocation default-location)
      (.addMouseListener mouse-input-listener)
      (.addMouseMotionListener mouse-input-listener)
      (.addMouseListener (CenterMapListener. (:viewer @map-viewer)))
      (.addMouseWheelListener (ZoomMouseWheelListenerCursor. (:viewer @map-viewer)))
      (.addKeyListener (PanKeyListener. (:viewer @map-viewer)))
      (.setOverlayPainter (:waypoint-painter @map-viewer)))
    (doto (JScrollPane.
           (doto (JPanel.)
             (.setLayout (BorderLayout.))
             (.add (:viewer @map-viewer)))))))

(defn init-toolbar
  []
  (doto (JToolBar.)
    (.setFloatable false)
    (.setRollover true)
    (.add (doto (JButton.)
            (.setText "Back")
            (.addActionListener
             (proxy [ActionListener] []
               (actionPerformed [e]
                 (open-prev-folder @current-explorer))))))
    (.add (doto (JButton.)
            (.setText "Up")
            (.addActionListener
             (proxy [ActionListener] []
               (actionPerformed [e]
                 (do
                   (add-to-file-history @current-explorer (:folder (get @explorers @current-explorer)))
                   (swap! explorers assoc-in [@current-explorer :folder] (.getParentFile (:folder (get @explorers @current-explorer))))
                   (open-folder @current-explorer)))))))
    (.add (doto (JButton.)
            (.setText "Reload")
            (.addActionListener
             (proxy [ActionListener] []
               (actionPerformed [e]
                 (open-folder @current-explorer))))))
    (.add (:text @tool-bar))))

(defn init-app
  "Initializes frame and creates panes corresponding to the user interface"
  []
  (do
    (let [file-chooser (doto (JFileChooser.)
                          (.setFileSelectionMode JFileChooser/DIRECTORIES_ONLY)
                          (.showOpenDialog app-frame))]
      (reset! root-folder (.getSelectedFile file-chooser)))
    (let [content-pane
          (doto (JSplitPane. JSplitPane/HORIZONTAL_SPLIT
                             (doto (JSplitPane. JSplitPane/HORIZONTAL_SPLIT
                                                (tree-pane)
                                                (file-explorer @root-folder))
                               (.setResizeWeight 0.2))
                             (doto (JSplitPane. JSplitPane/VERTICAL_SPLIT
                                                (info-pane)
                                                (gps-pane))
                               (.setResizeWeight 0.7)))
            (.setResizeWeight 0.7))]
      (doto app-frame
        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.setLayout (BorderLayout.))
        (.add (init-toolbar) BorderLayout/NORTH)
        (.add content-pane BorderLayout/CENTER)
        (.setLocationRelativeTo nil)
        (.pack)
        (.setVisible true))))
  (swap! explorers assoc-in [0 :folder] @root-folder)
  (open-folder 0)
  (select-image nil))
