package com.company;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static ArrayList<ArrayList<Integer>> horniLegenda;
    static ArrayList<ArrayList<Integer>> levaLegenda;
    static ArrayList<ArrayList<Integer>> velikostiMezer;

    static Integer ZmenenyRadek;
    static ArrayList<Integer> nejlepsiMezery;

    static boolean[][] tajenka;

    static int nasobekIteraci = 8;

    static int vyska, sirka;

    static int soucasnyFitness;
    static int nejlepsiFitnessEver;
    static int fitnessKandidata;

    static int iteraciBezZlepseni;

    static String jmenoVstupu = "23x26.txt";

    public static void printPole() {

        for (int i = 0; i < vyska; i++) {
            printRadek(i, velikostiMezer.get(i));
        }
    }

    public static void printRadek(int i, ArrayList<Integer> Mezery) {

        ArrayList<Integer> policka = levaLegenda.get(i);
        //  if(i<10){System.out.print(" " + i );}else{
        //       System.out.print("" + i );
        //   }

        for (int j = 0; j < Mezery.get(0); j++) {
            System.out.print("  ");
        }

        for (int j = 0; j < policka.size(); j++) {

            // vytisknu jedno policko
            for (int k = 0; k < policka.get(j); k++) {
                System.out.print("##");
            }

            for (int k = 0; k < Mezery.get(j + 1); k++) {
                System.out.print("  ");
            }
        }

        System.out.print("|");
        System.out.println();
    }

    public static void initializeVariables() {

        iteraciBezZlepseni = 0;
        nejlepsiFitnessEver = -500;

        sirka = horniLegenda.size();
        vyska = levaLegenda.size();
        tajenka = new boolean[vyska][sirka];
        velikostiMezer = new ArrayList<>();


        for (int i = 0; i < vyska; i++) {
            velikostiMezer.add(new ArrayList<Integer>());
        }
    }

    // Vyplni mezery, mezery 1 a vycentrovana doprostred
    public static void vytvorMezery() {

        ArrayList<Integer> VelikostiPoli;
        ArrayList<Integer> Mezery;
        int velikost;

        for (int i = 0; i < vyska; i++) { // i je radek

            VelikostiPoli = levaLegenda.get(i);
            Mezery = velikostiMezer.get(i);

            if (VelikostiPoli.isEmpty()) { // nebo 1? Nejspis nikdy nenastane
                Mezery.add(sirka);
            } else {
                velikost = 0;

                for (Integer aVelikostiPoli : VelikostiPoli) { //j je poradi cisla v radku
                    velikost += aVelikostiPoli;
                }

                velikost += VelikostiPoli.size() - 1;
                double zbytek = sirka - velikost;
                Mezery.add((int) Math.ceil(zbytek / 2));

                for (int j = 0; j < VelikostiPoli.size() - 1; j++) {
                    Mezery.add(1);
                }
                Mezery.add((int) Math.floor(zbytek / 2));
            }
            if (VelikostiPoli.size() + 1 != Mezery.size()) {

                System.out.println("ALERT policek a mezer" + VelikostiPoli.size() + "," + Mezery.size());
            }
        }

    }

    public static void vyplnCelouTajenkuPodleLegendyAMezer() {

        for (int i = 0; i < vyska; i++) { // i je radek
            VyplnRadekTajenky(i, velikostiMezer.get(i));
        }
    }

    public static void VyplnRadekTajenky(int radek, ArrayList<Integer> mezeryVRadku) {

        ArrayList<Integer> polickaVRadku = levaLegenda.get(radek);

        int pointer = 0; // ukazatel na pozici, kterou menim

        for (int j = 0; j < mezeryVRadku.get(0); j++) { // prvni mezera
            tajenka[radek][pointer] = false;
            pointer++;
        }

        //   System.out.println( radek + " * " + polickaVRadku.size() + " " + mezeryVRadku.size());

        for (int j = 0; j < polickaVRadku.size(); j++) { // pro vsehna policka

            for (int k = 0; k < polickaVRadku.get(j); k++) {
                tajenka[radek][pointer] = true;
                pointer++;
            }

            for (int k = 0; k < mezeryVRadku.get(j + 1); k++) {
                tajenka[radek][pointer] = false;
                pointer++;
            }
        }

    }

    // podle tajenky a legendy spocte needlemana pro jeden radek/sloupec
    public static int needlemanWunch(ArrayList<Integer> legenda, ArrayList<Integer> tajenka) {

        ArrayList<Integer> x = new ArrayList<>(legenda);
        ArrayList<Integer> y = new ArrayList<>(tajenka);

        x.add(0, 0);
        y.add(0, 0);

        int[][] H = new int[y.size()][x.size()];

        H[0][0] = 0;

        for (int i = 1; i < y.size(); i++) {
            H[i][0] = H[i - 1][0] - y.get(i);
        }

        for (int i = 1; i < x.size(); i++) {
            H[0][i] = H[0][i - 1] - x.get(i);
        }

        for (int j = 1; j < x.size(); j++) {
            for (int i = 1; i < y.size(); i++) {

                H[i][j] = Math.max(H[i - 1][j] - y.get(i),
                        Math.max(H[i][j - 1] - x.get(j),
                                H[i - 1][j - 1] - Math.abs(x.get(j) - y.get(i))));

            }
        }

        return H[y.size() - 1][x.size() - 1];
    }

    // vraci sloupec tajenky ve jako ve "sloucenem" tvaru
    public static ArrayList<Integer> arraylistFromPole(int sloupec) {

        ArrayList<Integer> a = new ArrayList<>();
        int kombo = 0;

        for (int i = 0; i < vyska; i++) {
            if (tajenka[i][sloupec]) {
                kombo++;
            } else {
                if (kombo != 0) {
                    a.add(kombo);
                }
                kombo = 0;
            }
        }

        if (kombo != 0) {
            a.add(kombo);
        }

        return a;
    }

    // spocte sumu needlemanu vsech sloupcu
    public static int spoctiFitness() {
        int suma = 0;

        for (int i = 0; i < sirka; i++) {
            suma += needlemanWunch(horniLegenda.get(i), arraylistFromPole(i));

            // uz nepotrebuju - zvyseni
            //suma += needlemanWunch(horniLegenda.get(i), arraylistFromPole(i)) * (Math.abs(Math.pow(i - sirka / 2, 2))+1)  ;
        }
        return suma;
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

    static ArrayList<Integer> najdiCoMuzuUbrat(int radek) {

        ArrayList<Integer> IndexyMezerKtereMuzuUbrat = new ArrayList<>();

        ArrayList<Integer> mezeryVAktualnimRadku = velikostiMezer.get(radek);

        // prvni mezera
        if (mezeryVAktualnimRadku.get(0) > 0) {
            IndexyMezerKtereMuzuUbrat.add(0);
        }

        // posledni mezera
        if (mezeryVAktualnimRadku.size() > 1 && mezeryVAktualnimRadku.get(mezeryVAktualnimRadku.size() - 1) > 0) {
            IndexyMezerKtereMuzuUbrat.add(mezeryVAktualnimRadku.size() - 1);
        }

        // uvnitr radku najdu mista na vkladani a vybirani
        for (int j = 1; j < mezeryVAktualnimRadku.size() - 1; j++) {

            if (mezeryVAktualnimRadku.get(j) > 1) {
                IndexyMezerKtereMuzuUbrat.add(j);
            }
        }

        return IndexyMezerKtereMuzuUbrat;
    }

    public static void prehodJednumezeruVJednomRadku(int radek, ArrayList<Integer> mezeryKtereMenim) {

        int i, j, kolikuberu, indexZeKterehoUbiram;

         ArrayList<Integer> IndexyMezerKtereMuzuUbrat = najdiCoMuzuUbrat(radek);

        if(IndexyMezerKtereMuzuUbrat.isEmpty()) return;

        do {
            i = (int) (Math.random() * IndexyMezerKtereMuzuUbrat.size());
            j = (int) (Math.random() * mezeryKtereMenim.size());
        } while (IndexyMezerKtereMuzuUbrat.get(i) == j);
        //   System.out.println(i + " * " +j );

        // zmen mezery
        indexZeKterehoUbiram = IndexyMezerKtereMuzuUbrat.get(i);

        // kdyz ubiram z prvni nebo posledni mezery, muzu ubrat vsechny policka
        if (indexZeKterehoUbiram == 0 || indexZeKterehoUbiram == mezeryKtereMenim.size() - 1) {
            kolikuberu = (int) (Math.random() * (mezeryKtereMenim.get(indexZeKterehoUbiram))) + 1;
        } else kolikuberu = (int) (Math.random() * (mezeryKtereMenim.get(indexZeKterehoUbiram) - 1)) + 1;

        mezeryKtereMenim.set(indexZeKterehoUbiram, mezeryKtereMenim.get(indexZeKterehoUbiram) - kolikuberu);
        mezeryKtereMenim.set(j, mezeryKtereMenim.get(j) + kolikuberu);


    }

    public static void prehazimMezery() {

        ZmenenyRadek = (int) (Math.random() * vyska);
        ArrayList<Integer> mezeryKtereMenim;
        ArrayList<Integer> zalohaMezer = CopyArray(velikostiMezer.get(ZmenenyRadek));

        mezeryKtereMenim = velikostiMezer.get(ZmenenyRadek);

        if(mezeryKtereMenim.size() == 1) return;

        int kolikrat = (int)(Math.random() * (3 + iteraciBezZlepseni/20) ) + 2;

        // kolikrat prehazim mezery v ramci jednoho radku
        for (int l = 0; l < kolikrat; l++) {
            prehodJednumezeruVJednomRadku(ZmenenyRadek, mezeryKtereMenim);
        }

        // vypln tajenku a spocti fitness
        VyplnRadekTajenky(ZmenenyRadek, mezeryKtereMenim);
        fitnessKandidata = spoctiFitness();

        nejlepsiMezery = mezeryKtereMenim;

        // vrat puvodni hodnoty
        velikostiMezer.set(ZmenenyRadek, zalohaMezer);
        VyplnRadekTajenky(ZmenenyRadek, zalohaMezer);

    }

    public static void main(String[] args) {

        horniLegenda = new ArrayList<>(sirka);
        levaLegenda = new ArrayList<>(vyska);

        readInput();

        int restartu = 0;
        int iteraci = 0;

        while (true) {

            initializeVariables();
            vytvorMezery();
            vyplnCelouTajenkuPodleLegendyAMezer();


            soucasnyFitness = spoctiFitness();
            System.out.println("soucasny fitness:" + soucasnyFitness);
            fitnessKandidata = soucasnyFitness;

            // opakovani optimalizace
            for (int p = 0; p < 300000 * nasobekIteraci; p++) {

                prehazimMezery();

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
                        velikostiMezer.set(ZmenenyRadek, CopyArray(nejlepsiMezery));
                        VyplnRadekTajenky(ZmenenyRadek, velikostiMezer.get(ZmenenyRadek));
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
                            + soucasnyFitness + " Iteraci bez zlepseni " + iteraciBezZlepseni);
                }

                if ((p > 50000 * nasobekIteraci && soucasnyFitness < -40) ||
                        (p > 75000 * nasobekIteraci && soucasnyFitness < -30) ||
                        (p > 150000 * nasobekIteraci && soucasnyFitness < -20)) {
                    System.out.println();
                    System.out.println();
                    break;
                }

                iteraci++;
            }

            if (fitnessKandidata == 0) {
                printPole();
                return;
            }

            restartu++;

        }
    }
}
