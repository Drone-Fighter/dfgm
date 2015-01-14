(ns dfgm.services.game.handlers
  (:require [clojurewerkz.route-one.compojure :refer :all]
            [compojure.core :as compojure]
            [dfgm.services.game.cmd :refer [build-cmd]]
            [dfgm.services.game.proto :refer [handle-cmd]]
            [org.httpkit.server :refer [with-channel on-close on-receive send! websocket?]]))

(compojure/defroutes game-routes
  (GET ws "/ws" req (fn [req]
                      (with-channel req ch
                        (on-close ch (fn [status]
                                       (println "channel closed:" status)))
                        (on-receive ch (fn [data]
                                         (let [cmd (build-cmd data ch)]
                                           (handle-cmd (:gm req) cmd))))))))
