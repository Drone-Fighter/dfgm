(ns dfgm.request)

(declare ^{:dynamic true} *request*)

(defn get-request []
  *request*)

(defn wrap-request-binding [handler]
  (fn [req]
    (binding [*request* req]
      (handler req))))
