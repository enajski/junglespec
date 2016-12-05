(ns junglespec.spec
  (:require
    #?(:clj [clojure.spec :as s]
       :cljs [cljs.spec :as s])
    #?(:clj [leipzig.scale :as scale]
       :cljs [leipzig.scale :as scale])))

(s/def ::note (s/cat :pitch (s/int-in 0 12)
                     :duration #{2 4}))

(s/def ::bass-seq (s/coll-of ::note :kind vector?))

(s/def ::scale #{[scale/C scale/minor scale/low scale/low]
                 [scale/F scale/minor scale/low scale/low]})

(s/def ::tempo #{172})

(s/def ::amen-sounds #{:pu :ci :ta :cii :cita :pupu})

(s/def ::amen-sound
  (s/or :kick (s/coll-of #{:pu} :min-count 1 :max-count 2)
        :ride (s/coll-of #{:ci} :count 1)
        :ride-semisnare (s/coll-of #{:cita} :min-count 1 :max-count 4)
        :snare (s/coll-of #{:ta} :count 1)
        :crash (s/coll-of #{:cii} :count 1)
        :doublekick (s/coll-of #{:pupu} :count 1)))

(s/def ::amen-seq (s/coll-of ::amen-sound :min-count 32))

;; Should be paths to samples or buffers
(s/def ::ragga-sample (s/cat :note #{"/Users/dev/Music/ragga_samples/all_junglists.wav"
                                     "/Users/dev/Music/ragga_samples/all_ganjaman_let_loose.wav"
                                     "/Users/dev/Music/ragga_samples/6_million_wayz(1).wav"
                                     "/Users/dev/Music/ragga_samples/alrightholdon.wav"
                                     "/Users/dev/Music/ragga_samples/bad_bwoyy.wav"
                                     "/Users/dev/Music/ragga_samples/dibbydibby_soun.wav"
                                     "/Users/dev/Music/ragga_samples/heyheyheyhey.wav"
                                     "/Users/dev/Music/ragga_samples/imakehits.wav"
                                     "/Users/dev/Music/ragga_samples/justcome.wav"}
                             :duration (s/int-in 8 32)))

(s/def ::ragga-seq (s/coll-of ::ragga-sample))

(s/def ::breakdown (s/keys :req [::bass-seq ::ragga-seq]))

(s/def ::verse (s/keys :req [::amen-seq ::bass-seq ::ragga-seq]))

(s/def ::arrangement (s/cat :intro ::breakdown
                            :verse ::verse
                            :breakdown ::breakdown
                            :reprise ::verse
                            :outro ::breakdown))

(s/def ::artist-name #{"Foo Junglist"
                       "Bar Man"
                       "Baz Ooka"})

(s/def ::song-title string?)

(s/def ::album-name string?)

(s/def ::song (s/keys :req [::arrangement ::scale
                            ::artist-name ::song-title]))

(s/def ::songs (s/coll-of ::song))

(s/def ::album (s/keys :req [::songs ::album-name]))

(s/def ::discography (s/coll-of ::album))

(s/def ::dubplate (s/cat :a-side ::song
                         :b-side ::song))

