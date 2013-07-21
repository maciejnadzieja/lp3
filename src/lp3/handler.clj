(ns lp3.handler
  (:use compojure.core)
  (:use ring.middleware.json)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clj-http.client :as http-client]
            [clojure.data.json :as json]
            [net.cgrand.enlive-html :as html]
	   ))
(defn fetch-last-lp3 [] (html/html-resource (java.net.URL. "http://lp3.polskieradio.pl/notowania/")))
(def last-number [:div#numerGlosowania :span.number])
(defn fetch-number [lp3] (first (first (map :content (html/select lp3 last-number)))))

(defn fetch-lp3 [number] (html/html-resource (java.net.URL. (str "http://lp3.polskieradio.pl/notowania/print.aspx?numer=" number))))
(def author [:table.bigList :tr :td.aT :span.title :b ])
(def pos [:table.bigList :tr html/first-child])
(def title [:table.bigList :tr :td.aT html/first-child])
(defn fetch-day [number, lp3] (last (:content (first (html/select lp3 #{[:span.zDnia ]})))))
(defn fetch-editor [number, lp3] (last (:content (first (html/select lp3 #{[:span.prowadzacy ]})))))
(defn fetch-positions [number, lp3] (partition 3 (flatten (rest (map :content (html/select lp3 #{pos author title}))))))
(def spotify-url "http://ws.spotify.com/search/1/track.json?")
(defn fetch-spotify-song [position] (first ((json/read-str (:body (http-client/get spotify-url {:query-params {"q" (str (nth position 1) " " (nth position 2))}} {:as :json}))) "tracks")))
(defn fetch-spotify-title [position] (let [song (fetch-spotify-song position)] (if (empty? song) nil (song "href"))))
(defn spotify-positions [positions] (filter identity (map fetch-spotify-title positions)))

(defn get-chart-by-number [number] (let [lp3 (fetch-lp3 number)]
    {
   :status 200 :body {
                  :number number
                  :date (fetch-day number lp3)
                  :editor (fetch-editor number lp3)
                  :positions (fetch-positions number lp3)}
   :headers {"Content-Type" "application/json"}}))

(defn get-spotified-chart-by-number [number] (let [lp3 (fetch-lp3 number)]
    {
   :status 200 :body {
                  :number number
                  :date (fetch-day number lp3)
                  :editor (fetch-editor number lp3)
                  :positions (spotify-positions (fetch-positions number lp3))}
   :headers {"Content-Type" "application/json"}}))

(def chart-cache (memoize get-chart-by-number))
(def chart-spotified-cache (memoize get-spotified-chart-by-number)) 

(defroutes app-routes
  (GET ["/lp3/last"] [] (fetch-number (fetch-last-lp3)))
  (GET ["/lp3/:number", :number #"[0-9]+"] [number] (chart-cache number))
  (GET ["/lp3/spotify/:number", :number #"[0-9]+"] [number] (chart-spotified-cache number))
  (GET ["/"] [] "/lp3/:number - get chart by number<br/>/lp3/spotify/:number - get chart by number with spotify links<br/><br/>http://lp3.polskieradio.pl/notowania/")
  (route/not-found "Not Found"))

(def app
  (wrap-json-response (handler/site app-routes)))
