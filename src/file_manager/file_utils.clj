(ns file-manager.file-utils
  (:require [clojure.java.io :as io])
  (:import java.io.File))

; Internal Utilities
(defn absolute-paths
  "Converts all java.io File in a list into absolute paths"
  [path-list]
  (map #(.getAbsolutePath %) path-list))

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
  "Recursively copies files/folders to a destination folder."
  [dest src]
  (if (.isDirectory src)
    (do
      (.mkdirs dest)
      (doseq [file (.listFiles src)]
        (copy (io/file dest (.getName file)) file)))
    (io/copy src dest)))

(defn copy-into
  "Recursively copies files/folders, but creates a subdirectory if destination exists already"
  [dest src]
  (if (.exists dest)
    (copy (io/file dest (.getName src)) src)
    (copy dest src)))

(defn delete
  "Recursively deletes a file/folder"
  [object]
  (do
    (if (.isDirectory object)
      (doseq [file (.listFiles object)]
        (delete file)))
    (.delete object)))
