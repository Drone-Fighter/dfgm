(ns dfgm.services.game.cmd
  (:require [cheshire.core :refer [parse-string generate-string]]
            [dfgm.services.game.proto :refer [new-or-join-game start-game-if-possible
                                              find-drone-by-ch inc-score find-game-by-drone]]
            [org.httpkit.server :refer [send!]]
            [taoensso.timbre :as log]))

(defn build-cmd [s ch]
  (-> (parse-string s keyword)
      (update-in [:cmd] keyword)
      (assoc :ch ch)))

(defn send-cmd! [ch cmd]
  (send! ch (generate-string cmd)))

(defn notify-drones [game cmd]
  (let [channels (map (comp :ch last) (:drones game))]
    (doseq [ch channels]
      (send-cmd! ch cmd))))

(defmulti handle-cmd (fn [gm cmd] (:cmd cmd)))

(defmethod handle-cmd :default [gm cmd]
  (log/info "Unknown command")
  (send-cmd! (:ch cmd) {:type       :error
                        :error-type :unknown-command}))

(defmethod handle-cmd :register [gm cmd]
  (let [ch  (:ch cmd)
        ret (new-or-join-game gm {:ch ch :duration (:duration cmd)})]
    (send-cmd! ch (assoc ret :type :registered))
    (start-game-if-possible gm (:game-id ret))))

(defmethod handle-cmd :inc [gm cmd]
  (let [drone (find-drone-by-ch  gm (:ch cmd))
        game  (find-game-by-drone  gm drone)]
    (inc-score gm (:id game) (:id drone))))
