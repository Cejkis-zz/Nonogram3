package com.company;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static com.company.Main.*;

/**
 * Created by cejkis on 8.11.15.
 */

// Individual, that is represented by sizes of spaces between blocks
public class IndividualSmart extends AbstractIndividual {

    ArrayList<ArrayList<Integer>> sizesOfSpaces;

    boolean[][] grid;

    // this is for initial population
    public IndividualSmart() {

        grid = new boolean[Main.height][Main.width];
        sizesOfSpaces = new ArrayList<>();
        genOfBirth = 0;

        for (int i = 0; i < Main.height; i++) {
            sizesOfSpaces.add(new ArrayList<Integer>());
        }
        createSpaces();
        fillGridAccordingToSpaces();
    }

    // for new individual in crossing
    public IndividualSmart(int g) {

        grid = new boolean[Main.height][Main.width];
        sizesOfSpaces = new ArrayList<>();
        genOfBirth = g;
    }

    // for local search
    public IndividualSmart(AbstractIndividual ss){

        IndividualSmart s = (IndividualSmart) ss;

        grid = new boolean[Main.height][Main.width];
        sizesOfSpaces = new ArrayList<>();

        for (int i = 0; i < Main.height; i++) {
            sizesOfSpaces.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < s.sizesOfSpaces.size(); i++) {
            ArrayList<Integer> radek = s.sizesOfSpaces.get(i) ;

            for (Integer ii: radek ){
                sizesOfSpaces.get(i).add(ii);
            }
        }
        fillGridAccordingToSpaces();
        fitness = s.fitness;
        genOfBirth = s.genOfBirth;
    }

    public void printGrid() {

        for (int i = 0; i < Main.height; i++) {
            printRow(i, sizesOfSpaces.get(i));
        }
    }

    public void printRow(int i, ArrayList<Integer> Spaces) {

        ArrayList<Integer> legendRow = Main.leftLegendAL.get(i);

        for (int j = 0; j < Spaces.get(0); j++) {
            System.out.print("  ");
        }

        for (int j = 0; j < legendRow.size(); j++) {

            // print one box
            for (int k = 0; k < legendRow.get(j); k++) {
                System.out.print("##");
            }

            for (int k = 0; k < Spaces.get(j + 1); k++) {
                System.out.print("  ");
            }
        }

        System.out.print("|");
        System.out.println();
    }

    public void createSpaces() {

        ArrayList<Integer> legendRow;
        ArrayList<Integer> spaces;
        int size;

        for (int i = 0; i < Main.height; i++) { // i is row

            legendRow = Main.leftLegendAL.get(i);
            spaces = sizesOfSpaces.get(i);

            if (legendRow.isEmpty()) { // probably never happens
                spaces.add(Main.width);
            } else {
                size = 0;

                for (Integer rowGridSize : legendRow) { //j iterates through row
                    size += rowGridSize;
                }

                size += legendRow.size() - 1;
                double rest = Main.width - size;
                spaces.add((int) Math.ceil(rest / 2));

                for (int j = 0; j < legendRow.size() - 1; j++) {
                    spaces.add(1);
                }
                spaces.add((int) Math.floor(rest / 2));
            }
        }
    }

    ArrayList<Integer> findWhatToShrink(int row) {

        ArrayList<Integer> indecesOfSpacesToShrink = new ArrayList<>();

        ArrayList<Integer> spacesInCurrentRow = sizesOfSpaces.get(row);

        // first space
        if (spacesInCurrentRow.get(0) > 0) {
            indecesOfSpacesToShrink.add(0);
        }

        // last space
        if (spacesInCurrentRow.size() > 1 && spacesInCurrentRow.get(spacesInCurrentRow.size() - 1) > 0) {
            indecesOfSpacesToShrink.add(spacesInCurrentRow.size() - 1);
        }

        // find spaces inside of row for removing and adding
        for (int j = 1; j < spacesInCurrentRow.size() - 1; j++) {

            if (spacesInCurrentRow.get(j) > 1) {
                indecesOfSpacesToShrink.add(j);
            }
        }

        return indecesOfSpacesToShrink;
    }

    public void moveOneSpaceInOneRow(int row, ArrayList<Integer> spacesToChange) {

        int i, j, howManySpaces, indexTo;

        ArrayList<Integer> indecesToShrink = findWhatToShrink(row);

        if (indecesToShrink.isEmpty()) return;

        do {
            i = (int) (Math.random() * indecesToShrink.size());
            j = (int) (Math.random() * spacesToChange.size());
        } while (indecesToShrink.get(i) == j);
        //   System.out.println(i + " * " +j );

        // change spaces
        indexTo = indecesToShrink.get(i);

        // When removing from first or last space, I can remove all boxes
        if (indexTo == 0 || indexTo == spacesToChange.size() - 1) {
            howManySpaces = (int) (Math.random() * (spacesToChange.get(indexTo))) + 1;
        } else howManySpaces = (int) (Math.random() * (spacesToChange.get(indexTo) - 1)) + 1;

        spacesToChange.set(indexTo, spacesToChange.get(indexTo) - howManySpaces);
        spacesToChange.set(j, spacesToChange.get(j) + howManySpaces);
    }

    // vyber nahodny radek, nahodnekrat v nem prehazej mezery
//    public void vyberRadekAPrehazejHo() {
//
//        changedRow = (int) (Math.random() * Main.height);
//        ArrayList<Integer> mezeryKtereMenim;
//
//        ArrayList<Integer> spacesBackup =  Main.CopyArray(sizesOfSpaces.get(changedRow));
//
//        mezeryKtereMenim = sizesOfSpaces.get(changedRow);
//
//        if (mezeryKtereMenim.size() == 1) return;
//
//        int kolikrat = (int) (Math.random() * (3 + Main.itersWithoutImprovement / 20)) + 2;
//
//        // kolikrat prehazim mezery v ramci jednoho radku
//        for (int l = 0; l < kolikrat; l++) {
//            moveOneSpaceInOneRow(changedRow, mezeryKtereMenim);
//        }
//
//        // vypln tajenku a spocti fitness
//        updateGridRow(changedRow, mezeryKtereMenim);
//        fitnessKandidata = spoctiFitness();
//
//        Main.bestSpaces = mezeryKtereMenim;
//
//       // System.out.println(changedRow);
//
//       // printRow(changedRow, sizesOfSpaces.get(changedRow));
//
//        // vrat puvodni hodnoty
//        sizesOfSpaces.set(changedRow, spacesBackup);
//        updateGridRow(changedRow, spacesBackup);
//
//    }


    // Fills the whole binary grid according to spaces in arraylists
    public void fillGridAccordingToSpaces() {

        for (int i = 0; i < Main.height; i++) { // i je radek
            updateGridRow(i, sizesOfSpaces.get(i));
        }
    }

    // updates binary grid according to spaces in arraylists
    public void updateGridRow(int row, ArrayList<Integer> spacesInRow) {

        ArrayList<Integer> boxesInRow = Main.leftLegendAL.get(row);

        int pointer = 0; // to position that im changing

        for (int j = 0; j < spacesInRow.get(0); j++) { // prvni mezera
            grid[row][pointer] = false;
            pointer++;
        }

        //   System.out.println( row + " * " + boxesInRow.size() + " " + spacesInRow.size());

        for (int j = 0; j < boxesInRow.size(); j++) { // for all boxes

            for (int k = 0; k < boxesInRow.get(j); k++) {
                grid[row][pointer] = true;
                pointer++;
            }

            for (int k = 0; k < spacesInRow.get(j + 1); k++) {
                grid[row][pointer] = false;
                pointer++;
            }
        }

    }

    @Override
    public void countFitness() {
        fitness = 0;

        for (int i = 0; i < Main.width; i++) {
            ArrayList<Integer> column = arraylistOfColumn(i);
            column.add(0,0);
            int[] c = new int[column.size()];

            for (int j = 0; j < column.size() ; j++) {
                c[j] = column.get(j);
            }

            fitness += needlemanWunschOptimized(upperLegend[i], c, sizesOfUpperLegend[i]);
        }
    }

    // returns grid column in smart representation.
    public ArrayList<Integer> arraylistOfColumn(int column) {

        ArrayList<Integer> a = new ArrayList<>();
        int kombo = 0;

        for (int i = 0; i < Main.height; i++) {
            if (grid[i][column]) {
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

    @Override
    public AbstractIndividual cross(AbstractIndividual b, int gen) {
        IndividualSmart c = new IndividualSmart(gen);

        for (int i = 0; i < height; i++) {
            if (Math.random() > 0.5)
                c.sizesOfSpaces.add(new ArrayList<>(sizesOfSpaces.get(i)));
            else
                c.sizesOfSpaces.add(new ArrayList<>(((IndividualSmart)b).sizesOfSpaces.get(i)));
        }

        c.fillGridAccordingToSpaces();

        return c;
    }

    @Override
    public void mutate() {
        int row = (int) (Math.random() * height);
        ArrayList<Integer> spacesToChange = sizesOfSpaces.get(row);

        if (spacesToChange.size() <= 1) return;

        moveOneSpaceInOneRow(row, spacesToChange);

        // update grid
        updateGridRow(row, spacesToChange);
    }

    @Override
    public int difference(AbstractIndividual i1) {
        IndividualSmart is = (IndividualSmart) i1;
        int diff = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (is.grid[i][j] != is.grid[i][j]) {
                    diff++;
                }
            }
        }

        return diff;
    }

    @Override
    public void printToViz(JTextArea[][] okna) {
        boolean[][] g = grid;

        for (int j = 0; j < okna.length; j++) {
            for (int k = 0; k < okna[0].length ; k++) {

                if (g[k][j] == false)
                    okna[j][k].setBackground(Color.WHITE);
                else
                    okna[j][k].setBackground(Color.BLACK);
            }
        }
    }

    @Override
    public int compareTo(AbstractIndividual individual) {
        return individual.fitness - fitness;
    }

    //////////

    int bestFitnessEver;

    ArrayList<Integer> bestSpaces;
    Integer changedRow;

    static int itersWithoutImprovement;

    public AbstractIndividual localOptimalization(int numberOfOptimalization){

        bestFitnessEver = fitness;

        int tolerance = 6;
        if(bestFitnessEver >= -60 ) tolerance = 4;
        if(bestFitnessEver >= -26 ) tolerance = 2;
        if(bestFitnessEver >= -2 ) tolerance = 0;

        AbstractIndividual bestIn = new IndividualSmart(this);

        // optimalization cycle
        for (int p = 0; p < numberOfOptimalization; p++) {

            ArrayList<Integer> spacesBackup = swapSpaces();

            if ( fitness >= bestFitnessEver - tolerance) {

                if(fitness >= bestIn.fitness  ){
                    bestIn = new IndividualSmart(this);
                }

                if(bestFitnessEver >= -60 ) tolerance = 4;
                if(bestFitnessEver >= -26 ) tolerance = 2;
                if(bestFitnessEver >= -2 ) tolerance = 0;

                if(fitness > bestFitnessEver){
                    bestFitnessEver = fitness;
                }

//                if (bestSpaces != null) {
//                    sizesOfSpaces.set(changedRow, new ArrayList<Integer>(bestSpaces));
//                    updateGridRow(changedRow, sizesOfSpaces.get(changedRow));
//                }

                if (fitness == 0) {
                    System.out.println("Correct solution found in local optimisation!!!");
                    return this;
                }

            }else{
                // vrat puvodni hodnoty
                sizesOfSpaces.set(changedRow, spacesBackup);
                updateGridRow(changedRow, spacesBackup);
            }

//            if (p % 1000 == 0) {
//                System.out.println(p + ". Round. fitness: "
//                        + fitness + " Iterations without improvement: " + itersWithoutImprovement);
//            }


        }
        return bestIn;
    }

    public ArrayList<Integer> swapSpaces() {

        changedRow = (int) (Math.random() * Main.height);

        ArrayList<Integer> spacesToChange = sizesOfSpaces.get(changedRow);

        ArrayList<Integer> spacesBackup = new ArrayList<>(sizesOfSpaces.get(changedRow));

        if(spacesToChange.size() == 1) return spacesBackup; // this is perhaps not necessary

        int nrOfChanges = 1;//(int)(Math.random() * (3 + itersWithoutImprovement/20) ) + 2;

        // how many times swap spaces in one row
        for (int l = 0; l < nrOfChanges; l++) {
            moveOneSpaceInOneRow(changedRow, spacesToChange);
        }

        // fill grid and count fitness
        updateGridRow(changedRow, spacesToChange);
        countFitness();
        bestSpaces = spacesToChange;
        return spacesBackup;
    }

}