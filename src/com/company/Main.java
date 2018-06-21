package com.company;

import jcuda.Sizeof;
import jcuda.driver.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static jcuda.driver.JCudaDriver.*;

public class Main {

    // main controls
    static boolean VIZ = false;
    static boolean CROWDING = false;
    static boolean BINARYINDIVIDUAL = false;
    static boolean GPU = false;
    static String input = "inputs/40x40.txt"; // 10x13 25x20 40x30

    static int GENERATIONS = 10000;
    static int height, width, gridSize;

    // references to kernels
    static CUmodule optimisePopulationModule;
    static CUmodule updatePopulationModule;

    // references to kernel functions
    static CUfunction createChildrenFunction = new CUfunction();
    static CUfunction updatePopulationFunction = new CUfunction();
    static CUfunction fitnessOfAllColumnsFunction = new CUfunction();
    static CUfunction fitnessOfAllRowsFunction = new CUfunction();
    static CUfunction evolutionFunction = new CUfunction();

    // input arrays
    static ArrayList<ArrayList<Integer>> leftLegendAL;
    static ArrayList<ArrayList<Integer>> upperLegendAL;

    static int[][] upperLegend;
    static int[] upperLegend1D;
    static int[] shiftsOfUpperLegend;
    static int[] sizesOfUpperLegend;

    static int[][] leftLegend;
    static int[] leftLegend1D;
    static int[] shiftsOfLeftLegend;
    static int[] sizesOfLeftLegend;

    // parses input to legends
    static void readInput(Scanner in) {

        Scanner rowScanner;

        // Left legend
        String row = in.nextLine();

        leftLegendAL = new ArrayList<>();
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
            newRow.add(0, 0); // first element is zero for needlemanWunschOptimized wunsch
            legendSize++;
            leftLegendAL.add(newRow);
            if (max < newRow.size()) max = newRow.size();
        }

        sizesOfLeftLegend = new int[leftLegendAL.size()];
        leftLegend1D = new int[legendSize];
        shiftsOfLeftLegend = new int[leftLegendAL.size()];
        shiftsOfLeftLegend[0] = 0;
        leftLegend = new int[leftLegendAL.size()][max];

        for (int i = 0; i < leftLegendAL.size(); i++) {
            sizesOfLeftLegend[i] = leftLegendAL.get(i).size();
            if (0 < i)
                shiftsOfLeftLegend[i] = shiftsOfLeftLegend[i - 1] + sizesOfLeftLegend[i - 1];

            for (int j = 0; j < leftLegendAL.get(i).size(); j++) {
                Main.leftLegend[i][j] = leftLegendAL.get(i).get(j);
                leftLegend1D[shiftsOfLeftLegend[i] + j] = Main.leftLegend[i][j];
            }
            leftLegendAL.get(i).remove(0);
        }

        ////////////////////// upper legend

        upperLegendAL = new ArrayList<>();

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
            newRow.add(0, 0); // first element is zero for needlemanWunschOptimized wunsch
            legendSize++;
            upperLegendAL.add(newRow);
            if (max < newRow.size()) max = newRow.size();
        }

        sizesOfUpperLegend = new int[upperLegendAL.size()];
        upperLegend1D = new int[legendSize];
        shiftsOfUpperLegend = new int[upperLegendAL.size()];
        shiftsOfUpperLegend[0] = 0;
        upperLegend = new int[upperLegendAL.size()][max];

        for (int i = 0; i < upperLegendAL.size(); i++) {
            sizesOfUpperLegend[i] = upperLegendAL.get(i).size();
            if (0 < i)
                shiftsOfUpperLegend[i] = shiftsOfUpperLegend[i - 1] + sizesOfUpperLegend[i - 1];

            for (int j = 0; j < upperLegendAL.get(i).size(); j++) {
                upperLegend[i][j] = upperLegendAL.get(i).get(j);
                upperLegend1D[shiftsOfUpperLegend[i] + j] = upperLegend[i][j];
            }
            upperLegendAL.get(i).remove(0);
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

        // Create the PTX file by calling the NVCC
        String ptxFileNameUpdate = preparePtxFile("update.cu");

        // Initialize the driver and create a context for the first device.
        cuInit(0);
        CUdevice device = new CUdevice();
        cuDeviceGet(device, 0);
        CUcontext context = new CUcontext();
        cuCtxCreate(context, 0, device);

        // Load the ptx file.
        optimisePopulationModule = new CUmodule();
        cuModuleLoad(optimisePopulationModule, ptxFileName);

        // Load the ptx file.
        updatePopulationModule = new CUmodule();
        cuModuleLoad(updatePopulationModule, ptxFileNameUpdate);

        // load functions
        cuModuleGetFunction(createChildrenFunction, optimisePopulationModule, "createChildren");
        cuModuleGetFunction(updatePopulationFunction, updatePopulationModule, "updatePopulation");

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
        initConstantMemory(updatePopulationModule, "heightWidth", new int[]{height, width, gridSize, Island.popSize});
    }

    public static Scanner readArgs(String[] args){
        Scanner inputScanner = null;

        for (String arg: args) {

            if (arg.equals("par")){ // parallel computation of individual
                GPU = true;
                GPUCrowdingIsland.fitnessSingleThread = false;
                continue;
            }

            if (arg.equals("ser")){ // serial computation of individual
                GPU = true;
                GPUCrowdingIsland.fitnessSingleThread = true;
                continue;
            }

            if (arg.equals("viz")){ // serial computation of individual
                VIZ = true;
                continue;
            }

            if (arg.equals("bin")){ // serial computation of individual
                BINARYINDIVIDUAL = true;
                continue;
            }

            if (arg.equals("dc")){ // serial computation of individual
                CROWDING = true;
                continue;
            }

            try {
                int number = Integer.parseInt(arg);

                if (number < 50){
                    GPUCrowdingIsland.nrSMX = number;
                }else{
                    GENERATIONS = number;
                }

            } catch (NumberFormatException e) {
                try {
                    inputScanner = new Scanner(new FileReader(arg));
                } catch (FileNotFoundException ex) {
                    System.err.println("Use argument \"cpu\",\"par\" or \"ser\". Use integer to set number of SMs. Your argument:" + arg + " was used as filename and no file was found.");
                    System.exit(1);
                }
            }
        }

        if (inputScanner == null){
            System.err.println("Please specify input location.");
            System.exit(1);
        }

        return inputScanner;
    }

    public static void main(String[] args) throws IOException {

        Scanner s =readArgs(args);
        readInput(s);
        // readInput(new Scanner(new FileReader(input))); // no command line arguments

        if (GPU)
            setupGPU();

        Island is;

        if (CROWDING){
            if (GPU)
             is = new GPUCrowdingIsland();
            else
             is = new CPUCrowdingIsland();
        }else
            is = new ClassicIsland();

        System.out.println("START" + new SimpleDateFormat("HH:mm:ss").format(new Date()));

        for (int g = 0; g < GENERATIONS; g++) {

            if (g % 1 == 0) {
                System.out.print(g + ". generation " +new SimpleDateFormat("HH:mm:ss ").format(new Date()));
                is.printStatistics();
            }

            is.optimise(g);
        }
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
