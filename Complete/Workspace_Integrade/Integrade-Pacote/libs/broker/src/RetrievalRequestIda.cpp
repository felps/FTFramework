#include "RetrievalRequestIda.hpp"
#include "OppStoreUtils.hpp"
#include "OppStoreUtilsSSL.hpp"
#include "Benchmark.hpp"
#include "BrokerLogger.hpp"

#include <sstream>
#include <openssl/sha.h>
#include <cassert>

RetrievalRequestIda::RetrievalRequestIda(DataRetrievalManager *dataRetrievalManager) {
    this->dataRetrievalManager_ = dataRetrievalManager;
    //this->neededFragments_      = neededFragments;
    //this->totalFragments_       = totalFragments;
    this->idaImpl               = IDAImpl::getInstance();
    this->dataInfoFreed         = false;
}

RetrievalRequestIda::~RetrievalRequestIda() {
}

int RetrievalRequestIda::retrieveData (const char *key, const char *outputPath, void * & data, long & dataSize, void(*appCallback)(int), bool wait, const unsigned char *encKey) {
  
    // Currently the broker only supports blocking retrieval calls
    assert (wait == true);
  
    brokerLogger.debug( "RetrievalRequestIda::retrieveData" ); 
          
    CodedDataInfo *dataInfo = new CodedDataInfo();
    dataInfo->encKey = encKey;
    dataInfo->appCallback = appCallback;
    dataInfo->operationCallback = this;
    if (wait == true) {
        dataInfo->callerMutex = new pthread_mutex_t;
        pthread_mutex_init (dataInfo->callerMutex, NULL);
        dataInfo->callerCond  = new pthread_cond_t;
        pthread_cond_init (dataInfo->callerCond, NULL);
    }
    dataInfo->fileKey = new char[OppStoreUtils::binaryKeySize];
    OppStoreUtils::convertHexToBinary(key, (char *)dataInfo->fileKey);
    //SHA1( (unsigned char*)id, strlen(id), (unsigned char*)dataInfo->fileKey);
    dataInfo->filePath = outputPath;

    //Benchmark::getCurrentBenchmark()->nFragments = totalFragments_;
    //Benchmark::getCurrentBenchmark()->nRecover   = neededFragments_;
                
    /**
     * Request data retrieval.
     */ 
    if (wait == true)                
        pthread_mutex_lock( dataInfo->callerMutex );
    
    Benchmark::getCurrentBenchmark()->locationStart = OppStoreUtils::timeInMillis();
    this->dataRetrievalManager_->retrieveData( dataInfo );
    Benchmark::getCurrentBenchmark()->locationFinish = OppStoreUtils::timeInMillis();
    
    int status = 0;
    if (wait == true) {
        pthread_cond_wait( dataInfo->callerCond, dataInfo->callerMutex );
        pthread_mutex_unlock( dataInfo->callerMutex );
        status = dataInfo->status;
        
        brokerLogger.debug("RetrievalRequestIda -> Thread released!");
           
        /**
         * Tests the recovered data
         */         
        if (dataInfo->status >= 0 && dataInfo->filePath == NULL) {
        	char *recoveredKey = new char[OppStoreUtils::binaryKeySize];
        	SHA1( (unsigned char*)dataInfo->data, dataInfo->dataSize, (unsigned char*)recoveredKey);
        	
        	cout << "DataSize: " << dataInfo->dataSize << endl;
        	
            {
            char *key = new char[OppStoreUtils::binaryKeySize*2+1]; 
            OppStoreUtils::convertBinaryToHex(dataInfo->fileKey, key);
            key[OppStoreUtils::binaryKeySize*2] = 0;

            cout << "fileKey:      ";
            OppStoreUtils::printHexKey(key, cout);
            cout << endl;
            }

            {
            char *key = new char[OppStoreUtils::binaryKeySize*2+1]; 
            OppStoreUtils::convertBinaryToHex(recoveredKey, key);
            key[OppStoreUtils::binaryKeySize*2] = 0;

            cout << "recoveredKey: ";
            OppStoreUtils::printHexKey(key, cout);
            cout << endl;
            }

               
//        	for (int i=0; i<OppStoreUtils::binaryKeySize; i++)
//        		if( ((char *)recoveredKey)[i] != ((char *)dataInfo->fileKey)[i] ) {
//        			cerr << "ERROR: RetrievalRequestIda -> checksum for file does not match at position " << i << endl;
//        			ostringstream logStr;
//        			logStr << "ERROR: RetrievalRequestIda -> checksum for file does not match at position " << i; 
//        			brokerLogger.debug( logStr.str() );                 
//        			exit(-1);
//        		}
                           	
        }
        
    	data = dataInfo->data;
    	dataSize = dataInfo->dataSize;        	        	
        releaseMemory(dataInfo);
    }    
    
    brokerLogger.debug("RetrievalRequestIda::retrieveData -> Finished recovering data!");
    
    return status;
}

void RetrievalRequestIda::releaseMemory( CodedDataInfo *dataInfo ) {
    
    this->dataInfoFreed = true;
    
    delete[] dataInfo->fileKey;
    if (dataInfo->callerMutex != NULL) delete dataInfo->callerMutex;
    if (dataInfo->callerCond  != NULL) delete dataInfo->callerCond;
    delete dataInfo;
    
}

void RetrievalRequestIda::finishedDataOperation( CodedDataInfo *dataInfo, int status ) {
       
	dataInfo->status = status;
        
    /**
     * Releases the waiting thread.
     */
    brokerLogger.debug("RetrievalRequestIda -> Releasing calling thread!");
    void (*appCallback) (int) = dataInfo->appCallback;
    if (dataInfo->callerMutex != NULL && dataInfo->callerCond != NULL) {        
        pthread_mutex_lock( dataInfo->callerMutex );
          pthread_cond_signal( dataInfo->callerCond );                 
        pthread_mutex_unlock( dataInfo->callerMutex );       
    }
    else {
        releaseMemory(dataInfo);
    }

    //cout << "Calling back application" << endl << flush;
    
    if (appCallback != NULL)
        appCallback(status);    
}

