#ifndef BENCHMARK_HPP_
#define BENCHMARK_HPP_

#include <vector>
#include <string>
using namespace std;

class Benchmark {

    Benchmark();

public:
    virtual ~Benchmark();

    int benchmarkId; // *
        
    int dataSize; // *
    int fragmentSize; // *
    
    int nFragments; // *
    int nRecover; // *
     
    long requestStart; // *
    long requestFree; //
    long requestFinish; // *
      
    long locationStart; // *
    long locationFinish; // *
    
    long idaStart; // *
    long idaFinish; // *
    
    long *fragmentStart; // *
    long *fragmentFinish; // *
    int nFragmentTimes;
    
    vector<string> fragmentAddress; // *
    
    static Benchmark *getCurrentBenchmark();
 
    static void writeBenchmarkToFile(Benchmark *benchmark, char *filename);
    
    static Benchmark *createNewBenchmark();
    
};

#endif /*BENCHMARK_HPP_*/
