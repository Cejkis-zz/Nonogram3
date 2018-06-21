package com.company;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import javax.swing.*;

import java.awt.*;

import static com.company.Main.*;
import static jcuda.driver.JCudaDriver.*;

/**
 * Created by cejkis on 8.11.15.
 */

// Individual represented by binary 2D array
public class IndividualBinary extends AbstractIndividual {

    int[] grid;

    public IndividualBinary(int g) {

        grid = new int[gridSize];

        for (int i = 0; i < gridSize; i++) {
            if (Math.random() < 0.5){
                grid[i] = 1;
            }
        }

        genOfBirth = g;
    }

//    public AbstractIndividual(AbstractIndividual s){
//
//        grid = new int[gridSize];
//
//        for (int i = 0; i < gridSize; i++) {
//                grid[i] = s.grid[i];}
//
//        fitness = s.fitness;
//        genOfBirth = s.genOfBirth;
//
//    }

    public void countFitness() {
        if (Main.GPU && !Main.CROWDING)
            fitness = computeFitnessGPU();
        else
            fitness = computeFitnessCPU();
    }

    @Override
    public AbstractIndividual cross(AbstractIndividual b, int gen) {

        IndividualBinary c = new IndividualBinary(gen);
        IndividualBinary bb = (IndividualBinary)(b);

        for (int i = 0; i < Main.gridSize; i++) {
            if (Math.random() > 0.5)
                c.grid[i] = grid[i];
            else
                c.grid[i] = bb.grid[i];
        }

        return c;
    }

    public int difference(AbstractIndividual j) {

        IndividualBinary ii = (IndividualBinary) j;
        int diff = 0;

        for (int i = 0; i < gridSize; i++) {
            if (grid[i] != ii.grid[i]) {
                diff++;
            }
        }

        return diff;
    }

    @Override
    public void printToViz(JTextArea[][] okna) {
        int[] taj = grid;

        for (int j = 0; j < Main.width; j++) {
            for (int k = 0; k < Main.height; k++) {

                if (taj[ Main.width *k + j ] == 0)
                    okna[j][k].setBackground(Color.WHITE);
                else
                    okna[j][k].setBackground(Color.BLACK);
            }
        }
    }

    // just for testing purposes
    public int computeFitnessGPU(){

        int hostOutput[] = new int[1];

        CUdeviceptr fitness_D = new CUdeviceptr();
        cuMemAlloc(fitness_D, 1*Sizeof.INT);

        CUdeviceptr grid_D = new CUdeviceptr();
        cuMemAlloc(grid_D, grid.length*Sizeof.INT);
        cuMemcpyHtoD(grid_D, Pointer.to(grid), grid.length * Sizeof.INT);

        Pointer kernelParameters = Pointer.to(Pointer.to(grid_D), Pointer.to(fitness_D));

        cuLaunchKernel(fitnessOfAllColumnsFunction,
                width, 1, 1,      // Grid dimension
                1, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );

        cuMemcpyDtoH(Pointer.to(hostOutput), fitness_D,1 * Sizeof.INT);
        fitness = hostOutput[0];
        //System.out.println(fitness);

        cuMemFree(grid_D);
        cuMemFree(fitness_D);
        return fitness;

    }

    public int computeFitnessCPU() {

        int fitnessR = 0;
        int fitnessC = 0;

        for (int i = 0; i < width; i++) {
            fitnessC += needlemanWunschOptimized(upperLegend[i], computeColumn(i), sizesOfUpperLegend[i] );
        }

        for (int i = 0; i < height; i++) {
            fitnessR += needlemanWunschOptimized(leftLegend[i], computeRow(i), sizesOfLeftLegend[i] );
        }

        return fitnessR + fitnessC;
    }

    public int [] computeColumn(int column) {

        int [] gridColumn = new int [height];
        int size = 0;
        int kombo = 0;

        for (int i = 0; i < height; i++) {
            if (grid[i* width + column] == 1) {
                kombo++;
            } else {
                if (kombo != 0) {
                    gridColumn[size++] = kombo;
                }
                kombo = 0;
            }
        }

        if (kombo != 0) {
            gridColumn[size++] = kombo; // last box is filled
        }

        int [] withZero = new int[size + 1];
        withZero[0] = 0;

        for (int j = 1; j < withZero.length; j++) {
            withZero[j] = gridColumn[j-1];
        }

        return withZero;

    }

    public int [] computeRow(int row) {

        int [] gridRow = new int [Main.width];
        int size = 0;
        int kombo = 0;

        for (int j = 0; j < Main.width; j++) {
            if (grid[row* width + j] == 1) {
                kombo++;
            } else {
                if (kombo != 0) {
                    gridRow[size++] = kombo;
                }
                kombo = 0;
            }
        }

        if (kombo != 0) {
            gridRow[size++] = kombo; // last box is filled
        }

        int [] withZero = new int[size + 1];
        withZero[0] = 0;

        for (int j = 1; j < withZero.length; j++) {
            withZero[j] = gridRow[j-1];
        }

        return withZero;

    }

    public void mutate() {

        int numberOfChanges = 4;//(int) (Math.random() * (15)) + 2;;
        // (int) (Math.random() * (3 + itersWithoutImprovement / 20)) + 2;

        for (int i = 0; i < numberOfChanges; i++) {

            int x = (int) (Math.random() * gridSize);

            grid[x] = 1- grid[x];
        }
    }


//    public AbstractIndividual localOptimalization(int numberOfOptimalization){
//
//        bestFitnessEver = fitness;
//       // fitnessKandidata = fitness;
//        int tolerance = 6;
//        if(bestFitnessEver  >= -60 ) tolerance = 4;
//        if(bestFitnessEver  >= -26 ) tolerance = 2;
//        if(bestFitnessEver  >= -2 ) tolerance = 0;
//
//        AbstractIndividual nejlepsi = new AbstractIndividual(this);
//
//        // opakovani optimalizace
//        for (int p = 0; p < numberOfOptimalization; p++) {
//
//            swapSpaces();
//
//            if ( fitness >= bestFitnessEver - tolerance) {
//
//                if(fitness >= nejlepsi.fitness  ){
//                    nejlepsi = new AbstractIndividual(this);
//                }
//
//                if(bestFitnessEver  >= -60 ) tolerance = 4;
//                if(bestFitnessEver  >= -26 ) tolerance = 2;
//                if(bestFitnessEver  >= -2 ) tolerance = 0;
//
//                if(fitness > bestFitnessEver){
//                    bestFitnessEver = fitness;
//                }
//
////                if (bestSpaces != null) {
////                    sizesOfSpaces.set(changedRow, new ArrayList<Integer>(bestSpaces));
////                    updateGridRow(changedRow, sizesOfSpaces.get(changedRow));
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
//                sizesOfSpaces.set(changedRow, spacesBackup);
//                updateGridRow(changedRow, spacesBackup);
//            }
//
////            if (p % 1000 == 0) {
////                System.out.println(p + ". KOLO. fitness: "
////                        + fitness + " Iteraci bez zlepseni " + itersWithoutImprovement);
////            }
//
//
//        }
//        return nejlepsi;
//    }

//    public void swapSpaces() {
//
//        changedRow = (int) (Math.random() * Main.height);
//
//        ArrayList<Integer> mezeryKtereMenim = sizesOfSpaces.get(changedRow);
//
//        spacesBackup = new ArrayList<>(sizesOfSpaces.get(changedRow));
//
//        if(mezeryKtereMenim.size() == 1) return;
//
//        int kolikrat = 1;//(int)(Math.random() * (3 + itersWithoutImprovement/20) ) + 2;
//
//        // kolikrat prehazim mezery v ramci jednoho radku
//        for (int l = 0; l < kolikrat; l++) {
//            moveOneSpaceInOneRow(changedRow, mezeryKtereMenim);
//        }
//
//        // vypln tajenku a spocti fitness
//        updateGridRow(changedRow, mezeryKtereMenim);
//
//        fitness = countFitness();
//
//        bestSpaces = mezeryKtereMenim;
//
//    }

}