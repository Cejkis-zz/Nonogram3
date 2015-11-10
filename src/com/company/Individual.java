package com.company;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by cejkis on 8.11.15.
 */


public class Individual implements  Comparable<Individual>{

    ArrayList<ArrayList<Integer>> velikostiMezer;
    //Integer ZmenenyRadek;
    boolean[][] tajenka;
    int fitness;
    //int nejlepsiFitnessEver;
    //int fitnessKandidata;

    public Individual() {

      //  nejlepsiFitnessEver = -500;

        tajenka = new boolean[Main.vyska][Main.sirka];
        velikostiMezer = new ArrayList<>();

       // foucasnyFitness = spoctiFitness();
    }

    public void basicInit(){

        for (int i = 0; i < Main.vyska; i++) {
            velikostiMezer.add(new ArrayList<Integer>());
        }

        vytvorMezery();
        vyplnCelouTajenkuPodleLegendyAMezer();

    }

    public void printPole() {

        for (int i = 0; i < Main.vyska; i++) {
            printRadek(i, velikostiMezer.get(i));
        }

    }

    public void printRadek(int i, ArrayList<Integer> Mezery) {

        ArrayList<Integer> policka = Main.levaLegenda.get(i);
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

    public void vytvorMezery() {

        ArrayList<Integer> VelikostiPoli;
        ArrayList<Integer> Mezery;
        int velikost;

        for (int i = 0; i < Main.vyska; i++) { // i je radek

            VelikostiPoli = Main.levaLegenda.get(i);
            Mezery = velikostiMezer.get(i);

            if (VelikostiPoli.isEmpty()) { // nebo 1? Nejspis nikdy nenastane
                Mezery.add(Main.sirka);
            } else {
                velikost = 0;

                for (Integer aVelikostiPoli : VelikostiPoli) { //j je poradi cisla v radku
                    velikost += aVelikostiPoli;
                }

                velikost += VelikostiPoli.size() - 1;
                double zbytek = Main.sirka - velikost;
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

    // spocte sumu needlemanu vsech sloupcu
    public int spoctiFitness() {
        int suma = 0;

        for (int i = 0; i < Main.sirka; i++) {
            suma += needlemanWunch(Main.horniLegenda.get(i), arraylistFromPole(i));

            // uz nepotrebuju - zvyseni
            //suma += needlemanWunch(horniLegenda.get(i), arraylistFromPole(i)) * (Math.abs(Math.pow(i - sirka / 2, 2))+1)  ;
        }
        return suma;
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
    public ArrayList<Integer> arraylistFromPole(int sloupec) {

        ArrayList<Integer> a = new ArrayList<>();
        int kombo = 0;

        for (int i = 0; i < Main.vyska; i++) {
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

    ArrayList<Integer> najdiCoMuzuUbrat(int radek) {

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

    public void prehodJednumezeruVJednomRadku(int radek, ArrayList<Integer> mezeryKtereMenim) {

        int i, j, kolikuberu, indexZeKterehoUbiram;

        ArrayList<Integer> IndexyMezerKtereMuzuUbrat = najdiCoMuzuUbrat(radek);

        if (IndexyMezerKtereMuzuUbrat.isEmpty()) return;

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

    // vyber nahodny radek, nahodnekrat v nem prehazej mezery
//    public void vyberRadekAPrehazejHo() {
//
//        ZmenenyRadek = (int) (Math.random() * Main.vyska);
//        ArrayList<Integer> mezeryKtereMenim;
//
//        ArrayList<Integer> zalohaMezer =  Main.CopyArray(velikostiMezer.get(ZmenenyRadek));
//
//        mezeryKtereMenim = velikostiMezer.get(ZmenenyRadek);
//
//        if (mezeryKtereMenim.size() == 1) return;
//
//        int kolikrat = (int) (Math.random() * (3 + Main.iteraciBezZlepseni / 20)) + 2;
//
//        // kolikrat prehazim mezery v ramci jednoho radku
//        for (int l = 0; l < kolikrat; l++) {
//            prehodJednumezeruVJednomRadku(ZmenenyRadek, mezeryKtereMenim);
//        }
//
//        // vypln tajenku a spocti fitness
//        VyplnRadekTajenky(ZmenenyRadek, mezeryKtereMenim);
//        fitnessKandidata = spoctiFitness();
//
//        Main.nejlepsiMezery = mezeryKtereMenim;
//
//       // System.out.println(ZmenenyRadek);
//
//       // printRadek(ZmenenyRadek, velikostiMezer.get(ZmenenyRadek));
//
//        // vrat puvodni hodnoty
//        velikostiMezer.set(ZmenenyRadek, zalohaMezer);
//        VyplnRadekTajenky(ZmenenyRadek, zalohaMezer);
//
//    }

    public void zmutujRadek() {

        int zmenenyRadek = (int) (Math.random() * Main.vyska);
        ArrayList<Integer> mezeryKtereMenim = velikostiMezer.get(zmenenyRadek);

        if (mezeryKtereMenim.size() <= 1) return;

        // int kolikrat = (int) (Math.random() * (3 + Main.iteraciBezZlepseni / 20)) + 2;
        int kolikrat = mezeryKtereMenim.size();

        for (int l = 0; l < kolikrat; l++) {
            prehodJednumezeruVJednomRadku(zmenenyRadek, mezeryKtereMenim);
        }

        // vypln tajenku a spocti fitness
        VyplnRadekTajenky(zmenenyRadek, mezeryKtereMenim);


    }

    public void spoctiANastavFitness(){

        fitness = spoctiFitness();

    }


//    public boolean lepsiFitness(){
//
//        return (fitnessKandidata >= soucasnyFitness - 2 && fitnessKandidata >= nejlepsiFitnessEver - 4 && fitnessKandidata < -20)
//                || (fitnessKandidata >= soucasnyFitness - 2 && fitnessKandidata >= nejlepsiFitnessEver - 2 && fitnessKandidata < -4)
//                || (fitnessKandidata >= -4 && fitnessKandidata >= soucasnyFitness);
//
//    }

    // Vyplni mezery, mezery 1 a vycentrovana doprostred
    public void vyplnCelouTajenkuPodleLegendyAMezer() {

        for (int i = 0; i < Main.vyska; i++) { // i je radek
            VyplnRadekTajenky(i, velikostiMezer.get(i));
        }
    }

    public void VyplnRadekTajenky(int radek, ArrayList<Integer> mezeryVRadku) {

        ArrayList<Integer> polickaVRadku = Main.levaLegenda.get(radek);

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

    @Override
    public int compareTo(Individual individual) {
        return individual.fitness - fitness;
    }
}