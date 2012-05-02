(ns hsnews.views.user
  (:use noir.core
        hiccup.page-helpers
        hiccup.form-helpers)
  (:require [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [hsnews.models.user :as users]
            [hsnews.models.post :as posts]
            [hsnews.views.common :as common]
            [hsnews.utils :as utils]))

(defpartial hs-link [hs_id & [title]]
            (let [username (users/get-username hs_id)
                  title (if title title username)]
              (link-to (str "https://www.hackerschool.com/people/" hs_id) title)))

(defpartial comments-link [hs_id & [title]]
            (let [username (users/get-username hs_id)
                  title (if title title username)]
              (link-to (str "/users/" hs_id "/comments") title)))

(defpartial posts-link [hs_id & [title]]
            (let [username (users/get-username hs_id)
                  title (if title title username)]
              (link-to (str "/users/" hs_id "/posts") title)))

(defpartial user-fields [{:keys [username] :as user}]
            [:ul.userForm
             [:li
              (text-field {:placeholder "Username"} :username username)
              (vali/on-error :username common/error-text)]
             [:li
              (password-field {:placeholder "Password"} :password)
              (vali/on-error :password common/error-text)]])

(defpage "/login" {:as user}
         (common/layout
          [:h2 "Log in"]
          (form-to [:post "/sessions/create"]
                    (user-fields user)
                   (submit-button {:class "submit"} "Log in"))))

;(defpage "/register" {:as user}
;         (common/layout
;          [:h2 "Create Account"]
;          (form-to [:post "/users/create"]
;                   (user-fields user)
;                   (submit-button {:class "submit"} "create account"))))
;
;(defpage [:post "/users/create"] {:as user}
;         (if (users/add! user)
;           (resp/redirect "/")
;           (render "/register" user)))

(defpage [:post "/sessions/create"] {:as user}
         (let [return-uri (session/flash-get)]
           (if (users/login! user)
             (resp/redirect (or return-uri "/"))
             (render "/login" user))))

(defpage "/logout" {}
         (session/clear!)
         (resp/redirect "/"))

(defpage "/users/:hs_id" {:keys [hs_id]}
         (let [user (users/get-user hs_id)
               username (user :username)]
           (common/layout
             [:ul.userFields
              [:li
               [:span.label "user:"]
               username]
              [:li
               [:span.label "created:"]
               (utils/time-ago (:ts user))]
              [:li
               [:span.label "karma"]
               (users/get-karma hs_id)]
              [:li
               [:span.label "link:"]
               (hs-link hs_id)]
              [:li
               [:span.label " "]
               (posts-link hs_id "submissions")]
              [:li
               [:span.label " "]
               (comments-link hs_id "comments")]])))

(defpage "/users/:hs_id/comments" {:keys [hs_id]}
         (common/layout
           [:h2 "Comments"]
            (common/comment-list (users/get-comments hs_id))))

(defpage "/users/:hs_id/posts" {:keys [hs_id]}
         (common/layout
           [:h2 "Submissions"]
            (common/post-list (users/get-posts hs_id))))

(defpartial user-item [{:keys [hs_id] :as user}]
            [:li (common/user-link hs_id)])

(defpartial list-users [users]
            [:ol.userList
             (map user-item users)])

(defpage "/lists" {}
         (common/layout
           [:div {:class "listLink"}
            (link-to "/leaders" "leaders")
            [:span "Users with most karma."]]))

(defpage "/leaders" {}
         (common/layout
           [:h2 "Top Users"]
            (list-users (users/get-top-users))))
