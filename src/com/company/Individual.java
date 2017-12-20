package com.company;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.company.Main.*;
import static jcuda.driver.JCudaDriver.*;

/**
 * Created by cejkis on 8.11.15.
 */


public class Individual implements  Comparable<Individual>{

    int[] tajenka;
    int fitness;
    int birth;

    public Individual(int g) {

        tajenka = new int[velikost];

        birth = g;
    }

    public  Individual(Individual s){

        tajenka = new int[velikost];

        for (int i = 0; i < velikost; i++) {
                tajenka[i] = s.tajenka[i];}

        fitness = s.fitness;
        birth = s.birth;

    }

    public void printRadek(int i, ArrayList<Integer> Mezery) {

        int[] policka = Main.levaLegenda[i];
        //  if(i<10){System.out.print(" " + i );}else{
        //       System.out.print("" + i );
        //   }

        for (int j = 0; j < Mezery.get(0); j++) {
            System.out.print("  ");
        }

        for (int j = 0; j < policka.length; j++) {

            // vytisknu jedno policko
            for (int k = 0; k < policka[j]; k++) {
                System.out.print("##");
            }

            for (int k = 0; k < Mezery.get(j + 1); k++) {
                System.out.print("  ");
            }
        }

        System.out.print("|");
        System.out.println();
    }

    // spocte sumu needlemanu vsech sloupcu
    public void spoctiFitness() {


        if (Main.fitnessCounted%10000 == 0){
            System.out.println( new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }

        Main.fitnessCounted ++;
        spoctiFitnessCPU();
        //spoctiFitnessGPU();

    }

    public void spoctiFitnessGPU(){

        //printPole();

        CUdeviceptr fitness_GPU = new CUdeviceptr();
        cuMemAlloc(fitness_GPU, sirka*Sizeof.INT);

        CUdeviceptr tajenka_D = new CUdeviceptr();
        cuMemAlloc(tajenka_D, tajenka.length*Sizeof.INT);
        cuMemcpyHtoD(tajenka_D, Pointer.to(tajenka),
                tajenka.length * Sizeof.INT);

        Pointer kernelParameters = Pointer.to(
                Pointer.to(tajenka_D),
                Pointer.to(fitness_GPU)
        );

        cuLaunchKernel(fitnessColumnFunction,
                sirka, 1, 1,      // Grid dimension
                1, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );

        cuCtxSynchronize();

        int hostOutput[] = new int[1];
        cuMemcpyDtoH(Pointer.to(hostOutput), fitness_GPU,
                1 * Sizeof.INT);

        fitness = hostOutput[0];

        //System.out.println("success" + ++succ);

        cuMemFree(tajenka_D);
        cuMemFree(fitness_GPU);

    }

    public int needlemanWunchP(int sloupec, final int [] legenda1D, int [] velikostiLegend, int [] posunyLegend, final boolean [] tajenka1D, int vyska ) {

        int velikostTajenky = 1;

        int [] tajenkaSloupec = new int [vyska];

        tajenkaSloupec[0] = 0;
        int kombo = 0;

        for (int i = 0; i < vyska; i++) {
            if (tajenka1D[sloupec*vyska + i]) {
                kombo++;
            } else {
                if (kombo != 0) {
                    tajenkaSloupec[velikostTajenky++] = kombo;
                }
                kombo = 0;
            }
        }

        if (kombo != 0) {
            tajenkaSloupec[velikostTajenky++] = kombo; // posledni ctverecek je cerny
        }
        
        //////////////////////////////////////////////////
        
        int velikostLegendy = velikostiLegend[sloupec];
        int zacatekLegendy = posunyLegend[sloupec];
        int[][] H = new int[velikostTajenky][velikostLegendy];

        H[0][0] = 0;

        for (int i = 1; i < velikostTajenky; i++) {
            H[i][0] = H[i - 1][0] - tajenkaSloupec[i];
        }

        for (int i = 1; i < velikostLegendy ; i++) {
            H[0][i] = H[0][i - 1] - legenda1D[ zacatekLegendy+i];
        }

        //---------------

        for (int j = 1; j < velikostLegendy; j++) {
            int legendaJ = legenda1D[ zacatekLegendy+ j];

            for (int i = 1; i < velikostTajenky; i++) {

                H[i][j] = Math.max(H[i - 1][j    ] - tajenkaSloupec[i],
                          Math.max(H[i    ][j - 1] - legendaJ,
                                   H[i - 1][j - 1] - Math.abs(legendaJ - tajenkaSloupec[i])));
            }
        }

        return H[velikostTajenky-1 ][velikostLegendy-1 ];
    }

    public void spoctiFitnessCPU() {

        Main.fitnessCounted ++;

        int fitnessR=0;
        int fitnessC=0;


        for (int i = 0; i < sirka; i++) {
            fitnessC += needlemanWunch(horniLegenda[i], computeColumn(i), sizesOfHorniLegenda[i] );
        }

        for (int i = 0; i < vyska; i++) {
            fitnessR += needlemanWunch(levaLegenda[i], computeRow(i), sizesOfLevaLegenda[i] );
        }

        double pomer = Math.max(0.2,Math.random());

        fitness = fitnessC + fitnessR ;//2*(int)(pomer*fitnessC + (1-pomer)*fitnessR);


    }

    public int [] computeColumn(int sloupec) {

        int [] tajenkaRadek = new int [Main.vyska];
        int plnost = 0;
        int kombo = 0;

        for (int j = 0; j < Main.vyska; j++) {
            if (tajenka[sloupec*vyska + j] == 1) {
                kombo++;
            } else {
                if (kombo != 0) {
                    tajenkaRadek[plnost++] = kombo;
                }
                kombo = 0;
            }
        }

        if (kombo != 0) {
            tajenkaRadek[plnost++] = kombo; // posledni ctverecek je cerny
        }

        int [] tajenkaRadekSNulou = new int[plnost + 1];
        tajenkaRadekSNulou[0] = 0;

        for (int j = 1; j < tajenkaRadekSNulou.length; j++) {
            tajenkaRadekSNulou[j] = tajenkaRadek[j-1];
        }

        return tajenkaRadekSNulou;

    }

    public int [] computeRow(int radek) {

        int [] tajenkaRadek = new int [Main.sirka];
        int plnost = 0;
        int kombo = 0;

        for (int j = 0; j < Main.sirka; j++) {
            if (tajenka[radek*sirka + j] == 1) {
                kombo++;
            } else {
                if (kombo != 0) {
                    tajenkaRadek[plnost++] = kombo;
                }
                kombo = 0;
            }
        }

        if (kombo != 0) {
            tajenkaRadek[plnost++] = kombo; // posledni ctverecek je cerny
        }

        int [] tajenkaRadekSNulou = new int[plnost + 1];
        tajenkaRadekSNulou[0] = 0;

        for (int j = 1; j < tajenkaRadekSNulou.length; j++) {
            tajenkaRadekSNulou[j] = tajenkaRadek[j-1];
        }

        return tajenkaRadekSNulou;

    }

    // podle tajenky a legendy spocte needlemana pro jeden radek/sloupec
    public static int needlemanWunch(int [] legenda, final int [] tajenkaRadekSNulou, int actuallegends) {

        int[][] H = new int[tajenkaRadekSNulou.length][actuallegends];

        H[0][0] = 0;

        for (int i = 1; i < tajenkaRadekSNulou.length; i++) {
            H[i][0] = H[i - 1][0] - tajenkaRadekSNulou[i];
        }

        for (int i = 1; i < actuallegends ; i++) {
            H[0][i] = H[0][i - 1] - legenda[i];
        }

        for (int j = 1; j < actuallegends; j++) {
            for (int i = 1; i < tajenkaRadekSNulou.length; i++) {

                H[i][j] = Math.max(H[i - 1][j] - tajenkaRadekSNulou[i],
                        Math.max(H[i][j - 1] - legenda[j],
                                H[i - 1][j - 1] - Math.abs(legenda[j] - tajenkaRadekSNulou[i])));

            }
        }

        return H[tajenkaRadekSNulou.length - 1][actuallegends - 1];
    }


    public void zmutuj() {

        int pocetzmen = 1;//(int) (Math.random() * (15)) + 2;;

        for (int i = 0; i < pocetzmen; i++) {

            int x = (int) (Math.random() * velikost);

            tajenka[x] = 1-tajenka[x];
        }

        //int kolikrat = (int) (Math.random() * (3 + iteraciBezZlepseni / 20)) + 2;



    }

    @Override
    public int compareTo(Individual individual) {
        return individual.fitness - fitness;
    }

    int nejlepsiFitnessEver;

    ArrayList<Integer> nejlepsiMezery;

    Integer ZmenenyRadek;

    ArrayList<Integer> zalohaMezer;

    int a = 5;

//    public Individual localOptimalization(int numberOfOptimalization){
//
//        nejlepsiFitnessEver = fitness;
//       // fitnessKandidata = fitness;
//        int tolerance = 6;
//        if(nejlepsiFitnessEver  >= -60 ) tolerance = 4;
//        if(nejlepsiFitnessEver  >= -26 ) tolerance = 2;
//        if(nejlepsiFitnessEver  >= -2 ) tolerance = 0;
//
//        Individual nejlepsi = new Individual(this);
//
//        // opakovani optimalizace
//        for (int p = 0; p < numberOfOptimalization; p++) {
//
//            prehazimMezery();
//
//            if ( fitness >= nejlepsiFitnessEver - tolerance) {
//
//                if(fitness >= nejlepsi.fitness  ){
//                    nejlepsi = new Individual(this);
//                }
//
//                if(nejlepsiFitnessEver  >= -60 ) tolerance = 4;
//                if(nejlepsiFitnessEver  >= -26 ) tolerance = 2;
//                if(nejlepsiFitnessEver  >= -2 ) tolerance = 0;
//
//                if(fitness > nejlepsiFitnessEver){
//                    nejlepsiFitnessEver = fitness;
//                }
//
////                if (nejlepsiMezery != null) {
////                    velikostiMezer.set(ZmenenyRadek, new ArrayList<Integer>(nejlepsiMezery));
////                    VyplnRadekTajenky(ZmenenyRadek, velikostiMezer.get(ZmenenyRadek));
////                    fitness = fitnessKandidata;
////                }
//
//                if (fitness == 0) {
//                    System.out.println("MAM SPRAVNY NONOGRAM - optimalizace!!!");
//                    return this;
//                }
//
//            }else{
//                // vrat puvodni hodnoty
//                velikostiMezer.set(ZmenenyRadek, zalohaMezer);
//                VyplnRadekTajenky(ZmenenyRadek, zalohaMezer);
//            }
//
////            if (p % 1000 == 0) {
////                System.out.println(p + ". KOLO. fitness: "
////                        + fitness + " Iteraci bez zlepseni " + iteraciBezZlepseni);
////            }
//
//
//        }
//        return nejlepsi;
//    }

//    public void prehazimMezery() {
//
//        ZmenenyRadek = (int) (Math.random() * Main.vyska);
//
//        ArrayList<Integer> mezeryKtereMenim = velikostiMezer.get(ZmenenyRadek);
//
//        zalohaMezer = new ArrayList<>(velikostiMezer.get(ZmenenyRadek));
//
//        if(mezeryKtereMenim.size() == 1) return;
//
//        int kolikrat = 1;//(int)(Math.random() * (3 + iteraciBezZlepseni/20) ) + 2;
//
//        // kolikrat prehazim mezery v ramci jednoho radku
//        for (int l = 0; l < kolikrat; l++) {
//            prehodJednumezeruVJednomRadku(ZmenenyRadek, mezeryKtereMenim);
//        }
//
//        // vypln tajenku a spocti fitness
//        VyplnRadekTajenky(ZmenenyRadek, mezeryKtereMenim);
//
//        fitness = spoctiFitness();
//
//        nejlepsiMezery = mezeryKtereMenim;
//
//    }

}