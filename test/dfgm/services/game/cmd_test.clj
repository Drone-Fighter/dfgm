(ns dfgm.services.game.cmd-test
  (:require [aleph.http :refer [websocket-client]]
            [cheshire.core :refer [parse-string generate-string]]
            [dfgm.services.app.service :refer [app-service]]
            [dfgm.services.game.handlers :refer :all]
            [dfgm.services.game.service :refer [game-service]]
            [dfgm.services.webserver.service :refer [http-kit-service]]
            [lamina.core :refer [wait-for-result receive-all receive enqueue wait-for-message read-channel]]
            [midje.sweet :refer :all]
            [puppetlabs.trapperkeeper.app :refer [get-service app-context]]
            [puppetlabs.trapperkeeper.testutils.bootstrap :refer [with-app-with-config]]))


(def default-conf {:webserver {:port 3021} :app {:endpoint "dfgm.routes/main-routes"}})
(def ws-ep "ws://localhost:3021/ws")

(defn capture []
  (let [a (atom nil)]
    [a
     (fn [mes]
       (reset! a mes)
       ;; (println mes)
       true)
     ]))

(defn send-cmd [ch cmd]
  (enqueue ch (generate-string cmd)))

(defn get-result [v]
  (parse-string @v keyword))

(defn cmd-result [ch v cmd]
  (send-cmd ch cmd)
  (wait-for-message ch 3000)
  (get-result v))

(facts "ws"
  (fact "connect"
    (with-app-with-config app
      [game-service http-kit-service app-service]
      default-conf
      (let [s (get-service app :GameService)
            context (:GameService @(app-context app))
            [v callback] (capture)
            [v2 callback2] (capture)
            c (websocket-client {:url ws-ep})
            ch (wait-for-result c)
            c2 (websocket-client {:url ws-ep})
            ch2 (wait-for-result c2)]
        (receive-all ch callback)
        (receive-all ch2 callback2)
        (send-cmd ch {:cmd :register :duration 100})
        (wait-for-message ch 3000)
        (get-result v) => (just {:type "registered"
                                 :game-id #"[a-zA-Z0-9\-]+"
                                 :drone-id #"[a-zA-Z0-9\-]+"})
        (send-cmd ch2 {:cmd :register :duration 100})
        (wait-for-message ch 3000)
        (get-result v) => (just {:type "started"
                                 :game-id #"[a-zA-Z0-9\-]+"})
        (send-cmd ch {:cmd :inc})
        (wait-for-message ch 3000)
        (get-result v) => (just {:type "ended"
                                 :game-id #"[a-zA-Z0-9\-]+"
                                 :winner (just [#"[a-zA-Z0-9\-]+"])})
        ))))
