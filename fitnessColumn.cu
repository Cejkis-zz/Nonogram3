
__constant__ int legenda1DH[2500];
__constant__ int velikostiLegendH[100];
__constant__ int posunyLegendH[100];

__constant__ int legenda1DL[2500];
__constant__ int velikostiLegendL[100];
__constant__ int posunyLegendL[100];

__constant__ int vyskaSirka[3]; // vyska sirka


extern "C"
__global__ void fitnessPerColumn(int * tajenka1D, int * fitness)
{

    int sloupec = blockDim.x * blockIdx.x + threadIdx.x ;

    int velikostTajenky = 1;

    int * tajenkaSloupec = new int [vyskaSirka[0]];

    tajenkaSloupec[0] = 0;
    int kombo = 0;

    for (int i = 0; i < vyskaSirka[0]; i++) {
        if (tajenka1D[i*vyskaSirka[1] + sloupec] == 1) {
            kombo++;
        } else {
            if (kombo != 0) {
                tajenkaSloupec[velikostTajenky++] = kombo;
            }
            kombo = 0;
        }
    }

    if (kombo != 0) {
        tajenkaSloupec[velikostTajenky++] = kombo; // posledni ctverecek je cerny
    }

    //////////////////////////////////////////////////

    int velikostLegendy = velikostiLegendH[sloupec];
    int zacatekLegendy = posunyLegendH[sloupec];

    int ** H = new int*[velikostTajenky];

    for(int i = 0; i < velikostTajenky; i++){
        H[i] = new int[velikostLegendy];
    }

    H[0][0] = 0;

    for (int i = 1; i < velikostTajenky; i++) {
        H[i][0] = H[i - 1][0] - tajenkaSloupec[i];
    }

    for (int i = 1; i < velikostLegendy ; i++) {
        H[0][i] = H[0][i - 1] - legenda1DH[zacatekLegendy+i];
    }

    //---------------

    for (int j = 1; j < velikostLegendy; j++) {
        int legendaJ = legenda1DH[ zacatekLegendy+ j];

        for (int i = 1; i < velikostTajenky; i++) {

            H[i][j] = max(H[i - 1][j    ] - tajenkaSloupec[i],
                      max(H[i    ][j - 1] - legendaJ,
                          H[i - 1][j - 1] - abs(legendaJ - tajenkaSloupec[i])));
        }
    }


    atomicAdd(fitness, H[velikostTajenky - 1][velikostLegendy - 1]);

    for(int i =0 ; i < velikostTajenky;i++){
        free(H[i]);
    }

    free(H);
    free(tajenkaSloupec);
}



__device__ int getfive(){
    return 5;
}


