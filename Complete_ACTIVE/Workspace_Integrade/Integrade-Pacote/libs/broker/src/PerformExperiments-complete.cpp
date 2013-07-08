#include "OppStoreBroker.h"
#include "OppStoreUtils.hpp"
#include "Benchmark.hpp"

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
    
//    unsigned char *fileKey1 = new unsigned char[OppStoreUtils::binaryKeySize];
//    OppStoreUtils::convertHexToBinary(key, (char *)fileKey1);
//    unsigned char *fileKey2 = new unsigned char[OppStoreUtils::binaryKeySize];    
//    SHA1( (unsigned char*)recoveredData, recoveredSize, (unsigned char*)fileKey2);
//   
//    for (int i=0; i<OppStoreUtils::binaryKeySize; i++)
//        assert( fileKey1[i] == fileKey2[i] );
     
     std::cout << "Removing recovered data!" << std::endl;   
     delete[] (char *)recoveredData;
     //delete[] fileKey1;
     //delete[] fileKey2;     
}

int main(int argc, char **argv) {

//    vector<string> allowedAdrs;
//    allowedAdrs.push_back("127.0.0.1:9003");
//    allowedAdrs.push_back("127.0.0.1:9004");
    //allowedAdrs.push_back("127.0.0.1:9005");
    //allowedAdrs.push_back("127.0.0.1:9006");
    //allowedAdrs.push_back("127.0.0.1:9007");

    launchBroker();
    std::cout << "Finished launching broker!" << std::endl;
    
    srand(12345);
    
    vector <char *> fileKeyList;
    
    vector <long> fileDataSizes;
    fileDataSizes.push_back(         100 * 1000 ); // 100kB
    fileDataSizes.push_back(         100 * 1000 );
    fileDataSizes.push_back(         100 * 1000 ); // 100kB
    fileDataSizes.push_back(         100 * 1000 );
    fileDataSizes.push_back(         100 * 1000 ); // 100kB
    fileDataSizes.push_back(         100 * 1000 );
    fileDataSizes.push_back(    1 * 1000 * 1000 ); // 1MB
    fileDataSizes.push_back(    1 * 1000 * 1000 );
    fileDataSizes.push_back(   10 * 1000 * 1000 ); // 10MB
    fileDataSizes.push_back(   10 * 1000 * 1000 );
//    fileDataSizes.push_back(  100 * 1000 * 1000 ); // 100MB
//    fileDataSizes.push_back(  100 * 1000 * 1000 );
//    fileDataSizes.push_back( 1000 * 1000 * 1000 ); // 500MB

    vector <long> dataSizesExperimentList;
    for (unsigned int k=0; k<fileDataSizes.size(); k++)
        dataSizesExperimentList.push_back( fileDataSizes[k] );

    /**
     * Perform data storage
     */ 
    for (unsigned int exp=0; exp<dataSizesExperimentList.size(); exp++) {

        long dataSize = dataSizesExperimentList[exp];

        char *data = new char [dataSize];
        for (long pos=0; pos<dataSize; pos++)
            data[pos] = rand()%256;

        OppStoreUtils::resetClock();            
        Benchmark *benchmark = Benchmark::createNewBenchmark();
        benchmark->benchmarkId = exp;                        
        benchmark->dataSize = dataSize; 
        benchmark->requestStart = OppStoreUtils::timeInMillis();        
        
        char *key = performFileStorage(data, dataSize);
        fileKeyList.push_back( key );
        
        benchmark->requestFinish = OppStoreUtils::timeInMillis();
        Benchmark::writeBenchmarkToFile(benchmark, "bench.dat");
        
        delete[] data;
        
        cout << "Finished storage " << exp << " with dataSize " << (dataSize/1000) << " kbytes." << endl;
        cout << "_____________________________________________________________________________________________________" << endl << endl << endl;
 

        /**
        * Perform data retrieval
        **/ 
    
        for (int r=0; r<2; r++) { 
            vector<string> allowedAdrs;
            if (r==0) {
                allowedAdrs.push_back("143.107.45.184:9003"); // Giga
                allowedAdrs.push_back("143.107.45.212:9003"); // GSD
                //allowedAdrs.push_back("127.0.0.1:4001"); // GSD
            }    
            else if (r==1) {
                allowedAdrs.push_back("143.107.45.111:4001"); // Eclipse
                allowedAdrs.push_back("200.137.197.139:9003"); // UFG
                //allowedAdrs.push_back("127.0.0.1:4001"); // GSD
            }    
            
            //setAllowedAdrs(allowedAdrs);
             
              
            long dataSize = dataSizesExperimentList[exp];
                   
            OppStoreUtils::resetClock();            
            Benchmark *benchmark = Benchmark::createNewBenchmark();
            benchmark->benchmarkId = 1000 + exp;                        
            benchmark->requestStart = OppStoreUtils::timeInMillis();        
            
            performFileRetrieval(key);        
                    
            benchmark->requestFree   = OppStoreUtils::timeInMillis();
            benchmark->requestFinish = OppStoreUtils::timeInMillis();
            Benchmark::writeBenchmarkToFile(benchmark, "bench.dat");
                    
           cout << "Finished recovering " << exp << " with dataSize " << (dataSize/1000) << " kbytes." << endl;
           cout << "_____________________________________________________________________________________________________" << endl << endl << endl;

        }
    }
    
    std::cout << "Test finished succesfully!" << std::endl;
    
    sleep(2);
        
    return 0;
}
