(ns simple-search.core
  (:use simple-search.knapsack-examples.knapPI_11_20_1000
        simple-search.knapsack-examples.knapPI_13_20_1000
        simple-search.knapsack-examples.knapPI_16_20_1000
        simple-search.knapsack-examples.knapPI_16_200_1000))

;;; An answer will be a map with (at least) four entries:
;;;   * :instance
;;;   * :choices - a vector of 0's and 1's indicating whether
;;;        the corresponding item should be included
;;;   * :total-weight - the weight of the chosen items
;;;   * :total-value - the value of the chosen items

;;; ****
(defrecord Answer
  [instance choices total-weight total-value])

;;; ****
(defn included-items
  "Takes a sequences of items and a sequence of choices and
  returns the subsequence of items corresponding to the 1's
  in the choices sequence."
  [items choices]
  (map first
       (filter #(= 1 (second %))
               (map vector items choices))))

;;; ****
(defn make-answer
  [instance choices]
  (let [included (included-items (:items instance) choices)]
    (->Answer instance choices
              (reduce + (map :weight included))
              (reduce + (map :value included)))))

;;; ****
(defn random-answer
  "Construct a random answer for the given instance of the
  knapsack problem."
  [instance]
  (let [choices (repeatedly (count (:items instance))
                            #(rand-int 2))]
    (make-answer instance choices)))

;;; It might be cool to write a function that
;;; generates weighted proportions of 0's and 1's.
(defn score
  "Takes the total-weight of the given answer unless it's over capacity,
   in which case we return 0."
  [answer]
  (if (> (:total-weight answer)
         (:capacity (:instance answer)))
    0
    (:total-value answer)))

;;; ****
(defn penalized-score
  "Takes the total-weight of the given answer unless it's over capacity,
   in which case we return the negative of the total weight."
  [answer]
  (if (> (:total-weight answer)
         (:capacity (:instance answer)))
    (- (:total-weight answer))
    (:total-value answer)))

(defn lexi-score
  [answer]
  (let [shuffled-items (shuffle (included-items (:items (:instance answer))
                                                (:choices answer)))
        capacity (:capacity (:instance answer))]
    (loop [value 0
           weight 0
           items shuffled-items]
      (if (empty? items)
        value
        (let [item (first items)
              w (:weight item)
              v (:value item)]
          (if (> (+ weight w) capacity)
            (recur value weight (rest items))
            (recur (+ value v)
                   (+ weight w)
                   (rest items))))))))

;;; ****
(defn add-score
  "Computes the score of an answer and inserts a new :score field
   to the given answer, returning the augmented answer."
  [scorer answer]
  (assoc answer :score (scorer answer)))

(defn random-search
  [scorer instance max-tries]
  (apply max-key :score
         (map (partial add-score scorer)
              (repeatedly max-tries #(random-answer instance)))))

(defn mutate-choices
  [choices]
  (let [mutation-rate (/ 1 (count choices))]
    (map #(if (< (rand) mutation-rate) (- 1 %) %) choices)))

(defn mutate-answer
  [answer]
  (make-answer (:instance answer)
               (mutate-choices (:choices answer))))


(defn hill-climber
  [mutator scorer instance max-tries]
  (loop [current-best (add-score scorer (random-answer instance))
         num-tries 1]
    (let [new-answer (add-score scorer (mutator current-best))]
      (if (>= num-tries max-tries)
        current-best
        (if (> (:score new-answer)
               (:score current-best))
          (recur new-answer (inc num-tries))
          (recur current-best (inc num-tries)))))))


;;; ====================== MARK AND JACOB START HERE. =================


(defn choose-best
  "Takes a population consisting of a list of answers and returns the survivor-rate best of them."
  [population survivor-rate]
    (take survivor-rate (sort-by :score > population)))

;;; max-tries should be divisible by population-size, which should be divisible by survivor-rate
(defn mutate-GA
  [tweak scorer population-size survivor-rate instance max-tries]
  (let [initial (repeatedly population-size #(add-score scorer (random-answer instance)))]
    ))

;;; Starting after population is made:
;;; 1. Want to store the best of our initial population (function or something)
;;; 2. determine the n = suvivor-rate best instances
;;; 3. Start a new population
;;; 4. Create population-size/survivor-rate tweaked answers for each in best
;;;     Note: that's the new population
;;; 5. Then repeat this max-tries




;;; -=-=-=-=-=-=-=-=-=-=- RUN TESTING OF FUNCTIONS HERE. -=-=-=-=-=-=-=-

;;; Testing making our population and sorting it and getting best from it.
;(take 2 (sort-by :score > (repeatedly 5 #(add-score penalized-score (random-answer knapPI_11_20_1000_4)))))
