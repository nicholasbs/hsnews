(ns hsnews.views.user
  (:use noir.core
        hiccup.page-helpers
        hiccup.form-helpers)
  (:require [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [hsnews.models.user :as users]
            [hsnews.views.common :as common]))

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

(defpage "/register" {:as user}
         (common/layout
          [:h2 "Create Account"]
          (form-to [:post "/users/create"]
                   (user-fields user)
                   (submit-button {:class "submit"} "create account"))))

(defpage [:post "/sessions/create"] {:as user}
         (if (users/login! user)
           (resp/redirect "/")
           (render "/login" user)))

(defpage [:post "/users/create"] {:as user}
         (if (users/add! user)
           (resp/redirect "/")
           (render "/register" user)))

(defpage "/logout" {}
         (session/clear!)
         (resp/redirect "/"))
