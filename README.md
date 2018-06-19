# Nonogram3  

This is my evolutionary algorithm for solving Nonograms (grid puzzles)  

Fitness is computed by Needleman-wunsch algorithm.  

The individual is represented either by binary 2d array or arraylist of space sizes of each row (called smart individual).   
Smart always has left legend fulfilled correctly.  

There is both "Classic" EA approach, as well as Deterministic crowding.  
Deterministic crowding can also be run on CUDA GPU.  
There is support for the "island model" with catastrophes and migrations. (Islands run in one thread.)  
There is also simple vizualization of the best individual.  
There are several example inputs attached.  

## Usage
Application is controlled by command line parameters.  

use these paramters:  
par, ser - to turn on CUDA computation. par and ser means whether individual's fitness will be computed in parallel or serially. Serial is faster. Default: CPU will be used  
viz - turn vizualization on. Default: off. CPU only  
bin - use binary individual. Default: Smart representation. GPU uses only binary.  
dc - use deterministic crowding. Default: normal evolution. CPU onlu.     
integer number - if it's lower than 50, it will be used as number of SMP's. If it's higher, it will be used as number of generations. Default: 2 SMP's and 10000 generations  
Every string that doesn't match these will be used as a path to input file of puzzle.    

Example: java -jar Nonogram.jar inputs/40x40.txt 4 par 1000"

