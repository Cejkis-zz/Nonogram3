package com.company;

import java.util.ArrayList;

public abstract class Island {

    static int popSize = 256;
    static int velikostSelekce = 70;
    static int pocetDeti = 100;

    static double pravdepodobnostKrizeniSNejlepsim = 0.0;
    static double pravdpodobnostMutacePopulace = 1;
    static double pravdepodobnostMutaceDitete = 1;


    int bestScore = -1000000000;

    public Individual bestInd;

    public abstract void optimise(int g);


}
