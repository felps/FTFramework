#include "RemovalRenewalRequest.hpp"
#include "OppStoreUtils.hpp"
#include "OppStoreUtilsSSL.hpp"
#include "Benchmark.hpp"
#include "BrokerLogger.hpp"

#include <sstream>
#include <openssl/sha.h>
#include <cstring>
 
RemovalRenewalRequest::RemovalRenewalRequest(DataRemovalRenewalManager *dataRemovalRenewalManager) {
    
    this->dataRemovalRenewalManager_ = dataRemovalRenewalManager;
}

RemovalRenewalRequest::~RemovalRenewalRequest() {}

int RemovalRenewalRequest::removeData (char * & key, void(*appCallback)(int), bool wait) {

    { 
        ostringstream logStr;
        logStr << "RemovalRenewalRequestManager::removeData."; 
        brokerLogger.debug( logStr.str() ); 
    }        
    
    /**
     * Creates dataInfo and initializes its fields
     */
    CodedDataInfo *dataInfo = new CodedDataInfo();
    dataInfo->appCallback = appCallback;
    dataInfo->operationCallback = this; 
    dataInfo->operationType = OPP_REMOVAL;
    dataInfo->fileKey = new char[OppStoreUtils::binaryKeySize];
    OppStoreUtils::convertHexToBinary(key, (char *)dataInfo->fileKey); 
    
    if (wait == true) {
        dataInfo->callerMutex = new pthread_mutex_t;
        pthread_mutex_init (dataInfo->callerMutex, NULL);
        dataInfo->callerCond  = new pthread_cond_t;
        pthread_cond_init (dataInfo->callerCond, NULL);
    }
    
    if (wait == true)                
        pthread_mutex_lock( dataInfo->callerMutex );
    
    this->dataRemovalRenewalManager_->removeData(dataInfo);
    
    int status = 0;
    if (wait == true) {
       pthread_cond_wait( dataInfo->callerCond, dataInfo->callerMutex );
       pthread_mutex_unlock( dataInfo->callerMutex );
       status = dataInfo->status;
       releaseMemory(dataInfo);
    }
    
    return status;
}

int RemovalRenewalRequest::renewDataLease (char * & key, void(*appCallback)(int), bool wait, int timeoutMinutes) {

    { 
        ostringstream logStr;
        logStr << "RemovalRenewalRequestManager::renewDataLease."; 
        brokerLogger.debug( logStr.str() ); 
    }        
    
    /**
     * Creates dataInfo and initializes its fields
     */
    CodedDataInfo *dataInfo = new CodedDataInfo();
    dataInfo->appCallback = appCallback;
    dataInfo->operationCallback = this; 
    dataInfo->timeoutMinutes = timeoutMinutes;
    dataInfo->operationType = OPP_RENEWAL;
    dataInfo->fileKey = new char[OppStoreUtils::binaryKeySize];
    OppStoreUtils::convertHexToBinary(key, (char *)dataInfo->fileKey);  
    
    if (wait == true) {
        dataInfo->callerMutex = new pthread_mutex_t;
        pthread_mutex_init (dataInfo->callerMutex, NULL);
        dataInfo->callerCond  = new pthread_cond_t;
        pthread_cond_init (dataInfo->callerCond, NULL);
    }
    
    if (wait == true)                
        pthread_mutex_lock( dataInfo->callerMutex );
    
    this->dataRemovalRenewalManager_->renewDataLease(dataInfo);
    
    int status = 0;
    if (wait == true) {
       pthread_cond_wait( dataInfo->callerCond, dataInfo->callerMutex );
       pthread_mutex_unlock( dataInfo->callerMutex );
       status = dataInfo->status;
       releaseMemory(dataInfo);
    }
    
    return status;
}

void RemovalRenewalRequest::releaseMemory( CodedDataInfo *dataInfo ) {

    delete[] dataInfo->fileKey;
    delete dataInfo->remainingDownloads;
    if (dataInfo->callerMutex != NULL) delete dataInfo->callerMutex;
    if (dataInfo->callerCond  != NULL) delete dataInfo->callerCond;
    delete dataInfo;
    
}

void RemovalRenewalRequest::finishedDataOperation( CodedDataInfo *dataInfo, int status ) {
	
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
