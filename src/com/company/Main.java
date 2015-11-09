package com.company;

import sun.java2d.loops.CompositeType;

import javax.naming.InitialContext;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {

    // static Individual mujInd;

    static ArrayList<ArrayList<Integer>> horniLegenda;
    static ArrayList<ArrayList<Integer>> levaLegenda;

    //  static ArrayList<Integer> nejlepsiMezery;

    static int nasobekIteraci = 8;

    static int vyska, sirka;

    static int iteraciBezZlepseni;

    static String jmenoVstupu = "25x20.txt";

    public static void initializeVariables() {

        iteraciBezZlepseni = 10;

        sirka = horniLegenda.size();
        vyska = levaLegenda.size();


        //  mujInd = new Individual();
    }

    public static ArrayList<Integer> CopyArray(ArrayList<Integer> source) {

        ArrayList<Integer> target = new ArrayList<>();

        for (int i = 0; i < source.size(); i++) {
            target.add(new Integer(source.get(i)));
        }
        return target;
    }

    static void readInput() {

        Scanner in = null;
        Scanner radeksc = null;

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

            novyRadek = new ArrayList<Integer>();

            radeksc = new Scanner(radek);

            while (radeksc.hasNextInt()) {
                novyRadek.add(radeksc.nextInt());
            }
            levaLegenda.add(novyRadek);
        }

        while (in.hasNext()) {
            radek = in.nextLine();
            novyRadek = new ArrayList<Integer>();

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
            c.velikostiMezer.add(CopyArray(a.velikostiMezer.get(i)));
        }

        for (int i = vyska / 2; i < vyska; i++) {
            c.velikostiMezer.add(CopyArray(b.velikostiMezer.get(i)));
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

        System.out.println("Prumer " + prumernyFitness + " nejvyssi " + nejvyssiFitness + " nejnizsi " + nejnizsiFitness + " velikost " + populace.size());

    }


    public static int biggestOfThree(int a, int b , int c){

        if(a < b){
            a = b;
        }

        if(a > c){
            return a;
        }
        return c;

    }

    public static void main(String[] args) {

        horniLegenda = new ArrayList<>(sirka);
        levaLegenda = new ArrayList<>(vyska);

        readInput();
        initializeVariables();

        int restartu = 0;
        int iteraci = 0;

        int velikostPopulace = 100;
        int velikostSelekce = 50;
        int pocetDeti = 100;


        ArrayList<Individual> populace = new ArrayList<>();
        ArrayList<Individual> rodice = new ArrayList<>();

        for (int i = 0; i < velikostPopulace; i++) {

            Individual j = new Individual();
            j.basicInit();
            j.zmutujRadek();
            if (j.fitness == 0) j.fitness = j.spoctiFitness();

            populace.add(j);
        }

        for (int g = 0; g < 3000; g++) {

            System.out.println("GENERACE " + g);

            // zmutuju veskerou populaci
            for (int i = 0; i < velikostPopulace; i++) {
                populace.get(i).zmutujRadek();
            }

            Collections.sort(populace);

            System.out.println("zmutovana populace");
            statistiky(populace);

            // vezmu nekolik nejlepsich jedincu
            for (int i = 0; i < velikostSelekce; i++) {
                rodice.add(populace.get(i));
            }

            // vytvorim deti z nejlepsich jedincu
            for (int i = 0; i < pocetDeti; i++) {

                int j = (int) (Math.random() * velikostSelekce/3);
                int k = (int) (Math.random() * velikostSelekce/3);

                Individual dite = krizeni(populace.get(j), populace.get(k));

                dite.vyplnCelouTajenkuPodleLegendyAMezer();
                dite.fitness = dite.spoctiFitness();

                rodice.add(dite);
            }

            System.out.println("rodice a deti ");
            statistiky(rodice);
//
//        for (int i = 0; i < rodice.size(); i++) {
//            System.out.println(rodice.get(i).fitness);
//        }

            Collections.sort(rodice);

            ArrayList<Individual> novaPopulace = new ArrayList<>();

            // oriznu populaci na
//            for (int i = 0; i < velikostPopulace; i++) {
//                novaPopulace.add(rodice.get(i));
//            }

          //  rodice.clear();

          //  System.out.println("Nova generace");
          //  statistiky(novaPopulace);

            populace = rodice;//novaPopulace;
            rodice = new ArrayList();
            System.out.println();

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
