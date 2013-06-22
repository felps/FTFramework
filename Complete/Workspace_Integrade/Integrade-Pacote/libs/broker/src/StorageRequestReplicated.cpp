#include "StorageRequestReplicated.hpp"
#include "OppStoreUtils.hpp"
#include "OppStoreUtilsSSL.hpp"
#include "Benchmark.hpp"
#include "BrokerLogger.hpp"
#include "DataReaderThread.hpp"

#include <sstream>
#include <openssl/sha.h>
#include <cstring>
 
StorageRequestReplicated::StorageRequestReplicated(int numberOfReplicas, DataStorageManager *dataStorageManager) {
    
    this->dataStorageManager_ = dataStorageManager;
    this->numberOfReplicas_ = numberOfReplicas;
}

StorageRequestReplicated::~StorageRequestReplicated() {}

int StorageRequestReplicated::storeData (char * & key, const char *inputPath, void *data, long dataSize, void(*appCallback)(int), bool wait, const unsigned char *encKey, bool storeGlobal, int timeoutMinutes) {

    { 
        ostringstream logStr;
        logStr << "StorageRequestManager::storeData dataSize = " << dataSize << "."; 
        brokerLogger.debug( logStr.str() ); 
    }        
    
    /**
     * Creates dataInfo and initializes its fields
     */
    CodedDataInfo *dataInfo = new CodedDataInfo();
    dataInfo->appCallback = appCallback;
    dataInfo->operationCallback = this;
    if (wait == true) {
        dataInfo->callerMutex = new pthread_mutex_t;
        pthread_mutex_init (dataInfo->callerMutex, NULL);
        dataInfo->callerCond  = new pthread_cond_t;
        pthread_cond_init (dataInfo->callerCond, NULL);
    }
    dataInfo->dataSize = dataSize;
    dataInfo->data = data;
    dataInfo->encKey = encKey; 
    dataInfo->timeoutMinutes = timeoutMinutes;
    dataInfo->filePath = inputPath;

    dataInfo->fileKey = new char[OppStoreUtils::binaryKeySize];
    OppStoreUtils::generateRandomKey((unsigned char *)dataInfo->fileKey);
        
//    if (inputPath == NULL)
//    	dataInfo->dataReaderThread = DataReaderThread::createDataReaderThread((unsigned char *)data, dataSize, encKey);
//    else
//    	dataInfo->dataReaderThread = DataReaderThread::createDataReaderThreadFromFile(inputPath, dataSize, encKey);
    dataInfo->neededFragments = 1; // CHANGED
    dataInfo->remainingCodings = 0; // CHANGED

    //Benchmark::getCurrentBenchmark()->nFragments = totalFragments_;
    //Benchmark::getCurrentBenchmark()->nRecover   = neededFragments_;    
    //Benchmark::getCurrentBenchmark()->fragmentSize = fragmentSize;
    
    unsigned char *fragmentShaSource = new unsigned char[OppStoreUtils::binaryKeySize+1];
    for (int i=0; i<OppStoreUtils::binaryKeySize; i++)
        fragmentShaSource[i] = dataInfo->fileKey[i]; 

    /**
     * Complete the dataInfo fields.
     */
    for (int i=0; i<numberOfReplicas_; i++) {
                
        //dataInfo->encData.push_back( (char *)data ); // CHANGED -> data obtained from DataReaderThread
        dataInfo->availableBytes.push_back( &dataSize ); // CHANGED        
        dataInfo->fragmentSizeList.push_back( dataSize ); // CHANGED

        /**
         * Generates a random key for the fragment.
         * Uses the file key as the basis for generating the fragment key.
         */
        fragmentShaSource[OppStoreUtils::binaryKeySize] = i;                        
        unsigned char *fragmentKey = new unsigned char[OppStoreUtils::binaryKeySize]; 
        SHA1( fragmentShaSource, OppStoreUtils::binaryKeySize+1, fragmentKey );
        dataInfo->fragmentKeyList.push_back( (char*)fragmentKey );
        
        /**
         * Distributes the fragments ids homogeneously over the id space.
         * Used in experiments to force fragments into different and deterministic clusters.
         */
         //fragmentKey[0] = 256/totalFragments_ * i;
         //fragmentKey[OppStoreUtils::binaryKeySize-1] = 256/totalFragments_ * i;
    }

    if (wait == true)                
        pthread_mutex_lock( dataInfo->callerMutex );
    
    //Benchmark::getCurrentBenchmark()->locationStart = OppStoreUtils::timeInMillis();
    this->dataStorageManager_->storeData(dataInfo, storeGlobal);
    //Benchmark::getCurrentBenchmark()->locationFinish = OppStoreUtils::timeInMillis();
    
    int status = 0;
    if (wait == true) {
       pthread_cond_wait( dataInfo->callerCond, dataInfo->callerMutex );
       pthread_mutex_unlock( dataInfo->callerMutex );
       status = dataInfo->status;
    }  
   
    key = new char[OppStoreUtils::binaryKeySize*2+1]; 
    OppStoreUtils::convertBinaryToHex(dataInfo->fileKey, key);
    key[OppStoreUtils::binaryKeySize*2] = 0;

    if (wait == true)
    	releaseMemory(dataInfo);       
       
    return status;
}

void StorageRequestReplicated::releaseMemory( CodedDataInfo *dataInfo ) {
	
    for (int i=0; i<numberOfReplicas_; i++) {
    	//delete[] dataInfo->encData[i]; // CHANGED
        delete[] dataInfo->fragmentKeyList[i];
    }
    delete[] dataInfo->fileKey;
    delete dataInfo->remainingUploads;
    delete dataInfo->dataReaderThread;
    // delete dataInfo->remainingCodings; // CHANGED
    if (dataInfo->callerMutex != NULL) delete dataInfo->callerMutex;
    if (dataInfo->callerCond  != NULL) delete dataInfo->callerCond;
    delete dataInfo;
    
    // delete encodingThread; // CHANGED
}

void StorageRequestReplicated::finishedDataOperation( CodedDataInfo *dataInfo, int status ) {

	dataInfo->status = status;
    void (*appCallback) (int) = dataInfo->appCallback;
    if (dataInfo->callerMutex != NULL && dataInfo->callerCond != NULL) {
            
        pthread_mutex_lock( dataInfo->callerMutex );
          pthread_cond_signal( dataInfo->callerCond );
       pthread_mutex_unlock( dataInfo->callerMutex );
    }
    else {
        releaseMemory(dataInfo);
    }
    if (appCallback != NULL)
        appCallback(status);
}
