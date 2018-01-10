package com.company;

import java.util.*;

import static com.company.Main.*;

/**
 * Created by Čejkis on 19.04.2017.
 */
public class ClassicIsland extends Island{

    public ArrayList<Individual> population;

    private Vizual frame;

    public ClassicIsland(){
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



    public void optimise(int g) {

        // nejlepsiho jedince zachovam
        bestInd = population.get(0);

        // provedu local search u nejlepsiho

        if (Main.VIZ && g % 5 == 0)
            frame.printBorec(bestInd, g);

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
            //System.out.println(islandNr + " " + bestInd.birth);
        }

        // vyselektuju rodice a vytvorim deti
        ArrayList<Individual> rodiceAsArray = new ArrayList<>(selectParents(population));
        ArrayList<Individual> offspring = new ArrayList<>();

        for (int i = 0; i < pocetDeti; i++) {
            offspring.add(
                    krizeni(rodiceAsArray.get((int) (Math.random() * velikostSelekce)),
                            rodiceAsArray.get((int) (Math.random() * velikostSelekce)), g
                    )  // TODO parallel
            );
        }

        //jeste zkrizim nejlepsiho s nekterymi jedinci
        for (int i = 1; i < popSize; i++) {
            if (Math.random() < pravdepodobnostKrizeniSNejlepsim)
                offspring.add(krizeni(bestInd, population.get(i), g));  // TODO parallel
        }

        // zmutuju deti
        for (Individual dite : offspring) {

//                    if (dite.fitness == 0) {
//                        System.out.println("** MAM RESENI v generaci " + g);
//                        dite.printPole();
//                        return;
//                    }

            if (Math.random() < pravdepodobnostMutaceDitete) {
                dite.mutate();  // TODO parallel
            }

            dite.countFitness(); // TODO parallel
        }

        // nahodne zmutuju cast stare population  - bez krizeni
        for (Individual i : population) {

            if (i == bestInd) continue;

            if (Math.random() < pravdpodobnostMutacePopulace) {
                i.mutate();  // TODO parallel
                i.countFitness();  // TODO parallel
            }
            offspring.add(i);
        }

        offspring.add(bestInd);

        population = offspring;


        //if (g % 50 == 0) {
        // statistiky(population);
        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //}

        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //  System.out.println("fitness spocteno " + fitnessCounted);

        Collections.sort(population);

        if (population.get(0).fitness == 0) {
            statistiky(population);
            //System.out.println("MAM RESENI v generaci " + g);
            //population.get(0).printPole();
            return;
        }

        //odeberu ty horsi, at mam opet puvodni pocet
        for (int i = population.size() - 1; i >= popSize; i--) {
            population.remove(i);
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
