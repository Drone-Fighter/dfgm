(ns dfgm.services.game.service
  (:require [clj-time.core :as t]
            [compojure.core :refer [routes GET]]
            [dfgm.services.game.cmd :as cmd]
            [dfgm.services.game.core :as core]
            [dfgm.services.game.proto :refer :all]
            [dfgm.utils :as u]
            [org.httpkit.server :refer [send!]]
            [org.httpkit.timer :as timer]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [puppetlabs.trapperkeeper.services :refer [service-context]]
            [taoensso.timbre :as log]))

(defservice game-service
  GameService
  []
  (init [this context]
        (log/info "Initializing game service")
        (let [state {:games (atom {})}]
          (assoc context :state state)))
  (start [this context]
         context)
  (stop [this context]
        (log/info "Shutting down game service")
        context)
  (new-game [this]
            (new-game this {}))
  (new-game [this opt]
            (let [games  (get-games this)
                  new-id (core/game-id)
                  game   (atom (core/create-game (merge {:id new-id} opt)))]
              (swap! games assoc new-id game)
              new-id))
  (find-or-create-game [this]
                       (find-or-create-game this {}))
  (find-or-create-game [this opt]
                       (or (core/find-joinable-game @(get-games this))
                           (let [gid (new-game this opt)]
                             (get-game this gid))))
  (new-or-join-game [this]
                    (new-or-join-game this {}))
  (new-or-join-game [this opt]
                    (let [game  (find-or-create-game this opt)
                          drone (core/create-drone {:ch (:ch opt)})]
                      (swap! game core/join-game drone)
                      {:game-id (:id @game) :drone-id (:id drone)}))
  (get-game [this id]
            (get @(get-games this) id))
  (get-games [this]
             (get-in (service-context this) [:state :games]))
  (wrap-gm [this handler]
           (fn [req]
             (handler (assoc req :gm this))))
  (handle-cmd [this cmd]
              (cmd/handle-cmd this cmd))
  (start-game-if-possible [this game-id]
                          (when (core/startable? @(get-game this game-id))
                            (start-game this game-id)))
  (start-game [this game-id]
              (let [game (get-game this game-id)]
                (swap! game core/start-game)
                (cmd/notify-drones @game {:type :started
                                          :game-id game-id})
                (let [timer (timer/schedule-task
                             (:duration @game)
                             (do
                               (swap! game core/stop-game)
                               (cmd/notify-drones @game {:type :ended
                                                         :game-id game-id
                                                         :winner (:winner @game)})))]
                  (swap! game assoc :timer timer))))
  (cancel-game [this game-id]
               (let [game  (get-game this game-id)
                     timer (:timer @game)]
                 (when (timer/cancel timer)
                   (cmd/notify-drones @game {:type :canceled}))
                 (swap! (get-games this) dissoc game-id)))
  (inc-score [this game-id drone-id]
             (let [game (get-game this game-id)]
               (swap! game core/inc-score drone-id)))
  (find-drone-by-ch [this ch]
                    (some (fn [game]
                            (some (fn [d]
                                    (when (= ch (:ch d))
                                      d)) (vals (:drones @game))))
                          (vals @(get-games this))))
  (find-game-by-drone [this drone]
                      (some (fn [game]
                              (when (contains? (:drones @game) (:id drone))
                                @game))
                            (vals @(get-games this)))))
