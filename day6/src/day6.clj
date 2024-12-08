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

(defn is-blocked? [c] (= c "#"))

(defn change-dir [path]
  (let [[pos curr-dir] (first path)
        new-dir (cond
                  (= curr-dir "up") "right"
                  (= curr-dir "right") "down"
                  (= curr-dir "down") "left"
                  :else "up")]
    (cons [pos new-dir] (rest path))))

(defn extend-path [path]
  (let [[[x y] dir] (first path)
        next-pos (cond
                   (= dir "up") [[x (- y 1)] dir]
                   (= dir "down") [[x (+ y 1)] dir]
                   (= dir "left") [[(- x 1) y] dir]
                   (= dir "right") [[(+ x 1) y] dir])]
    (cons next-pos path)))

(defn solve-puzzle [area-map]
  (def max-cols (apply max (map #(second (first %)) area-map)))

  (def max-rows (apply max (map #(first (first %)) area-map)))

  (defn is-guard-out? [[x y]]
    (or (> x max-cols) (< x 0) (> y max-rows) (< y 0)))

  (defn traverse-path [path]
    (let [curr-pos (first (first path))
          curr-content (area-map curr-pos)]
      (cond
        (is-guard-out? curr-pos) path
        (is-blocked? curr-content) (recur (change-dir (rest path)))
        :else (recur (extend-path path)))))

  (defn find-obstructable-points [path]
    (throw (UnsupportedException "TBD")))

  (let [start-pos (first (first (filter #(= (second %) "^") area-map)))
        path (traverse-path (list [start-pos "up"]))
        path-length (- (count (set (map first path))) 1)
        obstructable-points (find-obstructable-points (reverse path))
        num-obs-points (count (filter #(not= start-pos %) obstructable-points))]
    [path-length num-obs-points]))

(defn -main [filename]
  (let [area-map (read-area-map filename)]
    (println (solve-puzzle area-map))))
