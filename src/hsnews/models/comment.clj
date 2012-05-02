(ns hsnews.models.comment
  (:use somnium.congomongo)
  (:require [hsnews.models.user :as users]
            [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]
            [noir.validation :as vali]))

(defn id->comment [id]
  (fetch-by-id :comments (object-id id)))

(defn upvote-url [{:keys [_id]}]
  (str "/comments/" _id "/upvote"))

(defn is-author? [{:keys [author]}]
  (= author (users/current-user)))

(defn valid? [{:keys [body]}]
  (vali/rule (vali/has-value? body)
             [:body "Please enter a thoughtful comment"])
  (vali/rule (vali/max-length? body 65536)
             [:body "Comments can be at most 65,536 characters long"])
  (not (vali/errors? :body)))

(defn prepare-new [com]
  (let [ts (ctime/now)]
    (-> com
      (assoc :ts (coerce/to-long ts))
      (assoc :post_id (object-id (get com :post_id)))
      (assoc :author (users/current-user))
      (assoc :points 1)
      (assoc :voters {(users/current-user) true}))))

(defn add! [com]
  (when (valid? com)
    (insert! :comments (prepare-new com))))

(defn voted? [{:keys [voters]}]
  (contains? voters (keyword (users/current-user))))

(defn upvote! [{:keys [author] :as com}]
  (if-not (voted? com)
    (do
      (update! :comments
               com
               {:$inc {:points 1} :$set {(str "voters." (users/current-user)) true}})
      (update! :users
               (fetch-one :users :where {:hs_id author})
               {:$inc {:karma 1}}))))

(defn get-top-comments []
  (let [ts (ctime/now)
        oneweekago (- (coerce/to-long ts) 604800000)]
    (fetch :comments :where {:ts {:$gte oneweekago}} :sort {:points -1} :limit 100)))



