(defproject dfgm "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/Drone-Fighter/dfgm"
  :clean-targets ^{:protect false} [:target-path "resources/public/js/dist"]
  :license {:name "MIT" :url "http://opensource.org/licenses/MIT"}
  :target-path "target/%s"
  :jvm-opts ["-server" "-XX:-OmitStackTraceInFastThrow"]
  :min-lein-version "2.5.1"
  :uberjar-name "dfgm.jar"
  :repl-options {:init-ns user}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2816"]
                 [materia "0.1.0-SNAPSHOT"]

                 [figwheel "0.2.5-SNAPSHOT"]
                 [figwheel-sidecar "0.2.5-SNAPSHOT"]
                 [reagent "0.4.3"]
                 [cljsjs/react "0.12.2-8"]
                 [aleph "0.3.3"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-cljsbuild "1.0.5"]]

  :cljsbuild {:builds {:app {:source-paths ["src-cljs"]
                             :compiler {:output-to     "resources/public/js/dist/app.js"
                                        :output-dir    "resources/public/js/dist/out"
                                        :source-map    "resources/public/js/dist/out.js.map"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns user
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [midje "1.6.3"]
                                  [puppetlabs/trapperkeeper "1.1.0" :classifier "test"]
                                  [puppetlabs/kitchensink "1.0.0" :classifier "test"]
                                  [org.clojure/tools.nrepl "0.2.7"]
                                  [com.h2database/h2 "1.4.186"]]

                   :plugins      [[lein-midje "3.1.3"]]

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
