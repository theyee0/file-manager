(ns file-manager.metadata
  (:gen-class)
  (:use file-manager.filetree-model)
  (:import [com.drew
            imaging.ImageMetadataReader
            metadata.exif.GpsDirectory]))

(defn gps-data
  [filepath]
  (let [metadata (. ImageMetadataReader readMetadata filepath)]
    (first (filter #(and (not (nil? %)) (not (.isZero %)))
            (mapv #(.getGeoLocation %) (.getDirectoriesOfType metadata GpsDirectory))))))

(defn exif-info
  [filepath]
  (let [metadata (. ImageMetadataReader readMetadata filepath)]
    (mapcat
     #(map (fn [x] (list (.getTagName x) (.getDescription x))) (.getTags %))
     (.getDirectories metadata))))
