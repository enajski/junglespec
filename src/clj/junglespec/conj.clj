
(ns junglespec.conj
  (:use [overtone.live])
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test.check.generators :as gens]
            [mud.chords :as c]))


;The note

(defn make [spec]
  (gen/generate (s/gen spec)))

(require '[overtone.orchestra.cello :refer [cello]])

(s/def ::cello-note (s/cat :note (s/int-in 36 84)
                           :length (s/int-in 0 3)))

(apply cello (make ::cello-note))
(odoc cello)


(ns junglespec.conj
  (:require [clojure.spec :as s]))

(s/def ::note (s/cat :pitch (s/int-in 24 48)
                     :duration #{1/4 1/2 1/1}))


(ns junglespec.conj
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(s/def ::note (s/cat :pitch (s/int-in 24 48)
                     :duration #{1/4 1/2 1/1}))

(gen/generate (s/gen ::note))




(ns junglespec.conj
  (:use [overtone.live])
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(s/def ::note (s/cat :pitch (s/int-in 24 48)
                     :duration #{1/2 1/1 2}))

(gen/generate (s/gen ::note))

(require '[overtone.inst.synth :as synth])
(recording-start "/Users/dev/Music/bass1.wav")

(synth/tb303 37 1 0.1 0.1 0.2 0.8 0.3)

(recording-stop)


(s/def ::note (s/cat :pitch (s/int-in 0 24)
                     :duration #{2 4}))

(s/def ::bass-seq (s/coll-of ::note :kind vector?))

(defn play-bass [{:keys [pitch duration]}]
  (let [cutoff (make (s/int-in 150 290))]
    (synth/tb303 pitch 1 0.7 0.01 0.2 duration 0.2 cutoff 0.9)))

(defn play-bass-sequence [[first-sound & others]]
  (let [conformed-note (s/conform ::note first-sound)]
    (at 0 (play-bass conformed-note))
    (apply-at (+ (* 1000 (:duration conformed-note)) (now))
              play-bass-sequence [others])))
(recording-start "/Users/dev/Music/bass2.wav")
(play-bass-sequence (gen/generate (s/gen ::bass-seq)))
(recording-stop)

(require '[leipzig.scale :as scale]
         '[leipzig.melody :refer [phrase all tempo bpm
                                  where with rhythm times]]
         '[leipzig.live :as l])

(s/def ::scale #{[scale/C scale/minor scale/low scale/low]
                 [scale/F scale/minor scale/low scale/low]})

(defn unzip [coll]
  (vector (take-nth 2 (rest coll))
          (take-nth 2 coll)))

(defn leipzigise [section]
  (->> section
       unzip
       (apply phrase)))

(defmethod l/play-note :bass [note]
  (play-bass note))
(recording-start "/Users/dev/Music/bass4.wav")

(->> (->> ::bass-seq
          s/gen
          gen/generate
          flatten
          leipzigise
          (all :part :bass)
          (where :pitch (apply comp (gen/generate (s/gen ::scale)))))
     (tempo (bpm 172))
     (l/play))

(recording-stop)

(stop)

(->> (->> ::bass-seq
          s/gen
          gen/generate
          flatten
          leipzigise
          (all :part :bass)
          (where :pitch (apply comp (gen/generate (s/gen ::scale)))))
     (tempo (bpm 172))
     (l/play))


(s/def ::amen-sounds #{:pu :ci :ta :cii :cita :pupu})

(s/def ::amen-sound
  (s/or :kick (s/coll-of #{:pu} :min-count 1 :max-count 2)
        :ride (s/coll-of #{:ci} :count 1)
        :ride-semisnare (s/coll-of #{:cita} :min-count 1 :max-count 4)
        :snare (s/coll-of #{:ta} :count 1)
        :crash (s/coll-of #{:cii} :count 1)
        :doublekick (s/coll-of #{:pupu} :count 1)))

(s/def ::amen-seq (s/coll-of ::amen-sound :min-count 32))

(defn rhythmise
  [section]
  (let [timing (rhythm (repeat (count section) 1/2))]
    (->> (interleave timing (map #(hash-map :note % ) section))
         (partition 2)
         (map #(apply merge %)))))

(defonce breaks (load-samples "/Users/dev/Music/real_jungle_loops_by_noise_relations/*.wav"))

(defonce breaks
  (load-samples "breaks/*.wav"))

(defn slicer [buf start end]
  (stereo-partial-player buf 1 start end))

(def amen1 (nth breaks 32))
(def amen2 (nth breaks 31))
(def amen3 (nth breaks 33))

(recording-start "/Users/dev/Music/amen1pu.wav")
(slicer amen1 0/8 1/8)
(recording-stop)


(slicer (nth breaks 31) 0/8 8/8)

(defmulti amen :note)

(defmethod amen :pu [_]
  (apply slicer [amen1 0 1/8]))

(defmethod amen :ci [_]
  (apply slicer [amen1 1/8 2/8]))

(defmethod amen :ta [_]
  (apply slicer [amen1 2/8 3/8]))

(defmethod amen :cita [_]
  (apply slicer [amen1 3/8 4/8]))

(defmethod amen :pupu [_]
  (apply slicer [amen2 5/8 6/8]))

(defmethod amen :cii [_]
  (slicer (load-sample (freesound-path 26884)) 0 1))

(defmethod l/play-note :amen
  [{sound :note}]
  (amen {:note sound}))



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

(def bass1 (->> ::bass-seq
                s/gen
                gen/generate
                flatten
                (take 8)
                leipzigise
                (all :part :bass)
                (where :pitch (apply comp (gen/generate (s/gen ::scale))))))

(def breaks1 (->> ::amen-seq
                  s/gen
                  gen/generate
                  flatten
                  (take 32)
                  rhythmise
                  (all :part :amen)))

(defn rhythmise2
  [section]
  (let [timing (rhythm (repeat (count section) 4))]
    (->> (interleave timing (map #(hash-map :note % ) section))
         (partition 2)
         (map #(apply merge %)))))

(s/def ::breakbeat
  (s/cat :note (set breaks)
         :duration #{16}))

(require '[clojure.test.check.generators :as gens])

(s/def ::breakbeat-sequence
  (s/with-gen (s/coll-of ::breakbeat)
    #(gens/let [first (s/gen ::breakbeat)
                second (s/gen ::breakbeat)]
       (gens/return [first first first first]))))

(defmethod l/play-note :backing-break [{sound :note duration :duration}]
  (slicer sound 0 8/8))

(def backbeat1 (->> ::breakbeat-sequence
                    s/gen
                    gen/generate
                    flatten
                    (take 4)
                    rhythmise2
                    (all :part :backing-break)))


(recording-start "/Users/dev/Music/drumnbass2.wav")
(->> (with bass1 breaks1 backbeat1)
     (tempo (bpm 172))
     (times 2)
     (l/play))
(recording-stop)












;(s/def ::tb303 (s/cat :note (s/int-in 20 60)
;                      :wave (s/int-in 0 2)
;                      :r (s/double-in :min 0.01 :max 0.19)
;                      :attack (s/double-in :min 0.001 :max 0.1)
;                      :decay (s/double-in :min 0.1 :max 0.9)
;                      :sustain (s/double-in :min 0.1 :max 0.3)
;                      :release (s/double-in :min 0.1 :max 0.2)
;                      :cutoff (s/int-in 100 500)
;                      :env-amount (s/double-in :min 0.1 :max 0.2)
;                      :amp (s/double-in :min 0.6 :max 0.8)))
;
;(make ::tb303)
;
;(require '[overtone.inst.synth :refer [tb303]])
;(odoc tb303)
;(apply tb303 (make ::tb303))
;(stop)























;
;
;(defn make [spec] (gen/generate (s/gen spec)))
;
;(defonce breaks (load-samples "/Users/dev/Music/real_jungle_loops_by_noise_relations/*.wav"))
;(defonce ragga_samples (load-samples "/Users/dev/Music/ragga_samples/*.wav"))
;
;(defsynth play-break [buf 1 start 0.0 end 1.0]
;          (let [buf-len (buf-frames buf)
;                dry (play-buf 2 buf 1 0 (* start buf-len) false)]
;            (out 0 [dry])))
;
;;(defn play-sequence [[first-sound & others]]
;;  (at 0 (play-break first-sound))
;;  (apply-at (+ (* 1000 (:duration first-sound)) (now)) play-sequence [others]))
;
;(s/def ::drum-loop (set breaks))
;(s/def ::break (s/cat :buf (s/and buffer? ::drum-loop)
;                      :rate number?
;                      :start number?
;                      :len pos?))
;
;(defn play-sequence [[first-sound & others]]
;  (let [sound (s/conform ::break first-sound)
;        interval (* (:len sound) 1000 (:duration (:buf sound)))
;        _ (println interval)]
;    (at 0 (apply stereo-partial-player first-sound))
;    (when others (apply-at (+ (now) interval) play-sequence [others]))))
;
;
;
;(do (play-break (rand-nth breaks))
;    (play-break (rand-nth ragga_samples)))
;
;
;;(do (play-break (rand-nth ragga_samples))
;;    (play-sequence (flatten (vector (repeat 4 (rand-nth breaks))))))
;
;(s/def ::breakbeat (set breaks))
;(s/def ::breakbeat-sequence (s/coll-of ::breakbeat :count 4))
;
;(s/def ::breakbeat-sequence
;  (s/with-gen (s/coll-of ::breakbeat)
;              #(gens/let [first (s/gen ::breakbeat)
;                          second (s/gen ::breakbeat)]
;                         (gens/return [first first first second]))))
;;
;;(do (play-break (rand-nth ragga_samples))
;;    (play-sequence (flatten (repeat 2 (gen/generate (s/gen ::breakbeat-sequence)))))
;;    (play-sequence (flatten (repeat 2 (gen/generate (s/gen ::breakbeat-sequence))))))
;
;(let [sample1 (make ::breakbeat)
;      sample2 (make ::breakbeat)
;      break [[sample1 1 0 3/8]
;             [sample1 1 0 3/8]
;             [sample1 1 0 3/8]
;             [sample1 1 0 3/8]
;             [sample1 1 0/8 4/8]]
;      var1 (into [] (map #(assoc % 0 sample2) break))]
;  (play-sequence (apply concat (repeat 2 break))))
;;(do (play-sequence (apply concat (repeat 4 break)))
;;    (play-sequence (apply concat (repeat 4 var1)))
;;    (play-break (rand-nth ragga_samples))))
;
;(stereo-partial-player (first breaks) 1 2/8 6/8)
;
;(midi-connected-devices)
;
;(on-event [:midi :control-change]
;          (fn [{note :note velocity :velocity}]
;            (println note " " velocity))
;          ::event-printer)
;
;
;(definst grainy [b 0] (let [
;                            ;trate (mouse-y:kr 1 30)
;                            trate (rand-int 30)
;                            dur (/ 2 trate)]
;                        (t-grains:ar 0.1 (impulse:ar trate) b 1 (mouse-x:kr 0 (buf-dur:kr b)) dur 0 (line 0.8 0.0 2.0 0))))
;
;(grainy (buffer-mix-to-mono (rand-nth ragga_samples)))
;(rand-nth ragga_samples)
;
;(odoc t-grains)
;(odoc pluck)
