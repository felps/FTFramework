#include "OppStoreBroker.h"
#include "OppStoreUtils.hpp"
#include "Benchmark.hpp"
#include "FragmentDownloadThread.hpp"

#include <cstdlib>
#include <cassert>
#include <string>
#include <iostream>
#include <vector>

#include <openssl/sha.h>

char *performFileStorage(char *data, long dataSize) {

    std::cout << "testFileStorage -> Started storing data with size " << dataSize << "!" << std::endl;        
    char *key;
    storeDataW(key, data, dataSize, NULL);
    std::cout << "testFileStorage -> Finished storing data!" << std::endl;
    
    return key;
}

void performFileRetrieval(char *key) {
    
    std::cout << "testFileRetrieval -> Started recovering data!" << std::endl;
    void *recoveredData;
    long recoveredSize;    
    retrieveDataW( key, recoveredData, recoveredSize, NULL );
    std::cout << "testFileRetrieval -> Finished recovering data! nbytes=" << recoveredSize << std::endl;
        
     std::cout << "Removing recovered data!" << std::endl;   
     delete[] (char *)recoveredData;
}

void *timeCounter( void *ptr ) {
    sleep( *((int *)ptr) );
    exit(0);
}

int main(int argc, char **argv) {

    launchBroker();
    std::cout << "Finished launching broker!" << std::endl;
    
    if (argc < 4)
        return 0;

    long dataSize = atoi(argv[1]);
    long waitTime = atoi(argv[2]);
    long expId    = atoi(argv[3]);
    
    srand (expId);

    {
        int *waitTimePtr = new int;
        *waitTimePtr = waitTime;
             
        pthread_t thread1;
        pthread_create( &thread1, NULL, timeCounter, waitTimePtr );
        pthread_detach(thread1);
    } 
        
    char *data = new char [dataSize];
    for (long pos=0; pos<dataSize; pos++)
        data[pos] = rand()%256;

    OppStoreUtils::resetClock();            
    Benchmark *benchmark = Benchmark::createNewBenchmark();
    benchmark->benchmarkId = expId;                        
    benchmark->dataSize = dataSize; 
    benchmark->requestStart = OppStoreUtils::timeInMillis();        
        
    char *key = performFileStorage(data, dataSize);
        
    benchmark->requestFinish = OppStoreUtils::timeInMillis();
    Benchmark::writeBenchmarkToFile(benchmark, "bench.dat");
       
    delete[] data;
        
    cout << "Finished storage " << expId << " with dataSize " << (dataSize/1000) << " kbytes." << endl;
    cout << "_____________________________________________________________________________________________________" << endl << endl << endl;

    /**
     * Perform data retrieval
     **/     
    for (int r=0; r<2; r++) { 
        vector<string> allowedAdrs;
        if (r==0) {
            allowedAdrs.push_back("143.107.45.184:9003"); // Giga
            allowedAdrs.push_back("143.107.45.212:9003"); // GSD
            allowedAdrs.push_back("127.0.0.1:9003"); // GSD
        }    
        else if (r==1) {
            allowedAdrs.push_back("143.107.45.111:4001"); // Eclipse
            allowedAdrs.push_back("200.137.197.139:9003"); // UFG
            allowedAdrs.push_back("127.0.0.1:9003"); // GSD
        }    
        
        FragmentDownloadThread::setAllowedAdrs(allowedAdrs);
                               
        OppStoreUtils::resetClock();            
        Benchmark *benchmark = Benchmark::createNewBenchmark();
        benchmark->benchmarkId = expId;                        
        benchmark->requestStart = OppStoreUtils::timeInMillis();        
            
        performFileRetrieval(key);        
                    
        benchmark->requestFree   = OppStoreUtils::timeInMillis();
        benchmark->requestFinish = OppStoreUtils::timeInMillis();
        Benchmark::writeBenchmarkToFile(benchmark, "bench.dat");
                    
        cout << "Finished recovering " << expId << " with dataSize " << (dataSize/1000) << " kbytes." << endl;
        cout << "_____________________________________________________________________________________________________" << endl << endl << endl;

    }
        
    std::cout << "Test finished succesfully!" << std::endl;
    
    sleep(2);
        
    return 0;
}
