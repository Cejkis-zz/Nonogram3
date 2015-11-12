package com.company;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Main {


    static ArrayList<ArrayList<Integer>> horniLegenda;
    static ArrayList<ArrayList<Integer>> levaLegenda;

    //  static ArrayList<Integer> nejlepsiMezery;

    //  static int nasobekIteraci = 8;

    static int vyska, sirka;

    static int iteraciBezZlepseni;

    static String jmenoVstupu = "25x20.txt";

    static HashMap<ArrayList<ArrayList<Integer>>, Integer> tabu = new HashMap<>();

    static int velikostPopulace = 100;
    static int velikostSelekce = 50;
    static int pocetDeti = 100;

    public static void initializeVariables() {

        iteraciBezZlepseni = 10;

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

        for (int i = 0; i < vyska / 2; i++) {
            c.velikostiMezer.add(new ArrayList<>(a.velikostiMezer.get(i)));
        }

        for (int i = vyska / 2; i < vyska; i++) {
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

        System.out.println("Generace " + generace + "   Prumer: " + prumernyFitness + "   NEJLEPSI: " + nejvyssiFitness + "    Nejhorsi: " + nejnizsiFitness + "    Velikost: " + populace.size());
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
            j.basicInit();

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

        ////////////// VIVA LA EVOLUCION

        for (int g = 0; g < 10000; g++) {

            // vyselektuju rodice
            ArrayList<Individual> rodiceAsArray = new ArrayList<>(selectParents(populace));

            // nejlepsiho jedince zachovam
            Individual nejlepsiBorec = populace.get(0);

            // vytvorim deti - dva nahodni rodice
            ArrayList<Individual> offspring = new ArrayList<>();

            for (int i = 0; i < pocetDeti; i++) {

                offspring.add(
                        krizeni(rodiceAsArray.get((int) (Math.random() * rodiceAsArray.size())),
                                rodiceAsArray.get((int) (Math.random() * rodiceAsArray.size()))));
            }

            for (int i = 1; i < velikostPopulace; i++) {

                //   if(Math.random() > 0.8) continue;

                offspring.add(
                        krizeni(nejlepsiBorec, populace.get(i)));
            }

            offspring.add(nejlepsiBorec);

            // zmutuju vsechny deti
            for (Individual dite : offspring) {

                dite.spoctiANastavFitness();

                if (dite.fitness == 0) {
                    System.out.println("** MAM RESENI v generaci " + g);
                    dite.printPole();
                    return;
                }

                //do
                dite.zmutujRadek();
                // while (isInTabu(dite));

                dite.spoctiANastavFitness();

            }

            // if(!isInTabu(nejlepsiBorec)){
            //     offspring.add(nejlepsiBorec);
            // }else System.out.println("TABU HIT borec");

            populace = offspring;

            Collections.sort(populace);

            if (offspring.get(0).fitness == 0) {
                System.out.println("MAM RESENI v generaci " + g);
                offspring.get(0).printPole();
                return;
            }


            for (int i = populace.size() - 1; i >= velikostPopulace; i--) {
                populace.remove(i);
            }

            if (g % 100 == 0) {
                statistiky(populace, g);
                System.out.println();
            }
        }

//        while (true) {
//
//            initializeVariables();
//
//            System.out.println("soucasny fitness:" + mujInd.soucasnyFitness);
//            mujInd.fitnessKandidata = mujInd.soucasnyFitness;
//
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

    private static boolean isInTabu(Individual dite) {

        Integer tabuKolik = tabu.get(dite.velikostiMezer);

        if (tabuKolik == null) {
            tabu.put(dite.velikostiMezer, 0);
            tabuKolik = 0;
        }

        if (tabuKolik > 30) {
            return true;
        }

        tabu.put(dite.velikostiMezer, tabuKolik + 1);

        return false;
    }
}
