(ns file-manager.file-utils
  (:require [clojure.java.io :as io])
  (:import java.io.File))

; Internal Utilities
(defn absolute-paths
  "Converts all java.io File in a list into absolute paths"
  [path-list]
  (map #(.getAbsolutePath %) path-list))

(defn repeats
  "Checks whether or not any files in a list are equivalent"
  [path-list]
  (let [path-list (absolute-paths path-list)]
    (not= (count path-list) (count (set path-list)))))

; Useful user utilities
(defn rename
  "Renames a file or folder"
  [src dest]
  (.renameTo src dest))

(defn copy-items
  [dest files])

(defn equivalent-paths
  [folder & args]
  (or map #(.equals folder %) args))

