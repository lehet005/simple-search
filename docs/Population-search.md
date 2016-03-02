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


# 
