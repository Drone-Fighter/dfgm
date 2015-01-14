(ns dfgm.routes
  (:require [clojurewerkz.route-one.compojure :refer :all]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [dfgm.response :refer [json-not-found]]
            [dfgm.services.game.handlers :refer [game-routes]]))

(compojure/defroutes main-routes
  game-routes
  (route/not-found (json-not-found)))
