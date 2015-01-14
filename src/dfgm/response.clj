(ns dfgm.response
  (:require [cheshire.generate :refer [add-encoder encode-str]]))

(add-encoder org.joda.time.DateTime encode-str)

(defn json
  ([response-hash]
   (json response-hash 200))
  ([response-hash status]
   {:render :json
    :status status
    :response-hash response-hash}))

(defn json-not-found []
  (json {:error :not-found} 404))
