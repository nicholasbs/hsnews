(ns hsnews.db
  (:use somnium.congomongo
        [somnium.congomongo.config :only [*mongo-config*]]))

; Taken from http://thecomputersarewinning.com/post/clojure-heroku-noir-mongo/
(defn split-mongo-url [url]
  "Parses mongodb url from heroku, eg. mongodb://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher))))) ;; Construct an options map.

(defn maybe-init []
  "Checks if connection and collection exist, otherwise initialize."
  (when (not (connection? *mongo-config*)) ;; If global connection doesn't exist yet.
    (let [mongo-url "mongodb://:@localhost:27017/hsnews" ;(get (System/getenv) "MONGOHQ_URL") ;; Heroku puts it here.
    config    (split-mongo-url mongo-url)] ;; Extract options.
      (println "Initializing mongo @ " mongo-url)
      (mongo! :db (:db config) :host (:host config) :port (Integer. (:port config))) ;; Setup global mongo.
      (authenticate (:user config) (:pass config)) ;; Setup u/p.
      (or (collection-exists? :posts) ;; Create collection named 'firstcollection' if it doesn't exist.
    (create-collection! :posts)))))
