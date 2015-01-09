(ns dfgm.services.db.service-test
  (:require ;; [korma.config :as conf]
   ;; [korma.db :as db]
   [dfgm.services.db.core :refer :all]
   [dfgm.services.db.service :refer :all]
   [midje.sweet :refer :all]
   [puppetlabs.trapperkeeper.app :refer [get-service app-context]]
   [puppetlabs.trapperkeeper.testutils.bootstrap :refer [with-app-with-config]]))

(def default-conf {:db {:adapter :h2
                        :url "jdbc:h2:mem:test"}})


(facts "wrap-db"
  (fact "request has :db key"
    (with-app-with-config app
      [db-service]
      default-conf
      (let [f (wrap-db (get-service app :DatabaseService)
                       (fn [req] req))]
        (:db (f {})) => (contains {:datasource anything})))))

(facts "wrap-with-db"
  (fact "*current-db* should be nil outside wrapped handlers"
    *current-db* => nil)

  (fact "*current-db* should be non-nil inside wrapped handlers"
    (with-app-with-config app
      [db-service]
      default-conf
      (let [f (wrap-with-db (get-service app :DatabaseService)
                            (fn [_] *current-db*))]
        (f {}) => (contains {:datasource anything})))))
