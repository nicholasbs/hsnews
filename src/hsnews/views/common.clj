(ns hsnews.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css html5 link-to]])
  (:require [clojure.string :as string]
            [noir.session :as session]))

(defpartial error-text [errors]
            [:span (string/join " " errors)])

(defpartial layout [& content]
            (html5
              [:head
               [:title "Hacker School News"]
               (include-css "/css/style.css")]
              [:body
               [:div#wrapper
                [:header
                 (link-to "/" [:img.logo {:src "/img/hacker-school.jpg"}])
                 [:h1#logo "Hacker School News"]
                 [:ul [:li (link-to "/" "new")]
                      [:li (link-to "/submit" "submit")]]]
                 [:div.user
                  (let [username (session/get :username)]
                    (if username
                        [:div
                          [:span username]
                          (link-to "/logout" "log out")]
                        (link-to "/login" "log in")))]
                [:div#content content]
                [:footer
                 [:ul
                  [:li (link-to "https://github.com/nicholasbs/hsnews" "source on github")]]]]]))
