(ns junglespec.player
  (:require
    [junglespec.jungle :refer :all]
    [overtone.live :as overtone
     :refer [freesound-path sample sample-player
             now at apply-at play-buf out
             load-sample load-samples demo
             buf-rd lf-noise1 buf-frames
             defsynth free-verb]]
    ;[overtone.orchestra.cello :refer [cello]]
    ;[overtone.orchestra.double-bass :refer [double-bass]]
    [clojure.spec :as s]
    [clojure.spec.gen :as gen]
    [leipzig.melody
     :refer [phrase tempo bpm
             where then with
             times all rhythm]]
    [leipzig.live :as live]))

(defn make [spec]
  (gen/generate (s/gen spec)))

(defsynth play-break [buf 1 start 0.0 end 1.0]
          (let [dry (play-buf 2 buf 1 0 (* start (buf-frames buf)) false)]
            (out 0 [dry])))

(defn play-sequence [[first-sound & others]]
  (at 0 (play-break first-sound))
  (apply-at (+ (* 1000 (:duration first-sound)) (now)) play-sequence [others]))

(defn play-chord [a-chord]
  (doseq [note a-chord] (cello :length 3 :note note)))

(defn play-chord-progression [interval [first-sound & others]]
  (at 0 (play-chord first-sound))
  (apply-at (+ interval (now)) play-chord-progression [interval others]))


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

(phrase (repeat 1) (range 8))

(defmulti amen :sound)

(defmethod amen :pu [params]
  ((sample (freesound-path 26885))))
(defmethod amen :ci [params]
  ((sample (freesound-path 26879))))
(defmethod amen :ta [params]
  ((sample (freesound-path 26900))))
(defmethod amen :ty [params]
  ((sample (freesound-path 26896))))
  ;(let [buf (sample (freesound-path 26896))
  ;      _ (println ":ty")]
  ;  (play-buf (buf-rd 2 buf (* (lf-noise1 10) (buf-frames buf))))))

(defmethod live/play-note :beat [{sound :sound duration :duration}]
  (amen {:sound sound}))

(overtone/definst beep [freq 440 dur 1.0]
                  (-> freq
                      overtone/sin-osc
                      (* (overtone/env-gen (overtone/perc 0.01 dur) :action overtone/FREE))))

(defmethod live/play-note :default [{midi :pitch seconds :duration}]
  (-> midi overtone/midi->hz (/ 2) (beep seconds)))

(defmethod live/play-note :bass [{midi :pitch seconds :duration}]
  (-> midi overtone/midi->hz (/ 8) (beep seconds)))

;(defmethod live/play-note :cello [{midi :pitch seconds :duration}]
;  (cello :note midi :length seconds))


(def current-song (atom {}))

(defn bpm->ratio [bpm]
  (/ bpm 60))

(defn beat-length-at-bpm [bpm beats]
  (* beats (bpm->ratio bpm)))

(defn fixed-time-coll [bpm number-of-beats coll]
  (let [desired-length (beat-length-at-bpm bpm number-of-beats)
        notes-to-play (->> coll
                           (split-with (partial #(< (:time %) desired-length)))
                           first)]
    notes-to-play))

(s/fdef fixed-time-coll
        :args (s/cat :bpm pos?
                     :number-of-beats pos?
                     :coll (s/coll-of map? :kind list?))
        :ret (s/coll-of map? :kind list?))

(defn play-sample
  [path start length]
  (let [size (:size (load-sample path))
        buf (sample path
                    :start (int (* start size))
                    :size (int (* length size)))]
    (buf)))

(defonce breaks (load-samples "/Users/dev/Music/real_jungle_loops_by_noise_relations/*.wav"))

(defmethod live/play-note :break [{path :sound
                                   start :start
                                   length :duration}]
  (play-sample path start length))

(defn prepare-performance [song]
  (let [arr (:junglespec.jungle/arrangement song)
        key (apply comp (resolve (symbol (:junglespec.jungle/song-key song))))
        song-bpm (:junglespec.jungle/tempo song)
        phrase-duration (* 8 (:junglespec.jungle/meter song))
        intro (first arr)
        bass (leipzigise (:junglespec.jungle/bass intro))
        synth (leipzigise (:junglespec.jungle/synth intro))
        verse (second arr)
        verse-beats (rhythmise (s/conform :junglespec.jungle/break (:junglespec.jungle/break verse)))]
    (reset! current-song song)
    (->> (with
           (->> verse-beats
                (fixed-time-coll song-bpm phrase-duration)
                (all :part :beat))
           (->> synth
                (fixed-time-coll song-bpm phrase-duration)
                (all :part :default)
                (where :pitch key))
           (->> bass
                (fixed-time-coll song-bpm phrase-duration)
                (all :part :bass)
                (where :pitch key)))
         (times 2)
         (tempo (bpm song-bpm))
         (live/play))))

(def thing1 (let [song (gen/generate (s/gen :junglespec.jungle/jungle))
                  arr (:junglespec.jungle/arrangement song)
                  key (apply comp (:junglespec.jungle/song-key song))
                  song-bpm (:junglespec.jungle/tempo song)
                  phrase-duration (* 8 (:junglespec.jungle/meter song))
                  intro (first arr)
                  bass (leipzigise (:junglespec.jungle/bass intro))
                  synth (leipzigise (:junglespec.jungle/synth intro))
                  verse (second arr)
                  verse-beats (rhythmise (s/conform :junglespec.jungle/break (:junglespec.jungle/break verse)))]
              (reset! current-song song)
              (->> (with
                     (->> verse-beats
                          (fixed-time-coll song-bpm phrase-duration)
                          (all :part :beat))
                     (->> synth
                          (fixed-time-coll song-bpm phrase-duration)
                          (all :part :cello)
                          (where :pitch key))
                     (->> bass
                          (fixed-time-coll song-bpm phrase-duration)
                          (all :part :bass)
                          (where :pitch key)))
                   (times 1)
                   (tempo (bpm song-bpm)))))

;(def asd (->> (rhythmise
;                (s/conform :junglespec.jungle/break
;                           [
;                            :pu 1/2 :ci 1/2
;                            :ta 1/2 :ci 1/2
;                            :pu 1/2 :ta 1/2
;                            :pu 1/2 :ta 1/2
;                            :pu 1/4 :ty 1/4 :ci 1/4 :ty 1/4
;                            :ci 1/4 :ty 1/4 :ci 1/4 :ty 1/4
;                            :ci 1/4 :ty 1/4 :ci 1/4 :ty 1/4
;                            :ci 1/4 :ty 1/4 :ci 1/4 :ty 1/4]))
;              (all :part :beat)
;              (tempo (bpm 170))))

(comment (live/jam (var thing1)))
(comment (live/stop))
