(ns hsnews.server
  (:require [noir.server :as server]
            [hsnews.db :as db]
            [hsnews.views.common]
            [hsnews.views.user]
            [hsnews.views.post]
            [hsnews.utils :as utils]))

(utils/load-dotcloud-config)
(server/load-views "hsnews/views/")
(db/maybe-init)

(def handler (server/gen-handler {:mode :prod
                                  :ns 'hsnews}))
