(ns day21
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defn myprint [x] (println x) x)

(def numpad-neighbors
  {\7 [[:right \8] [:down \4]]
   \8 [[:left \7] [:right \9] [:down \5]]
   \9 [[:left \8] [:down \6]]
   \4 [[:up \7] [:right \5] [:down \1]]
   \5 [[:up \8] [:right \6] [:down \2] [:left \4]]
   \6 [[:up \9] [:left \5] [:down \3]]
   \1 [[:up \4] [:right \2]]
   \2 [[:up \5] [:right \3] [:down \0] [:left \1]]
   \3 [[:left \2] [:up \6] [:down \A]]
   \0 [[:up \2] [:right \A]]
   \A [[:left \0] [:up \3]]})

(def arrowpad-neighbors
  {\A [[:left \^] [:down \>]]
   \^ [[:right \A] [:down \v]]
   \< [[:right \v]]
   \v [[:left \<] [:up \^] [:right \>]]
   \> [[:left \v] [:up \A]]})

(defn read-codes [filename]
  (with-open [file (io/reader filename)]
    (into [] (line-seq file))))

(defn translate [code]
  (let [mapper {:up \^, :down \v, :left \<, :right \>, \A \A}]
    (str/join (map mapper code))))

; Ordering of the directions is critical to identifying the shortest path
(def directions [:left :down :up :right])

(defn gen-consecutive-pairs [lst] (map vector (drop-last 1 lst) (drop 1 lst)))

(defn get-movements [neighbors]
  (defn get-acyclic-paths [[path & remaining] paths]
    (if (nil? path)
      paths
      (let [last-node (last path)
            next-nodes (filter #(not (.contains path (second %)))
                               (neighbors last-node))]
        (get-acyclic-paths (concat remaining (map #(concat path %) next-nodes))
                           (conj paths path)))))

  (defn get-min-path-between [[src-trg paths]]
    (defn key-helper [path]
      (reduce (fn [acc v] (+ (* acc 10) (.indexOf directions v))) 0 path))
    (defn is-one-turn-path? [path]
      (let [consecutive-dirs (gen-consecutive-pairs path)
            turns (filter #(not= (first %) (second %)) consecutive-dirs)]
        (<= (count turns) 1)))
    (let [dir-only-paths (map (fn [p] (filter #(.contains directions %) p))
                              paths)
          one-turn-paths (filter #(is-one-turn-path? %) dir-only-paths)]
      [src-trg (apply min-key key-helper one-turn-paths)]))

  (defn get-min-paths [acc src]
    (->>
     (get-acyclic-paths [[src]] #{})
     (group-by (fn [x] [src (last x)]))
     (map get-min-path-between)
     (into acc)))
  (reduce get-min-paths {} (keys neighbors)))

(defn translate-code-to-moves [src-trg-pairs from-to-moves moves]
  (translate (flatten (map #(concat (from-to-moves %) '(\A)) src-trg-pairs))))

(defn get-length-of-expanded-code [code reps arrowpad-from-to-moves]
  (defn helper [code-segment depth memory]
    (cond
      (contains? memory [code-segment depth]) memory
      (== depth reps) (assoc memory [code-segment depth]
                             (- (count code-segment) 1))
      :else (let [new-code-segments (->>
                                     code-segment
                                     (gen-consecutive-pairs)
                                     (map arrowpad-from-to-moves)
                                     (map #(str "A" (translate %) "A")))
                  next-depth (+ 1 depth)
                  new-memory (reduce #(helper %2 next-depth %) memory
                                     new-code-segments)
                  length (reduce + (map #(new-memory [% next-depth])
                                        new-code-segments))]
              (assoc new-memory [code-segment depth] length))))

  (let [code-segments (map #(str "A" % "A") (str/split code #"A"))
        memory (reduce #(helper %2 0 %1) {} code-segments)]
    (reduce + (map #(memory [% 0]) code-segments))))

(defn solve [codes numpad-from-to-moves arrowpad-from-to-moves num-of-arrowpads]
  (defn type-code-on-numpad [code]
    (->>
     (gen-consecutive-pairs (cons \A (seq code)))
     (map #(concat (numpad-from-to-moves %) '(\A)))
     (flatten)
     (translate)))

  (defn process-code [code]
    (let [numpad-moves (type-code-on-numpad code)
          moves-length (get-length-of-expanded-code numpad-moves
                                                    num-of-arrowpads
                                                    arrowpad-from-to-moves)
          code-value (Integer. (str/replace code #"A" ""))]
      (* code-value moves-length)))
  (reduce + (map process-code codes)))

(defn -main [filename num-of-robots]
  (let [codes (read-codes filename)
        numpad-from-to-moves (get-movements numpad-neighbors)
        arrowpad-from-to-moves (get-movements arrowpad-neighbors)]
    (println (solve codes numpad-from-to-moves arrowpad-from-to-moves
                    (Integer. num-of-robots)))))
