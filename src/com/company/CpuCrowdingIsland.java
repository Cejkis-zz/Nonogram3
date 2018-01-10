package com.company;

import java.util.*;

import static com.company.Main.*;

/**
 * Created by Čejkis on 19.04.2017.
 */
public class CpuCrowdingIsland extends Island{

    public ArrayList<Individual> population;

    private Vizual frame;

    public CpuCrowdingIsland(){

        if (Main.VIZ)
            frame = new Vizual(width, Main.height);

        population = new ArrayList<Individual>() ;

        for (int i = 0; i < popSize; i++) {

            Individual j = new Individual(0);

            for (int k = 0; k < Main.height * 5; k++) {
                j.mutate();
            }

            j.countFitness();

            population.add(j);
        }
    }


    public int difference(Individual i1, Individual i2) {

        int diff = 0;

        for (int i = 0; i < gridSize; i++) {
            if (i1.tajenka[i] != i2.tajenka[i]) {
                diff++;
            }
        }

        return diff;
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

        if (Main.VIZ && g % 10 == 0) // kazdych 10 generaci updatuju vizualizaci
            frame.printBorec(bestInd, g);

        if (bestInd.fitness > bestScore) {
            bestScore = bestInd.fitness;
            if (Main.VIZ)
                frame.printBestEver(bestInd, g);
        }

        // sparuju a vytvorim deti
        Collections.shuffle(population);

        for (int i = 0; i < popSize; i += 2) {

            int n1 = i;
            int n2 = i + 1;

            Individual p1 = population.get(n1);
            Individual p2 = population.get(n2);

            Individual c1 = cross(p1, p2, g);
            Individual c2 = cross(p1, p2, g);

            // mutate c1?
            if (Math.random() < pravdepodobnostMutaceDitete) {
                c1.mutate();
            }
            if (Math.random() < pravdepodobnostMutaceDitete) {
                c2.mutate();
            }

            c1.countFitness();
            c2.countFitness();

            if (difference(p1, c1) + difference(p2, c2) < difference(p1, c2) + difference(p1, c2)) {
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

    static Individual cross(Individual a, Individual b, int gen) {

        Individual c = new Individual(gen);

        for (int i = 0; i < Main.gridSize; i++) {
            if (Math.random() > 0.5)
                c.tajenka[i] = a.tajenka[i];
            else
                c.tajenka[i] = b.tajenka[i];
        }

        return c;
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

    // tournamentovou metodou vyberu nove rodice
    public static Set<Individual> selectParents(ArrayList<Individual> populace) {

        Set<Individual> rodice = new HashSet<>();

        while (rodice.size() != velikostSelekce) {

            Individual a = populace.get((int) (Math.random() * populace.size()));
            Individual b = populace.get((int) (Math.random() * populace.size()));

            if (a.fitness > b.fitness)
                rodice.add(a);
            else rodice.add(b);
        }

        return rodice;
    }

}
