(defproject dfgm "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/Drone-Fighter/dfgm"
  :license {:name "MIT" :url "http://opensource.org/licenses/MIT"}
  :target-path "target/%s"
  :jvm-opts ["-server"]
  :min-lein-version "2.5.0"
  :uberjar-name "dfgm.jar"
  :source-paths ["src" "src-cljs"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2665" :scope "provided"]

                 ;; Web server
                 [http-kit "2.1.19"]
                 [clojurewerkz/gizmo "1.0.0-alpha4"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.3"]
                 [ring-refresh "0.1.1"]
                 [prone "0.8.0"]

                 [om "0.7.3"]
                 [sablono "0.2.22"]
                 [figwheel "0.2.1-SNAPSHOT"]
                 [com.cemerick/piggieback "0.1.5"]
                 [weasel "0.5.0"]
                 [leiningen "2.5.1"]

                 ;; Service management
                 [puppetlabs/trapperkeeper "1.0.1"]

                 ;; Logging
                 [com.taoensso/timbre "3.3.1"]

                 ;; misc
                 [environ "1.0.0"]
                 [clj-time "0.6.0"]
                 [aleph "0.3.3"]
                 [figwheel-sidecar "0.2.1-SNAPSHOT"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-cljsbuild "1.0.3"]]

  :cljsbuild {:builds {:app {:source-paths ["src-cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns dfgm.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [midje "1.6.3"]
                                  [puppetlabs/trapperkeeper "1.0.1" :classifier "test"]
                                  [puppetlabs/kitchensink "1.0.0" :classifier "test"]
                                  [com.h2database/h2 "1.4.184"]]

                   :plugins      [[lein-midje "3.1.3"]
                                  [lein-figwheel "0.2.1-SNAPSHOT"]]

                   :figwheel     {:http-server-root "public"
                                  :port 3449
                                  :css-dirs ["resources/public/css"]}

                   :env          {:dev true}

                   :cljsbuild    {:builds {:app {:source-paths ["env/dev/cljs"]}}}}

             :uberjar {:hooks       [leiningen.cljsbuild]
                       :env         {:production true}
                       :aot         [puppetlabs.trapperkeeper.main]
                       :cljsbuild   {:builds {:app
                                              {:source-paths ["env/prod/cljs"]
                                               :compiler
                                               {:optimizations :advanced
                                                :pretty-print false}}}}}}

  :aliases {"tk" ["trampoline" "run" "--config" "dev-resources/config.edn"]}
  :main puppetlabs.trapperkeeper.main)
