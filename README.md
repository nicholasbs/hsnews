# hsnews

A [Hacker News](http://news.ycombinator.com) clone written in [Clojure](http://clojure.org) using [Noir](http://webnoir.org).

Created for [Hacker School](http://hackerschool.com) batch[2].

## Example

A live version is viewable at [http://news.hackerschool.com](http://news.hackerschool.com). Login on this site is restricted to [Hacker School](http://hackerschool.com) students and alumni.


## Usage

First install leiningen

    brew install leiningen

Clone this repository and run

    lein deps
    lein ring server

By default, new user registration is disabled. If you want to enable user registration, uncomment the relevant lines in `src/hsnews/views/common.clj` and `src/hsnews/views/user.clj`.

## Deployment

Deployment specific code for [dotcloud](http://dotcloud.com) is included in `src/hsnews/db.clj` to load environment variables. If a `DOTCLOUD_DATA_MONGODB_URL` variable isn't present, then the site falls back to using a local mongodb database.

There is also the ability to use an external server for authentication. By default, users are authenticaed against the local database. If an `AUTH_URL` environment variable is present then that URL will be used for authentication.

## License

Copyright (C) 2012 Hacker School

Distributed under the terms of the AGPL

