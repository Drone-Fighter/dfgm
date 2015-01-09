(ns dfgm.services.webserver.service-test
  (:require [compojure.core :refer :all]
            [midje.sweet :refer :all]
            [dfgm.services.webserver.service :refer :all]
            [org.httpkit.client :as http]
            [puppetlabs.trapperkeeper.app :refer [get-service]]
            [puppetlabs.trapperkeeper.core :refer [service]]
            [puppetlabs.trapperkeeper.testutils.bootstrap :refer [with-app-with-config]]))

(def base-url "http://localhost:3021")

(def default-conf {:webserver {:port 3021}})

(defn gen-url [path]
  (str base-url path))

(defn http-get [path]
  @(http/get (gen-url path)))

(defmacro create-consumer [f]
  `(service [[:WebServerService ~'set-handler ~'add-handler]]
            (~'init [~'this ~'context]
                    ~f
                    ~'context)))

;;; tests
(facts "http-kit-service"
  (fact "default config"
    (with-app-with-config app
      [http-kit-service]
      default-conf
      (http-get "/") => (contains {:status  200
                                   :body    "Running"
                                   :headers (contains {:content-type #"text/html"})})))

  (fact "set-handler"
    (with-app-with-config app
      [http-kit-service
       (create-consumer (set-handler (GET "/" [] "foo")))]
      default-conf
      (http-get "/") => (contains {:status  200
                                   :body    "foo"
                                   :headers (contains {:content-type #"text/html"})})))

  (fact "add-handler"
    (with-app-with-config app
      [http-kit-service
       (create-consumer (do (add-handler (GET "/" [] "root"))
                            (add-handler (GET "/foo" [] "foo"))))]
      default-conf

      (http-get "/") => (contains {:status  200
                                   :body    "root"
                                   :headers (contains {:content-type #"text/html"})})
      (http-get "/foo") => (contains {:status  200
                                      :body    "foo"
                                      :headers (contains {:content-type #"text/html"})})))

  (fact "add-handler with base-path"
    (with-app-with-config app
      [http-kit-service
       (create-consumer (do (add-handler (GET "/" [] "foo") "/foo")
                            (add-handler (GET "/baz" [] "baz") "/foo")))]
      default-conf
      (http-get "/foo") => (contains {:status  200
                                      :body    "foo"
                                      :headers (contains {:content-type #"text/html"})})
      (http-get "/foo/baz") => (contains {:status  200
                                          :body    "baz"
                                          :headers (contains {:content-type #"text/html"})}))))
