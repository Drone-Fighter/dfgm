(ns dfgm.repl
  (:require [cemerick.piggieback :refer [cljs-repl]]
            [clojurescript-build.auto :as auto]
            [clojure.tools.namespace.repl :refer [refresh]]
            [dfgm.services.app.service :refer :all]
            [dfgm.services.game.service :refer :all]
            [dfgm.services.webserver.service :refer :all]
            [dfgm.utils :as u]
            [figwheel-sidecar.auto-builder :as fig-auto]
            [figwheel-sidecar.core :as fig]
            [leiningen.core.main :as lein]
            [puppetlabs.trapperkeeper.app :as tka]
            [puppetlabs.trapperkeeper.config :refer [load-config]]
            [puppetlabs.trapperkeeper.core :as tk]
            [weasel.repl.websocket :refer [repl-env]]))



;; a var to hold the main `TrapperkeeperApp` instance.
(defonce system nil)

(declare start-figwheel stop-figwheel)

(defn init []
  (alter-var-root #'system
                  (fn [_] (tk/build-app
                           [http-kit-service app-service game-service]
                           (-> (load-config "dev-resources/config.edn")
                               (assoc-in [:webserver :port] 8080)))))

  (alter-var-root #'system tka/init)
  (tka/check-for-errors! system)
  (start-figwheel))

(defn start []
  (alter-var-root #'system
                  (fn [s] (if s (tka/start s))))
  (tka/check-for-errors! system))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (tka/stop s))))
  (stop-figwheel))

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

(defn browser-repl []
  (cljs-repl :repl-env (repl-env :ip "0.0.0.0" :port 9001)))

(defn get-in-project [ks]
  (get-in (->> "project.clj"
               slurp
               read-string
               (drop 2)
               (cons :version)
               (apply hash-map))
          ks))

(defn get-in-project-with-profile [profile ks]
  (u/deep-merge-with (fn [& vals]
                       (if (every? sequential? vals)
                         (into (empty (first vals)) (apply concat vals))
                         (last vals)))
                     (get-in-project ks)
                     (get-in-project (concat [:profiles :dev] ks))))

(defn fix-cljsbuild-conf [conf]
  (update-in conf [:builds] (fn [bs]
                              (if (map? bs)
                                (map (fn [[k v]]
                                       (assoc v :id (name k))) bs)
                                bs))))

(defonce figwheel-server (atom nil))

(defonce fig-builder (atom nil))

(defn stop-figwheel []
  (when @fig-builder
    ;; XXX
    (reset! fig-builder (do (auto/stop-autobuild! @fig-builder)
                            nil)))
  (when @figwheel-server
    (swap! figwheel-server fig/stop-server)))

(defn start-figwheel []
  (when (or @figwheel-server @fig-builder)
    (stop-figwheel))
  (reset! figwheel-server
          (fig/start-server (get-in-project-with-profile :dev [:figwheel])))
  (reset! fig-builder
          (-> (get-in-project-with-profile :dev [:cljsbuild])
              (assoc :figwheel-server @figwheel-server)
              fix-cljsbuild-conf
              fig-auto/autobuild*)))
