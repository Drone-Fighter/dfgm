(defproject dfgm "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/Drone-Fighter/dfgm"
  :license {:name "MIT" :url "http://opensource.org/licenses/MIT"}
  :target-path "target/%s"
  :jvm-opts ["-server"]
  :repl-options {:init-ns dfgm.repl}
  :dependencies [[org.clojure/clojure "1.6.0"]

                 ;; Web server
                 [http-kit "2.1.19"]
                 [clojurewerkz/gizmo "1.0.0-alpha4"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [ring-refresh "0.1.1"]
                 [prone "0.8.0"]

                 ;; Service management
                 [puppetlabs/trapperkeeper "1.0.1"]

                 ;; Logging
                 [com.taoensso/timbre "3.3.1"]

                 ;; DB
                 [mysql/mysql-connector-java "5.1.34"]
                 [clojure.jdbc "0.3.2"]
                 [stch-library/sql "0.1.1"]
                 [clojure.jdbc/clojure.jdbc-hikari "0.3.2"]

                 ;; misc
                 [environ "1.0.0"]
                 [clj-time "0.6.0"]]
  :plugins [[lein-ancient "0.5.5"]
            [lein-environ "1.0.0"]]
  :profiles {:uberjar {:aot [puppetlabs.trapperkeeper.main]}
             :dev     {:dependencies [[ring-mock "0.1.5"]
                                      [ring/ring-devel "1.3.2"]
                                      [midje "1.6.3"]
                                      [puppetlabs/trapperkeeper "1.0.1" :classifier "test"]
                                      [puppetlabs/kitchensink "1.0.0" :classifier "test"]
                                      [com.h2database/h2 "1.4.184"]]
                       :plugins      [[lein-midje "3.1.3"]]
                       :env          {:dev true}}}
  :aliases {"tk" ["trampoline" "run" "--config" "dev-resources/config.edn"]}
  :main puppetlabs.trapperkeeper.main
  )
