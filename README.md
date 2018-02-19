# Nonogram3

This is my evolutionary algorithm for solving Nonograms (grid puzzles)

Fitness is computed by Needleman-wunsch algorithm.

The individual is represented either by binary 2d array or arraylist of space sizes of each row (called smart individual). 
Smart always has left legend fulfilled correctly.

There is both "Classic" EA approach, as well as Deterministic crowding.
DC can also be run on CUDA GPU. 
There is support for the "island model" with catastrophes and migrations. (Islands run in one thread.)
There is also simple vizualization of the best individual.
There is several example inputs attached.
