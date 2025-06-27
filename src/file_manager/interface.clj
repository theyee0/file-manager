(ns file-manager.interface
  (:gen-class)
  (:import [java.awt Color GridBagLayout GridBagConstraints Insets])
  (:import [javax.swing JLabel JButton JPanel JFrame]))

(defmacro set-grid! [constraint field value]
  `(set! (. ~constraint ~(symbol (name field)))
         ~(if (keyword? value)
            `(. java.awt.GridBagConstraints
                ~(symbol (name value)))
            value)))

(defmacro grid-bag-layout [container & body]
  (let [c (gensym "c")
        cntr (gensym "cntr")]
    `(let [~c (new java.awt.GridBagConstraints)
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

(defn init-app []
  (let [panel (doto (JPanel. (GridBagLayout.))
                (grid-bag-layout
                    :fill :BOTH
                    :gridx 0, :gridy 0
                    (JButton. "Button One")
                    :gridx 1, :gridy 0
                    (JButton. "Button Two")
                    :gridx 2, :gridy 0
                    (JButton. "Button Three")
                    :ipady 40
                    :gridwidth 3, :gridheight 2
                    :gridx 0, :gridy 1
                    (JButton. "Long-named Button 4")
                    :ipady 0
                    :fill :HORIZONTAL, :insets (Insets. 10 0 0 0), :gridwidth 2
                    :gridx 1 :gridy 4
                    (JButton. "Button Five")))]
    (doto app-frame
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setContentPane panel)
      (.pack)
      (.setVisible true))))
