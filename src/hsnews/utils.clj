(ns hsnews.utils
  (:use [clojure.data.json :only [read-json]])
  (:require [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]))

(defn load-dotcloud-config []
  (try
    (let [config (read-json (slurp "/home/dotcloud/environment.json"))]
      (def data-mongodb-url (config :DATA_MONGODB_URL))
      (def auth-url (config :AUTH_URL)))
    (catch Exception e
      (def data-mongodb-url "mongodb://:@localhost:27017/hsnews")
      (def auth-url nil))))

(defn time-ago [ts]
  (if ts
    (let [now (ctime/now)
          diff (- (coerce/to-long now) ts)
          oneday 86400000
          onehour 3600000
          oneminute 60000]
      (cond
        (> diff (* oneday 2)) (str (quot diff oneday) " days ago")
        (> diff oneday) (str (quot diff oneday) " day ago")
        (> diff (* onehour 2)) (str (quot diff onehour) " hours ago")
        (> diff onehour) (str (quot diff onehour) " hour ago")
        (> diff (* oneminute 2)) (str (quot diff oneminute) " minutes ago")
        (<= diff (* oneminute 2)) "1 minute ago"))))
