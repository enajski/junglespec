(ns junglespec.player
  (:require [junglespec.jungle :refer :all]
            [overtone.live :as overtone :refer [freesound-path sample sample-player now at apply-at]]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [leipzig.melody :refer [phrase tempo bpm where then with times all rhythm]]
            [leipzig.live :as live]))

(defn unzip [coll]
  (vector (take-nth 2 (rest coll))
          (take-nth 2 coll)))

(defn leipzigise [section]
  (->> section
       unzip
       (apply phrase)))

(defn rhythmise [section]
  (let [timing (rhythm (map :duration section))]
    (->> (interleave timing section)
         (partition 2)
         (map #(apply merge %)))))

(defmulti amen :sound)

(defmethod amen :pu [params]
  ((sample (freesound-path 26885))))
(defmethod amen :ci [params]
  ((sample (freesound-path 26879))))
(defmethod amen :ta [params]
  ((sample (freesound-path 26900))))
(defmethod amen :ty [params]
  ((sample (freesound-path 26896))))

(defmethod live/play-note :beat [{sound :sound duration :duration}]
  (amen {:sound sound}))

(overtone/definst beep [freq 440 dur 1.0]
                  (-> freq
                      overtone/pulse
                      (* (overtone/env-gen (overtone/perc 0.01 dur) :action overtone/FREE))))

(defmethod live/play-note :default [{midi :pitch seconds :duration}]
  (-> midi overtone/midi->hz (beep seconds)))

(defmethod live/play-note :bass [{midi :pitch seconds :duration}]
  (-> midi overtone/midi->hz (/ 2) (beep seconds)))

(def current-song (atom {}))

(defn fixed-time-coll [sequence-length coll]
    (->> coll
         (split-with (partial #(< (:time %) sequence-length)))
         first))

(comment (let [song (gen/generate (s/gen :junglespec.jungle/jungle))
               arr (:junglespec.jungle/arrangement song)
               key (apply comp (:junglespec.jungle/song-key song))
               tempo (:junglespec.jungle/tempo song)
               phrase-duration (* 8 (:junglespec.jungle/meter song))
               intro (first arr)
               bass (leipzigise (:junglespec.jungle/bass intro))
               synth (leipzigise (:junglespec.jungle/synth intro))
               verse (second arr)
               verse-beats (rhythmise (s/conform :junglespec.jungle/break (:junglespec.jungle/break verse)))]
           (reset! current-song song)
           (->> (with
                  (->> verse-beats
                       (fixed-time-coll phrase-duration)
                       (all :part :beat))
                  (->> synth
                       (fixed-time-coll phrase-duration)
                       (all :part :default)
                       (where :pitch key))
                  (->> bass
                       (fixed-time-coll phrase-duration)
                       (all :part :bass)
                       (where :pitch key)))
                (times 2)
                (tempo (bpm tempo))
                (live/play))))






