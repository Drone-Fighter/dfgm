(ns dfgm.app
  (:require [materia.middleware :as middleware]
            [materia.services.app.proto :refer :all]
            [materia.utils :as u]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [ring.middleware.defaults :refer [site-defaults]]))

(defservice app-service
  AppService
  [[:ConfigService get-in-config]
   [:MiddlewareService add-middlewares wrap]
   [:GameService wrap-gm]]
  (build-app [this]
             (let [conf (u/deep-merge site-defaults (get-in-config [:app :middleware]))
                   middlewares (-> (middleware/middlewares conf)
                                   (concat [[100 wrap-gm]]))]
               (add-middlewares middlewares)

               (-> (get-in-config [:app :endpoint])
                   (u/parse-ns-var!)
                   (->> (apply u/resolve-ns-var!))
                   wrap))))
