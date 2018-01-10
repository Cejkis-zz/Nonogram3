package com.company;

import jcuda.Sizeof;
import jcuda.driver.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static jcuda.driver.JCudaDriver.*;

public class Main {

    static int height, width, gridSize;
    final static int GENERATIONS = 10000;
    final static boolean VIZ = false;
    final static boolean CROWDING = true;
    final static boolean GPU = true;

    static String input = "inputs/10x13.txt"; // 10x13 40x30

    final static int fitnessCountCeil = 200 * 50000; // after this many fitness countings, program will stop
    static int fitnessCounted;

    // reference to kernel
    static CUmodule optimisePopulationModule;

    // references to kernel functions
    static CUfunction createChildrenFunction = new CUfunction();
    static CUfunction updatePopulationFunction = new CUfunction();
    static CUfunction fitnessOfAllColumnsFunction = new CUfunction();
    static CUfunction fitnessOfAllRowsFunction = new CUfunction();
    static CUfunction evolutionFunction = new CUfunction();

    static int[][] upperLegend;
    static int[] upperLegend1D;
    static int[] shiftsOfUpperLegend;
    static int[] sizesOfUpperLegend;

    static int[][] leftLegend;
    static int[] leftLegend1D;
    static int[] shiftsOfLeftLegend;
    static int[] sizesOfLeftLegend;

    // parses input to legends
    static void readInput(String filename) {

        Scanner in = null;
        Scanner rowScanner;

        try {
            in = new Scanner(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.out.println("Cannot find file " + filename);
        }

        // Left legend

        String row = in.nextLine();

        ArrayList<ArrayList<Integer>> leftLegendTemp = new ArrayList<>();
        ArrayList<Integer> newRow;

        int legendSize = 0;
        int max = 0;

        while (true) {
            row = in.nextLine();

            if (row.length() == 0) {
                row = in.nextLine();
                break;
            }

            newRow = new ArrayList<>();
            rowScanner = new Scanner(row);

            while (rowScanner.hasNextInt()) {
                newRow.add(rowScanner.nextInt());
                legendSize++;
            }
            newRow.add(0, 0); // first element is zero for needleman wunsch
            legendSize++;
            leftLegendTemp.add(newRow);
            if (max < newRow.size()) max = newRow.size();
        }

        sizesOfLeftLegend = new int[leftLegendTemp.size()];
        leftLegend1D = new int[legendSize];
        shiftsOfLeftLegend = new int[leftLegendTemp.size()];
        shiftsOfLeftLegend[0] = 0;
        leftLegend = new int[leftLegendTemp.size()][max];

        for (int i = 0; i < leftLegendTemp.size(); i++) {
            sizesOfLeftLegend[i] = leftLegendTemp.get(i).size();
            if (0 < i)
                shiftsOfLeftLegend[i] = shiftsOfLeftLegend[i - 1] + sizesOfLeftLegend[i - 1];

            for (int j = 0; j < leftLegendTemp.get(i).size(); j++) {
                Main.leftLegend[i][j] = leftLegendTemp.get(i).get(j);
                leftLegend1D[shiftsOfLeftLegend[i] + j] = Main.leftLegend[i][j];
            }
        }

        ////////////////////// upper legend

        ArrayList<ArrayList<Integer>> upperLegendTemp = new ArrayList<>();

        max = 0;
        legendSize = 0;

        while (in.hasNext()) {
            row = in.nextLine();

            newRow = new ArrayList<>();
            rowScanner = new Scanner(row);

            while (rowScanner.hasNextInt()) {
                newRow.add(0, rowScanner.nextInt());
                legendSize++;
            }
            newRow.add(0, 0); // first element is zero for needleman wunsch
            legendSize++;
            upperLegendTemp.add(newRow);
            if (max < newRow.size()) max = newRow.size();
        }

        sizesOfUpperLegend = new int[upperLegendTemp.size()];
        upperLegend1D = new int[legendSize];
        shiftsOfUpperLegend = new int[upperLegendTemp.size()];
        shiftsOfUpperLegend[0] = 0;
        upperLegend = new int[upperLegendTemp.size()][max];

        for (int i = 0; i < upperLegendTemp.size(); i++) {
            sizesOfUpperLegend[i] = upperLegendTemp.get(i).size();
            if (0 < i)
                shiftsOfUpperLegend[i] = shiftsOfUpperLegend[i - 1] + sizesOfUpperLegend[i - 1];

            for (int j = 0; j < upperLegendTemp.get(i).size(); j++) {
                upperLegend[i][j] = upperLegendTemp.get(i).get(j);
                upperLegend1D[shiftsOfUpperLegend[i] + j] = upperLegend[i][j];
            }
        }

        //
        width = upperLegend.length;
        height = leftLegend.length;
        gridSize = width * height;

    }

    // initializes constant memory of name in module and sets its value to array
    private static void initConstantMemory(CUmodule module, String name, int[] array) {
        long sizeArray[] = {0};
        CUdeviceptr pointer = new CUdeviceptr();
        cuModuleGetGlobal(pointer, sizeArray, module, name);
        cuMemcpyHtoD(pointer, pointer.to(array), array.length * Sizeof.INT);
    }

    // source: https://stackoverflow.com/questions/25242889/error-while-compiling-jcuda-sample-from-sdk-in-eclipse-input-file-not-found-jc
    public static void setupGPU() throws IOException {

        // Enable exceptions and omit all subsequent error checks
        JCudaDriver.setExceptionsEnabled(true);

        // Create the PTX file by calling the NVCC
        String ptxFileName = preparePtxFile("kernels.cu");

        // Initialize the driver and create a context for the first device.
        cuInit(0);
        CUdevice device = new CUdevice();
        cuDeviceGet(device, 0);
        CUcontext context = new CUcontext();
        cuCtxCreate(context, 0, device);

        // Load the ptx file.
        optimisePopulationModule = new CUmodule();
        cuModuleLoad(optimisePopulationModule, ptxFileName);

        // load functions
        cuModuleGetFunction(createChildrenFunction, optimisePopulationModule, "createChildren");
        cuModuleGetFunction(updatePopulationFunction, optimisePopulationModule, "updatePopulation");

        cuModuleGetFunction(fitnessOfAllColumnsFunction, optimisePopulationModule, "countFitnessOfAllColumns");
        cuModuleGetFunction(fitnessOfAllRowsFunction, optimisePopulationModule, "countFitnessOfAllRows");

        cuModuleGetFunction(evolutionFunction, optimisePopulationModule, "evolution");

        // load constant memories
        initConstantMemory(optimisePopulationModule, "legendU", upperLegend1D);
        initConstantMemory(optimisePopulationModule, "sizesOfLegendsU", sizesOfUpperLegend);
        initConstantMemory(optimisePopulationModule, "shiftsOfLegendsU", shiftsOfUpperLegend);
        initConstantMemory(optimisePopulationModule, "legendL", leftLegend1D);
        initConstantMemory(optimisePopulationModule, "sizesOfLegendsL", sizesOfLeftLegend);
        initConstantMemory(optimisePopulationModule, "shiftsOfLegendsL", shiftsOfLeftLegend);
        initConstantMemory(optimisePopulationModule, "heightWidth", new int[]{height, width, gridSize, Island.popSize});
    }

    public static void main(String[] args) throws IOException {

        System.out.println((byte)(258));

        readInput(input);

        setupGPU();

        Island is;



        if (CROWDING){
            if (GPU)
             is = new GPUCrowdingIsland();
            else
             is = new CpuCrowdingIsland();
        }else
            is = new ClassicIsland();

        System.out.println("START" + new SimpleDateFormat("HH:mm:ss").format(new Date()));

        for (int g = 0; g < GENERATIONS; g++) {

            if (g % 100 == 1) System.out.println(g + ". generation " +new SimpleDateFormat("HH:mm:ss").format(new Date()));

            if (fitnessCountCeil < fitnessCounted) {
                break;
            }

            is.optimise(g);

            //System.out.println("Best: " + is.bestScore);
            //System.out.println("Current: " + is.bestInd.fitness);
        }
        System.out.println(fitnessCounted);
    }


    /**
     * The extension of the given file name is replaced with "ptx".
     * If the file with the resulting name does not exist, it is
     * compiled from the given file using NVCC. The name of the
     * PTX file is returned.
     *
     * @param cuFileName The name of the .CU file
     * @return The name of the PTX file
     * @throws IOException If an I/O error occurs
     */
    private static String preparePtxFile(String cuFileName) throws IOException {
        int endIndex = cuFileName.lastIndexOf('.');
        if (endIndex == -1) {
            endIndex = cuFileName.length() - 1;
        }
        String ptxFileName = cuFileName.substring(0, endIndex + 1) + "ptx";
        File ptxFile = new File(ptxFileName);
        if (ptxFile.exists()) {
            return ptxFileName;
        }

        File cuFile = new File(cuFileName);
        if (!cuFile.exists()) {
            throw new IOException("Input file not found: " + cuFileName);
        }
        String modelString = "-m" + System.getProperty("sun.arch.data.model");
        String command =
                "nvcc " + modelString + " -ptx " +
                        cuFile.getPath() + " -o " + ptxFileName;

        System.out.println("Executing\n" + command);
        Process process = Runtime.getRuntime().exec(command);

        String errorMessage =
                new String(toByteArray(process.getErrorStream()));
        String outputMessage =
                new String(toByteArray(process.getInputStream()));
        int exitValue = 0;
        try {
            exitValue = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "Interrupted while waiting for nvcc output", e);
        }

        if (exitValue != 0) {
            System.out.println("nvcc process exitValue " + exitValue);
            System.out.println("errorMessage:\n" + errorMessage);
            System.out.println("outputMessage:\n" + outputMessage);
            throw new IOException(
                    "Could not create .ptx file: " + errorMessage);
        }

        System.out.println("Finished creating PTX file");
        return ptxFileName;
    }

    /**
     * Fully reads the given InputStream and returns it as a byte array
     *
     * @param inputStream The input stream to read
     * @return The byte array containing the data from the input stream
     * @throws IOException If an I/O error occurs
     */
    private static byte[] toByteArray(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[8192];
        while (true) {
            int read = inputStream.read(buffer);
            if (read == -1) {
                break;
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }


}
