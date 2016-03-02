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

# Mutation-Based GA

