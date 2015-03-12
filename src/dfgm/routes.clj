(ns dfgm.routes
  (:require [cheshire.core :refer [generate-string]]
            [clojure.java.io :as io]
            [clojurewerkz.route-one.compojure :refer :all]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [dfgm.response :refer [json-not-found]]
            [dfgm.services.game.core :refer [serialize-games]]
            [dfgm.services.game.handlers :refer [game-routes]]
            [dfgm.services.game.proto :as gs]
            [environ.core :refer [env]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [org.httpkit.server :refer [with-channel on-close]]))

(deftemplate page (io/resource "index.html") [])

(compojure/defroutes main-routes
  game-routes
  (route/resources "/")
  (route/resources "/react" {:root "reagent"})
  (GET ws "/ws-internal" req (fn [req]
                               (with-channel req ch
                                 (gs/watch (:gm req) ch)

                                 (on-close ch (fn [status]
                                                (gs/unwatch (:gm req) ch)
                                                (println "channel closed:" status)))
                                 (println (generate-string {:games (serialize-games @(gs/get-games (:gm req)))}))
                                 (gs/notify-state (:gm req)))))
  (GET p "/*" req (page))
  (route/not-found (json-not-found)))
