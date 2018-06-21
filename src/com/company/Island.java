package com.company;

public abstract class Island {

    static int popSize = 320;
    static int selectionSize = 50;
    static int numberChildren = 320;

    static double probabilityCrossBest = 0.1;
    static double probPopulationMutation = 0.1;
    static double probChildMutation = 0.9;

    int bestScore = -1000000000;

    public AbstractIndividual bestInd;

    public abstract void optimise(int g);

    public abstract void printStatistics();


}
