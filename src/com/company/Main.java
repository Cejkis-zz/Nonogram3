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

    static int velikostPopulace = 100;
    static int velikostSelekce = 50;
    static int pocetDeti = 100;

    public static void initializeVariables() {

        iteraciBezZlepseni = 10;

        sirka = horniLegenda.size();
        vyska = levaLegenda.size();

        //  mujInd = new Individual();
    }

//    public static ArrayList<Integer> CopyArray(ArrayList<Integer> source) {
//
//        ArrayList<Integer> target = new ArrayList<>();
//
//        for (int i = 0; i < source.size(); i++) {
//            target.add(new Integer(source.get(i)));
//        }
//        return target;
//    }

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
        Individual e,f;

        double rand = Math.random();

        if(rand > 0.5){
            e = a;
            f = b;
        } else {
            e = b;
            f = a;
        }

        for (int i = 0; i < vyska / 2; i++) {
            //c.velikostiMezer.add(CopyArray(a.velikostiMezer.get(i)));
            c.velikostiMezer.add(new ArrayList<>(e.velikostiMezer.get(i)));
        }

        for (int i = vyska / 2; i < vyska; i++) {
            //  c.velikostiMezer.add(CopyArray(b.velikostiMezer.get(i)));
            c.velikostiMezer.add(new ArrayList<>(f.velikostiMezer.get(i)));
        }

        c.vyplnCelouTajenkuPodleLegendyAMezer();

        return c;

    }

    public static void statistiky(ArrayList<Individual> populace) {

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

        System.out.println("-----------------Prumer " + prumernyFitness + " nejlepsi " + nejvyssiFitness + " nejhorsi " + nejnizsiFitness + " velikost " + populace.size());

    }

    public static int biggestOfThree(int a, int b, int c) {

        if (a < b) {
            a = b;
        }

        if (a > c) {
            return a;
        }
        return c;

    }

    public static Set<Individual> selectParents(ArrayList<Individual> populace) {

        Set<Individual> rodice = new HashSet<>();

        // tournamentovou metodou vyberu nove rodice
        for (int i = 0; i < velikostSelekce; i++) {

            Individual a = populace.get((int) (Math.random() * velikostPopulace));
            Individual b = populace.get((int) (Math.random() * velikostPopulace));

            if (a.fitness > b.fitness)
                rodice.add(a);
            else rodice.add(b);
        }

        return rodice;
    }

    public static ArrayList<Individual> initPopulation(){

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

        //  int restartu = 0;
        //  int iteraci = 0;

        ArrayList<Individual> populace = initPopulation();

        ////////////// VIVA LA EVOLUCION

        for (int g = 0; g < 10000; g++) {

                       // vyselektuju rodice
            ArrayList<Individual> rodiceAsArray = new ArrayList<>(selectParents(populace));

            // vytvorim deti - dva nahodni rodice
            ArrayList<Individual> offspring = new ArrayList<>();

            for (int i = 0; i < pocetDeti; i++) {

                int j = (int) (Math.random() * rodiceAsArray.size());
                int k = (int) (Math.random() * rodiceAsArray.size());

                Individual dite = krizeni(rodiceAsArray.get(j), rodiceAsArray.get(k));

                dite.vyplnCelouTajenkuPodleLegendyAMezer();
                dite.fitness = dite.spoctiFitness();

                offspring.add(dite);
            }

            // nejlepsiho jedince zachovam
            Individual nejlepsiBorec = populace.get(0);

            offspring.add(nejlepsiBorec);

            for (int i = 1; i < velikostPopulace; i++) {

                Individual dite = krizeni(nejlepsiBorec, populace.get(i));

                dite.vyplnCelouTajenkuPodleLegendyAMezer();
                dite.fitness = dite.spoctiFitness();

                offspring.add(dite);
            }

            // zmutuju vsechny deti
            for (int i = 0; i < offspring.size(); i++) {
                offspring.get(i).zmutujRadek();
                offspring.get(i).spoctiANastavFitness();
            }

          //  populace.addAll(offspring);

            populace = offspring;

            Collections.sort(populace);

           // ArrayList<Individual> NovaPopulace = new ArrayList<>();

            for (int i = populace.size() -1 ; i >= velikostPopulace; i--) {
                populace.remove(i);
            }

//            for (int i = 0; i < velikostPopulace; i++) {
//                NovaPopulace.add(populace.get(i));
//            }

            //statistiky(NovaPopulace);

           if(g%20 == 0) {
               System.out.println("GENERACE " + g);
               statistiky(populace);
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
}
