(ns dfgm.services.webserver.service
  (:require [dfgm.services.webserver.core :as core]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]))

(defprotocol WebServerService
  (set-handler [this handler])
  (add-handler [this handler] [this handler base-path]))

(defservice http-kit-service
  "Provides a Http Kit server as a service."
  WebServerService
  [[:ConfigService get-in-config]]
  (init [this context]
        (core/init context))
  (start [this context]
         (core/start context (get-in-config [:webserver])))
  (stop [this context]
        (core/stop context))
  (set-handler [this handler]
               (let [context (service-context this)]
                 (core/set-handler! context handler)))
  (add-handler [this handler]
               (let [context (service-context this)]
                 (core/add-handler! context handler)))
  (add-handler [this handler base-path]
               (let [context (service-context this)]
                 (core/add-handler! context handler base-path))))
