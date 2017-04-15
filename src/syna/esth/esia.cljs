#!/usr/bin/env lumo
;; -*- clojurescript -*-
(ns syna.esth.esia
  (:require [lumo.core :refer [*command-line-args*]]
            [lumo.util :refer [debug-prn]]
            [clojure.string :as str]))

(def ChildProcess (js/require "child_process"))
(def Stream (js/require "stream"))
(def FS (js/require "fs"))

(def PassThroughStream (.-PassThrough Stream))
(def ReadableStream (.-PassThrough Stream))

(defn file-out-stream [file]
  (.createWriteStream FS file))

(defn str->stream [s]
  (let [r (ReadableStream.)]
    (.push r s)
    (.push r nil)
    r))

(defn buffer->stream [b]
  (let [buf-stream (PassThroughStream.)]
    (.end buf-stream b)
    buf-stream))

(defn out-str [proc]
  (buffer->stream (.-stdout proc)))

(defn >> [p1 p2 & ps]
  (let [p (.pipe p1 p2)]
    (if (seq ps)
      (apply >> p ps)
      p)))

(defn cmd! [[cmd & args] & [opts]]
  (let [args (clj->js args)
        opts (clj->js opts)
        spawn-args (cond-> [cmd]
                     args (conj args)
                     opts (conj opts))
        process (apply (.-spawn ChildProcess) spawn-args)]
    (when (.-error process)
      (println (.-error process))
      (js/process.exit 1))
    {:in (.-stdin process)
     :out (.-stdout process)
     :err (.-stderr process)}))

(defn split-stream [in-stream size]
  (let [out1 (PassThroughStream.)
        out2 (PassThroughStream.)
        pos (atom 0)]
    (.on in-stream "data" (fn [in]
                            (let [remaining (max (- size @pos) 0)
                                  s1 (.slice in 0 remaining)
                                  s2 (.slice in remaining)]
                              (when (> (.-length s1) 0)
                                (println [:head remaining (.-length s1) @pos])
                                (.write out1 s1))
                              (when (> (.-length s2) 0)
                                (println [:body remaining (.-length s2) @pos])
                                (.end out1)
                                (.write out2 s2))
                              (swap! pos + (.-length in)))))
    (.on in-stream "end" (fn [in]
                           (println "ending!")
                           (.end out2)))

    [out1 out2]))

(defn concat-streams [s1 s2]
  (let [out (PassThroughStream.)
        pos (atom 0)
        state (atom "s1")
        in->out (fn [in]
                  (when in
                    (prn [:concat @state (.-length in) @pos])
                    (swap! pos + (.-length in))
                    (.write out in)))]
    (.on s1 "data" in->out)
    (.on s1 "end" (fn [in]
                    (reset! state "s2")
                    (.on s2 "data" in->out)))
    (.on s2 "end" (fn [] (.end out)))
    out))

(def sox-args (str/split "sox -r 44100 -b 32 -c 5 -e unsigned-integer -t raw - -t raw - delay 0 0s 0 7s 0" " "))

(let [infile (first *command-line-args*)
      convert (cmd! ["convert" infile "bmp:-"])
      sox (cmd! sox-args)
      [head body] (split-stream (:out convert) 1000)
      out-file (file-out-stream "output.bmp")]
  (>> (:err convert) (file-out-stream "convert-error.log"))
  (>> (:err sox) (file-out-stream "sox-error.log"))
  (>> body (:in sox))
  (>> (concat-streams head (:out sox)) out-file))
