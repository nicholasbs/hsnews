(ns hsnews.views.common
  (:use noir.core
        [hiccup.page-helpers :only [include-css html5 link-to]]
        somnium.congomongo)
  (:require [clojure.string :as string]
            [noir.response :as resp]
            [noir.request :as req]
            [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]
            [hsnews.models.user :as users]
            [hsnews.models.post :as posts]
            [hsnews.models.comment :as comments]
            [noir.session :as session]))

(defn get-request-uri []
  (:uri (req/ring-request)))

(defn get-referer []
  ((:headers (req/ring-request)) "referer"))

(pre-route "/*" {:keys [uri]}
           (when-not (or
                       (users/current-user)
                       (= uri "/login")
                       (= uri "/sessions/create")
                       ;(= uri "/register") ;; uncomment to allow registration
                       ;(= uri "/users/create") ;; uncomment to allow registration
                       (re-find #"^/(css)|(img)|(js)|(favicon)" uri))
            (session/flash-put! (get-request-uri))
            (resp/redirect "/login")))

(defn time-ago [ts]
  (if ts
    (let [now (ctime/now)
          diff (- (coerce/to-long now) ts)
          oneday 86400000
          onehour 3600000
          oneminute 60000]
      (cond
        (> diff (* oneday 2)) (str (quot diff oneday) " days ago")
        (> diff oneday) (str (quot diff oneday) " day ago")
        (> diff (* onehour 2)) (str (quot diff onehour) " hours ago")
        (> diff onehour) (str (quot diff onehour) " hour ago")
        (> diff (* oneminute 2)) (str (quot diff oneminute) " minutes ago")
        (<= diff (* oneminute 2)) "1 minute ago"))))

(defn extract-domain-from-url [url]
  (second (re-find #"^(?:[^:/]*://)?(?:www\.)?([^/\?]+)(?:.*)$" url)))

(defpartial user-link [hs_id]
  (link-to (str "/users/" hs_id) (users/get-username hs_id)))

(defpartial upvote-comment-link [com]
  (if (comments/is-author? com) [:span.isAuthor.indicator "*"])
  (if-not (comments/voted? com)
    (link-to {:class "upvote indicator"} (comments/upvote-url com) "&#9650;")))

(defpartial comment-count [{:keys [_id score] :as post}]
            (let [comment-count (fetch-count :comments :where {:post_id _id})]
              (link-to {:title score} (posts/post-url post) (str comment-count " comment" (if (not= comment-count 1) "s" "")))))

(defpartial comment-subtext [{:keys [ts author points post_id] :as com}]
            [:div.subtext.comment
              (upvote-comment-link com)
              [:span.author (user-link author)]
              [:span.date (time-ago ts)]])

(defpartial comment-item [{:keys [body] :as com}]
            [:li
             (comment-subtext com)
             [:div.commentBody body]])

; TODO Make this function less horrible and inefficient.
; (no need for extra map over comments)
(defpartial comment-list [comments]
            (let [posts (fetch-by-ids :posts (map #(get % :post_id) comments))
                  posts-by-id (reduce #(assoc %1 (get %2 :_id) %2) {} posts)
                  comments (map #(assoc % :post_title (get (get posts-by-id (get % :post_id)) :title)) comments)]
              [:ol.commentList
               (map comment-item comments)]))

(defpartial upvote-link [post]
  (if (posts/is-author? post) [:span.isAuthor.indicator "*"])
  (if-not (posts/voted? post)
    (link-to {:class "upvote indicator"} (posts/upvote-url post) "&#9650;")))

(defpartial post-subtext [{:keys [ts author points] :as post}]
            [:div.subtext
              [:span.points points " points"]
              [:span.author (user-link author)]
              [:span.date (time-ago ts)]
              [:span.commentCount (comment-count post)]])

(defpartial post-item [{:keys [link title author ts] :as post}]
            (when post
             [:li.post
              [:h3.title
                (upvote-link post)
                (link-to {:class "postLink"} link title)
                [:span.domain "(" (extract-domain-from-url link) ")"]]
              (post-subtext post)]))

(defpartial post-list [items]
            (if (not-empty items)
              [:ol.postList (map post-item items)]
              [:div.empty "No posts"]))

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
                  [:li (link-to "/newest" "new")]
                  [:li (link-to "http://www.hackruiter.com/companies" "jobs")]
                  [:li (link-to "/submit" "submit")]]
                 (let [hs_id (users/current-user)]
                  (if hs_id
                    [:div.user.loggedin
                      [:span.username (user-link hs_id) " (" (users/get-karma hs_id) ")"]
                      (link-to "/logout" "log out")]
                    [:div.user.loggedout
                      ;(link-to "/register" "register") ;; Uncomment to allow registration
                      (link-to "/login" "log in")]))]
                [:div#content content]
                [:footer
                 [:ul
                  [:li (link-to "/lists" "Lists")]
                  [:li (link-to "http://www.hackerschool.com" "Hacker School")]
                  [:li (link-to "https://github.com/nicholasbs/hsnews/issues" "Feature Requests")]
                  [:li (link-to "https://github.com/nicholasbs/hsnews" "Source on Github")]]]]]))
