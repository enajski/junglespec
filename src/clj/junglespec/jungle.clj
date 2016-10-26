(ns junglespec.jungle
  (:require [clojure.spec :as s]
            [leipzig.scale :as scale]))


(s/def ::jungle (s/keys :req [::song-key
                              ::arrangement
                              ::tempo
                              ::meter]))

(s/def ::song-key #{[scale/C scale/major] [scale/D scale/dorian]})

(s/def ::tempo (s/int-in 145 190))

(s/def ::meter #{8/8 7/8})

(s/def ::arrangement (s/cat :intro ::intro
                            :verse ::verse
                            :dropdown ::intro
                            :reprise ::verse
                            :ending ::intro))

(s/def ::intro (s/keys :req [::synth]
                       :opt [::bass ::samples]))

(s/def ::verse (s/keys :req [::break ::synth ::bass ::samples]))

(s/def ::duration #{1/4 2/4 4/4})
(s/def ::break-duration #{2/4})

(s/def ::synth (s/+ ::note))
(s/def ::bass (s/+ ::note))
(s/def ::samples (s/+ ::sample))
(s/def ::break (s/+ ::drum-hit))

(s/def ::note (s/cat :pitch ::pitch
                     :duration ::duration))

(s/def ::sample (s/cat :url ::url
                       :duration ::duration))

(s/def ::drum-hit (s/cat :sound ::drum-sound
                         :duration ::break-duration))

(s/def ::drum-sound #{:pu :ci :ta :ty})

(s/def ::pitch (s/int-in 0 12))

(s/def ::url string?)





