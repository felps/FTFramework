#include "StorageRequestIda.hpp"
#include "OppStoreUtils.hpp"
#include "OppStoreUtilsSSL.hpp"
#include "Benchmark.hpp"
#include "BrokerLogger.hpp"

#include <sstream>
#include <openssl/sha.h>
#include <cstring>
 
StorageRequestIda::StorageRequestIda(int neededFragments, int totalFragments, DataStorageManager *dataStorageManager) {
    
    this->dataStorageManager_ = dataStorageManager;
    this->neededFragments_    = neededFragments;
    this->totalFragments_     = totalFragments;
    this->idaImpl             = IDAImpl::getInstance();
}

StorageRequestIda::~StorageRequestIda() {}

int StorageRequestIda::storeData (char * & key, const char *inputPath, void *data, long dataSize, void(*appCallback)(int), bool wait, const unsigned char *encKey, bool storeGlobal, int timeoutMinutes) {

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

    dataInfo->remainingCodings = new int; // DIFFERENT
    dataInfo->neededFragments = this->neededFragments_; // DIFFERENT
    int fragmentSize = (dataSize%neededFragments_ == 0) ? dataSize/neededFragments_ : dataSize/neededFragments_+1; // DIFFERENT
    
    //Benchmark::getCurrentBenchmark()->fragmentSize = fragmentSize;
    
    unsigned char *fragmentShaSource = new unsigned char[OppStoreUtils::binaryKeySize+1];
    for (int i=0; i<OppStoreUtils::binaryKeySize; i++)
        fragmentShaSource[i] = dataInfo->fileKey[i]; 

    /**
     * Complete the dataInfo fields.
     */
    for (int i=0; i < totalFragments_ + 1; i++) {
            	
    	long availableBytes = fragmentSize;
        dataInfo->availableBytes.push_back( &availableBytes );
        if (i==0)
        	dataInfo->fragmentSizeList.push_back( dataSize );
        else
        	dataInfo->fragmentSizeList.push_back( fragmentSize );

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
         fragmentKey[0] = 256/totalFragments_ * i;
         fragmentKey[OppStoreUtils::binaryKeySize-1] = 256/totalFragments_ * i;
    }

    if (wait == true)                
        pthread_mutex_lock( dataInfo->callerMutex );
    
    Benchmark::getCurrentBenchmark()->locationStart = OppStoreUtils::timeInMillis();
    this->dataStorageManager_->storeData(dataInfo, storeGlobal);
    Benchmark::getCurrentBenchmark()->locationFinish = OppStoreUtils::timeInMillis();
    
    int status;
    if (wait == true) {
       pthread_cond_wait( dataInfo->callerCond, dataInfo->callerMutex );
       pthread_mutex_unlock( dataInfo->callerMutex );
       status = dataInfo->status;
    }
    else {
        OppStoreUtils::waitVar(100, dataInfo->remainingCodings);
        // It waits because the fileKey is set in the end?
        // What is the stored file Key? The random or the newly generated one?
        // The newly generated can be passed to application callback
        // Where is fileKey updated?
    }
    
    key = new char[OppStoreUtils::binaryKeySize*2+1]; 
    OppStoreUtils::convertBinaryToHex(dataInfo->fileKey, key);
    key[OppStoreUtils::binaryKeySize*2] = 0;               

    if (wait == true)
    	releaseMemory(dataInfo);
    
    return status;
}

void StorageRequestIda::releaseMemory( CodedDataInfo *dataInfo ) {

    for (int i=0; i<totalFragments_; i++) {
        //delete[] dataInfo->encData[i];
        delete[] dataInfo->fragmentKeyList[i];
    }
    delete[] dataInfo->fileKey;
    delete dataInfo->remainingUploads;
    delete dataInfo->remainingCodings;
    if (dataInfo->callerMutex != NULL) delete dataInfo->callerMutex;
    if (dataInfo->callerCond  != NULL) delete dataInfo->callerCond;
    delete dataInfo->dataReaderThread;
    delete dataInfo;
    
    //delete encodingThread;
}

void StorageRequestIda::finishedDataOperation( CodedDataInfo *dataInfo, int status ) {
	
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
