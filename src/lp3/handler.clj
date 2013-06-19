(ns lp3.handler
  (:use compojure.core)
  (:use ring.middleware.json)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [net.cgrand.enlive-html :as html]))

(defn fetch-lp3 [number] (html/html-resource (java.net.URL. (str "http://lp3.polskieradio.pl/notowania/print.aspx?numer=" number))))
(def author [:table.bigList :tr :td.aT :span.title :b ])
(def pos [:table.bigList :tr html/first-child])
(def title [:table.bigList :tr :td.aT html/first-child])
(defn fetch-day [number, lp3] (last (:content (first (html/select lp3 #{[:span.zDnia ]})))))
(defn fetch-editor [number, lp3] (last (:content (first (html/select lp3 #{[:span.prowadzacy ]})))))
(defn fetch-positions [number, lp3] (partition 3 (flatten (rest (map :content (html/select lp3 #{pos author title}))))))

(defn get-chart-by-number [number] (let [lp3 (fetch-lp3 number)]
    {
   :status 200 :body {
                  :number number
                  :date (fetch-day number lp3)
                  :editor (fetch-editor number lp3)
                  :positions (fetch-positions number lp3)}
   :headers {"Content-Type" "application/json"}}))

(defroutes app-routes
  (GET ["/lp3/:number", :number #"[0-9]+"] [number] (get-chart-by-number number))
  (route/resources "/" "x")
  (route/not-found "Not Found"))

(def app
  (wrap-json-response (handler/site app-routes)))
