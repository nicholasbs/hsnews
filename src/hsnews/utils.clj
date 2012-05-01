(ns hsnews.utils
  (:use [clojure.data.json :only [read-json]]))

(defn load-dotcloud-config []
  (try
    (let [config (read-json (slurp "/home/dotcloud/environment.json"))]
      (def data-mongodb-url (config :DATA_MONGODB_URL))
      (def auth-url (config :AUTH_URL)))
    (catch Exception e
      (def data-mongodb-url "mongodb://:@localhost:27017/hsnews")
      (def auth-url nil))))
