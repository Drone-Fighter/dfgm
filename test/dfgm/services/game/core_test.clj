(ns dfgm.services.game.core-test
  (:require [dfgm.services.game.core :refer :all]
            [midje.sweet :refer :all]))

(facts "gm"
  (fact "winners"
    (winners {:drones {:a {:score 1}
                       :b {:score 2}
                       :c {:score 0}}})
    => [:b]

    (winners {:drones {:a {:score 1}
                       :b {:score 2}
                       :c {:score 2}}})
    => (just [:b :c] :in-any-order))
  (fact "joinable?"
    (joinable? {:capacity 2 :drones {:a nil :b nil}}) => false
    (joinable? {:capacity 3 :drones {:a nil :b nil}}) => true))
