package com.company;

import javax.swing.*;

public abstract class Individual implements Comparable<Individual> {

    protected int fitness;
    protected int genOfBirth;

    public abstract void  countFitness();
    public abstract Individual cross(Individual b, int gen);
    public abstract void mutate();
    public abstract int difference(Individual i);
    public abstract void printToViz(JTextArea[][] okna);

    @Override
    public int compareTo(Individual individual) {
        return individual.fitness - fitness;
    }

    // this version uses only 2 columns instead of whole matrix.
    // number of legend is there, because "legend" argument is slice of 2D array with possibly larger size
    public static int needlemanWunschOptimized(int [] legend, final int [] gridSlice, int numberOfLegends){

        int[] H0 = new int[gridSlice.length];
        H0[0] = 0;

        int[] H1 = new int[gridSlice.length];
        H1[0] = 0;

        for (int i = 1; i < gridSlice.length; i++) {
            H0[i] = H0[i - 1] - gridSlice[i];
        }

        //---------------

        for (int j = 1; j < numberOfLegends; j++) {

            int legendJ = legend[j];

            H1[0] = H0[0] - legendJ;

            for (int i = 1; i < gridSlice.length; i++) {

                H1[i] = Math.max(H1[i - 1] - gridSlice[i],
                        Math.max(H0[i    ] - legendJ,
                                H0[i - 1] - Math.abs(legendJ - gridSlice[i])));
            }

            int[] swap = H0;
            H0 = H1;
            H1 = swap;
        }

        return H0[gridSlice.length - 1]; // swapped, so H0

    }


}
