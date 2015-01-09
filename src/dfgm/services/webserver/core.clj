(ns dfgm.services.webserver.core
  (:require [compojure.core :as compojure]
            [dfgm.utils :as u]
            [org.httpkit.server :as kit]
            [taoensso.timbre :as log]))

(def default-server-config {:port 8090})

(defn default-handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Running"})

(defn bundle-handlers
  "Bundle multiple handlers into one handler."
  [handlers]
  (when-not (empty? handlers)
    (apply compojure/routes handlers)))

(defn set-handler [handlers handler]
  [handler])

(defn set-handler! [context handler]
  (let [handlers (:handlers context)]
    (swap! handlers set-handler handler)))

(defn add-handler
  ([handlers handler base-path]
   ;; TODO: Add support for route parameter
   (add-handler handlers (compojure/context base-path [] handler)))
  ([handlers handler]
   (conj handlers handler)))

(defn add-handler!
  ([context handler base-path]
   (let [handlers (:handlers context)]
     (swap! handlers add-handler handler base-path)))
  ([context handler]
   (let [handlers (:handlers context)]
     (swap! handlers add-handler handler))))

(defn init
  "Initialize web server."
  [context]
  (log/info "Initializing web server.")
  (-> context
      (assoc :server (atom nil))
      (assoc :handlers (atom []))))

(defn start
  "Start web server."
  [context opts]
  (let [server   (:server context)
        handlers @(:handlers context)
        handler  (or (bundle-handlers handlers)
                     default-handler)
        opts     (u/deep-merge default-server-config
                               opts)]
    (log/infof "Starting web server on port: %d" (:port opts))
    (reset! server (kit/run-server handler opts)))
  context)

(defn stop
  "Stop http server."
  [context]
  (log/info "Shutting down web server.")
  (let [server  (:server context)
        handler (:handlers context)]
    (@server)                           ; shutdown
    (reset! server nil)
    (reset! handler nil))
  context)
