package com.company;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    static ArrayList<ArrayList<Integer>> horniLegenda;
    static ArrayList<ArrayList<Integer>> levaLegenda;

    static int vyska, sirka;
    final static int ITERS = 5;
    final static int ISLANDS = 5;
    final static int GENERATIONS = 1000000; // na tomto cisle nezalezi
    final static double CROSSINTERVAL = 0.001;
    final static double CATASTROPHY = 0.0002;
    final static boolean VIZ = false;
    final static boolean CROWDING = true;
    static int fitnessCounted;
    final static int fitnessCountCeil = 200 * 50000; // 200 ohodnoceni ~ 1 generace

    static void readInput(String jmenoVstupu) {

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

    public static void main(String[] args) {

        horniLegenda = new ArrayList<>();
        levaLegenda = new ArrayList<>();

        readInput("50x50.txt");

        sirka = horniLegenda.size();
        vyska = levaLegenda.size();


        for (int iterace = 0; iterace < ITERS; iterace++) {

            System.out.println(iterace + ". " + new SimpleDateFormat("HH:mm:ss").format(new Date()));

            fitnessCounted = 0;

            Island[] Islands = new Island[ISLANDS];
            for (int i = 0; i < ISLANDS; i++) {
                Islands[i] = new Island(i);
            }

            for (int g = 0; g < GENERATIONS; g++) {

                if (g % 2000 == 0) System.out.println("generace " + g);

                if (fitnessCountCeil < fitnessCounted) {
                    break;
                }

                for (int i = 0; i < ISLANDS; i++) {

                    if (fitnessCountCeil < fitnessCounted) {
                        break;
                    }

                    if (CROWDING) {
                        Islands[i].optimiseCrowd(g);
                    } else {
                        Islands[i].optimise(g);
                    }

                    // prenos na ostatni ostrovy
                    if (Math.random() < CROSSINTERVAL && 1 < ISLANDS ) {
                        System.out.println("Prenos z " + i);
                        for (int j = 0; j < ISLANDS; j++) {
                            if (i == j) continue;
                            Islands[j].populace.add(new Individual(Islands[i].nejlepsiBorec));
                            Collections.sort(Islands[j].populace);
                        }
                    }

                    // katastrofa - smazu nahodnou pulku populace a nejlepsiho
                    if (Math.random() < CATASTROPHY) {

                        System.out.println("Katastrofa " + i);

                        if (!CROWDING) {

                            Islands[i].populace.remove(0);

                            for (int j = 0; j < Island.velikostPopulace / 2; j++) {
                                int rem = (int) (Math.random() * Islands[i].populace.size());
                                Islands[i].populace.remove(rem);
                            }
                        }

                        for (int j = 0; j < Islands[i].populace.size(); j++) {
                            for (int k = 0; k < 5; k++) {
                                Islands[i].populace.get(j).zmutuj();
                            }
                            Islands[i].populace.get(j).spoctiANastavFitness();
                        }

                    }

                }

            }
            double sum = 0;
            double sum2 = 0;

            for (int i = 0; i < ISLANDS; i++) {
                sum += Islands[i].bestScore;
                sum2 += Islands[i].nejlepsiBorec.fitness;
                System.out.println(Islands[i].bestScore);
            }

            System.out.println("Best Avg: " + sum / ISLANDS);
            System.out.println("Current Avg: " + sum2 / ISLANDS);

        }
        System.out.println(fitnessCounted);

    }


}
