(ns dfgm.services.game.core
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]
            [materia.utils :as u]))


(defn id-generator []
  (let [i (atom 0)]
    (fn []
      (str (swap! i inc)))))

(def game-id (id-generator))

(def drone-id (id-generator))

(defn create-map []
  ;; TODO
  {:targets []})

(defn create-game [{:keys [duration capacity id]
                    :or   {duration 30000
                           capacity 2}}]
  {:id         id
   :created-at (c/to-long (t/now))
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
  (let [st (c/to-long (t/now))]
    (-> game
        (assoc :started? true)
        (assoc :started-at st)
        (assoc :ends-at (+ (:duration game) st))
        )))

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
        (assoc :ended? true)
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

(defn serialize-drone [drone]
  (dissoc drone :ch))

(defn serialize-drones [drones]
  (->> drones
       (map (juxt first (comp serialize-drone last)))
       (into {})))

(defn serialize-game [game]
  (-> game
      (update-in [:drones] serialize-drones)
      (dissoc :timer)))

(defn serialize-games [games]
  (->> games
       (map (juxt first (comp serialize-game deref last)))
       (into {})))
