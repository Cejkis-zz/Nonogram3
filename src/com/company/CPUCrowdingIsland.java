package com.company;

import java.util.*;

import static com.company.Main.*;

/**
 * Created by Čejkis on 19.04.2017.
 */
public class CPUCrowdingIsland extends Island{

    public ArrayList<Individual> population;

    private Vizual frame;

    public CPUCrowdingIsland(){

        if (Main.VIZ)
            frame = new Vizual(width, Main.height);

        population = new ArrayList<Individual>() ;

        for (int i = 0; i < popSize; i++) {

            Individual j;

            if (Main.BINARYINDIVIDUAL){ // TODO should be factory
                j = new IndividualBinary(0);
            }
            else{
                IndividualSmart ie = new IndividualSmart();
                j = ie;
            }

            for (int k = 0; k < Main.height * 5; k++) {
                j.mutate();
            }

            j.countFitness();

            population.add(j);
        }
    }




    @Override
    public void optimise(int g) {

        int bestScr = -1000000;

        for (Individual i : population) {
            if (bestScr < i.fitness) {
                bestInd = i;
                bestScr = i.fitness;
            }
        }

        if (Main.VIZ && g % 10 == 0) // update vizualization every 10 generations
            frame.vizualizeBestInd(bestInd, g);

        if (bestInd.fitness > bestScore) {
            bestScore = bestInd.fitness;
            if (Main.VIZ)
                frame.printBestEver(bestInd, g);
        }

        // make pair and create children
        Collections.shuffle(population);

        for (int i = 0; i < popSize; i += 2) {

            int n1 = i;
            int n2 = i + 1;

            Individual p1 = population.get(n1);
            Individual p2 = population.get(n2);

            Individual c1 = p1.cross(p2, g);
            Individual c2 = p1.cross(p2, g);

            // mutate c1?
            if (Math.random() < probChildMutation) {
                c1.mutate();
            }
            if (Math.random() < probChildMutation) {
                c2.mutate();
            }

            c1.countFitness();
            c2.countFitness();

            if (p1.difference(c1) + p2.difference(c2) < p1.difference(c2) + p1.difference(c2)) {
                if (c1.fitness >= p1.fitness) {
                    population.set(n1, c1);
                }
                if (c2.fitness >= p2.fitness) {
                    population.set(n2, c2);
                }
            } else {
                if (c2.fitness >= p1.fitness) {
                    population.set(n1, c2);
                }
                if (c1.fitness >= p2.fitness) {
                    population.set(n2, c1);
                }
            }
        }

        if (population.get(0).fitness == 0) {
            System.out.println("Solution found in generation" + g);
        }

    }



    @Override
    public void printStatistics() {

        int bestFit = -100000;
        float sum = 0;

        for (Individual i: population
             ) {

            sum += i.fitness;

            if (bestFit < i.fitness) {
                bestFit = i.fitness;
            }
        }

        System.out.println(" best: " + bestFit + " avg: " + sum / popSize);
    }


}
