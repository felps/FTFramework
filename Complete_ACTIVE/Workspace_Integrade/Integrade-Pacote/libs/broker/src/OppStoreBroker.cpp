#include "OppStoreBroker.h"

#include "StorageRequestIda.hpp"
#include "RetrievalRequestIda.hpp"
#include "RemovalRenewalRequest.hpp"
#include "StorageRequestReplicated.hpp"
//#include "RetrievalRequestReplicated.hpp"

#include "BrokerServerManager.hpp"
#include "utils/c++/FileUtils.hpp"

/**
 * setAllowedAdrs
 */  
#include "FragmentDownloadThread.hpp"

#include <pthread.h>
#include <fcntl.h>

BrokerServerManager *brokerServerManager = NULL;

StorageRequestIda *storageRequestIda = NULL;
StorageRequestReplicated *storageRequestReplicated = NULL;
RemovalRenewalRequest *removalRenewalRequest = NULL;

RetrievalRequestIda *retrievalRequest = NULL;

int timeoutMinutesTmp = 1;

int launchBroker() {
	int numberOfReplicas = 1;
	
	brokerServerManager = new BrokerServerManager();    
    storageRequestIda   = new StorageRequestIda  ( 2, 5, brokerServerManager->getStorageManager()   );
    retrievalRequest = new RetrievalRequestIda( brokerServerManager->getRetrievalManager() );
    removalRenewalRequest = new RemovalRenewalRequest ( brokerServerManager->getRemovalRenewalManager() );
    storageRequestReplicated   = new StorageRequestReplicated  ( numberOfReplicas+1, brokerServerManager->getStorageManager()   );
    //retrievalRequestReplicated = new RetrievalRequestReplicated( 1, brokerServerManager->getRetrievalManager() );    
    return 0;
}

const unsigned char *currentEncKey = NULL;
void setEncryptionKey(const unsigned char *encKey) {
    currentEncKey = encKey;
}

int storeDataEphemeral(char * & key, void *data, long dataSize, void(*appCallback)(int)) {
    if (storageRequestIda == NULL) launchBroker();
    return storageRequestReplicated->storeData(key, NULL, data, dataSize, appCallback, false, currentEncKey, false, timeoutMinutesTmp);
}

int storeDataEphemeralW(char * & key, void *data, long dataSize, void(*appCallback)(int)) {
    if (storageRequestIda == NULL) launchBroker();
    return storageRequestReplicated->storeData(key, NULL, data, dataSize, appCallback, true, currentEncKey, false, timeoutMinutesTmp);
}

int storeFileEphemeral(char * & key, const char *filePath, void(*appCallback)(int)) {
    if (storageRequestIda == NULL) launchBroker();
    int inputFileHandler_ = open( filePath, O_RDONLY );
    if (inputFileHandler_ > 0) {     	
    	long fileSize = FileUtils::getFileSize( filePath );    	    	   	           
    	return storageRequestReplicated->storeData(key, filePath, NULL, fileSize, appCallback, false, currentEncKey, false, timeoutMinutesTmp);
    }
    return -1;
}

int storeFileEphemeralW(char * & key, const char *filePath, void(*appCallback)(int)) {
    if (storageRequestIda == NULL) launchBroker();
    int inputFileHandler_ = open( filePath, O_RDONLY );
    if (inputFileHandler_ > 0) {     	
    	long fileSize = FileUtils::getFileSize( filePath );    	    	   	           
        return storageRequestReplicated->storeData(key, filePath, NULL, fileSize, appCallback, true, currentEncKey, false, timeoutMinutesTmp);
    }
    return -1;
}

int storeData(char * & key, void *data, long dataSize, void(*appCallback)(int)) {
    if (storageRequestIda == NULL) launchBroker();
    return storageRequestIda->storeData(key, NULL, data, dataSize, appCallback, false, currentEncKey, true, timeoutMinutesTmp);
}

int storeDataW(char * & key, void *data, long dataSize, void(*appCallback)(int)) {
    if (storageRequestIda == NULL) launchBroker();
    return storageRequestIda->storeData(key, NULL, data, dataSize, appCallback, true, currentEncKey, true, timeoutMinutesTmp);
}

int storeFile(char * & key, const char *filePath, void(*appCallback)(int)) {
    if (storageRequestIda == NULL) launchBroker();
    int inputFileHandler_ = open( filePath, O_RDONLY );
    if (inputFileHandler_ > 0) {     	
    	long fileSize = FileUtils::getFileSize( filePath );    	    	   	           
    	return storageRequestIda->storeData(key, filePath, NULL, fileSize, appCallback, false, currentEncKey, true, timeoutMinutesTmp);
    }
    return -1;
}

int storeFileW(char * & key, const char *filePath, void(*appCallback)(int)) {
    if (storageRequestIda == NULL) launchBroker();
    int inputFileHandler_ = open( filePath, O_RDONLY );
    if (inputFileHandler_ > 0) {     	
    	long fileSize = FileUtils::getFileSize( filePath );    	    	   	           
        return storageRequestIda->storeData(key, filePath, NULL, fileSize, appCallback, true, currentEncKey, true, timeoutMinutesTmp);
    }
    return -1;
}

int retrieveDataW(const char * key, void * & data, long & dataSize, void(*appCallback)(int)) {
    if (retrievalRequest == NULL) launchBroker();
    return retrievalRequest->retrieveData(key, NULL, data, dataSize, appCallback, true, currentEncKey);    
}

int retrieveFileW(const char * key, const char *filePath, long & fileSize, void(*appCallback)(int)) {
    if (retrievalRequest == NULL) launchBroker();
    int outputFileHandler_ = open( filePath, O_WRONLY | O_CREAT, 0600 );
        
    if (outputFileHandler_ > 0) {
    	void *data = NULL;
        return retrievalRequest->retrieveData(key, filePath, data, fileSize, appCallback, true, currentEncKey);
    }
    return -1;
}

int removeDataW(char * & key, void(*appCallback)(int)) {
	if (removalRenewalRequest == NULL) launchBroker();
	return removalRenewalRequest->removeData(key, appCallback, true);
}

int renewStorageLeaseW(char * & key, void(*appCallback)(int)) {
	if (removalRenewalRequest == NULL) launchBroker();
	return removalRenewalRequest->renewDataLease(key, appCallback, true, timeoutMinutesTmp);
}

int setAllowedAdrs(vector<string> & allowedAdrs) {
    return FragmentDownloadThread::setAllowedAdrs(allowedAdrs);
}
