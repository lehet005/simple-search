(ns simple-search.core
  (:use simple-search.knapsack-examples.knapPI_11_20_1000
        simple-search.knapsack-examples.knapPI_13_20_1000
        simple-search.knapsack-examples.knapPI_16_20_1000
        simple-search.knapsack-examples.knapPI_11_1000_1000
        simple-search.knapsack-examples.knapPI_13_1000_1000
        simple-search.knapsack-examples.knapPI_16_1000_1000))

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

;;; ..................... TWEAK FUNCTION FROM JACOB AND PETER .........
;;; NOTE: Loop/recur in this area comes from when we were working on hill-climbing.

(defn find-and-remove-choice
  "Takes a list of choices and returns the same list with one choice removed randomly."
  [choices]
  (def choicesVector (vec choices))
  (loop [rand #(rand-int (count choicesVector))]
    (if (= (get choicesVector rand) 1)
      (reverse (into '() (assoc choicesVector rand 0)))
      (recur (#(rand-int (count choicesVector))))
      )))


;; If time allows, combine this and find-and-remove-choice.
(defn find-and-add-choice
  "Takes a list of choices and returns the same list with one choice added randomly."
  [choices]
  (def choicesVector (vec choices))
  (loop [rand #(rand-int (count choicesVector))]
    (if (= (get choicesVector rand) 0)
      (reverse (into '() (assoc choicesVector rand 1)))
      (recur (#(rand-int (count choicesVector))))
      )))


(defn reconstruct-answer
  "takes an instance and a set list of choices and returns the new answer."
  [instance choices]
  (let [included (included-items (instance :items) choices)]
  {:instance instance
   :choices choices
   :total-weight (reduce + (map :weight included))
   :total-value (reduce + (map :value included))}))


;; This will be our initial tweak function.
(defn remove-then-random-replace
  "Takes an answer. If the answer is over capacity, removes items until it is not. If it is not, removes a random and add a random."
  [answer]
  (if (> (:total-weight answer) (:capacity (:instance answer)))
    (remove-then-random-replace (reconstruct-answer (:instance answer) (find-and-remove-choice (:choices answer))))
    (add-score penalized-score (reconstruct-answer (:instance answer)
                        (find-and-add-choice
                         (find-and-remove-choice (:choices answer)))))))




;;; ====================== MARK AND JACOB START HERE. =================

;;; --------------------- Mutation-Based GA --------------------------
(defn assess-fitness
  "Takes a population and a best answer and returns the best answer between both of them."
  [best population]
    (if (= (count population) 0)
      best
      (assess-fitness (if (> (:score best) (:score (first population)))
                        best
                        (first population))
                      (rest population))))

(defn truncation-selection
  "Takes a population consisting of a list of answers and returns the survivor-rate best of them."
  [population survivor-rate]
    (take survivor-rate (sort-by :score > population)))

(defn breeding
  "Takes a population, and generates children-num answers for each individual using the provided tweak function."
  [population children-num tweak]
  (flatten (for [x population] (repeatedly children-num #(tweak x)))))

;;; Starting after population is made:
;;; 1. Check all in pop for seeing which is best. Update best answer.
;;; 2. determine the n = suvivor-rate best instances
;;; 3. Start a new population
;;; 4. Create population-size/survivor-rate tweaked answers for each in best
;;;     Note: that's the new population
;;; 5. Then repeat this max-tries
;;; mu,lambda -> surv,pop
(defn mutate-GA
  "max-tries should be divisible by population-size, which should be divisible by survivor-rate
  Performs a Genetic algorithm using pure mutations of individuals in a population."
  [tweak scorer population-size survivor-rate instance max-tries]
  (let [initial (repeatedly population-size #(add-score scorer (random-answer instance)))]
    (loop [best (first initial)
           population initial
           loop-times (/ max-tries population-size)]
      (if (> loop-times 0)
        (recur (assess-fitness best population) (breeding (truncation-selection population survivor-rate) (/ population-size survivor-rate) tweak) (dec loop-times))
        best))))

;;; NOTE TO SELVES - We hard-coded in the scorer to the tweak fxn, but not the overall. Could use some refactoring.




;;; --------------------- Crossover GAs --------------------------

(defn random-subset
  "Returns a subset of the population determined by size."
  [population size]
  (take size (shuffle population)))

(defn tournament-selection
  "Uses tournament selection to pick a subset of a population and then chooses the best answer among the subset."
  [population tourn-size]
  (let [subset (random-subset population tourn-size)]
    (assess-fitness (first subset) subset)))


;;; Probability for determining parent will be hard-coded
(defn uniform-crossover
  "Takes two parent answers and returns a child answer using uniform crossover. Randomly selects which parent to take
  an allele from for each gene in a chromosome."
  [parent-a parent-b scorer]
  (let [chromosome-a (:choices parent-a)
        chromosome-b (:choices parent-b)]
    (add-score scorer (reconstruct-answer (:instance parent-a) (vec (map (fn [x y] (if (> (rand-int 100) 50) x y)) chromosome-a chromosome-b))))))

(defn two-point-crossover
  "Performs two-point crossover using parent a for the 'outside' alleles, and parent b
  for the 'inside' alleles. If the indices for boundaries are equal, parent a will be returned."
  [parent-a parent-b scorer]
  (let [chromosome-a (:choices parent-a)
        chromosome-b (:choices parent-b)
        point1 (rand-int (inc (count parent-a)))
        point2 (rand-int (inc (count parent-b)))]
    (if (> point1 point2)
      (add-score scorer
                 (reconstruct-answer
                  (:instance parent-a)
                  (vec (flatten (conj
                                 (last (split-at point1 chromosome-a))
                                 (last (split-at point2 (first (split-at point1 chromosome-b))))
                                 (first (split-at point2 chromosome-a)))))))
      (add-score scorer
                 (reconstruct-answer
                  (:instance parent-a)
                  (vec (flatten (conj
                                 (last (split-at point2 chromosome-a))
                                 (last (split-at point1 (first (split-at point2 chromosome-b))))
                                 (first (split-at point1 chromosome-a)))))))
    )))

;;; Selection algorithm will be hard-coded as tournament-selection

(defn crossover-GA
  ""
  [crossover-fn tweak tourn-size scorer population-size instance max-tries]
  (let [initial (repeatedly population-size #(add-score scorer (random-answer instance)))]
    (loop [best (first initial)
           population initial
           loop-times (/ max-tries population-size)]
      (if (> loop-times 0)
        (recur (assess-fitness best population)
               (repeatedly population-size
                         #(tweak (crossover-fn (tournament-selection population tourn-size)
                                               (tournament-selection population tourn-size)
                                               scorer)))
               (dec loop-times))
        best))))




;;; -=-=-=-=-=-=-=-=-=-=- RUN TESTING OF FUNCTIONS HERE. -=-=-=-=-=-=-=-

;;; Testing making our population and sorting it and getting best from it.
;(take 2 (sort-by :score > (repeatedly 5 #(add-score penalized-score (random-answer knapPI_11_20_1000_4)))))

;;; Testing that assess fitness does the thing.
;(assess-fitness {:score 10} '({:score 20} {:score 100} {:score 11}))

;;; Testing breeding
;(breeding (repeatedly 5 #(add-score penalized-score (random-answer knapPI_11_20_1000_4))) 2 remove-then-random-replace)

;;; Test tweak
;(remove-then-random-replace (random-answer knapPI_11_20_1000_4))

;;; Testing mutate-GA
;(mutate-GA remove-then-random-replace penalized-score 100 50 knapPI_11_1000_1000_4 1000)

;;; Test tournament-selection and random-subset
;(random-subset '({:score 20} {:score 100} {:score 11} {:score 56} {:score 34} {:score 1} {:score 1} {:score 22}) 4)
;(tournament-selection '({:score 20} {:score 100} {:score 11} {:score 56} {:score 34} {:score 1} {:score 1} {:score 22}) 4)

;;; Testicles uniform-crossover
;(uniform-crossover (random-answer knapPI_11_20_1000_4) (random-answer knapPI_11_20_1000_4) penalized-score)
;(vec (map (fn [x y] (if (> (rand-int 100) 50) x y)) [0 1 2 3] ["a" "b" "c" "d"]))

;;; Figuring out two-point crossover.
;;; [0 1 0 1 0] [1 0 1 0 1] -> 2 4 -> [0 1 1 0 0]
;(split-at 4 [1 2 3 4 5])
;(vec (flatten (conj (last (split-at 3 [0 1 0 1 0])) (last (split-at 3 (first (split-at 3 [1 0 1 0 1])))) (first (split-at 3 [0 1 0 1 0])))))

;;; Testing two-point crossover.
;(two-point-crossover (random-answer knapPI_11_20_1000_4) (random-answer knapPI_11_20_1000_4) penalized-score)

;;; Testing crossover-GA.
;(crossover-GA uniform-crossover remove-then-random-replace 10 penalized-score 100 knapPI_11_1000_1000_4 10000)
;(crossover-GA two-point-crossover remove-then-random-replace 10 penalized-score 100 knapPI_11_1000_1000_4 10000)
