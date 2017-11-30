package com.company;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static jcuda.driver.JCudaDriver.*;
import static jcuda.driver.JCudaDriver.cuMemFree;
import jcuda.*;
import jcuda.runtime.JCuda;
import jcuda.utils.KernelLauncher;

public class Main {

    static int vyska, sirka;
    final static int ITERS = 5;
    final static int ISLANDS = 1;
    final static int GENERATIONS = 1000000; // na tomto cisle nezalezi
    final static double CROSSINTERVAL = 0.001;
    final static double CATASTROPHY = 0.0002;
    final static boolean VIZ = true;
    final static boolean CROWDING = true;
    static int fitnessCounted;
    final static int fitnessCountCeil = 200 * 50000; // 200 ohodnoceni ~ 1 generace

    static int [][] horniLegenda;
    static int [] sizesOfHorniLegenda;
    static ArrayList<ArrayList<Integer>> levaLegenda;

    static Pointer LegendaPointer;

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

        ArrayList<ArrayList<Integer>> horniLegendaTemp = new ArrayList<>();
        int max = 0;

        while (in.hasNext()) {
            radek = in.nextLine();
            novyRadek = new ArrayList<>();

            radeksc = new Scanner(radek);
            while (radeksc.hasNextInt()) {
                novyRadek.add(0, radeksc.nextInt());
            }
            novyRadek.add(0, 0); // first element is zero for needleman wunsch
            horniLegendaTemp.add(novyRadek);

            if (max < novyRadek.size()) max = novyRadek.size();

        }

        sizesOfHorniLegenda = new int[horniLegendaTemp.size()];

        Main.horniLegenda = new int[horniLegendaTemp.size()][max];

        for (int i = 0; i < horniLegendaTemp.size() ; i++) {

            sizesOfHorniLegenda[i] = horniLegendaTemp.get(i).size();

            for (int j = 0; j < horniLegendaTemp.get(i).size() ; j++) {
                Main.horniLegenda[i][j] = horniLegendaTemp.get(i).get(j);
            }
        }

        Pointer LegendaPointer = new Pointer();
        JCuda.cudaMalloc(LegendaPointer, 4*sirka*max);


    }

    public static void testGPU() throws IOException {


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
        CUmodule module = new CUmodule();
        cuModuleLoad(module, ptxFileName);

        // Obtain a function pointer to the "add" function.
        CUfunction function = new CUfunction();
        cuModuleGetFunction(function, module, "add");


//        KernelLauncher kernelLauncher =
//                KernelLauncher.create("kernels.cu", "test");
//        CUmodule module0 = kernelLauncher.getModule();
//
//        System.out.println("Obtaining pointer to constant symbol...");
        CUdeviceptr constantMemoryPointer = new CUdeviceptr();

        long sizeArray[] = {0};
        cuModuleGetGlobal(constantMemoryPointer, sizeArray, module, "legenda2");
        int size = (int)sizeArray[0];

        int[] src = new int[] { 108, 5, 10 };

        cuMemcpyHtoD(constantMemoryPointer, Pointer.to(src), 16);
    //    int numElements2 = size / Sizeof.INT;
//
        System.out.println("constantMemoryPointer: " + constantMemoryPointer + " size " + size);


        int numElements = 100000;

        // Allocate and fill the host input data
        float hostInputA[] = new float[numElements];
        float hostInputB[] = new float[numElements];
        for (int i = 0; i < numElements; i++) {
            hostInputA[i] = (float) i;
            hostInputB[i] = (float) i;
        }

        // Allocate the device input data, and copy the
        // host input data to the device
        CUdeviceptr deviceInputA = new CUdeviceptr();
        cuMemAlloc(deviceInputA, numElements * Sizeof.FLOAT);
        cuMemcpyHtoD(deviceInputA, Pointer.to(hostInputA),
                numElements * Sizeof.FLOAT);
        CUdeviceptr deviceInputB = new CUdeviceptr();
        cuMemAlloc(deviceInputB, numElements * Sizeof.FLOAT);
        cuMemcpyHtoD(deviceInputB, Pointer.to(hostInputB),
                numElements * Sizeof.FLOAT);

        // Allocate device output memory
        CUdeviceptr deviceOutput = new CUdeviceptr();
        cuMemAlloc(deviceOutput, numElements * Sizeof.FLOAT);



        // Set up the kernel parameters: A pointer to an array
        // of pointers which point to the actual values.
        Pointer kernelParameters = Pointer.to(
                Pointer.to(new int[]{numElements}),
                Pointer.to(Pointer.to(hostInputA)),
                Pointer.to(deviceInputB),
                Pointer.to(deviceOutput)
        );



        // Call the kernel function.
        int blockSizeX = 256;
        int gridSizeX = (int) Math.ceil((double) numElements / blockSizeX);
        cuLaunchKernel(function,
                gridSizeX, 1, 1,      // Grid dimension
                blockSizeX, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );

        cuCtxSynchronize();

        // Allocate host output memory and copy the device output
        // to the host.
        float hostOutput[] = new float[numElements];
        cuMemcpyDtoH(Pointer.to(hostOutput), deviceOutput,
                numElements * Sizeof.FLOAT);

        // Verify the result
        boolean passed = true;
        for (int i = 0; i < numElements; i++) {
            float expected = i + i;
            if (Math.abs(hostOutput[i] - expected) > 1e-5) {
                System.out.println(
                        "At index " + i + " found " + hostOutput[i] +
                                " but expected " + expected);
                passed = false;
                break;
            }
        }
        System.out.println("Test " + (passed ? "PASSED" : "FAILED"));

        // Clean up.
       // cuMemFree(deviceInputA);
        cuMemFree(deviceInputB);
        cuMemFree(deviceOutput);
    }

    public static void main(String[] args) throws IOException {

        testGPU();

        levaLegenda = new ArrayList<>();

        readInput("40x30.txt");



        sirka = horniLegenda.length;
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
                    if (Math.random() < CROSSINTERVAL && 1 < ISLANDS) {
                        System.out.println("Prenos z " + i);
                        for (int j = 1; j < ISLANDS; j++) {
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
                            Islands[i].populace.get(j).spoctiFitness();
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
