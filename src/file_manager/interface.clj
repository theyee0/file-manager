(ns file-manager.interface
  (:gen-class)
  (:import [java.awt
            Color GridBagLayout BorderLayout GridBagConstraints
            Insets Dimension])
  (:import [javax.swing JLabel JButton JPanel JFrame JTree JScrollPane]))


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
(def current-folder (atom ""))
(def selected-photo (atom ""))
(def folder-root (atom ""))


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
  (doto (JScrollPane. (doto (JTree.)
                        (.setBackground Color/GREEN)
                        (.setPreferredSize (Dimension. 300 300))))))
    

(defn file-explorer
  "Creates grid layount of files in currently open directory"
  []
  (doto (JScrollPane. (doto (JPanel.)
                        (.setBackground Color/RED)
                        (.setPreferredSize (Dimension. 300 300))))))

(defn info-pane
  "Displays info and metadata from the selected file"
  []
  (doto (JScrollPane. (doto (JPanel.)
                        (.setBackground Color/BLUE)
                        (.setPreferredSize (Dimension. 300 300))))))

(defn gps-pane
  "Creates pane with GPS Location"
  []
  (doto (JScrollPane. (doto (JPanel.)
                        (.setBackground Color/ORANGE)
                        (.setPreferredSize (Dimension. 300 300))))))

(defn init-app
  "Initializes frame and creates panes corresponding to the user interface"
  []
  (let [content-pane (doto (JPanel. (GridBagLayout.))
                       (grid-bag-layout
                           :fill :BOTH :weightx 1 :weighty 1
                           :gridx 0 :gridy 0 :gridheight 2
                           (tree-frame)
                           :gridx 1 :gridy 0 :gridheight 2
                           (file-explorer)
                           :gridx 2 :gridy 0 :gridheight 1
                           (info-pane)
                           :gridx 2 :gridy 1 :gridheight 1
                           (gps-pane))
                       (.setSize 500 500))]
    (doto app-frame
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setLayout (BorderLayout.))
      (.add content-pane)
      (.setLocationRelativeTo nil)
      (.pack)
      (.setVisible true))))

