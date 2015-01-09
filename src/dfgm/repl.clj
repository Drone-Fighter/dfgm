(ns dfgm.repl
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [dfgm.services.app.service :refer :all]
            [dfgm.services.db.service :refer :all]
            [dfgm.services.webserver.service :refer :all]
            [puppetlabs.trapperkeeper.app :as tka]
            [puppetlabs.trapperkeeper.config :refer [load-config]]
            [puppetlabs.trapperkeeper.core :as tk]))

;; a var to hold the main `TrapperkeeperApp` instance.
(defonce system nil)

(defn init []
  (alter-var-root #'system
                  (fn [_] (tk/build-app
                           [http-kit-service app-service]
                           (-> (load-config "dev-resources/config.edn")
                               (assoc-in [:webserver :port] 8080)))))

  (alter-var-root #'system tka/init)
  (tka/check-for-errors! system))

(defn start []
  (alter-var-root #'system
                  (fn [s] (if s (tka/start s))))
  (tka/check-for-errors! system))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (tka/stop s)))))

(defn go []
  (init)
  (start))

(defn context []
  @(tka/app-context system))

;; pretty print the entire application context
(defn print-context []
  (clojure.pprint/pprint (context)))

(defn reset []
  (stop)
  (refresh :after 'dfgm.repl/go))
