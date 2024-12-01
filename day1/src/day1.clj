(ns day1
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn read-loc-id-lists [filename]
  (defn get-loc-id-pair [line]
    (map parse-long (str/split line #"   ")))
  (with-open [file (io/reader filename)]
    (->>
     file
     (line-seq)
     (map get-loc-id-pair)
     (apply mapv vector))))

(defn get-distance1 [loc-id-lists]
  (let [first-list (sort (first loc-id-lists))
        second-list (sort (second loc-id-lists))]
    (apply + (map (comp abs -) first-list second-list))))

(defn -main [filename]
  (let [loc-id-lists (read-loc-id-lists filename)]
    (println (get-distance1 loc-id-lists))))
