(ns junglespec.conj
  (:use [overtone.live])
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test.check.generators :as gens]
            [overtone.inst.synth :as synth]
            [leipzig.scale :as scale]
            [leipzig.melody :refer [phrase all tempo bpm
                                    where with rhythm times then]]
            [leipzig.live :as l]
            [junglespec.spec :refer :all]))

(defn make
  "Generate example given a spec name."
  [spec]
  (gen/generate (s/gen spec)))

(def ragga-sounds
  "Load your vocal samples for flavour."
  (load-samples "/Users/dev/Music/ragga_samples/*.wav"))

(:path (first ragga-sounds))

(def breaks
  "Load your breakbeats."
  (load-samples "/Users/dev/Music/real_jungle_loops_by_noise_relations/*.wav"))

(defn unzip
  "Splits a collection in two."
  [coll]
  (vector (take-nth 2 (rest coll))
          (take-nth 2 coll)))

(defn leipzigise
  "Creates a leipzig phrase from a melodic section."
  [section]
  (->> section
       unzip
       (apply phrase)))

(defn rhythmise
  "Creates an rhythm with absolute timing and a constant duration."
  [section]
  (let [duration 1/2
        timing (rhythm (repeat (count section) duration))]
    (->> (interleave timing (map #(hash-map :note % ) section))
         (partition 2)
         (map #(apply merge %)))))

(defn slicer
  "Play a part of an audio buffer giver start and end times."
  [buf start end]
  (stereo-partial-player buf 1 start end))

;; Three variations on the amen break that play nicely with each other.
(def amen1 (nth breaks 32))
(def amen2 (nth breaks 31))
(def amen3 (nth breaks 33))

;; Amen dispatch
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

(defmethod l/play-note :sample [{sound :note duration :duration}]
  (if (buffer? sound)
    (slicer sound 0 1)
    (slicer (first (filter #(= sound (:path %)) ragga-sounds)) 0 1)))

(defn play-bass [{:keys [pitch duration]}]
  (let [cutoff (make (s/int-in 150 290))]
    (synth/tb303 pitch 1 0.7 0.01 0.2 duration 0.2 cutoff 0.9)))

(defmethod l/play-note :bass [note]
  (play-bass note))

(defn rhythmise2
  [section]
  (let [timing (rhythm (map last section))]
    (->> (interleave timing (map #(hash-map :note (first %)) section))
         (partition 2)
         (map #(apply merge %)))))

(s/def ::breakbeat
  (s/cat :note (set breaks)
         :duration #{16}))

;; Example of custom generator usage
(s/def ::breakbeat-sequence
  (s/with-gen (s/coll-of ::breakbeat)
    #(gens/let [first (s/gen ::breakbeat)
                second (s/gen ::breakbeat)]
       (gens/return [first first first second]))))

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
        rhythmise2
        (all :part :sample)))

(defmacro leipzigise-bass
  [bass-seq scale]
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

;; This plays the whole song!
(let [song (gen/generate (s/gen :junglespec.spec/song))
      arrangement (:junglespec.spec/arrangement song)
      scale (:junglespec.spec/scale song)
      intro (nth arrangement 0)
      verse (nth arrangement 1)
      breakdown (nth arrangement 2)
      reprise (nth arrangement 3)
      outro (nth arrangement 4)]
  (->> (leipzigise-section (:junglespec.spec/amen-seq intro)
                           (:junglespec.spec/bass-seq intro)
                           (:junglespec.spec/ragga-seq intro)
                           scale)
       (then (leipzigise-section (:junglespec.spec/amen-seq verse)
                                 (:junglespec.spec/bass-seq verse)
                                 (:junglespec.spec/ragga-seq verse)
                                 scale))
       (then (leipzigise-section (:junglespec.spec/amen-seq breakdown)
                                 (:junglespec.spec/bass-seq breakdown)
                                 (:junglespec.spec/ragga-seq breakdown)
                                 scale))
       (then (leipzigise-section (:junglespec.spec/amen-seq reprise)
                                 (:junglespec.spec/bass-seq reprise)
                                 (:junglespec.spec/ragga-seq reprise)
                                 scale))
       (then (leipzigise-section (:junglespec.spec/amen-seq outro)
                                 (:junglespec.spec/bass-seq outro)
                                 (:junglespec.spec/ragga-seq outro)
                                 scale))
       (tempo (bpm 172))
       (l/play))
  song)

