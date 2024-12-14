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

(defn is-blocked? [area-map loc] (not (contains? area-map loc)))

(defn traverse-path [init-path area-map]
  (defn helper [path seen]
    (let [curr-pos (first path)
          curr-loc (first curr-pos)
          curr-content (area-map curr-loc)
          next-pos (get-next-pos curr-pos area-map)
          next-loc (first next-pos)]
      (cond
        (is-looping? seen next-pos) nil
        (is-blocked? area-map next-loc) path
        :else (recur (cons next-pos path)
                     (conj seen next-pos)))))
  (helper init-path (set nil)))

(defn is-map-loopy-with-obstacle [area-map obs-loc init-path]
  (let [new-map (assoc area-map obs-loc "#")]
    (traverse-path init-path new-map)))

(defn solve-puzzle [area-map]
  (def max-cols (apply max (map #(second (first %)) area-map)))
  (def max-rows (apply max (map #(first (first %)) area-map)))

  (let [start-loc (first (first (filter #(= (second %) "^") area-map)))
        init-path (list [start-loc :up])
        path (traverse-path init-path area-map)
        path-length (count (distinct (map first path)))
        loop-count (->>
                    path
                    (map #(first %))
                    (distinct)
                    (filter #(and (not= start-loc %)))
                    (map #(is-map-loopy-with-obstacle area-map %1 init-path))
                    (filter nil?)
                    (count))]
    [path-length loop-count]))

(defn -main [filename]
  (let [area-map (read-area-map filename)]
    (println (solve-puzzle area-map))))
