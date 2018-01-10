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


    public int rozdilnost(Individual i1, Individual i2) {

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

            Individual c1 = krizeni(p1, p2, g);  // TODO parallel
            Individual c2 = krizeni(p1, p2, g);  // TODO parallel

            // mutate c1?
            if (Math.random() < pravdepodobnostMutaceDitete) {
                c1.mutate();  // TODO parallel
            }
            if (Math.random() < pravdepodobnostMutaceDitete) {
                c2.mutate();  // TODO parallel
            }

            c1.countFitness(); // TODO parallel
            c2.countFitness();  // TODO parallel

            if (rozdilnost(p1, c1) + rozdilnost(p2, c2) < rozdilnost(p1, c2) + rozdilnost(p1, c2)) {
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

        //if (g % 50 == 0) {
        // statistiky(population);
        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //}
        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //  System.out.println("fitness spocteno " + fitnessCounted);

        if (population.get(0).fitness == 0) {
            statistiky(population);
            System.out.println("MAM RESENI v generaci " + g);
        }

    }

    static Individual krizeni(Individual a, Individual b, int gen) {

        Individual c = new Individual(gen);

        for (int i = 0; i < Main.gridSize; i++) {
            if (Math.random() > 0.5)
                c.tajenka[i] = a.tajenka[i];
            else
                c.tajenka[i] = b.tajenka[i];
        }

        return c;
    }

    public static void statistiky(ArrayList<Individual> populace) {

        //  double prumernyFitness;
        int nejvyssiFitness = populace.get(0).fitness;
//        int nejnizsiFitness = population.get(0).fitness;

        // int suma = 0;

//        for (Individual i : population) {
//            suma += i.fitness;
//
//            if (i.fitness > nejvyssiFitness) {
//                nejvyssiFitness = i.fitness;
//            }
//            if (i.fitness < nejnizsiFitness) {
//                nejnizsiFitness = i.fitness;
//            }
//        }
//
//        prumernyFitness = suma / (double)population.size();

        //System.out.println("Ohodnoceni;" + fitnessCounted + ";NEJLEPSI; " + nejvyssiFitness);
        System.out.print(nejvyssiFitness + ";");

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
