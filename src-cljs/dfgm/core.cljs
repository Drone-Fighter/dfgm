(ns dfgm.core
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce app-state (atom {}))

(defn drone-p [d]
  [:div.drone
   [:div.score (or (get d "score") "-")]
   [:div.name (str "Drone #" (or (get d "id") "-"))]])

(defn update-timer [state]
  (let [f (fn [[k g]]
            [k
             (assoc g "rem" (quot (- (get g "ends-at") (.getTime (js/Date.))) 1000))])]
    (update-in state ["games"] #(into {} (map f %)))))

(defn main-component []
  [:div#games
   (for [[_ g] (reverse (get @app-state "games"))]
     [:div.game
      {:class (cond
                (get g "ended?") "ended"
                (> (get g "capacity") (count (get g "drones"))) "pending"
                :else "")}
      [:div.title (str "Game #" (get g "id"))]
      [:div.drones
       (drone-p (last (first (get g "drones"))))
       [:div.drone-sep
        "VS"]
       (drone-p (last (second (get g "drones"))))]
      [:div.time (let [rem (get g "rem" -1)]
                   (if (>= rem 0)
                     (str rem " s")
                     ""))]])])

(defn main []
  (reagent/render-component [main-component] (. js/document (getElementById "app"))))

;;; TODO: state management
(def ws (js/WebSocket. (str "ws://" (.-host js/location) "/ws-internal")))

(aset ws "onmessage" (fn [m]
                       (let [data (.-data m)]
                         (reset! app-state (update-timer (js->clj (.parse js/JSON data)))))))

(js/setInterval (fn []
                  (swap! app-state update-timer)) 500)
