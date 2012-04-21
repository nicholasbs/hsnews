(ns hsnews.views.post
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers)
  (:require [hsnews.models.post :as posts]
            [noir.validation :as vali]
            [noir.response :as resp]
            [hsnews.views.common :as common]
            [clojure.string :as string]))

(defpartial error-text [errors]
            [:span (string/join " " errors)])

(defpartial post-fields [{:keys [title link]}]
            (vali/on-error :title error-text)
            (text-field {:placeholder "Title"} :title title)
            (vali/on-error :link error-text)
            (text-field {:placeholder "Link"} :link link))

; Main view
(defpartial post-item [{:keys [link title] :as post}]
            (when post
             [:li.post
              (link-to link title)]))

(defpartial post-list [items]
            [:ol.posts
             (map post-item items)])

(defpage "/" []
         (posts/maybe-init)
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
