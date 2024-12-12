(ns day11
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn read-stone-list [filename]
  (with-open [file (io/reader filename)]
    (str/split (first (line-seq file)) #" ")))

(defn expand [string]
  (defn helper [x] (str (Long/parseLong x)))
  (let [number (Long/parseLong string)]
    (cond
      (= number 0) ["1"]
      (= (mod (count string) 2) 0) (let [m (/ (count string) 2)]
                                     [(helper (subs string 0 m))
                                      (helper (subs string m))])
      :else [(str (* number 2024))])))

(defn solve-part-1 [stones n]
  (if (= 0 n)
    stones
    (recur (apply concat (map expand stones)) (- n 1))))

(defn -main [filename]
  (let [stones (read-stone-list filename)]
    (println (count (solve-part-1 stones 25)))))
