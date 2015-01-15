(ns dfgm.routes
  (:require [cheshire.core :refer [generate-string]]
            [clojure.java.io :as io]
            [clojurewerkz.route-one.compojure :refer :all]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [dfgm.response :refer [json-not-found]]
            [dfgm.services.game.core :refer [serialize-games]]
            [dfgm.services.game.handlers :refer [game-routes]]
            [dfgm.services.game.proto :refer [watch unwatch get-games notify-state]]
            [dfgm.utils :as u]
            [environ.core :refer [env]]
            [net.cgrand.enlive-html :refer [deftemplate set-attr prepend append html]]
            [org.httpkit.server :refer [with-channel on-close on-receive send! websocket?]]))

(def inject-devmode-html
  (comp
   (set-attr :class "is-dev")
   (prepend (html [:script {:type "text/javascript" :src "/js/out/goog/base.js"}]))
   (prepend (html [:script {:type "text/javascript" :src "/react/react.js"}]))
   (append  (html [:script {:type "text/javascript"} "goog.require('dfgm.dev')"]))))

(deftemplate page
  (io/resource "index.html") [] [:body] (if (env :dev) inject-devmode-html identity))

(compojure/defroutes main-routes
  game-routes
  (route/resources "/")
  (route/resources "/react" {:root "react"})
  (GET ws "/ws-internal" req (fn [req]
                               (with-channel req ch
                                 (watch (:gm req) ch)
                                 (on-close ch (fn [status]
                                                (unwatch (:gm req) ch)
                                                (println "channel closed:" status)))
                                 (println (generate-string {:games (serialize-games @(get-games (:gm req)))}))
                                 (notify-state (:gm req)))))
  (GET p "/*" req (page))
  (route/not-found (json-not-found)))
