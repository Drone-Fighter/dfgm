(ns dfgm.services.db.service
  (:require [jdbc.pool.hikari :as pool]
            [dfgm.services.db.core :as core]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]
            [taoensso.timbre :as log]))

(defprotocol DatabaseService
  (wrap-db [this handler])
  (wrap-with-db [this handler]))

;;; TODO: multiple db spec
(defservice db-service
  DatabaseService
  [[:ConfigService get-in-config]]
  (init [this context]
        (log/info "Initializing database")
        (let [spec (get-in-config [:db])]
          (-> context
              (assoc :db (pool/make-datasource-spec spec)))))
  (stop [this context]
        (log/info "Shutting down database")
        (when-let [src (:db context)]
          (.close src))
        context)
  (wrap-db [this handler]
           (let [context (service-context this)
                 db (:db context)]
             (fn [req]
               (handler (assoc req :db db)))))
  (wrap-with-db [this handler]
                (let [context (service-context this)
                      db (:db context)]
                  (fn [req]
                    (core/with-db db
                      (handler req))))))
