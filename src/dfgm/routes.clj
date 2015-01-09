(ns dfgm.routes
  (:require [clojurewerkz.route-one.compojure :refer :all]
            [compojure.core :as compojure]
            [compojure.route :as route]))

(compojure/defroutes main-routes
  (route/not-found "<h1>Page not found</h1>"))
