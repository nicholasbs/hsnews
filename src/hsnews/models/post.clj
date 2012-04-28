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
      (assoc :author (users/current-user))
      (assoc :points 1)
      (assoc :score 1.0)
      (assoc :voters {(users/current-user) true})
      (assoc :last-updated (coerce/to-long ts)))))

(defn id->post [id]
  (fetch-by-id :posts (object-id id)))

;Decay
(defn get-posts-to-decay []
  (let [ts (ctime/now) fivemin 300000]
    (fetch :posts :where {:last-updated {:$lte (- (coerce/to-long ts) fivemin)}})))

(defn decay-post [{:keys [score] :as post}]
  (let [multiplier 0.9 ts (ctime/now)]
    (update! :posts
             post
             {:$set {:score (* multiplier score) :last-updated (coerce/to-long ts)}})))

(defn decay! []
  (doall (map decay-post (get-posts-to-decay))))

(defn is-author? [{:keys [author]}]
  (= author (users/current-user)))

(defn add! [post]
  (when (valid? post)
    (do
      (insert! :posts (prepare-new post))
      (decay!))))

(defn voted? [{:keys [voters]}]
  (contains? voters (keyword (users/current-user))))

(defn upvote! [post]
  (if-not (voted? post)
    (do
      (update! :posts
               post
               {:$inc {:points 1 :score 1} :$set {(str "voters." (users/current-user)) true}})
      (decay!))))

(defn get-page [page]
  (let [page-num (dec (Integer. page))
        skip (* page-num posts-per-page)]
    (fetch :posts :limit posts-per-page :skip skip :sort {:score -1})))

(defn get-latest []
  (get-page 1))

(defn post-url [{:keys [_id] :as post}]
  (str "/posts/" _id))

(defn upvote-url [{:keys [_id]}]
  (str "/posts/" _id "/upvote"))

(defn get-comments [{:keys [_id]}]
  (fetch :comments :where {:post_id _id} :sort {:points -1}))

(defn get-by-user [username]
  (fetch :posts :where {:author username}))
