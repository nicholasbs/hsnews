(ns hsnews.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css html5 link-to]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "Hacker School News"]
               (include-css "/css/reset.css")]
              [:body
               [:div#wrapper
                [:header
                 [:h1#logo "Hacker School News"]
                 [:ul [:li (link-to "/newest" "New")]
                      [:li (link-to "/submit" "Submit")]]]
                [:div#content content]]]))
