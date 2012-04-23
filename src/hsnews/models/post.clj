(ns hsnews.models.post
  (:require [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]
            [noir.validation :as vali])
  (:use somnium.congomongo)
  (:use [somnium.congomongo.config :only [*mongo-config*]]))

(def posts-per-page 30)

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

(defn prepare-new [{:keys [title link] :as post}]
  (let [ts (ctime/now)]
    (-> post
      (assoc :ts (coerce/to-long ts)))))
      ; (assoc :username (users/me)) ; we'll need this

(defn add! [post]
  (when (valid? post)
    (insert! :posts (prepare-new post))))

(defn get-page [page]
  (let [page-num (dec (Integer. page))
        skip (* page-num posts-per-page)]
    (fetch :posts :limit posts-per-page :skip skip)))

(defn get-latest []
  (get-page 1))

(defn id->post [id]
  (fetch :posts :where {:_id id}))

(defn view-url [{:keys [_id] :as post}]
  (str "/posts/" _id))
