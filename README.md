# junglespec

## Overview

Based on ideas preseted in my Conj 2016 talk: [Composing music with clojure.spec](https://www.youtube.com/watch?v=404UXttr8kw)

[Slides](http://www.slideshare.net/WojciechFranke/wojciech-franke-composing-music-with-clojurespec-clojureconj-2016)

## Playing

As of 2016/12/05 needs the latest GitHub version of overtone to work with spec.

`lein git-deps`

`conj.clj` has the example composition from the talk. Eval it it your favourite editor or in the REPL to play.

This depends on having breakbeat and ragga samples as wav files. These are not bundled in the repo, you can download them separately [HERE](http://enajski.pl/junglespec_samples.zip). Change the paths of `ragga-sounds` and `breaks` accordingly.

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
