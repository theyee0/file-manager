(ns file-manager.tags
  (:require [clojure.java.jdbc :as jdbc])
  (:gen-class))

(def db
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "db/database.db"})

(defn initialize-db
  []
  (run! #(do-commands db %)
        [(jdbc/create-table-ddl :tags
                                [[:id :int]
                                 [:tag "varchar(64)"]])
        (jdbc/create-table-ddl :images
                               [[:id  :int]
                                [:tag :text]])
        (jdbc/create-table-ddl :bridge
                               [[:imageid :int]
                                [:tagid   :int]])]))
  
