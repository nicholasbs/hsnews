(ns hsnews.models.post
  (:use somnium.congomongo)
  (:require [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]
            [noir.validation :as vali]
            [hsnews.models.user :as users]))

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

(defn prepare-new [post]
  (let [ts (ctime/now)]
    (-> post
      (assoc :ts (coerce/to-long ts))
      (assoc :author (users/current-user)))))

(defn id->post [id]
  (fetch-by-id :posts (object-id id)))

(defn add! [post]
  (when (valid? post)
    (insert! :posts (prepare-new post))))

(defn is-author? [{:keys [author]}]
  (= author (users/current-user)))

(defn voted? [{:keys [voters]}]
  (and (not-empty voters) (not= (.indexOf voters (users/current-user)) -1)))

(defn upvote! [post]
  (if-not (voted? post)
    (update! :posts post {:$inc {:points 1} :$push {:voters (users/current-user)}})))

(defn get-page [page]
  (let [page-num (dec (Integer. page))
        skip (* page-num posts-per-page)]
    (fetch :posts :limit posts-per-page :skip skip :sort {:ts -1})))

(defn get-latest []
  (get-page 1))

(defn post-url [{:keys [_id] :as post}]
  (str "/posts/" _id))

(defn upvote-url [{:keys [_id]}]
  (str "/posts/" _id "/upvote"))

(defn get-comments [{:keys [_id]}]
  (fetch :comments :where {:post_id _id}))

(defn get-by-user [username]
  (fetch :posts :where {:author username}))
