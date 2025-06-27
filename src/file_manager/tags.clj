(ns file-manager.tags
  (:require [clojure.java.jdbc :as jdbc])
  (:gen-class))

(def db
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "db/database.db"})

(defn initialize-db
  "Create empty sqlite database with three tables"
  []
  (run! #(jdbc/db-do-commands db %)
        [(jdbc/create-table-ddl :tags
                                [[:id "integer not null primary key"]
                                 [:tag "varchar(64)"]] {:conditional? true})
        (jdbc/create-table-ddl :imagepath
                               [[:id  "integer not null primary key"]
                                [:img :text]] {:conditional? true})
        (jdbc/create-table-ddl :bridge
                               [[:imageid :int]
                                [:tagid   :int]] {:conditional? true})
         (jdbc/create-table-ddl :favorites
                                [[:id "integer not null primary key"]])]))

; Utilities for mapping integer ids to tags and images
(defn id-lookup
  "Finds an item in a given table with a certain id"
  [table id]
  (jdbc/get-by-id db table id :id))

(defn get-img-id
  "Given an image path, finds the corresponding image id"
  [img-path]
  (:id (jdbc/get-by-id db :imagepath img-path :img)))

(defn get-tag-id
  "Given a tag, finds the corresponding ID"
  [tag]
  (:id (jdbc/get-by-id db :tags tag :tag)))

; Add items to tables
(defn add-img
  "Adds an image to the database if it is not yet present"
  [img-path]
  (if (nil? (get-img-id img-path))
    (jdbc/insert! db :imagepath {:img img-path})))

(defn add-tag
  "Adds a tag to the database if it is not yet present"
  [tag]
  (if (nil? (get-tag-id tag))
    (jdbc/insert! db :tags {:tag tag})))

(defn get-tags
  "Lists all tags associated with a given image path"
  [img-path]
  (let [imgid (get-img-id img-path)]
    (->> (jdbc/find-by-keys db :bridge {:imageid imgid})
         (map :tagid)
         (map #(:tag (id-lookup :tags %))))))

(defn append-tag
  "Adds a tag to an image. If the tags and images are not yet in the database, they are added"
  [img-path tag]
  (do
    (add-img img-path)
    (add-tag tag)
    (let [imgid (get-img-id img-path)
          tagid (get-tag-id tag)]
      (if (empty? (filter #(= tag %) (get-tags img-path)))
        (jdbc/insert! db :bridge {:imageid imgid :tagid tagid})))))
