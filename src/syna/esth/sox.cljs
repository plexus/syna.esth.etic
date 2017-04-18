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
