(ns day16
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defrecord Cell [north south east west])

(defrecord Config [path path-score])

(defn read-maze [filename]
  (defn process-line [y line]
    (map-indexed (fn [x e] [[x y] e]) (str/split line #"")))

  (defn create-cell [x y mapping]
    (defn helper [p] (if (contains? mapping p) p))
    (Cell. (helper [x (- y 1)])
           (helper [x (+ y 1)])
           (helper [(+ x 1) y])
           (helper [(- x 1) y])))

  (defn find-loc [sym pos-to-sym]
    (first (first (filter #(= sym (last %)) pos-to-sym))))
  (let [pos-to-sym (with-open [file (io/reader filename)]
                     (->>
                      file
                      (line-seq)
                      (map-indexed process-line)
                      (apply concat)
                      (filter #(#{"." "S" "E"} (last %)))
                      (into {})))
        start-loc (find-loc "S" pos-to-sym)
        end-loc (find-loc "E" pos-to-sym)
        maze (->>
              pos-to-sym
              (keys)
              (map (fn [[x y]] [[x y] (create-cell x y pos-to-sym)]))
              (into {}))]
    [maze start-loc end-loc]))

(def directions #{:north :south :east :west})

(defn opposite-of [d]
  (case d
    :north :south
    :south :north
    :east :west
    :west :east))

(defn solve-part [maze start-loc end-loc]
  (defn get-next-positions [[loc dir]]
    (->>
     (disj directions (opposite-of dir))
     (map (fn [d] [(d (maze loc)) d]))
     (filter #(some? (first %)))))

  (defn make-new-config [pos config pos-to-score]
    (let [curr-path (:path config)
          curr-dir (second (first curr-path))
          new-dir (second pos)]
      (Config. (cons pos curr-path)
               (+ (:path-score config) (if (= curr-dir new-dir) 1 1001)))))

  (defn update-pos-to-score [pos-to-score config]
    (let [pos (first (:path config))
          pos-score (get pos-to-score pos Integer/MAX_VALUE)
          new-score (:path-score config)]
      (if (< new-score pos-score)
        (assoc pos-to-score pos new-score)
        pos-to-score)))

  (defn explore-paths [configs pos-to-score end-score best-positions]
    (let [config (first configs)
          rest-configs (rest configs)
          curr-pos  (first (:path config))
          path-score (:path-score config)]
      (if (and (some? end-score) (> path-score end-score))
        [end-score best-positions]
        (if (= (first curr-pos) end-loc)
          (recur rest-configs pos-to-score path-score
                 (concat best-positions (:path config)))
          (if (<= path-score (get pos-to-score curr-pos Integer/MAX_VALUE))
            (let [new-configs (->>
                               (get-next-positions curr-pos)
                               (map #(make-new-config % config pos-to-score)))]
              (recur (sort-by #(:path-score %)
                              (concat new-configs rest-configs))
                     (reduce update-pos-to-score pos-to-score new-configs)
                     end-score best-positions))
            (recur rest-configs pos-to-score end-score best-positions))))))

  (let [start-pos [start-loc :east]
        start-configs [(Config. [start-pos] 1)]
        [score best-tiles] (explore-paths start-configs {start-pos 1} nil #{})]
    [(- score 1)
     (count (distinct (map first best-tiles)))]))

(defn -main [filename]
  (let [[maze start-loc end-loc] (read-maze filename)]
    (println (solve-part maze start-loc end-loc))))
