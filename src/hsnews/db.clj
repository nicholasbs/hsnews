(ns hsnews.db
  (:use somnium.congomongo
        [somnium.congomongo.config :only [*mongo-config*]]
        [clojure.data.json :only [read-json]]))

(def local-fallback-mongo )

(defn get-dotcloud-config []
    (try
      ((read-json (slurp "/home/dotcloud/environment.json")) :DATA_MONGODB_URL)
      (catch Exception e (println e))))

; Taken from http://thecomputersarewinning.com/post/clojure-heroku-noir-mongo/
(defn split-mongo-url [url]
  "Parses mongodb url from heroku, eg. mongodb://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher))))) ;; Construct an options map.

(defn maybe-init []
  "Checks if connection and collection exist, otherwise initialize."
  (when (not (connection? *mongo-config*)) ;; If global connection doesn't exist yet.
    (let [config (split-mongo-url (or
                                    (get-dotcloud-config)
                                    "mongodb://:@localhost:27017/hsnews"))]
      (mongo! :db (:db config) :host (:host config) :port (Integer. (:port config))) ;; Setup global mongo.
      (authenticate (:user config) (:pass config)) ;; Setup u/p.
      (or (collection-exists? :posts) ;; Create collection named 'firstcollection' if it doesn't exist.
    (create-collection! :posts)))))
