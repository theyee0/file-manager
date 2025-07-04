(ns file-manager.filetree-model
  (:require [clojure.java.io :as io])
  (:import java.io.File)
  (:gen-class
   :name file-manager.filetree-model
   :implements [javax.swing.tree.TreeModel]
   :state state
   :init init
   :constructors {[java.io.File] []}))

(defn -init
  [file-path]
  [[] (ref {:root file-path})])

(defn -addTreeModelListener
  [this l])

(defn -getChild
  [this parent index]
  (nth (.listFiles parent) index))

(defn -getChildCount
  [this parent]
  (if (.isDirectory parent)
    (->> parent
         (.list)
         (into (list))
         count)
    0))


(defn -getIndexOfChild
  [this parent child]
  (.indexOf (into (list) (.listFiles parent)) child))

(defn -getRoot
  [this]
  (:root @(.state this)))

(defn -isLeaf
  [this node]
  (not (.isDirectory node)))

(defn -removeTreeModelListener
  [this l])

(defn -valueForPathChanged
  [this path newValue])
