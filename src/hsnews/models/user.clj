(ns hsnews.models.user
  (:use somnium.congomongo)
  (:require [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session]))

(defn current-user []
  (session/get :username))

(defn get-user [username]
  (fetch-one :users :where {:username username}))

(defn get-comments [username]
  (fetch :comments :where {:author username}))

(defn get-posts [username]
  (fetch :posts :where {:author username}))

(defn get-karma [username]
  (int (:karma (fetch-one :users :where {:username username}))))

(defn prepare [{password :password :as user}]
  (-> user
    (assoc :password (crypt/encrypt password))
    (assoc :karma 0)))

(defn valid-username? [username]
  (vali/rule (not (fetch-one :users :where {:username username}))
                  [:username "That username is already in use"])
  (vali/rule (vali/min-length? username 2)
             [:username "Username must be at least 2 characters"])
  (vali/rule (vali/max-length? username 20)
             [:username "Username must be less than 20 characters"])
  (not (vali/errors? :username :password)))

(defn valid-password? [password]
  (vali/rule (vali/min-length? password 8)
             [:password "Password must be at least 8 characters."])
  (not (vali/errors? :password)))

(defn get-all-users []
  (fetch :users))

(defn store! [user]
  (update! :users user user))

(defn autologin! [{:keys [username] :as user}]
  (session/put! :username username))

(defn login! [{:keys [username password] :as user}]
  (let [{stored-pass :password} (get-user username)]
    (if (and stored-pass
             (crypt/compare password stored-pass))
      (autologin! user)
      (vali/set-error :username "Invalid username and/or password"))))

(defn add! [{:keys [username password] :as user}]
  (when (valid-username? username)
    (when (valid-password? password)
      (do
        (-> user (prepare) (store!))
        (autologin! {:username username})))))
