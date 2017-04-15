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
