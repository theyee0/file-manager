(ns file-manager.metadata
  (:gen-class)
  (:use file-manager.filetree-model)
  (:import [com.drew
            imaging.ImageMetadataReader]))

(defn gps-data
  [filepath]
  (. ImageMetadataReader readMetadata filepath))

(defn exif-info
  [filepath]
  (let [metadata (. ImageMetadataReader readMetadata filepath)]
    (mapcat
     #(map (fn [x] (list (.getTagName x) (.getDescription x))) (.getTags %))
     (.getDirectories metadata))))
