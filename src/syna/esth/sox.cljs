(ns syna.esth.sox
  (:refer-clojure :exclude [delay])
  (:require [clojure.string :as str]
            [syna.esth.proc :refer [cmd!]]))

(def defaults {:rate 44100
               :depth 16
               :channels 2
               :encoding "unsigned-integer"})

(defn sox [opts & args]
  (let [{:keys [rate depth channels encoding]} (merge defaults opts)
        cmd (apply concat
                   ["sox"
                    "-r" rate
                    "-b" depth
                    "-c" channels
                    "-e" encoding
                    "-t" "raw" "-"
                    "-t" "raw" "-"]
                   args)]
    (println (str/join " " cmd))
    (cmd! cmd)))

(defn dcshift [shift]
  ["dcshift" shift])

(defn delay [& delays]
  (cons "delay" (map #(str % "s") delays)))

(defn echos [gain-in gain-out & delay-decay-pairs]
  (into ["echos" gain-in gain-out] delay-decay-pairs))

(defn bass [amount]
  ["bass" amount])

(defn treble [amount]
  ["treble" amount])

(defn bandreject [freq width]
  ["bandreject" freq width])

(defn fir [args]
  (cons "fir" args))

(defn reverb "reverb [-w|--wet-only] [reverberance (50%) [HF-damping (50%)
       [room-scale (100%) [stereo-depth (100%)
       [pre-delay (0ms) [wet-gain (0dB)]]]]]]

  Add  reverberation  to  the  audio using the `freeverb' algorithm.  A reverberation effect is sometimes desirable for concert halls that are too small or contain so many people that the hall's natural reverberance is diminished.  Applying a small amount of stereo reverb to a (dry) mono signal will usually make it sound more natural."
  [& [{:keys [wet-only reverberance hf-damping room-scale
              stereo-depth pre-delay wet-gain]
       :or {wet-only false
            reverberance 50
            hf-damping 50
            room-scale 100
            stereo-depth 100
            pre-delay 0
            wet-gain 0}}]]
  (into
   (cond-> ["reverb"] wet-only (conj "--wet-only"))
   [reverberance hf-damping room-scale
    stereo-depth pre-delay wet-gain]))
