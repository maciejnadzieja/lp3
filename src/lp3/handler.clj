(ns lp3.handler
  (:use compojure.core)
  (:use ring.middleware.json)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
   	[net.cgrand.enlive-html :as html]))

(defn fetch-lp3 [number] (html/html-resource (java.net.URL. (str "http://lp3.polskieradio.pl/notowania/print.aspx?numer=" number))))
(def author [:table.bigList :tr :td.aT :span.title :b])
(def pos [:table.bigList :tr html/first-child])
(def title [:table.bigList :tr :td.aT html/first-child])
(defn fetch-day [number] (last (:content (first (html/select (fetch-lp3 number) #{[:span.zDnia]})))))
(defn fetch-editor [number] (last (:content (first (html/select (fetch-lp3 number) #{[:span.prowadzacy]})))))

(defn get-chart-by-number [number] 
	{:status 200 :body {:number number :date (fetch-day number) :editor (fetch-editor number) :positions (rest (map :content (html/select (fetch-lp3 number) #{pos author title})))} :headers {"Content-Type" "application/json"}})

(defroutes app-routes
  (GET ["/lp3/:number", :number #"[0-9]+"] [number] (get-chart-by-number number))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-json-response (handler/site app-routes)))
