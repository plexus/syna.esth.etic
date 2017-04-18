(ns syna.esth.proc
  (:require [syna.esth.strm :refer [Pipe]]))

(def node-child-process (js/require "child_process"))

(defrecord Process [in out err proc]
  Pipe
  (-source [_] out)
  (-sink [_] in))

(defn cmd! [[cmd & args] & [opts]]
  (let [args (clj->js args)
        opts (clj->js opts)
        spawn-args (cond-> [cmd]
                     args (conj args)
                     opts (conj opts))
        process (apply (.-spawn node-child-process) spawn-args)]
    (when (.-error process)
      (js/process.exit 1))
    (map->Process
     {:in (.-stdin process)
      :out (.-stdout process)
      :err (.-stderr process)
      :process process})))

(defn cmd-sync! [cmd & [opts]]
  (let [opts (clj->js (merge {:encoding "utf8"} opts))]
    (.execSync node-child-process cmd opts)))
