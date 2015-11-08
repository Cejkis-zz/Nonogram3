package com.company;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static Individual mujInd;

    static ArrayList<ArrayList<Integer>> horniLegenda;
    static ArrayList<ArrayList<Integer>> levaLegenda;

    static ArrayList<Integer> nejlepsiMezery;

    static int nasobekIteraci = 8;

    static int vyska, sirka;

    static int iteraciBezZlepseni;

    static String jmenoVstupu = "23x26.txt";

    public static void initializeVariables() {

        iteraciBezZlepseni = 0;

        sirka = horniLegenda.size();
        vyska = levaLegenda.size();

        mujInd = new Individual();
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


    public static void main(String[] args) {

        horniLegenda = new ArrayList<>(sirka);
        levaLegenda = new ArrayList<>(vyska);

        readInput();

        int restartu = 0;
        int iteraci = 0;

        while (true) {

            initializeVariables();

            System.out.println("soucasny fitness:" + mujInd.soucasnyFitness);
            mujInd.fitnessKandidata = mujInd.soucasnyFitness;

            int fitnessKandidata = mujInd.fitnessKandidata;
            int soucasnyFitness = mujInd.soucasnyFitness;
            int nejlepsiFitnessEver = mujInd.nejlepsiFitnessEver;

            // opakovani optimalizace
            for (int p = 0; p < 300000 * nasobekIteraci; p++) {

                mujInd.zmutujRadek();

                if ((fitnessKandidata >= soucasnyFitness - 2  && fitnessKandidata >= nejlepsiFitnessEver - 4  && fitnessKandidata < -20 )
                || (fitnessKandidata >= soucasnyFitness - 2  && fitnessKandidata >= nejlepsiFitnessEver -2  && fitnessKandidata < -4 )
                        || (fitnessKandidata >= -4 && fitnessKandidata >= soucasnyFitness)
                        ) {
                    if (fitnessKandidata > soucasnyFitness)
                        iteraciBezZlepseni = 0;

                    if(fitnessKandidata > nejlepsiFitnessEver){
                        nejlepsiFitnessEver = fitnessKandidata;
                    }

                    if (nejlepsiMezery != null) {
                        mujInd.velikostiMezer.set(mujInd.ZmenenyRadek, CopyArray(nejlepsiMezery));
                        mujInd.VyplnRadekTajenky(mujInd.ZmenenyRadek, mujInd.velikostiMezer.get(mujInd.ZmenenyRadek));
                        soucasnyFitness = fitnessKandidata;
                    }

                    if (fitnessKandidata == 0) {
                        System.out.println("MAM SPRAVNY NONOGRAM!!!");
                        break;
                    }

                } else if (iteraciBezZlepseni < 200)
                    iteraciBezZlepseni++;


                if (iteraci % 1000 == 0) {
                    System.out.println(iteraci + ". KOLO. Restartu: " + restartu + " Fitness: "
                            + mujInd.soucasnyFitness + " Iteraci bez zlepseni " + iteraciBezZlepseni);
                }

                if ((p > 50000 * nasobekIteraci && mujInd.soucasnyFitness < -40) ||
                        (p > 75000 * nasobekIteraci && mujInd.soucasnyFitness < -30) ||
                        (p > 150000 * nasobekIteraci && mujInd.soucasnyFitness < -20)) {
                    System.out.println();
                    System.out.println();
                    break;
                }

                iteraci++;
            }

            if (mujInd.fitnessKandidata == 0) {
                mujInd.printPole();
                return;
            }

            restartu++;

        }
    }
}
