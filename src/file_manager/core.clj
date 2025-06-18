(ns file-manager.core
  (:gen-class)
  (:use file-manager.file-utils)
  (:import [javax.swing JFrame JLabel JButton]
           [java.awt.event WindowListener])
  (:require [clojure.java.io :as io])
  (:require clojure.pprint))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (copy (io/file "/home/personal/file") (io/file "/home/personal/Downloads"))
  (move (io/file "/home/personal/newdo") (io/file "/home/personal/file")))
