(defproject lp3 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
		[ring/ring-json "0.2.0"]
		[clj-http "0.7.3"]
		[org.clojure/data.json "0.2.2"]
		[enlive "1.1.1"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler lp3.handler/app}
  :min-lein-version "2.0.0"
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
