(ns hsnews.views.user
  (:use noir.core
        hiccup.page-helpers
        hiccup.form-helpers)
  (:require [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [hsnews.models.user :as users]
            [hsnews.models.post :as posts]
            [hsnews.views.common :as common]))

(defpartial hs-link [username & [title]]
            (let [title (if title title username)]
              (link-to (str "https://www.hackerschool.com/private/" username) title))) ; TODO get correct URL from HS API

(defpartial comments-link [username & [title]]
            (let [link-title (if title title username)]
              (link-to (str "/users/" username "/comments") title)))
(defpartial posts-link [username & [title]]
            (let [link-title (if title title username)]
              (link-to (str "/users/" username "/posts") title)))

(defpartial user-fields [{:keys [username] :as user}]
            [:ul.userForm
             [:li
              (text-field {:placeholder "Username"} :username username)
              (vali/on-error :username common/error-text)]
             [:li
              (password-field {:placeholder "Password"} :password)
              (vali/on-error :password common/error-text)]])

(defpartial user-item [{:keys [username] :as user}]
            [:li (common/user-link username)])

(defpartial list-users [users]
            [:ol.userList
             (map user-item users)])

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

(defpage "/users/:username" {:keys [username]}
         (common/layout
           [:ul.userFields
            [:li
              [:span.label "user:"]
              username]
            [:li ; TODO add timestamp for created
              [:span.label "link:"]
             (hs-link username)]
            [:li
             [:span.label " "]
             (posts-link username "submissions")]
            [:li
             [:span.label " "]
             (comments-link username "comments")]]))

(defpage "/users/:username/comments" {:keys [username]}
         (common/layout
           [:h2 "Comments"]
            (common/comment-list (users/get-comments username))))

(defpage "/users/:username/posts" {:keys [username]}
         (common/layout
           [:h2 "Submissions"]
            (common/post-list (users/get-posts username))))

(defpage "/lists" {}
         (common/layout
           [:h2 "All Users"]
            (list-users (users/get-all-users))))
