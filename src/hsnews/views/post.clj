(ns hsnews.views.post
  (:use noir.core
        somnium.congomongo
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers)
  (:require [hsnews.models.post :as posts]
            [hsnews.models.comment :as comments]
            [noir.validation :as vali]
            [noir.response :as resp]
            [clj-time.coerce :as coerce]
            [clj-time.format :as tform]
            [hsnews.views.common :as common]
            [clojure.string :as string]))


(defpartial post-fields [{:keys [title link]}]
            [:ul
              [:li 
                (text-field {:placeholder "Title"} :title title)
                (vali/on-error :title common/error-text)]
              [:li
                (text-field {:placeholder "Link"} :link link)
                (vali/on-error :link common/error-text)]])

; Main view
(defpartial post-item [{:keys [link title author ts] :as post}]
            (when post
             [:li.post
              (link-to link title)
              [:div.subtext
               [:span "by " (common/user-link author) " "]
               [:span.date (tform/unparse common/date-format (coerce/from-long ts))]
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
           [:h2 "Submit"]
           (form-to {:class "postForm"} [:post "/submit/create"]
                    (post-fields post)
                    (submit-button "submit"))
           [:div.disclaimer "Posts are visible only to Hacker Schoolers. Nevertheless, use common sense when posting sensitive stuff."]))

(defpage [:post "/submit/create"] {:keys [link title]}
         (let [post {:link link :title title}]
           (if (posts/add! post)
             (resp/redirect "/")
             (render "/submit" post))))

; Comments
(defpartial comment-form [{:keys [body]}
                          {:keys [_id]}]
            (form-to {:class "commentForm"} [:post "/comments/create"]
              [:ul
               [:li
                (text-area :body body)
                (vali/on-error :body common/error-text)]]
               (hidden-field :post_id _id)
               (submit-button "add comment")))

; View post / discuss page
(defpartial post-page [{:keys [title link author ts] :as post}
                       {:as comment}]
            (when post
              [:div.post
               [:h1 (link-to link title)]
               [:div.subtext
                [:span "by " (common/user-link author) " "]
                [:span.date (tform/unparse common/date-format (coerce/from-long ts))]]
               (comment-form comment post)
               (common/comment-list (posts/get-comments post))]))

(defpage "/posts/:_id" {:keys [_id]}
         (common/layout
           (post-page (posts/id->post _id) {})))

(defpage [:post "/comments/create"] {:keys [body post_id parent_id]}
         (let [comment {:body body :post_id post_id :parent_id parent_id}
               post_url (str "/posts/" (.toString post_id))]
          (if (comments/add! comment)
            (resp/redirect post_url) ; should redirect to post page
            (render post_url {:_id (.toString post_id)} comment))))

