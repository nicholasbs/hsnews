(ns hsnews.views.common
  (:use [noir.core]
        [hiccup.page-helpers :only [include-css html5 link-to]])
  (:require [clojure.string :as string]
            [noir.response :as resp]
            [noir.request :as req]
            [hsnews.models.user :as users]))



(pre-route "/*" {:keys [uri]}
           (when-not (or 
                       (users/current-user)
                       (= uri "/login")
                       (= uri "/sessions/create")
                       (re-find #"^/(css)|(img)|(js)|(favicon)" uri))
            (resp/redirect "/login")))

(defn user-link [username]
  (link-to (str "/users/" username) username))

(defpartial error-text [errors]
            [:span.error (string/join " " errors)])

(defpartial layout [& content]
            (html5
              [:head
               [:title "Hacker School News"]
               (include-css "/css/style.css")]
              [:body
               [:div#wrapper
                [:header
                 (link-to "/" [:img.logo {:src "/img/hacker-school-logo.png"}])
                 [:h1#logo
                  (link-to "/" "Hacker School News")]
                 [:ul [:li (link-to "/" "new")]
                      [:li (link-to "/submit" "submit")]]
                 (let [username (users/current-user)]
                  (if username
                    [:div.user.loggedin
                      [:span.username (user-link username)]
                      (link-to "/logout" "log out")]
                    [:div.user.loggedout
                      (link-to {:class "register"} "/register" "register")
                      (link-to "/login" "log in")]))]
                [:div#content content]
                [:footer
                 [:ul
                  [:li (link-to "http://www.hackerschool.com" "Hacker School")]
                  [:li (link-to "https://github.com/nicholasbs/hsnews" "Source on Github")]]]]]))
