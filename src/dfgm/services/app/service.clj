(ns dfgm.services.app.service
  (:require [environ.core :refer [env]]
            [dfgm.middleware :as middleware]
            [dfgm.utils :as u]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [ring.middleware.defaults :refer [site-defaults]]))

(defprotocol AppService)

(defservice app-service
  AppService
  [[:WebServerService add-handler]
   [:ConfigService get-in-config]]
  (init [this context]
        (let [conf (u/deep-merge site-defaults (get-in-config [:app :middleware]))]
          (when (env :dev)
            ;; See: https://groups.google.com/forum/#!msg/cider-emacs/bIVBvRnGO-U/nDszDbGoVzgJ
            (alter-var-root #'*out* (constantly *out*)))

          (->> (get-in-config [:app :endpoint])
               (u/parse-ns-var!)
               (apply u/resolve-ns-var!)
               (#(middleware/wrap % conf))
               add-handler))
        context))
