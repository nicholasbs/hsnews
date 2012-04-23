(ns hsnews.views.post
  (:use noir.core
        somnium.congomongo
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers)
  (:require [hsnews.models.post :as posts]
            [noir.validation :as vali]
            [noir.response :as resp]
            [clj-time.core :as ctime]
            [clj-time.format :as tform]
            [clj-time.coerce :as coerce]
            [hsnews.views.common :as common]
            [clojure.string :as string]))

(def date-format (tform/formatter "MM/dd/yy" (ctime/default-time-zone)))


(defpartial post-fields [{:keys [title link]}]
            (vali/on-error :title common/error-text)
            (text-field {:placeholder "Title"} :title title)
            (vali/on-error :link common/error-text)
            (text-field {:placeholder "Link"} :link link))

; Main view
(defpartial post-item [{:keys [link title ts] :as post}]
            (when post
             [:li.post
              (link-to link title)
              [:div.subtext
               [:span.date (tform/unparse date-format (coerce/from-long ts))]
               [:span " | "]
               (link-to (posts/view-url post) "discuss")]]))

(defpartial post-list [items]
            [:ol.posts
             (map post-item items)])

(defpage "/" []
         (common/layout
           (post-list (posts/get-latest))))

; New submission view
(defpage "/submit" {:as post}
         (common/layout
           (form-to [:post "/submit/create"]
                    (post-fields post)
                    (submit-button "Add post"))))

(defpage [:post "/submit/create"] {:keys [link title]}
         (let [post {:link link :title title}]
           (if (posts/add! post)
             (resp/redirect "/")
             (render "/submit" post))))


; View post / discuss page
(defpartial post-page [{:keys [title link ts] :as post}]
            (println post)
            (when post
              [:div.post
               [:h1 (link-to link title)]
               [:div.subtext
                [:span.date (tform/unparse date-format (coerce/from-long ts))]]]))
                ;[:span " | "]]];))

(defpage "/posts/:_id" {:keys [_id]}
         (common/layout
           (post-page (fetch-one :posts))))
