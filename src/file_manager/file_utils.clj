(ns file-manager.file-utils
  (:require [clojure.java.io :as io])
  (:import java.io.File))

; Internal Utilities
(defn absolute-paths
  "Converts all java.io File in a list into absolute paths"
  [path-list]
  (map #(.getAbsolutePath %) path-list))

(defn add-to-path
  "Concatenates a file/folder (str) name to an existing path"
  [dest addition]
  (io/file (str (.getPath dest) "/" addition)))

(defn repeats?
  "Checks whether or not any filepaths in a list are equivalent"
  [path-list]
  (let [path-list (absolute-paths path-list)]
    (not= (count path-list) (count (set path-list)))))

; Useful user utilities
(defn move 
  "Moves/renames a file or folder"
  [dest src]
  (.renameTo src dest))

(defn copy
  "Recursively copies files/folders to a destination folder. Makes all new folders."
  [dest object]
  (let [dest (add-to-path dest (.getName object))]
    (if (.isDirectory object)
      (do
        (.mkdirs dest)
        (doseq [file (.listFiles object)]
          (copy dest file)))
      (io/copy object dest))))

(defn delete
  "Recursively deletes a file/folder"
  [object]
  (do
    (if (.isDirectory object)
      (doseq [file (.listFiles object)]
        (delete file)))
    (.delete object)))
