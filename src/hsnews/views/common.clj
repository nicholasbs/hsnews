(ns hsnews.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css html5 link-to]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "Hacker School News"]
               (include-css "/css/style.css")]
              [:body
               [:div#wrapper
                [:header
                 [:img.logo {:src "/img/hacker-school.jpg"}]
                 [:h1#logo "Hacker School News"]
                 [:ul [:li (link-to "/" "new")]
                      [:li (link-to "/submit" "submit")]]]
                [:div#content content]
                [:footer
                 [:ul
                  [:li (link-to "https://github.com/nicholasbs/hsnews" "source on github")]]]]]))
