(ns file-manager.interface
  (:gen-class)
  (:import file-manager.filetree-model)
  (:require [clojure.java.io :as io])
  (:import java.io.File)
  (:import [java.awt
            Color GridBagLayout BorderLayout GridBagConstraints
            FlowLayout Insets Dimension])
  (:import [javax.swing JLabel JButton JPanel JFrame JTree
            JScrollPane JSplitPane]))


;; Simplifies the use of GridBagLayout

(defmacro set-grid!
  "Given a constraint, change a parameter to a specified value"
  [constraint field value]
  `(set! (. ~constraint ~(symbol (name field)))
         ~(if (keyword? value)
            `(. java.awt.GridBagConstraints
                ~(symbol (name value)))
            value)))


(defmacro grid-bag-layout
  "Given a GridBagLayout, add items in the body with specified parameters and objects:
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

(def app-frame (JFrame. "File Manager"))
(def current-folder (atom (io/file "")))
(def selected-photo (atom (io/file "")))
(def root-folder (atom (io/file "")))


;; Creates App Panes:
;; +---+-------------+---+
;; | A | B           | C |
;; |   |             |   |
;; |   |             +---+
;; |   |             | D |
;; |   |             |   |
;; +---+-------------+---+
;;
;; A - Tree Frame (Shows tree listing of files/directories, starting from a specified root)
;; B - File Explorer (Shows Windows-Explorer style listing of files/folders in grid)
;; C - Info Pane (Shows details of the selected file, i.e. EXIF data)
;; D - GPS Pane (Shows GPS info of current file and map of its location)

;; TODO: Link panes to functions and begin populating them

(defn tree-frame
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
  (doto (JScrollPane. (doto (JPanel.)
                        (.setLayout (FlowLayout.))
                        (.setBackground Color/RED)))))

(defn info-pane
  "Displays info and metadata from the selected file"
  []
  (doto (JScrollPane. (doto (JPanel.)
                        (.setLayout (BorderLayout.))
                        (.setBackground Color/BLUE)))))

(defn gps-pane
  "Creates pane with GPS Location"
  []
  (doto (JScrollPane. (doto (JPanel.)
                        (.setLayout (BorderLayout.))
                        (.setBackground Color/ORANGE)))))

(defn init-app
  "Initializes frame and creates panes corresponding to the user interface"
  []
  (do
    (reset! root-folder (io/file "/home/personal"))
    (reset! current-folder @root-folder)
    (let [content-pane
          (doto (JSplitPane. JSplitPane/HORIZONTAL_SPLIT
                             (JSplitPane. JSplitPane/HORIZONTAL_SPLIT
                                          (tree-frame)
                                          (file-explorer))
                             (JSplitPane. JSplitPane/VERTICAL_SPLIT
                                          (info-pane)
                                          (gps-pane))))]
      (doto app-frame
;;        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.setLayout (BorderLayout.))
        (.add content-pane)
        (.setLocationRelativeTo nil)
        (.pack)
        (.setVisible true)))))
