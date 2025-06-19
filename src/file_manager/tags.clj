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
                                 [:tag "varchar(64)"]])
        (jdbc/create-table-ddl :imagepath
                               [[:id  "integer not null primary key"]
                                [:img :text]])
        (jdbc/create-table-ddl :bridge
                               [[:imageid :int]
                                [:tagid   :int]])]))

(defn lookup-img-path
  [id]
  (jdbc/find-by-keys db :imagepath {:id id}))

(defn get-img-id
  [img-path]
  (jdbc/find-by-keys db :imagepath {:img img-path]))

(defn lookup-tags
  [id]
  (jdbc/find-by-keys db :tags {:id id}))

(defn get-tag-id
  [tag]
  (jdbc/find-by-keys db :tags {:tag tag}))

(defn add-img
  [img-path]
  (if (empty? (get-img-id img-path))
    (insert! db :imagepath {:img img-path})))

(defn add-tag
  [tag]
  (if (empty? (get-tag-id tag))
    (insert! db :tags {:tag tag})))

(defn append-tag
  [img-path tag]
  (do
    (add-img img-path)
    (add-tag tag)
    (let [imgid (lookup-img-path img-path) ; Properly get actual id
          tagid (lookup-tags tag)]
      (insert! db {:imageid imgid :tagid tagid}))))
