(ns hsnews.views.common
  (:use noir.core
        [hiccup.page-helpers :only [include-css html5 link-to]]
        somnium.congomongo)
  (:require [clojure.string :as string]
            [noir.response :as resp]
            [noir.request :as req]
            [clj-time.format :as tform]
            [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]
            [hsnews.models.user :as users]
            [hsnews.models.post :as posts]))

(pre-route "/*" {:keys [uri]}
           (when-not (or 
                       (users/current-user)
                       (= uri "/login")
                       (= uri "/sessions/create")
                       (re-find #"^/(css)|(img)|(js)|(favicon)" uri))
            (resp/redirect "/login")))

(def date-format (tform/formatter "MM/dd/yy" (ctime/default-time-zone)))

(defn extract-domain-from-url [url]
  (second (re-find #"^(?:[^:/]*://)?(?:www\.)?([^/\?]+)(?:.*)$" url)))

(defpartial user-link [username]
  (link-to (str "/users/" username) username))

(defpartial upvote-link [post]
  (link-to {:class "upvote"} (posts/upvote-url post) "&#9650;"))


(defpartial comment-count [{:keys [_id] :as post}]
            (let [comment-count (fetch-count :comments :where {:post_id _id})]
              (link-to (posts/view-url post) (str comment-count " comment" (if (not= comment-count 1) "s" "")))))

(defpartial subtext [{:keys [ts author post_id] :as item}]
            [:div.subtext
              [:span "by " (user-link author)]
              [:span.date (tform/unparse date-format (coerce/from-long ts))]
              (if-not post_id [:span.commentCount (comment-count item)])])

(defpartial comment-item [{:keys [body] :as comment}]
            [:li
             (subtext comment)
             [:div.commentBody body]])

; TODO Make this function less horrible and inefficient.
; (no need for extra map over comments)
(defpartial comment-list [comments]
            (let [posts (fetch-by-ids :posts (map #(get % :post_id) comments))
                  posts-by-id (reduce #(assoc %1 (get %2 :_id) %2) {} posts)
                  comments (map #(assoc % :post_title (get (get posts-by-id (get % :post_id)) :title)) comments)]
              [:ol.commentList
               (map comment-item comments)]))

(defpartial post-item [{:keys [link title author ts] :as post}]
            (when post
             [:li.post
              [:div.title
                (upvote-link post)
                (link-to {:class "postLink"} link title)
                [:span.domain "(" (extract-domain-from-url link) ")"]]
              (subtext post)]))

(defpartial post-list [items]
            [:ol.postList
             (map post-item items)])

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
                 [:ul 
                  [:li (link-to "/" "new")]
                  [:li (link-to "http://www.hackruiter.com/companies" "jobs")]
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
