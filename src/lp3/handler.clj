(ns lp3.handler
  (:use compojure.core)
  (:use ring.middleware.json)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defn get-chart-by-number [number] 
	{:status 200 :body {:number number :date "15.06.2013" :editor "Marek Niedźwiecki"} :headers {"Content-Type" "application/json"}})

(defroutes app-routes
  (GET ["/lp3/:number", :number #"[0-9]+"] [number] (get-chart-by-number number))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-json-response (handler/site app-routes)))
