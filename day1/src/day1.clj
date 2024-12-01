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

(defn get-distance1 [first-list second-list]
  (apply + (map (comp abs -) first-list second-list)))

(defn get-distance2 [first-list second-list]
  (let [loc-to-freq (frequencies second-list)]
    (defn calculate-distance [loc]
      (* loc (get loc-to-freq loc 0)))
    (apply + (map calculate-distance first-list))))

(defn -main [filename]
  (let [loc-id-lists (read-loc-id-lists filename)
        first-list (sort (first loc-id-lists))
        second-list (sort (second loc-id-lists))]
    (println (get-distance1 first-list second-list))
    (println (get-distance2 first-list second-list))))
