# Nonogram3

This is my evolutionary algorithm for solving Nonograms (grid puzzles)

Fitness is computed by Needleman-wunsch algorithm.

The individual is represented either by binary 2d array or arraylist of space sizes of each row (called smart individual). 
Smart always has left legend fulfilled correctly.

There is both "Classic" EA approach, as well as Deterministic crowding.
Deterministic crowding can also be run on CUDA GPU.
There is also simple vizualization of the best individual.
There are several example inputs attached.

## Usage
Application is controlled by command line parameters. Example: java -jar Nonogram.jar inputs/40x40.txt 4 par 1000"

First is path to the puzzle (must be in specific format, see examples)
Number od used CUDA multiprocessors (default is 2)
Computational mode ('cpu' - cpu computation, 'ser' - serial computation of fitness on gpu, 'par' - parallel computation of fitness on gpu)

