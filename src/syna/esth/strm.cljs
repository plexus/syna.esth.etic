(ns syna.esth.strm)

(def node-stream (js/require "stream"))
(def node-fs (js/require "fs"))
(def node-buffer (js/require "buffer"))

(def Readable (.-Readable node-stream))
(def Writable (.-Writable node-stream))
(def Duplex (.-Duplex node-stream))
(def PassThrough (.-PassThrough node-stream))
(def Transform (.-Transform node-stream))

(def Buffer (.-Buffer node-buffer))

(defprotocol Pipe
  (-source [this])
  (-sink [this]))

(defn source [s]
  (if s
    (-source s)
    (throw (new js/Error (str "Can't use " (prn s) " as a pipe source.")))))

(defn sink [s]
  (if s
    (-sink s)
    (throw (new js/Error (str "Can't use " (prn s) " as a pipe sink.")))))

(defn pass-through-stream []
  (PassThrough.))

(extend-protocol Pipe
  Readable
  (-source [this] this)
  (-sink [this] (throw (new js/Error "Can't pipe to Readable")))

  Writable
  (-source [this] (throw (new js/Error "Can't pipe from Writable")))
  (-sink [this] this)

  Duplex
  (-source [this] this)
  (-sink [this] this)

  Buffer
  (-source [this]
    (doto (pass-through-stream)
      (.end this)))
  (-sink [this]
    (throw (new js/Error "Can't pipe to a Buffer"))))

(defn readable-stream []
  (Readable.))

(defn <file [file]
  (if (= file "-")
    js/process.stdin
    (.createReadStream node-fs file)))

(defn >file [file]
  (if (= file "-")
    js/process.stdout
    (.createWriteStream node-fs file)))

(defn buffer-size [buffer]
  (.-length buffer))

(defn split-stream [in-stream size]
  (let [in-stream (source in-stream)
        out1 (pass-through-stream)
        out2 (pass-through-stream)
        pos (atom 0)]
    (.on in-stream "data" (fn [in]
                            (let [remaining (max (- size @pos) 0)
                                  s1 (.slice in 0 remaining)
                                  s2 (.slice in remaining)]
                              (when (> (buffer-size s1) 0)
                                (.write out1 s1))
                              (when (> (buffer-size s2) 0)
                                (.end out1)
                                (.write out2 s2))
                              (swap! pos + (buffer-size in)))))
    (.on in-stream "end" (fn [in]
                           (.end out2)))

    [out1 out2]))

(defn stream-concat [s1 s2]
  (let [s1 (source s1)
        s2 (source s2)
        out (pass-through-stream)
        in->out (fn [in] (when in (.write out in)))]
    (.on s1 "data" in->out)
    (.on s1 "end" (fn [in] (.on s2 "data" in->out)))
    (.on s2 "end" (fn [] (.end out)))
    out))

(defn append [s]
  (let [s (source s)]
    (Transform.
     #js {:transform (fn [in encoding cb]
                       (this-as $ (.push $ in))
                       (cb))
          :flush (fn [cb]
                   (.on s "data" (fn [in]
                                   (this-as $
                                     (.push $ in))))
                   (.on s "end" cb))})))

(defn truncate [size]
  (let [pos (atom 0)
        remaining #(max (- size @pos) 0)]
    (Transform.
     #js {:transform (fn [in encoding cb]
                       (when (> (remaining) 0)
                         (this-as $
                           (.push $ (.slice in 0 (remaining)))))
                       (swap! pos + (buffer-size in))
                       (cb))
          :flush (fn [cb]
                   (when (> (remaining) 0)
                     (this-as $
                       (.push $ (apply str (repeat (remaining) "\0")))))
                   (cb))})))


(defn sponge [cb]
  (let [in (pass-through-stream)
        data (atom (new Buffer #js []))]
    (.on in "data" (fn [in]
                     (swap! data #(.concat Buffer #js [% in]))))
    (.on in "end" (fn [] (cb @data)))
    in))

(defn dbg [name stream]
  (run! (fn [evt]
          (.on stream evt
               #(println (str name ":" evt))))
        ["close"
         "data"
         "drain"
         "end"
         "error"
         "error"
         "finish"
         "pipe"
         "readable"
         "unpipe"])
  stream)

(defn | [p1 p2 & ps]
  (.pipe (source p1) (sink p2))
  (if (seq ps)
    (apply | p2 ps)
    p2))
