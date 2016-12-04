(ns junglespec.conj
  (:use [overtone.live])
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test.check.generators :as gens]
            [overtone.inst.synth :as synth]
            [leipzig.scale :as scale]
            [leipzig.melody :refer [phrase all tempo bpm
                                    where with rhythm times]]
            [leipzig.live :as l]))

(defn make
  "Generate example given a spec name"
  [spec]
  (gen/generate (s/gen spec)))

(s/def ::note (s/cat :pitch (s/int-in 0 24)
                     :duration #{2 4}))

(s/def ::bass-seq (s/coll-of ::note :kind vector?))

(s/def ::scale #{[scale/C scale/minor scale/low scale/low]
                 [scale/F scale/minor scale/low scale/low]})


(s/def ::amen-sounds #{:pu :ci :ta :cii :cita :pupu})

(s/def ::amen-sound
  (s/or :kick (s/coll-of #{:pu} :min-count 1 :max-count 2)
        :ride (s/coll-of #{:ci} :count 1)
        :ride-semisnare (s/coll-of #{:cita} :min-count 1 :max-count 4)
        :snare (s/coll-of #{:ta} :count 1)
        :crash (s/coll-of #{:cii} :count 1)
        :doublekick (s/coll-of #{:pupu} :count 1)))

(s/def ::amen-seq (s/coll-of ::amen-sound :min-count 32))

(defn play-bass [{:keys [pitch duration]}]
  (let [cutoff (make (s/int-in 150 290))]
    (synth/tb303 pitch 1 0.7 0.01 0.2 duration 0.2 cutoff 0.9)))

(defmethod l/play-note :bass [note]
  (play-bass note))

(defn unzip [coll]
  (vector (take-nth 2 (rest coll))
          (take-nth 2 coll)))

(defn leipzigise [section]
  (->> section
       unzip
       (apply phrase)))

(defn rhythmise
  [section]
  (let [timing (rhythm (repeat (count section) 1/2))]
    (->> (interleave timing (map #(hash-map :note % ) section))
         (partition 2)
         (map #(apply merge %)))))

(defonce breaks (load-samples "/Users/dev/Music/real_jungle_loops_by_noise_relations/*.wav"))

(defn slicer [buf start end]
  (stereo-partial-player buf 1 start end))

(def amen1 (nth breaks 32))
(def amen2 (nth breaks 31))
(def amen3 (nth breaks 33))

(defmulti amen :note)

(defmethod amen :pu [_]
  (apply slicer (choose [[amen1 0 1/8]
                         [amen3 0 1/8]])))

(defmethod amen :ci [_]
  (slicer amen1 1/8 2/8))

(defmethod amen :ta [_]
  (apply slicer (choose [[amen1 2/8 3/8]
                         [amen3 2/8 3/8]])))
(defmethod amen :cita [_]
  (apply slicer (choose [[amen1 3/8 4/8]
                         [amen3 6/8 7/8]])))
(defmethod amen :pupu [_]
  (slicer amen2 5/8 6/8))

(defmethod amen :cii [_]
  ((sample (freesound-path 26884) :amp 0.4)))

(defmethod l/play-note :amen [{sound :note duration :duration}]
  (amen {:note sound}))

(defn rhythmise2
  [section]
  (let [timing (rhythm (repeat (count section) 4))]
    (->> (interleave timing (map #(hash-map :note % ) section))
         (partition 2)
         (map #(apply merge %)))))

(s/def ::breakbeat
  (s/cat :note (set breaks)
         :duration #{16}))

(s/def ::breakbeat-sequence
  (s/with-gen (s/coll-of ::breakbeat)
    #(gens/let [first (s/gen ::breakbeat)
                second (s/gen ::breakbeat)]
       (gens/return [first first first second]))))

(defmethod l/play-note :backing-break [{sound :note duration :duration}]
  (slicer sound 0 8/8))

(def backbeat1 (->> ::breakbeat-sequence
                    s/gen
                    gen/generate
                    flatten
                    (take 4)
                    rhythmise2
                    (all :part :backing-break)))



(s/def ::breakdown (s/keys :req [::bass-seq ::ragga-seq]))

(s/def ::verse (s/keys :req [::amen-seq ::bass-seq ::ragga-seq]))

(s/def ::arrangement (s/cat :intro ::breakdown
                            :verse ::verse
                            :breakdown ::breakdown
                            :reprise ::verse
                            :outro ::breakdown))

(s/def ::artist-name (s/and #{"Foo Junglist"
                              "Bar Man"
                              "Bazooka"}))

(s/def ::song-title string?)

(s/def ::album-name string?)

(s/def ::song (s/keys :req [::arrangement ::scale
                            ::artist-name ::song-title]))

(s/def ::songs (s/coll-of ::song))

(s/def ::album (s/keys :req [::songs ::album-name]))

(s/def ::discography (s/coll-of ::album))

(s/def ::dubplate (s/cat :a-side ::song
                         :b-side ::song))


(def ragga-sounds
  (load-samples "/Users/dev/Music/ragga_samples/*.wav"))

(s/def ::ragga-sample (s/cat :note (set ragga-sounds)
                             :duration (s/int-in 8 32)))

(s/def ::ragga-seq (s/coll-of ::ragga-sample))


(defn ragga-samples [] (->> ::ragga-seq
                            s/gen
                            gen/generate
                            (take 4)
                            (s/conform ::ragga-seq)
                            rhythmise2
                            (all :part :sample)))

(defn breaks [] (->> ::amen-seq
                     s/gen
                     gen/generate
                     flatten
                     (take 32)
                     rhythmise
                     (all :part :amen)))

(defmacro leipzigise-amen
  [amen-seq]
  `(->> ~amen-seq
        flatten
        (take 32)
        rhythmise
        (all :part :amen)))

(defmacro leipzigise-ragga
  [ragga-seq]
  `(->> ~ragga-seq
        (take 2)
        (s/conform ::ragga-seq)
        rhythmise2
        (all :part :sample)))

(defmacro leipzigise-bass [bass-seq scale]
  `(->> ~bass-seq
        flatten
        (take 8)
        leipzigise
        (all :part :bass)
        (where :pitch (apply comp ~scale))))


(defmacro leipzigise-section
  [amen bass ragga scale]
  `(->> (with (times 4 (with (leipzigise-amen ~amen)
                             (leipzigise-bass ~bass ~scale)))
              (leipzigise-ragga ~ragga))))

(let [section (make ::verse)]
  (leipzigise-section (::amen-seq section)
                      (::bass-seq section)
                      (::ragga-seq section)
                      (make ::scale)))

(leipzigise-song (make ::song))
(leipzigise-ragga (make ::breakbeat-sequence))
(->> (times 4 (leipzigise-amen (make ::amen-seq)))
     (tempo (bpm 172))
     (l/play))

(->> (with (times 4 (with bass1 (breaks)))
           (ragga-samples))
     (tempo (bpm 172))
     (l/play))

(defmacro leipzigise-song [song]
  (let [arrangement (::arrangement song)
        scale (::scale song)]
    `(->> (with (times 4 ())))))

(leipzigise-song (make ::song))

(make ::song)

(let [song (gen/generate (s/gen ::song))]
  (->> (::arrangement song)
       first))




(let [song (gen/generate (s/gen ::song))
      arrangement (::arrangement song)
      scale (::scale song)
      intro (first arrangement)
      verse (nth arrangement 1)
      breakdown (nth arrangement 2)
      reprise (nth arrangement 3)
      outro (nth arrangement 4)]
  (->> (leipzigise-section (::amen-seq intro)
                           (::bass-seq intro)
                           (::ragga-seq intro)
                           scale)
       (then (leipzigise-section (::amen-seq verse)
                                 (::bass-seq verse)
                                 (::ragga-seq verse)
                                 scale))
       (then (leipzigise-section (::amen-seq breakdown)
                                 (::bass-seq breakdown)
                                 (::ragga-seq breakdown)
                                 scale))
       (then (leipzigise-section (::amen-seq reprise)
                                 (::bass-seq reprise)
                                 (::ragga-seq reprise)
                                 scale))
       (then (leipzigise-section (::amen-seq outro)
                                 (::bass-seq outro)
                                 (::ragga-seq outro)
                                 scale))
       (tempo (bpm 172))
       (l/play))
  song)

