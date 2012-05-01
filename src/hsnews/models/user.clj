(ns hsnews.models.user
  (:use somnium.congomongo
        [clojure.data.json :only [read-json]]
        [clojure.string :only [blank?]]
        [hsnews.utils :only [auth-url]])
  (:require [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]
            [clj-http.client :as client]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session]))

(defn current-user []
  (session/get :hs_id))

(defn get-user [hs_id]
  (fetch-one :users :where {:hs_id hs_id}))

(defn get-username [hs_id]
  ((get-user hs_id) :username))

(defn get-comments [hs_id]
  (fetch :comments :where {:author hs_id}))

(defn get-posts [hs_id]
  (fetch :posts :where {:author hs_id}))

(defn get-karma [hs_id]
  (int (:karma (get-user hs_id))))

(defn prepare [{password :password :as user}]
  (let [ts (ctime/now)]
    (-> user
      (assoc :ts (coerce/to-long ts))
      (assoc :password (crypt/encrypt password))
      (assoc :karma 0))))

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

(defn get-top-users []
  (fetch :users :sort {:karma -1} :limit 100))

(defn store! [user]
  (update! :users user user))

(defn determine-username [{:keys [twitter github irc email hs_id] :as user}]
  (cond (not (blank? twitter)) twitter
        (not (blank? github)) github
        (not (blank? irc)) irc
        (not (blank? email)) email
        :else hs_id))

(defn get-or-create-user! [{:keys [hs_id twitter] :as user}]
  (let [existing (fetch-one :users :where {:hs_id hs_id})]
    (if existing
      (store! (merge existing user))
      (let [ts (ctime/now)
            new-user (-> user
                       (assoc :username (determine-username user))
                       (assoc :karma 0)
                       (assoc :ts (coerce/to-long ts)))]

        (insert! :users new-user)))))

(defn autologin! [{:keys [hs_id] :as user}]
  (session/put! :hs_id hs_id))

(defn login! [{:keys [username password]}]
  (let [request-url auth-url
        resp (client/post request-url {:throw-exceptions false :form-params {:email username :password password}})]
    (if (= 200 (resp :status))
      (let [resp-data (read-json (resp :body))
            hs_id (str (resp-data :hs_id))
            user-info {:hs_id hs_id
                       :twitter (resp-data :twitter)
                       :github (resp-data :github)
                       :email (resp-data :email)
                       :irc (resp-data :irc)}
            user (get-or-create-user! user-info)]
        (autologin! (fetch-one :users :where {:hs_id hs_id})))
      (vali/set-error :username "Invalid username and/or password"))))

  (comment
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
