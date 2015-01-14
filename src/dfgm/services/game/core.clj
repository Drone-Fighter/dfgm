(ns dfgm.services.game.core
  (:require [clj-time.core :as t]
            [dfgm.utils :as u]))

(defn game-id []
  (str (java.util.UUID/randomUUID)))

(def drone-id game-id)

(defn create-map []
  ;; TODO
  {:targets []})

(defn create-game [{:keys [duration capacity id]
                    :or   {duration 30000
                           capacity 2}}]
  {:id         id
   :created-at (t/now)
   :started?   false
   :duration   duration
   :capacity   capacity
   :map        (create-map)})

(defn create-drone [{:keys [x y z ch id]
                     :or   {x 0 y 0 z 0}
                     :as   opt}]
  {:pos   {:x x
           :y y
           :z z}
   :score 0
   :ch    ch
   :id    (or id (drone-id))})

(defn join-game [game drone]
  (assoc-in game [:drones (:id drone)] drone))

(defn leave-game [game drone-id]
  (u/dissoc-in game [:drones drone-id]))

(defn inc-score [game drone-id]
  (update-in game [:drones drone-id :score] inc))

(defn end-game [game]
  (assoc game :started? true))

(defn start-game [game]
  (-> game
      (assoc :started? true)
      (assoc :started-at (t/now))))

(defn max-score [game]
  (let [score (comp :score last)]
    (apply max (map score (:drones game)))))

(defn winners [game]
  (let [drones    (:drones game)
        score     (comp :score last)
        max-score (max-score game)]
    (->> drones
         (filter #(= max-score (score %)))
         (map first))))

(defn draw? [game]
  (> (count (winners game)) 1))

(defn stop-game [game]
  (let [winners (winners game)]
    (-> game
        (assoc :winner winners)
        (assoc :draw? (draw? game)))))

(defn joinable? [game]
  (> (:capacity game)
     (count (:drones game))))

(def startable? (complement joinable?))

(defn find-joinable-game [games]
  (->> games
       (filter (comp joinable? deref last))
       first
       last))
