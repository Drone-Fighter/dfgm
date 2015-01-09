(ns dfgm.services.db.core
  (:require [jdbc.core :as jdbc]
            [stch.sql.format :as fmt]
            [stch.sql :refer :all]
            [taoensso.timbre :as log]))

(def ^:dynamic *current-db* nil)
(def ^:dynamic *current-conn* nil)
(def ^:dynamic *print-sql?* false)

(defmacro with-db [db & body]
  `(jdbc/with-connection [conn# ~db]
     (binding [*current-db* ~db
               *current-conn* conn#]
       ~@body)))

(defn query
  ([q]
   (query q {}))
  ([q opt]
   (let [sql (fmt/format q)]
     (when *print-sql?*
       (log/debug sql))
     (jdbc/query *current-conn* sql opt))))

(defn wrap-sql-logger [handler]
  (fn [req]
    (binding [*print-sql?* true]
      (handler req))))
