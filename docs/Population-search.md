Mark Lehet && Jacob Opdahl

CSci 4553 - Population-Based Search Algorithms Exercise


# The Tweak Function

The same tweak function is used across all of our population-based algorithms. 
We decided to use the tweak function from Jacob and Peter's group for the Hill-Climb exercise. 
The main reason for this decision is that the algorithm is simple; Mark and Lenny had a more complex
tweak function that allowed for choosing how many genes flipped for each mutation. We wanted
to avoid having more parameters to test. Additionally, Jacob and Peter's performed reasonably well
in the last exercise such that it is worth using again.

*Note*: Jacob and Peter's algorithm still has the cheat/bias where it will perform more work than it arguably should be allowed
to for the first tweak. As a reminder, the tweak function has two possible modifications:
  * If this answer is overweight, keep randomly removing items until it's not.
  * Otherwise, randomly remove and add an item.
  
Thus, in the first tweak, many items can be removed (and likely will be for larger knapsack problems). This bias isn't as much of an issue for 
comparing across our different population-based algorithms since they all contain it. However, when comparing against other group's algorithms
as well as against random, this will need to be kept in mind.

This function takes an answer and scorer and returns a new answer.


# Mutation-Based GA

Our mutation-based GA is based heavily off of the (mu, lambda) evolution strategy in the Essentials of Metaheuristics on page 33.  The general algorithm is as follows:

1. Create an initial population with population-size individuals.
2. Repeatedly do the following:
 1. Check for best answer in population, and update as appropriate.
 2. Determine the survivor-rate number of individuals to be mutated.
 3. Create a new population using the population-size/survivor-rate tweaked answers for each survivor.
 4. Use this new population to repeat max-tries/population-size times.
3. Return best result at end.

The function is mutate-GA and takes the following arguments:
* tweak - Our mutation function (see above)
* scorer - A function that adds a score field to an answer
* population-size - The number of individuals to be considered in each generation
* survivor-rate - The number of individuals to survive the selection process
* instance - Problem instance to be solved
* max-tries - The maximum amount of mutations to occur

Note: max-tries should be divisible by population-size, which should be divisible by survivor-rate.

Fitness assesment in our algorithm occurs during phase 2.1.  During this, the current best is checked against the current population to see if there is a new best.

Selection in our algorithm is performed through truncation selection, as seen in 2.2.  With this, the survivor-rate number of best individuals are allowed to be mutated in latter steps.

Breeding and population reassembly occur together.  For breeding, each of the survivors is tweaked to generate populations-size/survivor-rate new individuals, which are then reassembled into a new population.


# Crossover GAs

As instructed, we made two population-based GAs that use crossover with parents to create subsequent generations. One uses Uniform Crossover, the other uses Two-Point Crossover (these are both the breeding portion of the overall GA general proccess). Since this is the only thing that would be different between the two GAs, we decided early on to make a general function, crossover-GA, that takes the crossover function to be used as an argument. This algorithm closely follows the general Genetic Algorithm described in Essentials of Metaheuristics on page 36. This overall algorithm works as follows:

Note: This is the same as mutate-GA up until, and including, 2.1.

1. Create an initial population with population-size individuals.
2. Repeatedly do the following:
 1. Check for best answer in population, and update as appropriate. 
 2. Repeatedly for population-size number of times:
  * - Use tournament-selection twice to determine two parents.
  * - Use the crossover function to get a *single* child.
  * - Apply the tweak/mutation function to the child.
  * - Add the child to the new population.
 3. Use this new population to repeat max-tries/population-size times.
3. Return best result at end.

The function crossover-GA takes the following arguments:
* crossover-fn - The crossover function to use
* tweak - Our mutation function (see above)
* tourn-size - The size of the subset of the population to hold a tournament during tournament selection
* scorer - A function that adds a score field to an answer
* population-size - The number of individuals to be considered in each generation
* instance - Problem instance to be solved
* max-tries - The maximum amount of mutations to occur

Note: max-tries should be divisible by population-size.

Fitness assesment in our algorithm occurs during phase 2.1.  During this, the current best is checked against the current population to see if there is a new best.

Selection for parents in our algorithm is performed using tournament-selection as described in Essentials of Metaheuristics on page 45. For this, a tournament-size subset of the population is randomly selected. Then, the individual with the highest score from that subset is chosen as a parent.

Breeding occurs through crossover of the parents chosen during selection. This could be done using either of our crossover function, which are described below. One important design decision to note here:  Essentials of Metaheuristics describes these algorithms as taking in two parents, and returning *two* children. We decided to only have them return a single child. One, this was easier; two, this emphasizes exploration over exploitation. We also examined these techniques in class as only returning single children, so we followed that approach. Last, both functions take in full answers as parents, and return a full answer as a child.

For population reassembly, the new child is tweaked/mutated and then added to the population, which will be used in the next generation. No parents are added directly to the new generation.


## Uniform Crossover

Our uniform-crossover function works as follows:

1. Take the chromosomes (bit vectors) from the parents.
2. For each respective gene in both parents, randomly choose one parent's allele in that location to fill the child's gene.
3. Construct a new answer using the new child chromosome.

Note: We use a 50% probability for determining which parent to take the allele value from.

This is pretty simple. Not a whole lot complicated going on here. The args for the function are as follows:

* parent-a - The first answer to be a parent.
* parent-b - The second answer to be a parent.
* scorer - Scoring function to be used when constructing the child answer.

## Two-Point Crossover
