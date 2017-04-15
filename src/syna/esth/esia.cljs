#!/usr/bin/env lumo
;; -*- clojurescript -*-
(ns syna.esth.esia
  (:require [lumo.core :refer [*command-line-args*]]
            [lumo.util :refer [debug-prn]]
            [clojure.string :as str]
            [syna.esth.proc :refer [cmd!]]
            [syna.esth.strm :refer [|
                                    pass-through-stream
                                    file-read-stream
                                    file-write-stream
                                    split-stream
                                    concat-streams]]))


(def sox-args (str/split "sox -r 44100 -b 32 -c 5 -e unsigned-integer -t raw - -t raw - delay 0 0s 0 7s 0" " "))

(defn -main [infile]
  (let [instream    (file-read-stream infile)
        to-bmp      (cmd! ["convert" "-" "bmp:-"])
        to-png      (cmd! ["convert" "bmp:-" "png:-"])
        sox         (cmd! sox-args)
        [head body] (split-stream to-bmp 1000)

        out-file    (file-write-stream "output.png")]

    (| (:err to-bmp) (file-write-stream "convert-error.log"))
    (| (:err sox) (file-write-stream "sox-error.log"))

    (| instream to-bmp)
    (| body sox)

    (| (concat-streams head sox) to-png out-file)))
