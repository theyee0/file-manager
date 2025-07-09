(ns file-manager.interface
  (:gen-class)
  (:import file-manager.filetree-model)
  (:use [file-manager.tags :as tags])
  (:require [clojure.java.io :as io])
  (:import java.io.File)
  (:import [java.awt
            Color GridBagLayout BorderLayout GridBagConstraints
            FlowLayout Insets Dimension])
  (:import [javax.swing JLabel JButton JPanel JFrame JTree
            JScrollPane JSplitPane])
  (:import [org.jxmapviewer
            JXMapViewer OSMTileFactoryInfo])
  (:import [org.jxmapviewer.viewer
            GeoPosition TileFactoryInfo DefaultTileFactory]))

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
(def open-files (atom (list)))            ; List of currently displayed files
(def selected-photo (atom (io/file "")))  ; File path to the image being observed currently
(def current-folder (atom (io/file "")))  ; Path to the folder open in the file explorer
(def root-folder (atom (io/file "")))     ; Path to the root folder being displayed in the tree
(def map-viewer (JXMapViewer.))

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
           (.setBackground Color/GREEN)
           (.setVisibleRowCount 10)))
    (.setVerticalScrollBarPolicy JScrollPane/VERTICAL_SCROLLBAR_ALWAYS)))

(defn file-explorer
  "Creates grid layount of files in currently open directory"
  []
  (doto (JScrollPane.
         (doto (JPanel.)
           (.setLayout (FlowLayout.))
           (.setBackground Color/RED)))))

(defn info-pane
  "Displays info and metadata from the selected file"
  []
  (doto (JScrollPane.
         (doto (JPanel.)
           (.setLayout (BorderLayout.))
           (.setBackground Color/BLUE)))))

(defn gps-pane
  "Creates pane with GPS Location"
  []
  (do
    (doto map-viewer
      (.setTileFactory (DefaultTileFactory. (OSMTileFactoryInfo.)))
      (.setZoom 6)
      (.setAddressLocation (GeoPosition. 50 9)))
    (doto (JScrollPane.
           (doto (JPanel.)
             (.setLayout (BorderLayout.))
             (.add map-viewer))))))


(defn init-app
  "Initializes frame and creates panes corresponding to the user interface"
  []
  (do
    (tags/initialize-db)
    (reset! root-folder (io/file "/home/personal"))
    (reset! current-folder @root-folder)
    (let [content-pane
          (doto (JSplitPane. JSplitPane/HORIZONTAL_SPLIT
                             (doto (JSplitPane. JSplitPane/HORIZONTAL_SPLIT
                                                (tree-pane)
                                                (file-explorer))
                               (.setResizeWeight 0.2))
                             (doto (JSplitPane. JSplitPane/VERTICAL_SPLIT
                                                (info-pane)
                                                (gps-pane))
                               (.setResizeWeight 0.7)))
            (.setResizeWeight 0.7))
          menu-bar
          (doto (JPanel.)
            (.setPreferredSize (Dimension. 100 50)))]
      (doto app-frame
;;        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.setLayout (BorderLayout.))
        (.add menu-bar BorderLayout/NORTH)
        (.add content-pane BorderLayout/CENTER)
        (.setLocationRelativeTo nil)
        (.pack)
        (.setVisible true)))))
