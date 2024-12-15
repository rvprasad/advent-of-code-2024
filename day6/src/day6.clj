(ns day6
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]))

(defn read-area-map [filename]
  (defn process-line [y line]
    (map-indexed (fn [x e] [[x y] e]) (str/split line #"")))

  (with-open [file (io/reader filename)]
    (->>
     file
     (line-seq)
     (map-indexed process-line)
     (apply concat)
     (into {}))))

(defn get-next-pos [[[x y] dir] area-map]
  (let [next-loc (cond
                   (= dir :up) [x (- y 1)]
                   (= dir :down) [x (+ y 1)]
                   (= dir :left) [(- x 1) y]
                   (= dir :right) [(+ x 1) y])
        next-content (area-map next-loc nil)]
    (if (not= next-content "#") [next-loc dir]
        [[x y] (case dir
                 :up :right
                 :down :left
                 :left :up
                 :right :down)])))

(defn is-looping? [seen pos] (contains? seen pos))

(defn is-out-of-area? [area-map loc] (not (contains? area-map loc)))

(defn traverse-path [path area-map]
  (defn helper [path seen]
    (let [curr-pos (first path)
          next-pos (get-next-pos curr-pos area-map)]
      (cond
        (is-looping? seen next-pos) nil
        (is-out-of-area? area-map (first next-pos)) path
        :else (recur (cons next-pos path)
                     (conj seen next-pos)))))
  (helper path (set (rest path))))

(defn obs-not-in-path? [obs-loc path]
  (->>
   [:up :down :right :left]
   (map #(vector obs-loc %))
   (not-any? (set path))))

(defn is-path-loopy-with-obstacle-at-head? [path area-map]
  (let [obs-loc (first (first path))
        rest-path (rest path)]
    (if (and (not= (area-map obs-loc) "^")
             (obs-not-in-path? obs-loc rest-path))
      (let [final-path (traverse-path rest-path (assoc area-map obs-loc "#"))]
        (if (nil? final-path) obs-loc)))))

(defn solve-puzzle [area-map]
  (def max-cols (apply max (map #(second (first %)) area-map)))
  (def max-rows (apply max (map #(first (first %)) area-map)))

  (let [start-loc (first (first (filter #(= (second %) "^") area-map)))
        path (traverse-path (list [start-loc :up]) area-map)
        path-length (count (distinct (map first path)))
        loop-count (->>
                    path
                    (reverse)
                    (reductions #(cons %2 %1) ())
                    (filter #(> (count %) 1))
                    (map #(is-path-loopy-with-obstacle-at-head? % area-map))
                    (filter some?)
                    (distinct)
                    (count))]
    [path-length loop-count]))

(defn -main [filename]
  (let [area-map (read-area-map filename)]
    (println (solve-puzzle area-map))))



