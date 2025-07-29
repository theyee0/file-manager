(ns file-manager.core
  (:gen-class)
  (:use file-manager.file-utils)
  (:use file-manager.tags)
  (:use file-manager.interface)
  (:import [javax.swing JFrame JLabel JButton]
           [java.awt.event WindowListener]
           [com.formdev.flatlaf FlatLaf FlatLightLaf])
  (:require [clojure.java.io :as io])
  (:require clojure.pprint))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (do
    (initialize-db)
    (. FlatLightLaf setup)
    (init-app)))
