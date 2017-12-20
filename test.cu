extern "C"

__global__ void test(int *output)
{
    int i = blockDim.x * blockIdx.x + threadIdx.x ;

    if(i<vyskaSirka[2])
        output[i] = legenda1D[i];
}