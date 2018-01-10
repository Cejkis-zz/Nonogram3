package com.company;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import java.util.ArrayList;
import java.util.Collections;

import static com.company.Main.*;
import static jcuda.driver.JCudaDriver.*;
import static jcuda.driver.JCudaDriver.cuMemcpyDtoH;

public class GPUCrowdingIsland extends Island {

    CUdeviceptr population_D = new CUdeviceptr();
    CUdeviceptr children_D = new CUdeviceptr();
    CUdeviceptr fitness_D = new CUdeviceptr();
    CUdeviceptr fitnessChildren_D = new CUdeviceptr();
    CUdeviceptr randomCross_D = new CUdeviceptr();
    CUdeviceptr randomSelection_D = new CUdeviceptr();

    int[] fitness = new int[popSize];
    int[] randomCross = new int[gridSize];
    int[] randomSelection = new int[popSize];

    boolean unitedComputation = true;

    ArrayList<Integer> selection = new ArrayList<>();

    // inits population, computes its fitness
    public GPUCrowdingIsland() {

        int[] populace = new int[popSize * gridSize];

        for (int i = 0; i < populace.length; i++) {
            if (Math.random()<0.5)
                populace[i] = 0;
            else
                populace[i] = 1;
        }

        for (int i = 0; i < gridSize; i++) {
            randomCross[i] = i;
        }

        cuMemAlloc(population_D, popSize * gridSize * Sizeof.INT);
        cuMemAlloc(children_D, popSize * gridSize * Sizeof.INT);
        cuMemAlloc(fitness_D, popSize * Sizeof.INT);
        cuMemAlloc(fitnessChildren_D, popSize * Sizeof.INT);
        cuMemAlloc(randomCross_D, gridSize * Sizeof.INT);
        cuMemAlloc(randomSelection_D, popSize * Sizeof.INT);

        cuMemcpyHtoD(population_D, Pointer.to(populace), popSize * gridSize * Sizeof.INT);

        // init fitness values
        Pointer kernelParameters = Pointer.to(Pointer.to(population_D), Pointer.to(fitness_D));

        cuLaunchKernel(fitnessOfAllColumnsFunction,
                popSize , 1, 1,      // Grid dimension
                width , 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );

        cuLaunchKernel(fitnessOfAllRowsFunction,
                popSize , 1, 1,      // Grid dimension
                height , 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );


        cuMemcpyDtoH(Pointer.to(fitness), fitness_D, popSize * Sizeof.INT);

    }

    @Override
    public void printStatistics() {

        int bestFit = -100000;
        float sum = 0;

        for (int i = 0; i < popSize; i++) {
            //System.out.printStatistics(fitness[i]);
            sum += fitness[i];
            System.out.print(fitness[i] + ", ");
            if (bestFit < fitness[i]) {
                bestFit = fitness[i];
            }
        }

        System.out.println(" best: " + bestFit + " avg: " + sum / popSize);

    }

    public void optimise(int g) {

        // shuffle randomCross numbers
        for (int j = 0; j < gridSize; j++) {
            int shuffle = randomCross[j];
            int r = (int) (Math.random() * gridSize);
            randomCross[j] = randomCross[r];
            randomCross[r] = shuffle;
        }

        selection = new ArrayList<>();

        // shuffle randomSelection numbers
        for (int i = 0; i < popSize; i++) {
            selection.add(i);
        }

        Collections.shuffle(selection);

        for (int j = 0; j < popSize; j+=2) {
            int i1 = selection.remove(0);
            int i2 = selection.remove(0);
            randomSelection[i1] = i2;
            randomSelection[i2] = i1;
        }

        cuMemcpyHtoD(randomSelection_D, Pointer.to(randomSelection), popSize * Sizeof.INT);
        cuMemcpyHtoD(randomCross_D,     Pointer.to(randomCross),     gridSize * Sizeof.INT);

        if (unitedComputation){ // alternative kernel computing everything for one individual and child

            Pointer kernelParameters = Pointer.to(Pointer.to(population_D),Pointer.to(children_D),Pointer.to(fitness_D), Pointer.to(fitnessChildren_D) ,Pointer.to(randomCross_D),Pointer.to(randomSelection_D));

            cuLaunchKernel(evolutionFunction,
                    popSize, 1, 1,      // Grid dimension
                    1, 1, 1,      // Block dimension
                    2048, null,  kernelParameters, null // Kernel- and extra parameters
            );

        }else{ //

            Pointer kernelParameters1 = Pointer.to(Pointer.to(population_D),Pointer.to(children_D), Pointer.to(randomCross_D),Pointer.to(randomSelection_D));

            cuLaunchKernel(createChildrenFunction,
                    popSize, 1, 1,      // Grid dimension
                    1, 1, 1,      // Block dimension
                    0, null,  kernelParameters1, null // Kernel- and extra parameters
            );

            // reset children
            cuMemcpyHtoD(fitnessChildren_D, Pointer.to(new int[popSize]), popSize *  Sizeof.INT);

            Pointer fitnesskernelParameters2 = Pointer.to(Pointer.to(children_D), Pointer.to(fitnessChildren_D));
            cuLaunchKernel(fitnessOfAllColumnsFunction,
                    popSize , 1, 1,      // Grid dimension
                    width , 1, 1,      // Block dimension
                    0, null, fitnesskernelParameters2, null); // Kernel- and extra parameters
            cuLaunchKernel(fitnessOfAllRowsFunction,
                    popSize , 1, 1,
                    height , 1, 1,
                    0, null, fitnesskernelParameters2, null); // Kernel- and extra parameters

            Pointer updateKernelParameters3 = Pointer.to(Pointer.to(population_D),Pointer.to(children_D), Pointer.to(fitness_D), Pointer.to(fitnessChildren_D), Pointer.to(randomSelection_D) );

            cuLaunchKernel(updatePopulationFunction,
                    popSize, 1, 1,      // Grid dimension
                    1, 1, 1,      // Block dimension
                    1024, null,  updateKernelParameters3, null); // Kernel- and extra parameters
        }


        cuMemcpyDtoH(Pointer.to(fitness), fitness_D, popSize * Sizeof.INT);

    }

}
