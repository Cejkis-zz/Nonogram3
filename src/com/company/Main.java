package com.company;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Main {

    static ArrayList<ArrayList<Integer>> horniLegenda;
    static ArrayList<ArrayList<Integer>> levaLegenda;

    static int vyska, sirka;

    static int fitnessCounted = 0;
    static String jmenoVstupu = "40x30.txt";

    static int velikostPopulace = 200;
    static int velikostSelekce = 60;
    static int pocetDeti = 200;
    static double pravdepodobnostKrizenisBorcem = 0.3;
    static double pravdpodobnostMutace = 0.2;
    static double pravdepodobnostMutaceDitete = 0.1;

    public static void initializeVariables() {

        sirka = horniLegenda.size();
        vyska = levaLegenda.size();

        //  mujInd = new Individual();
    }

    static void readInput() {

        Scanner in = null;
        Scanner radeksc;

        try {
            in = new Scanner(new FileReader(jmenoVstupu));
        } catch (FileNotFoundException ex) {
            System.out.println("Nemuzu najit soubor " + jmenoVstupu);
        }

        String radek = in.nextLine(); //"radky"

        ArrayList<Integer> novyRadek;

        while (true) {
            radek = in.nextLine();

            if (radek.startsWith("sloupce")) break;

            novyRadek = new ArrayList<>();

            radeksc = new Scanner(radek);

            while (radeksc.hasNextInt()) {
                novyRadek.add(radeksc.nextInt());
            }
            levaLegenda.add(novyRadek);
        }

        while (in.hasNext()) {
            radek = in.nextLine();
            novyRadek = new ArrayList<>();

            radeksc = new Scanner(radek);
            while (radeksc.hasNextInt()) {
                novyRadek.add(0, radeksc.nextInt());
            }
            horniLegenda.add(novyRadek);
        }

    }

    static Individual krizeni(Individual a, Individual b) {

        Individual c = new Individual();

        int bodZlomu = (int) (Math.random() * vyska - 2) + 1;

        for (int i = 0; i < bodZlomu; i++) {
            c.velikostiMezer.add(new ArrayList<>(a.velikostiMezer.get(i)));
        }

        for (int i = bodZlomu; i < vyska; i++) {
            c.velikostiMezer.add(new ArrayList<>(b.velikostiMezer.get(i)));
        }

        c.vyplnCelouTajenkuPodleLegendyAMezer();

        return c;
    }

    static Individual krizeni2(Individual a, Individual b) {

        Individual c = new Individual();
        Individual swap;

        int velikostBloku = (int) (Math.random() * (vyska / 2.0) - 1) + 2;

        for (int i = 0; i < vyska; i++) {

            if (i % velikostBloku == 0) {
                swap = a;
                a = b;
                b = swap;
            }
            c.velikostiMezer.add(new ArrayList<>(a.velikostiMezer.get(i)));
        }

        c.vyplnCelouTajenkuPodleLegendyAMezer();

        return c;
    }

    public static void statistiky(ArrayList<Individual> populace, int generace) {

        double prumernyFitness;
        int nejvyssiFitness = populace.get(0).fitness;
        int nejnizsiFitness = populace.get(0).fitness;

        int suma = 0;

        for (Individual i : populace) {
            suma += i.fitness;

            if (i.fitness > nejvyssiFitness) {
                nejvyssiFitness = i.fitness;
            }
            if (i.fitness < nejnizsiFitness) {
                nejnizsiFitness = i.fitness;
            }
        }

        prumernyFitness = suma / populace.size();

        System.out.println("Generace ;" + generace + ";     Prumer: ;" + prumernyFitness + ";     NEJLEPSI:; " + nejvyssiFitness + ";     Nejhorsi:; " + nejnizsiFitness);
    }

    public static Set<Individual> selectParents(ArrayList<Individual> populace) {

        Set<Individual> rodice = new HashSet<>();

        // tournamentovou metodou vyberu nove rodice
        while (rodice.size() != velikostSelekce) {

            Individual a = populace.get((int) (Math.random() * velikostPopulace));
            Individual b = populace.get((int) (Math.random() * velikostPopulace));

            if (a.fitness > b.fitness)
                rodice.add(a);
            else rodice.add(b);
        }

        return rodice;
    }

    public static ArrayList<Individual> initPopulation() {

        ArrayList<Individual> p = new ArrayList<>();

        for (int i = 0; i < velikostPopulace; i++) {

            Individual j = new Individual();

            for (int k = 0; k < vyska; k++) {
                j.zmutujRadek();
            }

            j.spoctiANastavFitness();

            if (j.fitness == 0) {
                j.fitness = j.spoctiFitness();
            }

            p.add(j);
        }

        return p;
    }

    public static void main(String[] args) {

        horniLegenda = new ArrayList<>(sirka);
        levaLegenda = new ArrayList<>(vyska);

        readInput();

        initializeVariables();

        ArrayList<Individual> populace = initPopulation();
        Collections.sort(populace);
        long startTime = System.nanoTime();
        ////////////// VIVA LA EVOLUCION

        for (int g = 0; g < 100000; g++) {

            // vyselektuju rodice
            ArrayList<Individual> rodiceAsArray = new ArrayList<>(selectParents(populace));

            // nejlepsiho jedince zachovam
            Individual nejlepsiBorec = populace.get(0);

            if(g%30 == 29) {
                System.out.println("nej pred " + nejlepsiBorec.fitness);

                //nejlepsiBorec = nejlepsiBorec.localOptimalization();

                nejlepsiBorec.localOptimalization2();
                System.out.println("nej po " + nejlepsiBorec.fitness);

            }

            // vytvorim deti - dva nahodni rodice
            ArrayList<Individual> offspring = new ArrayList<>();

            // deti nahodnych rodicu vybranych tournament metodou
            for (int i = 0; i < pocetDeti; i++) {
                offspring.add(
                        krizeni(rodiceAsArray.get((int) (Math.random() * rodiceAsArray.size())),
                                rodiceAsArray.get((int) (Math.random() * rodiceAsArray.size()))));
            }

            for (int i = 1; i < velikostPopulace; i++) {
                if (Math.random() < pravdepodobnostKrizenisBorcem)
                    offspring.add(krizeni(nejlepsiBorec, populace.get(i)));
            }

            // zmutuju vsechny deti
            for (Individual dite : offspring) {

                dite.spoctiANastavFitness();

//                    if (dite.fitness == 0) {
//                        System.out.println("** MAM RESENI v generaci " + g);
//                        dite.printPole();
//                        return;
//                    }
                if (Math.random() < pravdepodobnostMutaceDitete) continue; // nemutuju vsechny.

                dite.zmutujRadek();
                dite.spoctiANastavFitness();
            }

            // nahodne zmutuju cast stare populace  - nekrizim
            for (Individual i : populace) {
                if (i != nejlepsiBorec && Math.random() < pravdpodobnostMutace) {
                    i.zmutujRadek();
                    i.spoctiANastavFitness();
                    offspring.add(i);
                }
            }

            offspring.add(nejlepsiBorec);
            populace = (offspring);

            Collections.sort(populace);

            if (offspring.get(0).fitness == 0) {
                System.out.println("MAM RESENI v generaci " + g);
                offspring.get(0).printPole();
                break;
            }

            for (int i = populace.size() - 1; i >= velikostPopulace; i--) {
                populace.remove(i);
            }

            if (g % 50 == 0) {
                statistiky(populace, g);
                System.out.println();
            }


        }

        System.out.println(" V case " +(double)(System.nanoTime() - startTime) / 1000000000.0);
        System.out.println("fitness spocteno " + fitnessCounted);

//        while (true) {
//
//            initializeVariables();
//
//            System.out.println("soucasny fitness:" + mujInd.soucasnyFitness);
//            mujInd.fitnessKandidata = mujInd.soucasnyFitness;
//
//            // opakovani optimalizace
//            for (int p = 0; p < 300000 * nasobekIteraci; p++) {
//
//                mujInd.vyberRadekAPrehazejHo();
//
//                //  System.out.println(mujInd.fitnessKandidata);
//
//
//                if (mujInd.lepsiFitness()) {
//                    if (mujInd.fitnessKandidata > mujInd.soucasnyFitness)
//                        iteraciBezZlepseni = 0;
//
//                    if (mujInd.fitnessKandidata > mujInd.nejlepsiFitnessEver) {
//                        mujInd.nejlepsiFitnessEver = mujInd.fitnessKandidata;
//                    }
//
//                    if (nejlepsiMezery != null) {
//                        mujInd.velikostiMezer.set(mujInd.ZmenenyRadek, CopyArray(nejlepsiMezery));
//                        mujInd.VyplnRadekTajenky(mujInd.ZmenenyRadek, mujInd.velikostiMezer.get(mujInd.ZmenenyRadek));
//                        mujInd.soucasnyFitness = mujInd.fitnessKandidata;
//                    }
//
//                    if (mujInd.fitnessKandidata == 0) {
//                        System.out.println("MAM SPRAVNY NONOGRAM!!!");
//                        break;
//                    }
//
//                } else {
//                    if (iteraciBezZlepseni < 200)
//                        iteraciBezZlepseni++;
//                }
//
//                if (iteraci % 1000 == 0) {
//                    System.out.println(iteraci + ". KOLO. Restartu: " + restartu + " Fitness: "
//                            + mujInd.soucasnyFitness + " Iteraci bez zlepseni " + iteraciBezZlepseni);
//                }
//
//                if ((p > 50000 * nasobekIteraci && mujInd.soucasnyFitness < -40) ||
//                        (p > 75000 * nasobekIteraci && mujInd.soucasnyFitness < -30) ||
//                        (p > 150000 * nasobekIteraci && mujInd.soucasnyFitness < -20)) {
//                    System.out.println();
//                    System.out.println();
//                    break;
//                }
//
//                iteraci++;
//            }
//
//            if (mujInd.fitnessKandidata == 0) {
//                mujInd.printPole();
//                return;
//            }
//
//            restartu++;
//
//        }
    }


}
