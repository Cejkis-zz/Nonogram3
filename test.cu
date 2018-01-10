
__constant__ int legenda1DH[2500];
__constant__ int velikostiLegendH[100];
__constant__ int posunyLegendH[100];

__constant__ int legenda1DL[2500];
__constant__ int velikostiLegendL[100];
__constant__ int posunyLegendL[100];

__constant__ int vyskaSirka[3]; // vyska sirka

__device__ int countFitness(int * tajenkaPopulace);
__device__ int fitnessColumn(int * tajenkaPopulace, int index, int sloupec);
__device__ int fitnessRow(int* tajenkaPopulace, int index, int radek);
__device__ int needleman(int* Legenda1D, int * velikostiLegend, int * posunyLegend, int* tajenkaVyrezKomprimovany,  int sloupRad, int velikostTajenky);

extern "C"
__global__ void countFitness(int * tajenkaPopulace, int * fitness)
{
    int ind = blockDim.x * blockIdx.x + threadIdx.x ;
    int p1 = ind * vyskaSirka[2];
    fitness[ind]  = countFitness(p1);  // TODO parallel^2
}


__device__ int countFitness(int * tajenkaPopulace){

    int fitness = 0;

    for (int i = 0; i < vyskaSirka[1]; i++) { // sloupce
        fitness += fitnessColumn(tajenkaPopulace, 0, i);
    }

    for (int i = 0; i < vyskaSirka[0]; i++) { // radky
        fitness += fitnessRow(tajenkaPopulace, 0, i);
    }

    return fitness;
}


__device__ int fitnessColumn(int * tajenkaPopulace, int index, int sloupec){

    int velikostTajenky = 1;
    int kombo = 0;

    int * tajenkaVyrezKomprimovany = new int [vyskaSirka[0]];
    tajenkaVyrezKomprimovany[0] = 0;

    for (int i = 0; i < vyskaSirka[0]; i++) {
        if (tajenkaPopulace[index + i*vyskaSirka[1] + sloupec] == 1) {
            kombo++;
        } else {
            if (kombo != 0) {
                tajenkaVyrezKomprimovany[velikostTajenky++] = kombo;
            }
            kombo = 0;
        }
    }

    if (kombo != 0) {
        tajenkaVyrezKomprimovany[velikostTajenky++] = kombo; // posledni ctverecek je cerny
    }
    return needleman(legenda1DL,        velikostiLegendL,      posunyLegendL,       tajenkaVyrezKomprimovany,       sloupec,     velikostTajenky);
}

__device__ int fitnessRow(int* tajenkaPopulace, int index, int radek){

    int velikostTajenky = 1;
    int kombo = 0;

    int * tajenkaVyrezKomprimovany = new int [vyskaSirka[1]];
    tajenkaVyrezKomprimovany[0] = 0;

    for (int i = 0; i < vyskaSirka[1]; i++) {
        if (tajenkaPopulace[index + radek*vyskaSirka[0] + i] == 1) {
            kombo++;
        } else {
            if (kombo != 0) {
                tajenkaVyrezKomprimovany[velikostTajenky++] = kombo;
            }
            kombo = 0;
        }
    }

    if (kombo != 0) {
        tajenkaVyrezKomprimovany[velikostTajenky++] = kombo; // posledni ctverecek je cerny
    }

    return needleman(legenda1DH,        velikostiLegendH,      posunyLegendH,       tajenkaVyrezKomprimovany,       radek,      velikostTajenky);
}


__device__ int needleman(int* legenda1D, int * velikostiLegend, int * posunyLegend, int* tajenkaVyrezKomprimovany,  int sloupRad, int velikostTajenky){

    int velikostLegendy = velikostiLegendH[sloupRad];
    int zacatekLegendy = posunyLegendH[sloupRad];

    int ** H = new int*[velikostTajenky];

    for(int i =0; i < velikostTajenky; i++){
        H[i] = new int[velikostLegendy];
    }

    H[0][0] = 0;

    for (int i = 1; i < velikostTajenky; i++) {
        H[i][0] = H[i - 1][0] - tajenkaVyrezKomprimovany[i];
    }

    for (int i = 1; i < velikostLegendy ; i++) {
        H[0][i] = H[0][i - 1] - legenda1D[zacatekLegendy+i];
    }

    //---------------

    for (int j = 1; j < velikostLegendy; j++) {

        int legendaJ = legenda1DH[ zacatekLegendy+ j];

        for (int i = 1; i < velikostTajenky; i++) {

            H[i][j] = max(H[i - 1][j    ] - tajenkaVyrezKomprimovany[i],
                      max(H[i    ][j - 1] - legendaJ,
                          H[i - 1][j - 1] - abs(legendaJ - tajenkaVyrezKomprimovany[i])));
        }
    }

    for(int i =0 ; i < velikostTajenky;i++){
        free(H[i]);
    }

    free(H);
    free(tajenkaVyrezKomprimovany);

    return H[velikostTajenky - 1][velikostLegendy - 1];
}


