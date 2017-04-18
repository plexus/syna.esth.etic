(defproject synaesthetic "0.0.0"
  :dependencies [[org.clojure/tools.cli "0.3.5" :exclusions [[org.clojure/clojure]]]]
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]

                        :compiler {:main syna.esth.etic
                                   :target :nodejs}}]})
