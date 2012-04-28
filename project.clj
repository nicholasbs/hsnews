(defproject hsnews "0.1.0-SNAPSHOT"
            :description "A Hacker News clone in Clojure"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [org.clojure/data.json "0.1.2"]
                           [noir "1.2.1"]
                           [congomongo "0.1.9"]
                           [clj-time "0.4.1"]]
            :plugins      [[lein-ring "0.6.6"]]
            :ring {:handler hsnews.server/handler})

