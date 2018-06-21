
__constant__ int heightWidth[4]; // height, width, height*width of puzzle, population size


__device__ void overwrite( int * gridPopulation, int * gridChildren, int parent, int child);
__device__ int difference(int * population, int * children, int p, int c);

// replaces parents by children according to their fitness
extern "C"
__global__ void updatePopulation(int * gridPopulation, int * gridChildren, int * fitness, int * fitnessChildren, int * randomSelection)
{
    __shared__ int differenceArray[320];

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