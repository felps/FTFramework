#include "Benchmark.hpp"

#include <iostream>
#include <fstream>
using namespace std;

Benchmark *currentBench = NULL;

Benchmark::Benchmark() { 
    nFragments = 0;
    nFragmentTimes = 0;
    fragmentStart = NULL;
    fragmentFinish = NULL;
}

Benchmark::~Benchmark() { 
    if (fragmentStart != NULL)
        delete[] fragmentStart;
    if (fragmentFinish != NULL)        
        delete[] fragmentFinish;
}

Benchmark *Benchmark::getCurrentBenchmark() {
    if (currentBench == NULL) createNewBenchmark();
    return currentBench;
}
 
void Benchmark::writeBenchmarkToFile(Benchmark *benchmark, char *filename) {
    
    ofstream outFile(filename, ios::app);
    outFile << "id=" << benchmark->benchmarkId << " dataSize=" << benchmark->dataSize << ":" << benchmark->fragmentSize 
            << " ida=" << benchmark->nRecover << ":" << benchmark->nFragments
            << " request="  << benchmark->requestStart  << ":" << benchmark->requestFree << ":" << benchmark->requestFinish
            << " location=" << benchmark->locationStart << ":" << benchmark->locationFinish 
            << " ida=" << benchmark->idaStart << ":" << benchmark->idaFinish << " fragments=";
    for (int i=0; i<benchmark->nFragmentTimes-1; i++)
        outFile << benchmark->fragmentStart[i] << ":" << benchmark->fragmentFinish[i] << ":";
    if (benchmark->nFragmentTimes > 0)        
        outFile << benchmark->fragmentStart[benchmark->nFragmentTimes-1] << ":" << benchmark->fragmentFinish[benchmark->nFragmentTimes-1];
    
    outFile << endl;

    for (unsigned int i=0; i<benchmark->fragmentAddress.size(); i++)
        outFile << benchmark->fragmentAddress[i] << " ";
    outFile << endl;    
    
    outFile.close();
}
    
Benchmark *Benchmark::createNewBenchmark() {
    if (currentBench != NULL)
        delete currentBench;

    currentBench = new Benchmark();           
    
    return currentBench;
}
