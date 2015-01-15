(ns dfgm.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(defonce app-state (atom {:games {:1 {:id 1
                                      :ended? false
                                      :ends-at (js/Date.)
                                      :drones {:1 {:id 1
                                                   :score 1}
                                               :2 {:id 2
                                                   :score 3}}}
                                  :2 {:id 2
                                      :ended? false
                                      :ends-at (js/Date.)
                                      :drones {:3 {:id 3
                                                   :score 1}
                                               :4 {:id 4
                                                   :score 3}}}
                                  :3 {:id 3
                                      :ended? true
                                      :ends-at (js/Date.)
                                      :drones {:5 {:id 5
                                                   :score 1}
                                               :6 {:id 6
                                                   :score 3}}}}}))

(defn drone-p [d]
  [:.drone
   [:.score (or (get d "score") "-")]
   [:.name (str "Drone #" (or (get d "id") "-"))]])

(defn update-timer [state]
  (let [f (fn [[k g]]
            [k
             (assoc g "rem" (quot (- (get g "ends-at") (.getTime (js/Date.))) 1000))])]
    (update-in state ["games"] #(into {} (map f %)))))

(defn main []
  (om/root
   (fn [app owner]
     (reify
       om/IRender
       (render [_]
         (html [:#games
                (for [[_ g] (reverse (get app "games"))]
                  [:.game
                   {:class (cond
                            (get g "ended?") "ended"
                            (> (get g "capacity") (count (get g "drones"))) "pending"
                            :else "")}
                   [:.title (str "Game #" (get g "id"))]
                   [:.drones
                    (drone-p (last (first (get g "drones"))))
                    [:.drone-sep
                     "VS"]
                    (drone-p (last (second (get g "drones"))))]
                   [:.time (let [rem (get g "rem" -1)]
                             (if (>= rem 0)
                               (str rem " s")
                               "")
                             )]])]))))
   app-state
   {:target (. js/document (getElementById "app"))}))

;;; TODO: state management
(def ws (js/WebSocket. (str "ws://" (.-host js/location) "/ws-internal")))

(aset ws "onmessage" (fn [m]
                       (let [data (.-data m)]
                         (reset! app-state (update-timer (js->clj (.parse js/JSON data)))))))

(js/setInterval (fn []
                  (swap! app-state update-timer)) 500)
