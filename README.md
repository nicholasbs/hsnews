# hsnews

A [Hacker News](http://news.ycombinator.com) clone written in [Clojure](http://clojure.org) using [Noir](http://webnoir.org).

Created for [Hacker School](http://hackerschool.com) batch[2].

## Usage

First install leiningen

    brew install leiningen

Clone this repository and run

    lein deps
    lein run

By default, new user registration is disabled.  If you want to enable user registration, uncomment the relevant lines in `src/hsnews/views/common.clj` and `src/hsnews/views/user.clj`.

## License

Copyright (C) 2012 Hacker School

Distributed under the terms of the AGPL

