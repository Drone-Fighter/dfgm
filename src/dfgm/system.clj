(ns dfgm.system
  (:require [environ.core :refer [env]]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [taoensso.timbre.tools.logging :refer [use-timbre]]))

(defprotocol SystemService)

(defservice system-service
  SystemService
  [[:AppService build-app]
   [:ConfigService get-in-config]
   [:WebServerService add-handler]]
  (init [this context]
        (use-timbre)
        (when (env :dev)
          ;; See: https://groups.google.com/forum/#!msg/cider-emacs/bIVBvRnGO-U/nDszDbGoVzgJ
          (alter-var-root #'*out* (constantly *out*)))

        (add-handler (build-app))
        context))
