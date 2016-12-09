# junglespec

## Overview

Based on ideas preseted in my Conj 2016 talk: [https://www.youtube.com/watch?v=404UXttr8kw](Composing music with clojure.spec)

Slides: [http://www.slideshare.net/WojciechFranke/wojciech-franke-composing-music-with-clojurespec-clojureconj-2016](Slides)

## Playing

As of 2016/12/05 needs the latest GitHub version of overtone to work with spec.

`lein git-deps`

Open a terminal and type `lein repl` to start a Clojure REPL
(interactive prompt).

`conj.clj` has the example composition from the talk.

This depends on having breakbeat and ragga samples as wav files.

### Web

To start the cljs documentation in the REPL, type

```clojure
(run)
(browser-repl)
```

The call to `(run)` starts the Figwheel server at port 3449, which takes care of
live reloading ClojureScript code and CSS. Figwheel's server will also act as
your app server, so requests are correctly forwarded to the http-handler you
define.

Running `(browser-repl)` starts the Figwheel ClojureScript REPL. Evaluating
expressions here will only work once you've loaded the page, so the browser can
connect to Figwheel.

When you see the line `Successfully compiled "resources/public/app.js" in 21.36
seconds.`, you're ready to go. Browse to `http://localhost:3449` and enjoy.

**Attention: It is not needed to run `lein figwheel` separately. Instead we
launch Figwheel directly from the REPL**

## License

Copyright © 2016 Wojtek Franke

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

## Chestnut

Created with [Chestnut](http://plexus.github.io/chestnut/) 0.14.0 (66af6f40).
