(ns hsnews.models.comment
  (:require [hsnews.models.user :as users]
            [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]
            [noir.validation :as vali])
  (:use somnium.congomongo)
  (:use [somnium.congomongo.config :only [*mongo-config*]]))

(defn valid? [{:keys [body]}]
  (vali/rule (vali/has-value? body)
             [:body "Please enter a thoughtful comment"])
  (vali/rule (vali/max-length? body 65536)
             [:body "Comments can be at most 65,536 characters long"])
  (not (vali/errors? :body)))

(defn prepare-new [comment]
  (let [ts (ctime/now)]
    (-> comment
      (assoc :ts (coerce/to-long ts))
      (assoc :post_id (object-id (get comment :post_id)))
      (assoc :author (users/current-user)))))

(defn add! [comment]
  (when (valid? comment)
    (insert! :comments (prepare-new comment))))
