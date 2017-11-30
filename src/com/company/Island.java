package com.company;

import java.util.*;

/**
 * Created by Čejkis on 19.04.2017.
 */
public class Island {

    static int velikostPopulace = 200;
    static int velikostSelekce = 75;
    static int pocetDeti = 200;

    public ArrayList<Individual> populace;
    public Individual nejlepsiBorec;

    static double pravdepodobnostKrizeniSNejlepsim = 0.0;
    static double pravdpodobnostMutacePopulace = 1;
    static double pravdepodobnostMutaceDitete = 1;

    final static int INTERVALLS = 200;
    final static int LSITERS = 500;

    int bestScore = -1000000000;
    int islandNr;

    private Vizual frame;

    public Island(int islandNr) {

        if (Main.VIZ)
            frame = new Vizual(Main.sirka, Main.vyska, islandNr);

        this.islandNr = islandNr;

        // System.out.println();
        // System.out.println("reseni cislo " + iterace);

        populace = initPopulation(0);
    }

    public int rozdilnost(Individual i1, Individual i2) {

        int diff = 0;

        for (int i = 0; i < Main.vyska; i++) {
            for (int j = 0; j < Main.sirka; j++) {
                if (i1.tajenka[i][j] != i2.tajenka[i][j]) {
                    diff++;
                }
            }
        }

        return diff;
    }

    public void optimiseCrowd(int g) {

        int bestScr = -1000000;

        for (Individual i: populace){
            if( bestScr < i.fitness ){
                nejlepsiBorec = i;
                bestScr = i.fitness;
            }
        }

        if (Main.VIZ && g % 10 == 0) // kazdych 10 generaci updatuju vizualizaci
            frame.printBorec(nejlepsiBorec, g, islandNr);

        if (nejlepsiBorec.fitness > bestScore) {
            bestScore = nejlepsiBorec.fitness;
            if (Main.VIZ)
                frame.printBestEver(nejlepsiBorec, g, islandNr);
            //System.out.println(islandNr + " " + nejlepsiBorec.birth);
        }

        // lokální opt
//        if( g % INTERVALLS == 0 && g != 0) {
//
//            for (int i = 0; i < populace.size() ; i++) {
//                if(Math.random() < 0.1)
//                    populace.set(i, populace.get(i).localOptimalization(LSITERS));
//            }
//
////            if(nejlepsiBorec.fitness >= -14)
////                nejlepsiBorec = nejlepsiBorec.localOptimalization(LSITERS * 5);
////            else
////                nejlepsiBorec = nejlepsiBorec.localOptimalization(LSITERS);
//
//        }



        // sparuju a vytvorim deti
        Collections.shuffle(populace);

        for (int i = 0; i < velikostPopulace; i += 2) {

            int n1 = i;
            int n2 = i + 1;

            Individual p1 = populace.get(n1);
            Individual p2 = populace.get(n2);

            Individual c1 = krizeni(p1, p2, g);  // TODO parallel
            Individual c2 = krizeni(p1, p2, g);  // TODO parallel

            // mutate c1?
            if (Math.random() < pravdepodobnostMutaceDitete) {
                c1.zmutuj();  // TODO parallel
            }
            if (Math.random() < pravdepodobnostMutaceDitete) {
                c2.zmutuj();  // TODO parallel
            }

            c1.spoctiFitness(); // TODO parallel
            c2.spoctiFitness();  // TODO parallel

            if (rozdilnost(p1, c1) + rozdilnost(p2, c2) < rozdilnost(p1, c1) + rozdilnost(p2, c2)) {
                if(c1.fitness >= p1.fitness){
                    populace.set(n1,c1);
                }
                if(c2.fitness >= p2.fitness){
                    populace.set(n2,c2);
                }
            } else{
                if(c2.fitness >= p1.fitness){
                    populace.set(n1,c2);
                }
                if(c1.fitness >= p2.fitness){
                    populace.set(n2,c1);
                }
            }
        }

        //if (g % 50 == 0) {
        // statistiky(populace);
        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //}
        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //  System.out.println("fitness spocteno " + fitnessCounted);

        if (populace.get(0).fitness == 0) {
            statistiky(populace);
            System.out.println("MAM RESENI v generaci " + g);
            populace.get(0).printPole();
        }

    }

    public void optimise(int g) {

        // nejlepsiho jedince zachovam
        nejlepsiBorec = populace.get(0);

        // provedu local search u nejlepsiho

        if (Main.VIZ && g % 10 == 0)
            frame.printBorec(nejlepsiBorec, g, islandNr);

//        if( g % INTERVALLS == 0 && g != 0) {
//
//            for (int i = 0; i < populace.size() ; i++) {
//                if(Math.random() < 0.1)
//                    populace.set(i, populace.get(i).localOptimalization(LSITERS));
//            }
//
////            if(nejlepsiBorec.fitness >= -14)
////                nejlepsiBorec = nejlepsiBorec.localOptimalization(LSITERS * 5);
////            else
////                nejlepsiBorec = nejlepsiBorec.localOptimalization(LSITERS);
//
//        }

        if (nejlepsiBorec.fitness > bestScore) {
            bestScore = nejlepsiBorec.fitness;
            if (Main.VIZ)
                frame.printBestEver(nejlepsiBorec, g, islandNr);
            //System.out.println(islandNr + " " + nejlepsiBorec.birth);
        }

        // vyselektuju rodice a vytvorim deti
        ArrayList<Individual> rodiceAsArray = new ArrayList<>(selectParents(populace));
        ArrayList<Individual> offspring = new ArrayList<>();

        for (int i = 0; i < pocetDeti; i++) {
            offspring.add(
                    krizeni(rodiceAsArray.get((int) (Math.random() * velikostSelekce)),
                            rodiceAsArray.get((int) (Math.random() * velikostSelekce)), g
                    )  // TODO parallel
            );
        }

        // jeste zkrizim nejlepsiho s nekterymi jedinci
        for (int i = 1; i < velikostPopulace; i++) {
            if (Math.random() < pravdepodobnostKrizeniSNejlepsim)
                offspring.add(krizeni(nejlepsiBorec, populace.get(i), g));  // TODO parallel
        }

        // zmutuju deti
        for (Individual dite : offspring) {

//                    if (dite.fitness == 0) {
//                        System.out.println("** MAM RESENI v generaci " + g);
//                        dite.printPole();
//                        return;
//                    }

            if (Math.random() < pravdepodobnostMutaceDitete) {
                dite.zmutuj();  // TODO parallel
            }

            dite.spoctiFitness(); // TODO parallel
        }

        // nahodne zmutuju cast stare populace  - bez krizeni
        for (Individual i : populace) {

            if (i == nejlepsiBorec) continue;

            if (Math.random() < pravdpodobnostMutacePopulace) {
                i.zmutuj();  // TODO parallel
                i.spoctiFitness();  // TODO parallel
            }
            offspring.add(i);
        }

        // offspring.add(nejlepsiBorec);

        populace = offspring;

        //if (g % 50 == 0) {
        // statistiky(populace);
        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //}

        //  System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        //  System.out.println("fitness spocteno " + fitnessCounted);

        Collections.sort(populace);

        if (populace.get(0).fitness == 0) {
            statistiky(populace);
            //System.out.println("MAM RESENI v generaci " + g);
            //populace.get(0).printPole();
            return;
        }

        //odeberu ty horsi, at mam opet puvodni pocet
        for (int i = populace.size() - 1; i >= velikostPopulace; i--) {
            populace.remove(i);
        }

    }

    static Individual krizeni2(Individual a, Individual b, int gen) {

        Individual c = new Individual(gen);

        int bodZlomu = (int) (Math.random() * Main.vyska - 2) + 1;

        for (int i = 0; i < bodZlomu; i++) {
            c.velikostiMezer.add(new ArrayList<>(a.velikostiMezer.get(i)));
        }

        for (int i = bodZlomu; i < Main.vyska; i++) {
            c.velikostiMezer.add(new ArrayList<>(b.velikostiMezer.get(i)));
        }

        c.vyplnCelouTajenkuPodleLegendyAMezer();

        return c;
    }

    static Individual krizeni(Individual a, Individual b, int gen) {

        Individual c = new Individual(gen);

        for (int i = 0; i < Main.vyska; i++) {
            if (Math.random() > 0.5)
                c.velikostiMezer.add(new ArrayList<>(a.velikostiMezer.get(i)));
            else
                c.velikostiMezer.add(new ArrayList<>(b.velikostiMezer.get(i)));
        }

        c.vyplnCelouTajenkuPodleLegendyAMezer();

        return c;
    }

    public static void statistiky(ArrayList<Individual> populace) {

        //  double prumernyFitness;
        int nejvyssiFitness = populace.get(0).fitness;
//        int nejnizsiFitness = populace.get(0).fitness;

        // int suma = 0;

//        for (Individual i : populace) {
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
//        prumernyFitness = suma / (double)populace.size();

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

    public static ArrayList<Individual> initPopulation(int g) {

        ArrayList<Individual> p = new ArrayList<>();

        for (int i = 0; i < velikostPopulace; i++) {

            Individual j = new Individual(g);
            j.basicInit();

            for (int k = 0; k < Main.vyska * 5; k++) {
                j.zmutuj();
            }

            j.spoctiFitness(); // TODO parallel

            p.add(j);
        }

        return p;
    }

}
