(ns hsnews.db
  (:use somnium.congomongo
        [hsnews.utils :only [data-mongodb-url]]
        [somnium.congomongo.config :only [*mongo-config*]]
        [clojure.data.json :only [read-json]]))

; Taken from http://thecomputersarewinning.com/post/clojure-heroku-noir-mongo/
(defn split-mongo-url [url]
  "Parses mongodb url from heroku, eg. mongodb://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher))))) ;; Construct an options map.

(defn create-posts-collection! []
  (create-collection! :posts)
  (add-index! :posts [:ts :score :last-updated :author]))

(defn create-comments-collection! []
  (create-collection! :comments)
  (add-index! :comments [:post_id :parent_id :author :points :ts]))

(defn create-users-collection! []
  (create-collection! :users)
  (add-index! :users [:hs_id]))

(defn maybe-init []
  "Checks if connection and collections exist, otherwise initialize."
  (when (not (connection? *mongo-config*)) ;; If global connection doesn't exist yet.
    (let [config (split-mongo-url data-mongodb-url)]
      (mongo! :db (:db config) :host (:host config) :port (Integer. (:port config)))
      (authenticate (:user config) (:pass config))
      (or (collection-exists? :posts) (create-posts-collection!))
      (or (collection-exists? :comments) (create-comments-collection!))
      (or (collection-exists? :users) (create-users-collection!)))))
