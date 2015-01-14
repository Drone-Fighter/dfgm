(ns dfgm.services.game.service-test
  (:require [dfgm.services.game.proto :refer :all]
            [dfgm.services.game.service :refer :all]
            [midje.sweet :refer :all]
            [puppetlabs.trapperkeeper.app :refer [get-service app-context]]
            [puppetlabs.trapperkeeper.testutils.bootstrap :refer [with-app-with-config]]))

(def default-conf {})

(facts "game-service"
  (fact "state"
    (with-app-with-config app
      [game-service]
      default-conf
      (let [s (get-service app :GameService)
            context (:GameService @(app-context app))]
        (:state context) => truthy
        )))
  (fact "new-game"
    (with-app-with-config app
      [game-service]
      default-conf
      (let [s (get-service app :GameService)
            context (:GameService @(app-context app))
            id1 (new-game s {})
            id2 (new-game s {})]
        id1 => truthy
        id2 => truthy
        id1 =not=> id2
        (get-game s id1) => truthy
        (get-game s id1) => truthy)))
  (fact "new-or-join"
    (with-app-with-config app
      [game-service]
      default-conf
      (let [s (get-service app :GameService)
            context (:GameService @(app-context app))
            ret1 (new-or-join-game s)
            ret2 (new-or-join-game s)]
        ret1 => (just {:game-id #"[0-9a-zA-Z\-]+"
                       :drone-id #"[0-9a-zA-Z\-]+"})
        (:game-id ret1) => (:game-id ret2)
        (:drone-id ret1) =not=> (:drone-id ret2))))
  )
