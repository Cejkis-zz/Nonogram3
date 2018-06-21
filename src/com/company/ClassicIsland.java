package com.company;

import java.util.*;

import static com.company.Main.*;

/**
 * Created by Čejkis on 19.04.2017.
 *
 * Classic evolutionary island
 */
public class ClassicIsland extends Island{

    public ArrayList<AbstractIndividual> population;

    private Vizual frame;

    public ClassicIsland(){
        if (Main.VIZ)
            frame = new Vizual(width, Main.height);

        population = new ArrayList<AbstractIndividual>() ;

        for (int i = 0; i < popSize; i++) {

            AbstractIndividual j;

            if (Main.BINARYINDIVIDUAL){ // TODO should be factory
                j = new IndividualBinary(0);
            }
            else{
                IndividualSmart ie = new IndividualSmart();
                j =ie;
            }

            for (int k = 0; k < Main.height * 5; k++) {
                j.mutate();
            }

            j.countFitness();

            population.add(j);
        }

    }

    
    public void optimise(int g) {

        // nejlepsiho jedince zachovam
        bestInd = population.get(0);

        // provedu local search u nejlepsiho

        if (Main.VIZ && g % 100 == 0)
            frame.vizualizeBestInd(bestInd, g);

//        if( g % INTERVALLS == 0 && g != 0) {
//
//            for (int i = 0; i < population.size() ; i++) {
//                if(Math.random() < 0.1)
//                    population.set(i, population.get(i).localOptimalization(LSITERS));
//            }
//
////            if(bestInd.fitness >= -14)
////                bestInd = bestInd.localOptimalization(LSITERS * 5);
////            else
////                bestInd = bestInd.localOptimalization(LSITERS);
//
//        }

        if (bestInd.fitness > bestScore) {
            bestScore = bestInd.fitness;
            if (Main.VIZ)
                frame.printBestEver(bestInd, g);
            //System.out.println(islandNr + " " + bestInd.genOfBirth);
        }

        // select parents and create children
        ArrayList<AbstractIndividual> rodiceAsArray = new ArrayList<>(selectParents(population));
        ArrayList<AbstractIndividual> offspring = new ArrayList<>();

        for (int i = 0; i < numberChildren; i++) {
            AbstractIndividual p1 = rodiceAsArray.get((int) (Math.random() * selectionSize));
            AbstractIndividual p2 = rodiceAsArray.get((int) (Math.random() * selectionSize));
            offspring.add(p1.cross(p2, g));
        }

        // cross the best one with some individuals
        for (int i = 1; i < popSize; i++) {
            if (Math.random() < probabilityCrossBest)
                offspring.add(bestInd.cross(population.get(i), g));
        }

        // mutate children
        for (AbstractIndividual dite : offspring) {
            if (Math.random() < probChildMutation) {
                dite.mutate();
            }
            dite.countFitness();
        }

        // randomly mutate old population and merge with children
        for (AbstractIndividual i : population) {

            if (i == bestInd) continue;

            if (Math.random() < probPopulationMutation) {
                i.mutate();
                i.countFitness();
            }
            offspring.add(i);
        }

        //offspring.add(bestInd);

        population = offspring;

        //if (g % 50 == 0) {
        // statistiky(population);
        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //}


        Collections.sort(population);

        if (population.get(0).fitness == 0) {
            printStatistics();
            System.out.println("Solution in generation " + g);
            //population.get(0).printGrid();
            return;
        }

        // remove the worst ones, so the pop size is still the same
        for (int i = population.size() - 1; i >= popSize; i--) {
            population.remove(i);
        }

    }


    
    @Override
    public void printStatistics() {

        int biggestFitness = population.get(0).fitness;

        int sum = 0;

        for (AbstractIndividual i : population) {
            //System.out.print(i.fitness + " ");

            sum += i.fitness;
            if (i.fitness > biggestFitness) {
                biggestFitness = i.fitness;
            }
        }

        double avgFitness = sum / (double)population.size();

        System.out.println("AvgFitness " + (int)avgFitness + "; BestFitness: " + biggestFitness);

    }

    public static Set<AbstractIndividual> selectParents(ArrayList<AbstractIndividual> population) {

        Set<AbstractIndividual> parents = new HashSet<>();

        while (parents.size() != selectionSize) {

            AbstractIndividual a = population.get((int) (Math.random() * population.size()));
            AbstractIndividual b = population.get((int) (Math.random() * population.size()));

            if (a.fitness > b.fitness)
                parents.add(a);
            else parents.add(b);
        }

        return parents;
    }

}
