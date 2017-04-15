(ns syna.esth.sox
  (:refer-clojure :exclude [delay])
  (:require [clojure.string :as str]))

(def defaults {:rate 44100
               :depth 16
               :channels 2
               :encoding "unsigned-integer"})

(defn sox [opts]
  (let [{:keys [rate depth channels encoding]} (merge defaults opts)]
    ["sox"
     "-r" rate
     "-b" depth
     "-c" channels
     "-e" encoding
     "-t" "raw" "-"
     "-t" "raw" "-"]))

(defn dcshift [sox shift]
  (into sox ["dcshift" shift]))

(defn delay [sox & delays]
  (-> sox
      (conj "delay")
      (into (map #(str % "s") delays))))


(defn echos [sox gain-in gain-out & delay-decay-pairs]
  (-> sox
      (conj "echos" gain-in gain-out)
      (into delay-decay-pairs)))

(defn bass [sox amount]
  (conj sox "bass" amount))

(defn treble [sox amount]
  (conj sox "bass" amount))

(defn bandreject [sox freq width]
  (conj sox "bandreject" freq width))

(defn fir [sox & args]
  (-> sox
      (conj "fir")
      (into args)))
