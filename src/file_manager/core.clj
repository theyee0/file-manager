(ns file-manager.core
  (:gen-class)
  (:use file-manager.file-utils)
  (:import [javax.swing JFrame JLabel JButton]
           [java.awt.event WindowListener])
  (:require [clojure.java.io :as io])
  (:require clojure.pprint))

(defn -main
  "I don't do a whole lot ... yet."
  [& args])
