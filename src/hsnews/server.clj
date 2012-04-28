(ns hsnews.server
  (:require [noir.server :as server]
            [hsnews.db :as db]
            [hsnews.views.common]
            [hsnews.views.user]
            [hsnews.views.post]))

(server/load-views "hsnews/views/")

;(defn -main [& m]
;  (let [mode (keyword (or (first m) :dev))
;        port (Integer. (get (System/getenv) "PORT" "8080"))]
;    (db/maybe-init)
;    (server/start port {:mode mode
;                        :ns 'hsnews})))
;
(db/maybe-init)

(def handler (server/gen-handler {:mode :prod
                                  :ns 'hsnews}))
