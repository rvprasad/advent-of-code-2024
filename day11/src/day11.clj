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

(defn make-stone-level [stone n] (str stone '-' n))

(defn count-stones [stone n stone-level2count]
  (let [stone-level (make-stone-level stone  n)]
    (if (some? (stone-level2count stone-level))
      stone-level2count
      (if (= n 0)
        (assoc stone-level2count stone-level 1)
        (let [next-stones (expand stone)
              next-level (- n 1)
              next-stone-levels (map #(make-stone-level % next-level) next-stones)
              new-stone-level2count (reduce #(count-stones %2 next-level %1)
                                            stone-level2count
                                            next-stones)
              counts (map new-stone-level2count next-stone-levels)]
          (assoc new-stone-level2count stone-level (reduce + counts)))))))

(defn solve-part-2 [stones n]
  (let [stone-level2count (reduce #(count-stones %2 n %1) {} stones)
        stone-levels (map #(make-stone-level % n) stones)]
    (reduce + (map #(stone-level2count %) stone-levels))))

(defn -main [filename]
  (let [stones (read-stone-list filename)]
    (println (count (solve-part-1 stones 25)))
    (println (solve-part-2 stones 75))))
