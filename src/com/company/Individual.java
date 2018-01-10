package com.company;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

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

        tajenka = new int[gridSize];

        for (int i = 0; i < gridSize; i++) {
            if (Math.random() < 0.5){
                tajenka[i] = 1;
            }
        }

        birth = g;
    }

    public Individual(Individual s){

        tajenka = new int[gridSize];

        for (int i = 0; i < gridSize; i++) {
                tajenka[i] = s.tajenka[i];}

        fitness = s.fitness;
        birth = s.birth;

    }

    public void countFitness() {

        Main.fitnessCounted ++;

        if (Main.GPU && !Main.CROWDING)
            fitness = computeFitnessGPU();
        else
            fitness = computeFitnessCPU();

    }

    public int computeFitnessGPU(){

        int hostOutput[] = new int[1];

        CUdeviceptr fitness_D = new CUdeviceptr();
        cuMemAlloc(fitness_D, 1*Sizeof.INT);

        CUdeviceptr tajenka_D = new CUdeviceptr();
        cuMemAlloc(tajenka_D, tajenka.length*Sizeof.INT);
        cuMemcpyHtoD(tajenka_D, Pointer.to(tajenka),tajenka.length * Sizeof.INT);

        Pointer kernelParameters = Pointer.to(Pointer.to(tajenka_D), Pointer.to(fitness_D));

        cuLaunchKernel(fitnessOfAllColumnsFunction,
                width, 1, 1,      // Grid dimension
                1, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );

        cuMemcpyDtoH(Pointer.to(hostOutput), fitness_D,1 * Sizeof.INT);
        fitness = hostOutput[0];
        System.out.println(fitness);

        cuMemFree(tajenka_D);
        cuMemFree(fitness_D);
        return fitness;

    }

    public int computeFitnessCPU() {

        int fitnessR=0;
        int fitnessC=0;


        for (int i = 0; i < width; i++) {
            fitnessC += needlemanWunch(upperLegend[i], computeColumn(i), sizesOfUpperLegend[i] );
        }

        for (int i = 0; i < height; i++) {
            fitnessR += needlemanWunch(leftLegend[i], computeRow(i), sizesOfLeftLegend[i] );
        }

        return fitnessR + fitnessC ;
    }

    public int [] computeColumn(int sloupec) {

        int [] tajenkaRadek = new int [height];
        int plnost = 0;
        int kombo = 0;

        for (int i = 0; i < height; i++) {
            if (tajenka[i* width + sloupec] == 1) {
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

        int [] tajenkaRadek = new int [Main.width];
        int plnost = 0;
        int kombo = 0;

        for (int j = 0; j < Main.width; j++) {
            if (tajenka[radek* width + j] == 1) {
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
    public static int needlemanWunch(int [] legend, final int [] gridSlice, int actuallegends) {

        int[][] H = new int[gridSlice.length][actuallegends];

        H[0][0] = 0;

        for (int i = 1; i < gridSlice.length; i++) {
            H[i][0] = H[i - 1][0] - gridSlice[i];
        }

        for (int i = 1; i < actuallegends ; i++) {
            H[0][i] = H[0][i - 1] - legend[i];
        }

        for (int j = 1; j < actuallegends; j++) {
            for (int i = 1; i < gridSlice.length; i++) {

                H[i][j] = Math.max(H[i - 1][j    ] - gridSlice[i],
                          Math.max(H[i    ][j - 1] - legend[j],
                                   H[i - 1][j - 1] - Math.abs(legend[j] - gridSlice[i])));

            }
        }

        return H[gridSlice.length - 1][actuallegends - 1];
    }

    public void mutate() {

        int pocetzmen = 4;//(int) (Math.random() * (15)) + 2;;

        for (int i = 0; i < pocetzmen; i++) {

            int x = (int) (Math.random() * gridSize);

            tajenka[x] = 1-tajenka[x];
        }

        //int kolikrat = (int) (Math.random() * (3 + iteraciBezZlepseni / 20)) + 2;



    }

    @Override
    public int compareTo(Individual individual) {
        return individual.fitness - fitness;
    }

    int nejlepsiFitnessEver;



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
//        ZmenenyRadek = (int) (Math.random() * Main.height);
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
//        fitness = countFitness();
//
//        nejlepsiMezery = mezeryKtereMenim;
//
//    }



}