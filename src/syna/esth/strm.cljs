(ns syna.esth.strm)

(def node-stream (js/require "stream"))
(def node-fs (js/require "fs"))

(def Readable (.-Readable node-stream))
(def Writable (.-Writable node-stream))
(def Duplex (.-Duplex node-stream))
(def PassThrough (.-PassThrough node-stream))

(defprotocol Pipe
  (-source [this])
  (-sink [this]))

(defn source [s]
  (if s
    (-source s)
    (throw (new js/Error (str "Can't use " s " as a pipe source.")))))

(defn sink [s]
  (if s
    (-sink s)
    (throw (new js/Error (str "Can't use " s " as a pipe sink.")))))

(extend-protocol Pipe
  Readable
  (-source [this] this)
  (-sink [this] (throw (new js/Error "Can't pipe to Readable")))

  Writable
  (-source [this] (throw (new js/Error "Can't pipe from Writable")))
  (-sink [this] this)

  Duplex
  (-source [this] this)
  (-sink [this] this))

(defn pass-through-stream []
  (PassThrough.))

(defn readable-stream []
  (Readable.))

(defn file-read-stream [file]
  (.createReadStream node-fs file))

(defn file-write-stream [file]
  (.createWriteStream node-fs file))

(defn split-stream [in-stream size]
  (let [in-stream (source in-stream)
        out1 (pass-through-stream)
        out2 (pass-through-stream)
        pos (atom 0)]
    (.on in-stream "data" (fn [in]
                            (let [remaining (max (- size @pos) 0)
                                  s1 (.slice in 0 remaining)
                                  s2 (.slice in remaining)]
                              (when (> (.-length s1) 0)
                                (.write out1 s1))
                              (when (> (.-length s2) 0)
                                (.end out1)
                                (.write out2 s2))
                              (swap! pos + (.-length in)))))
    (.on in-stream "end" (fn [in]
                           (.end out2)))

    [out1 out2]))

(defn concat-streams [s1 s2]
  (let [s1 (source s1)
        s2 (source s2)
        out (pass-through-stream)
        in->out (fn [in] (when in (.write out in)))]
    (.on s1 "data" in->out)
    (.on s1 "end" (fn [in] (.on s2 "data" in->out)))
    (.on s2 "end" (fn [] (.end out)))
    out))



(defn | [p1 p2 & ps]
  (.pipe (source p1) (sink p2))
  (if (seq ps)
    (apply | p2 ps)
    p2))
