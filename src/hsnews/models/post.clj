(ns hsnews.models.post
  (:require [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]
            [clj-time.format :as tform]
            [noir.validation :as vali])
  (:use somnium.congomongo)
  (:use [somnium.congomongo.config :only [*mongo-config*]]))


(def date-format (tform/formatter "MM/dd/yy" (ctime/default-time-zone)))
(def time-format (tform/formatter "h:mma" (ctime/default-time-zone)))
(def posts-per-page 30)

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

(defn valid? [{:keys [title link]}]
  (vali/rule (vali/has-value? title)
             [:title "Please enter a title"])
  (vali/rule (vali/max-length? title 255)
             [:title "Titles can be no more than 255 characters"])
  (vali/rule (vali/has-value? link)
             [:title "Please enter a link"])
  (vali/rule (vali/max-length? link 2048)
             [:title "Links can be no more than 2048 characters"])
  (not (vali/errors? :title :link)))

(defn wrap-time [post]
  (let [ts (ctime/now)]
    (-> post
      (assoc :ts (coerce/to-long ts))
      (assoc :date (tform/unparse date-format ts))
      (assoc :tme (tform/unparse time-format ts)))))

(defn next-id []
  1);(str (db/update! :next-post-id inc)))  

(defn prepare-new [{:keys [title link] :as post}]
  (let [id (next-id)]
    (-> post
      (assoc :id id)
      ; (assoc :username (users/me)) ; we'll need this
      (wrap-time))))

(defn add! [post]
  (maybe-init)
  (when (valid? post)
    (insert! :posts post)))

(defn get-page [page]
  (let [page-num (dec (Integer. page))
        skip (* page-num posts-per-page)]
    (fetch :posts :limit posts-per-page :skip skip)))

(defn get-latest []
  (get-page 1))

