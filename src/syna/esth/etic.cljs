(ns syna.esth.etic
  (:refer-clojure :exclude [delay *out*])
  (:require [clojure.string :as str]
            [clojure.walk :refer [postwalk]]
            [cljs.tools.cli :refer [parse-opts]]
            [lumo.repl :as repl]
            [syna.esth.proc :refer [cmd! cmd-sync!]]
            [syna.esth.strm
             :refer [|
                     dbg
                     pass-through-stream
                     <file
                     >file
                     split-stream
                     stream-concat
                     truncate
                     sponge
                     buffer-size]]
            [syna.esth.sox :refer [sox dcshift delay echos bass treble bandreject fir]]
            [cljs.js :as cljs]
            [cljs.reader :refer [read-string]]))

(def sox-dsl '[sox dcshift delay echos bass treble bandreject fir])

(def *err* js/process.stderr)
(def *in* js/process.stdin)
(def *out* js/process.stdout)

(def node-fs (js/require "fs"))

(defn slurp [f]
  (.readFileSync node-fs f "utf-8"))

(defn eval [form & [callback]]
  (vreset! repl/current-ns 'syna.esth.etic)
  (binding [cljs/*load-fn* repl/load
            cljs/*eval-fn* repl/caching-node-eval]
    (cljs/eval
     js/lumo.repl.st
     (postwalk (fn [s] (if (some #{s} sox-dsl)
                         (symbol (str "syna.esth.sox/" s))
                         s)) form)
     (fn [{:keys [ns value error] :as ret}]
       (if error
         (js/lumo.repl.handle-error error)
         (if callback
           (callback value)))))))

(defn println-err [& args]
  (binding [*print-fn* *print-err-fn*]
    (apply println args)))

;; (def sox-args
;;   (-> (sox {:channels 2 :depth 8})
;;       (fir 1 0.8)
;;       (treble 4)
;;       (echos 0.7 0.8 250 0.4)
;;       #_(dcshift 0.2)))



(def cli-options [["-h" "--help" "Show this help screen"]
                  ["-s" "--script SCRIPT_FILE" "Use a sox script instead of passing options on the command line"]
                  ["-w" "--watch" "Watch the script for changes"]])

(def help
  (str/join \newline (flatten ["Syna.esth.etic. Command Line Glitcher Extraordinaire"
                               "Usage: synaesthetic [options] in-file out-file *sox-args"
                               ""
                               "Options:"
                               (map #(str "\t" (str/join "\t" (take 3 %))) cli-options)
                               ""
                               "in-file and out-file can be - to read from stdin or write to stdout"])))

(defn run-pipeline [infile outfile sox]
  (let [in            (<file infile)
        img->bmp      (cmd! ["convert" "-" "bmp:-"])
        bmp->png      (cmd! ["convert" "bmp:-" "png:-"])
        out           (>file outfile)]

    (| (:err img->bmp) *err*)
    (| (:err bmp->png) *err*)

    (| in

       ;; Turn the image into an uncompressed bitmap, can't do much with it otherwise
       img->bmp

       ;; Soak up the whole input, because we need to know the size of the BMP image
       (sponge (fn [bmp]
                 (let [bmp-size (buffer-size bmp)

                       ;; chop off the header, the header needs to stay intact,
                       ;; with the rest you can mess as much as you like
                       [head body] (split-stream bmp 1000)]

                   ;; pipe the rest of the image to sox, this is where the *magic* happens
                   (| body sox)

                   ;; now add the head and processed body together again
                   (| (stream-concat head sox)
                      ;; The size might have changed so "truncate" (this can
                      ;; either cut off bits, or pad the result with nulls)
                      (truncate bmp-size)

                      ;; finally convert back to something sensible
                      bmp->png

                      ;; and write it out
                      out)))))))

(def GLITCHING ["—" "—" "—" "Ḡ̶̲̻" "L̴̓̊ͅ" "Ȉ̸̩͘" "T̸̯̟͊̿" "C̷͚̳̱̔" "H̷̡̬̣̎" "I̸̕ͅ" "N̴̯̠̯̚" "G̴̢̡͜͝" "—" "—" "—"])

(defn BANNER! []
  (println (str/join "" GLITCHING))
  (println)
  (let [pos (atom 0)
        timeout (js/setInterval (fn [& args]
                                  (.write js/process.stdout (get GLITCHING @pos))
                                  (swap! pos #(mod (inc %) (count GLITCHING))))
                                666)]
    #(js/clearInterval timeout)))

(defn run-script [script callback]
  (try
    (eval (read-string (slurp script)) callback)
    (catch js/Error e
      (println-err "You have an error in your script:")
      (prn e))))

(defn check-sox! []
  (try
    (print (cmd-sync! "sox --version"))
    (catch js/Error e
      (println-err "FATAL: Failed to run sox. Make sure you have sox installed. Try running sox --version."))))

(defn check-convert! []
  (try
    (print (cmd-sync! "convert --version"))
    (catch js/Error e
      (println-err "FATAL: Failed to run convert. Make sure you have ImageMagick installed. Try running convert --version."))))

(defn -main [& args]
  (let [{:keys [options arguments]} (parse-opts args cli-options :in-order true)]
    (if (or (:help options) (empty? arguments))
      (println-err help)
      (do
        (let [stop-banner (atom (BANNER!))
              infile (first arguments)
              outfile (second arguments)
              script (:script options)
              run-sox (fn [sox-proc]
                        (.on (:out sox-proc) "end" @stop-banner)
                        (| (:err sox-proc) *err*)
                        (run-pipeline infile outfile sox-proc))]
          (check-sox!)
          (check-convert!)
          (if script
            (do
              (run-script script run-sox)
              (when (:watch options)
                (println "Watching" script "for changes.")
                (.watch node-fs script (fn [_ _]
                                         (reset! stop-banner (BANNER!))
                                         (run-script script run-sox)))))
            (run-sox (sox {:channels 2 :depth 8} (drop 2 arguments)))))))))
