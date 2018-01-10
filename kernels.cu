
__constant__ int legendU[2500]; // upper legends, concatenated
__constant__ int sizesOfLegendsU[100]; // sizes of each of upper legends
__constant__ int shiftsOfLegendsU[100]; // prefix sums of sizes, e.g. where legends begins

__constant__ int legendL[2500]; // left legends, concatenated
__constant__ int sizesOfLegendsL[100]; // sizes of each of left legends
__constant__ int shiftsOfLegendsL[100]; // prefix sums of sizes, e.g. where legends begins

__constant__ int heightWidth[4]; // height, width, height*width of puzzle, population size
__constant__ int numberOfMutations = 4;

// optimize
__device__ void mutate(int * gridPopulation, int * randomCross, int index);
__device__ void cross(int * gridPopulation, int * gridChildren, int i1, int i2, int * randomCross);
__device__ int* copy(int * gridPopulation, int i1); // just for testing

// count fintess
__device__ void needlemanParallel(int * fitness, int* legend1D, int  sizeOfLegend, int beginningOfLegend, int * sliceIntRepr, int sliceSize);

// update
__device__ void overwrite( int * gridPopulation, int * gridChildren, int parent, int child);
__device__ int difference(int * population, int * children, int p, int c);

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

// replaces parents by children according to their fitness
extern "C"
__global__ void updatePopulation(int * gridPopulation, int * gridChildren, int * fitness, int * fitnessChildren, int * randomSelection)
{
    __shared__ int differenceArray[256];

    int ind = blockDim.x * blockIdx.x + threadIdx.x ; // number of individual in population
    int ind2 = randomSelection[ind]; // the other individual

    int p,pp,c,cc;

    // each children computes difference of two pairs of child and parents
    if(ind < ind2 ){ // p1+c2, p2+c1
        p = ind;
        c = ind2;
        pp = ind2;
        cc = ind;
    }else{           // p1+c1, c2+p2
        p = ind;
        c = ind;
        pp = ind2;
        cc = ind2;
    }

    differenceArray[ind] = difference(gridPopulation, gridChildren, p, c);
    differenceArray[ind] += difference(gridPopulation, gridChildren, pp, cc);

    __syncthreads();

    if(differenceArray[ind] > differenceArray[ind2]){ // should I compare with direct or other parent?
         if (fitness[p] <= fitnessChildren[c]) {
          fitness[p] = fitnessChildren[c];
         p = p * heightWidth[2]; // index in population -> index in array
         c = c * heightWidth[2];
         cc = cc * heightWidth[2];
         overwrite(gridPopulation, gridChildren, p, c);

        }
    } else {
       if (fitness[p] <= fitnessChildren[cc]) {
            fitness[p] = fitnessChildren[cc];
            p = p * heightWidth[2]; // index in population -> index in array
            c = c * heightWidth[2];
            cc = cc * heightWidth[2];
           overwrite(gridPopulation, gridChildren, p, cc);

       }
    }
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


// overwrites
__device__ void overwrite( int * gridPopulation, int* gridChildren, int p, int c){
    for (int i = 0; i < heightWidth[2]; i++) {
        gridPopulation[p + i] = gridChildren[c + i];
    }
}

// counts how simmillar is parent to child
__device__ int difference(int * population, int* children, int p, int c){

    int diff = 0;
    p = p*heightWidth[2];
    c = c*heightWidth[2];

    for (int i = 0; i < heightWidth[2]; i++) {
        if (children[c + i] != population[p + i]) {
            diff++;
        }
    }

    return diff;
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
__device__ void needlemanParallel(int * fitness, int* legend1D, int  sizeOfLegend, int beginningOfLegend, int* sliceIntRepr, int sliceSize){

    int ** H = new int*[sliceSize];

    for(int i = 0; i < sliceSize; i++){
        H[i] = new int[sizeOfLegend];
    }

    H[0][0] = 0;

    for (int i = 1; i < sliceSize; i++) {
        H[i][0] = H[i - 1][0] - sliceIntRepr[i];
    }

    for (int i = 1; i < sizeOfLegend ; i++) {
        H[0][i] = H[0][i - 1] - legend1D[beginningOfLegend+i];
    }

    //---------------

    for (int j = 1; j < sizeOfLegend; j++) {

        int legendJ = legend1D[ beginningOfLegend+ j];

        for (int i = 1; i < sliceSize; i++) {

            H[i][j] = max(H[i - 1][j    ] - sliceIntRepr[i],
                      max(H[i    ][j - 1] - legendJ,
                          H[i - 1][j - 1] - abs(legendJ - sliceIntRepr[i])));
        }
    }

    atomicAdd(fitness, H[sliceSize - 1][sizeOfLegend - 1]);

    for(int i =0 ; i < sliceSize;i++){
        free(H[i]);
    }

    free(H);
    free(sliceIntRepr);
}






///
/// Single thread computation of fitness

__device__ int countFitness(int * gridPopulation, int index);
__device__ int fitnessColumn(int * gridPopulation, int index, int column);
__device__ int fitnessRow(int * gridPopulation, int index, int row);
__device__ int needleman(int* legend1D, int  sizeOfLegend, int beginningOfLegend, int* gridSlice, int sliceSize);

extern "C"
__global__ void evolution(int * gridPopulation, int * gridChildren, int * fitness, int * fitnessChildren, int* randomCross, int * randomSelection){

     __shared__ int differenceArray[256];

    int ind = blockDim.x * blockIdx.x + threadIdx.x ; // number of individual in population
    int ind2 = randomSelection[ind]; // the other individual



   cross(gridPopulation, gridChildren, ind * heightWidth[2], ind2 * heightWidth[2], randomCross);

   mutate(gridChildren, randomCross, ind * heightWidth[2]);



    fitnessChildren[ind] = countFitness(gridChildren, ind * heightWidth[2]);


    __syncthreads();

    int p,pp,c,cc;

    // each children computes difference of two pairs of child and parents
    if(ind < ind2 ){ // p1+c2, p2+c1
        p = ind;
        c = ind2;
        pp = ind2;
        cc = ind;
    }else{           // p1+c1, c2+p2
        p = ind;
        c = ind;
        pp = ind2;
        cc = ind2;
    }

    differenceArray[ind] = difference(gridPopulation, gridChildren, p, c);
    differenceArray[ind] += difference(gridPopulation, gridChildren, pp, cc);

    __syncthreads();

    if(differenceArray[ind] > differenceArray[ind2]){ // should I compare with direct or other parent?

         if (fitness[p] <= fitnessChildren[c]) {
             fitness[p] = fitnessChildren[c];
             overwrite(gridPopulation, gridChildren, ind * heightWidth[2], c* heightWidth[2]);
        }

    } else {
       if (fitness[p] <= fitnessChildren[cc]) {
            fitness[p] = fitnessChildren[cc];
           overwrite(gridPopulation, gridChildren, ind * heightWidth[2], cc* heightWidth[2]);
       }
    }



}

__device__ int countFitness(int * gridPopulation, int index ){

    int fitness = 0;

    for (int column = 0; column < heightWidth[1]; column++) { // sloupce
        fitness += fitnessColumn(gridPopulation, index, column);
    }

    for (int row = 0; row < heightWidth[0]; row++) { // radky
        fitness += fitnessRow(gridPopulation, index, row);
    }

    return fitness;
}


__device__ int fitnessColumn(int * gridPopulation, int index, int column){

    int sliceSize = 1;
    int combo = 0;

    int * gridSlice = new int [heightWidth[0]];
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

    return needleman(legendU,        sizesOfLegendsU[column], shiftsOfLegendsU[column],  gridSlice,    sliceSize);
}


__device__ int fitnessRow(int * gridPopulation, int index, int row){

    int sliceSize = 1;
    int combo = 0;

    int * gridSlice = new int [heightWidth[1]];
    gridSlice[0] = 0;

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

    return needleman(legendL, sizesOfLegendsL[row],  shiftsOfLegendsL[row],  gridSlice,   sliceSize);
}


__device__ int needleman(int* legend1D, int sizeOfLegend, int beginningOfLegend, int* gridSlice, int sliceSize){

    int ** H = new int*[sliceSize];

    for(int i =0; i < sliceSize; i++){
        H[i] = new int[sizeOfLegend];
    }

    H[0][0] = 0;

    for (int i = 1; i < sliceSize; i++) {
        H[i][0] = H[i - 1][0] - gridSlice[i];
    }

    for (int i = 1; i < sizeOfLegend ; i++) {
        H[0][i] = H[0][i - 1] - legend1D[beginningOfLegend+i];
    }

    //---------------

    for (int j = 1; j < sizeOfLegend; j++) {

        int legendJ = legend1D[ beginningOfLegend+ j];

        for (int i = 1; i < sliceSize; i++) {

            H[i][j] = max(H[i - 1][j    ] - gridSlice[i],
                      max(H[i    ][j - 1] - legendJ,
                          H[i - 1][j - 1] - abs(legendJ - gridSlice[i])));
        }
    }

    int subFitness = H[sliceSize - 1][sizeOfLegend - 1];

    for(int i =0 ; i < sliceSize;i++){
        free(H[i]);
    }

    free(H);
    free(gridSlice);

    return subFitness;

}
