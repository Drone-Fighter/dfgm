(ns dfgm.middleware
  (:require [clojure.string :as str]
            [clojurewerkz.gizmo.responder :refer [wrap-responder]]
            [compojure.core :refer [wrap-routes]]
            [environ.core :refer [env]]
            [dfgm.request :refer [wrap-request-binding]]
            [dfgm.services.db.core :refer [wrap-sql-logger]]
            [dfgm.utils :as u]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.refresh :refer [wrap-refresh]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :as res]
            [taoensso.timbre :as log]))

(defn wrap-request-logger [handler]
  (fn [req]
    (log/info (with-out-str (clojure.pprint/pprint req)))
    (handler req)))

(defn default-middleware [conf]
  #(wrap-defaults % conf))

(defn fix-header-key [s]
  (->> (str/split s #"-")
       (map str/capitalize)
       (str/join "-")))

(def fix-header
  (juxt (comp fix-header-key first) last))

(defn fix-headers [headers]
  (into {} (map fix-header headers)))

(defn wrap-header-fixer [handler]
  (fn [req]
    (let [res (handler req)]
      (update-in res [:headers] fix-headers))))

(defn middlewares [conf]
  (let [dev? (env :dev)]
    (u/conj-> []
              true wrap-responder
              true wrap-header-fixer
              dev? wrap-request-logger
              dev? wrap-request-binding
              dev? wrap-reload
              dev? wrap-refresh
              dev? wrap-exceptions
              dev? wrap-sql-logger
              true (default-middleware conf))))

(defn wrap-middlewares [middlewares handler]
  (reduce (fn [h m] (m h)) handler middlewares))

(defn wrap [handler conf]
  (wrap-middlewares (middlewares conf) handler))
