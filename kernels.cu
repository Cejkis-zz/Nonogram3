
// Constant memory
__constant__ int legendU[2500]; // upper legends, concatenated
__constant__ int sizesOfLegendsU[100]; // sizes of each of upper legends
__constant__ int shiftsOfLegendsU[100]; // prefix sums of sizes, e.g. where legends begins

__constant__ int legendL[2500]; // left legends, concatenated
__constant__ int sizesOfLegendsL[100]; // sizes of each of left legends
__constant__ int shiftsOfLegendsL[100]; // prefix sums of sizes, e.g. where legends begins

__constant__ int heightWidth[4]; // height, width, height*width of puzzle, population size
__constant__ int numberOfMutations = 4;

// create offspring
__device__ void mutate(int * gridPopulation, int * randomCross, int index);
__device__ void cross(int * gridPopulation, int * gridChildren, int i1, int i2, int * randomCross);

// count fintess
__device__ void needlemanParallel(int * fitness, int* legend1D, int  sizeOfLegend, int beginningOfLegend, int * gridSlice, int sliceSize);

/// Single thread computation of fitness
__device__ int countFitness(int * gridPopulation, int index);
__device__ int fitnessColumn(int * gridPopulation, int index, int column, int * H0, int * H1, int * gridSlice);
__device__ int fitnessRow(int * gridPopulation, int index, int row, int * H0, int * H1, int * gridSlice);
__device__ int needlemanOpt(int* legend1D, int  sizeOfLegend, int beginningOfLegend, int* gridSlice, int sliceSize, int * H0, int * H1);


// creates new generation of individuals and mutates them
// gridPopulation - current population
// gridChildren - array, in which new generation will be stored
// randomCross - random permutation of 1.. size of grid, determines which bits will be copied from which parent.
//                  also used for selection which bits to mutate.
// randomSelection - random permutation 1.. number of individuals, determines touples of parents
extern "C"
__global__ void createChildren(int * gridPopulation, int * gridChildren, int * randomCross, int * randomSelection)
{
    int ind = blockDim.x * blockIdx.x + threadIdx.x ; // number of individual in population

    int p1 = ind * heightWidth[2];       // beginning of grid
    int p2 = randomSelection[ind]* heightWidth[2]; // beginning of grid of the other parent

    cross(gridPopulation, gridChildren, p1, p2, randomCross);

    mutate(gridChildren, randomCross, p1);
}


// puts together a child from two parents, individuals bits are determined by permutation randomCross
__device__ void cross(int * gridPopulation, int * gridChildren, int parent1, int parent2, int * randomCross){

    for (int i = 0; i < heightWidth[2]/2 ; i++) {
        gridChildren[parent1 + randomCross[i]] = gridPopulation[parent1 + randomCross[i]];
    }

    for (int i = heightWidth[2]/2; i < heightWidth[2] ; i++) {
        gridChildren[parent1 + randomCross[i]] = gridPopulation[parent2 + randomCross[i]];
    }
}

// changes few bits in new individual
__device__ void mutate(int * gridChildren, int * randomPerm, int index){

    int x = randomPerm[0];

     for (int i = 0; i < numberOfMutations; i++) {

        x = randomPerm[x];
        gridChildren[index + x] = 1 - gridChildren[index + x];
    }
}


// creates representation of column, which is comparable to legend and runs NW function
extern "C"
__global__ void countFitnessOfAllColumns(int * gridPopulation, int * fitness){

    int columnIndex = blockDim.x * blockIdx.x + threadIdx.x ; // column number relative to all grids in population
    int numberOfIndividual = columnIndex / heightWidth[1]; // number in population
    int absStart = numberOfIndividual * heightWidth[2];  // start in array of population
    int column = columnIndex - numberOfIndividual * heightWidth[1]; // relative to grid

    int sliceSize = 1;

    int * columnIntRepr = new int [heightWidth[0]];

    columnIntRepr[0] = 0;
    int combo = 0;

    for (int i = 0; i < heightWidth[0]; i++) {
        if (gridPopulation[absStart + i*heightWidth[1] + column] == 1) {
            combo++;
        } else {
            if (combo != 0) {
                columnIntRepr[sliceSize++] = combo;
            }
            combo = 0;
        }
    }
    if (combo != 0) {
        columnIntRepr[sliceSize++] = combo; // for the case the last square is filled
    }

    needlemanParallel(&fitness[numberOfIndividual],legendU, sizesOfLegendsU[column], shiftsOfLegendsU[column], columnIntRepr, sliceSize);
}


// creates representation of row, which is comparable to legend and runs NW function
extern "C"
__global__ void countFitnessOfAllRows(int * gridPopulation, int * fitness){

    int absRadek = blockDim.x * blockIdx.x + threadIdx.x ;
    int numberOfIndividual = absRadek / heightWidth[0];
    int absStart = numberOfIndividual * heightWidth[2];  // index v tajence populace
    int row = absRadek - numberOfIndividual * heightWidth[0];

    int sliceSize = 1;
    int combo = 0;

    int * rowIntRepr = new int [heightWidth[1]];
    rowIntRepr[0] = 0;

    for (int i = 0; i < heightWidth[1]; i++) {
        if (gridPopulation[absStart + row*heightWidth[1] + i] == 1) {
            combo++;
        } else {
            if (combo != 0) {
                rowIntRepr[sliceSize++] = combo;
            }
            combo = 0;
        }
    }

    if (combo != 0) {
        rowIntRepr[sliceSize++] = combo; // for the case the last square is filled
    }

    needlemanParallel(&fitness[numberOfIndividual], legendL, sizesOfLegendsL[row], shiftsOfLegendsL[row], rowIntRepr, sliceSize);
}

// computes Needleman-Wunsch function, which measures the difference between two integer arrays and adds it to fitness.
// here, one array is legend of one row/column and the other is actual slice of individual's grid.
__device__ void needlemanParallel(int * fitness, int* legend, int sizeOfLegend, int shiftsOfLegends, int* sliceIntRepr, int sliceSize){

    int * H0 = new int[sliceSize];
    int * H1 = new int[sliceSize];

    int fitnessLocal = needlemanOpt(legend, sizeOfLegend,  shiftsOfLegends,  sliceIntRepr, sliceSize, H0, H1);

    atomicAdd(fitness, fitnessLocal);

    free(sliceIntRepr);
    free(H0);
    free(H1);
}




///
/// Single thread computation of evolution

extern "C"
__global__ void evolution(int * gridPopulation, int * gridChildren, int * fitness, int* fitnessChildren, int* randomCross, int * randomSelection){

    __shared__ int differenceArray[320];

    int ind = blockDim.x * blockIdx.x + threadIdx.x ; // number of individual in population
    int ind2 = randomSelection[ind]; // the other individual

    cross(gridPopulation, gridChildren, ind * heightWidth[2], ind2 * heightWidth[2], randomCross);

    mutate(gridChildren, randomCross, ind * heightWidth[2]);

    fitnessChildren[ind] = countFitness(gridChildren, ind * heightWidth[2]);

}

__device__ int countFitness(int * gridPopulation, int index ){

    int fitness = 0;
    int biggerSize = max(heightWidth[0], heightWidth[1])/2; // take the maximal possible size

    int * H0 = new int[biggerSize];
    int * H1 = new int[biggerSize];

    int * gridSlice = new int [biggerSize];
    gridSlice[0] = 0;

    for (int column = 0; column < heightWidth[1]; column++) { // sloupce
        fitness += fitnessColumn(gridPopulation, index, column, H0, H1, gridSlice);
    }

    for (int row = 0; row < heightWidth[0]; row++) { // radky
        fitness += fitnessRow(gridPopulation, index, row, H0, H1, gridSlice);
    }

    free(gridSlice);
    free(H0);
    free(H1);

    return fitness;
}


__device__ int fitnessColumn(int * gridPopulation, int index, int column, int * H0, int * H1, int * gridSlice){

    int sliceSize = 1;
    int combo = 0;

    gridSlice[0] = 0;

    for (int i = 0; i < heightWidth[0]; i++) {
        if (gridPopulation[index + i*heightWidth[1] + column] == 1) {
            combo++;
        } else {
            if (combo != 0) {
                gridSlice[sliceSize++] = combo;
            }
            combo = 0;
        }
    }

    if (combo != 0) {
        gridSlice[sliceSize++] = combo; // for the case the last square is filled
    }

    return needlemanOpt(legendU,        sizesOfLegendsU[column], shiftsOfLegendsU[column],  gridSlice,    sliceSize,  H0,  H1);
}


__device__ int fitnessRow(int * gridPopulation, int index, int row, int * H0, int * H1, int * gridSlice){

    int sliceSize = 1;
    int combo = 0;

    for (int i = 0; i < heightWidth[1]; i++) {
        if (gridPopulation[index + row*heightWidth[1] + i] == 1) {
            combo++;
        } else {
            if (combo != 0) {
                gridSlice[sliceSize++] = combo;
            }
            combo = 0;
        }
    }

    if (combo != 0) {
        gridSlice[sliceSize++] = combo; // for the case the last square is filled
    }
    return needlemanOpt(legendL, sizesOfLegendsL[row],  shiftsOfLegendsL[row],  gridSlice,   sliceSize, H0, H1);
}


__device__ int needlemanOpt(int* legend1D, int sizeOfLegend, int beginningOfLegend, int* gridSlice, int sliceSize, int* H0, int *H1){

    H0[0] = 0;
    H1[0] = 0;

    for (int i = 1; i < sliceSize; i++) {
        H0[i] = H0[i - 1] - gridSlice[i];
    }

    //---------------

    for (int j = 1; j < sizeOfLegend; j++) {

        int legendJ = legend1D[beginningOfLegend + j];

        H1[0] = H0[0] - legendJ;

        for (int i = 1; i < sliceSize; i++) {
            H1[i] = max(H1[i-1] - gridSlice[i],
                    max(H0[i  ] - legendJ,
                        H0[i-1] - abs(legendJ - gridSlice[i])));
        }

        int * swap = H0;
        H0 = H1;
        H1 = swap;
    }

    return  H0[sliceSize - 1]; // swapped, so H0;
}