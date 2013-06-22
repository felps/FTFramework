#ifndef STORAGEREQUESTREPLICATED_HPP_
#define STORAGEREQUESTREPLICATED_HPP_

#include "CodedDataInfo.hpp"
#include "DataStorageManager.hpp"

class StorageRequestReplicated : public DataOperationCallback {
        
    int numberOfReplicas_;
    DataStorageManager *dataStorageManager_;
    
    void releaseMemory( CodedDataInfo *dataInfo );
    
public:
	StorageRequestReplicated(int numberOfReplicas, DataStorageManager *dataStorageManager);
	virtual ~StorageRequestReplicated();
   
    int storeData (char * & key, const char *inputPath, void *data, long dataSize, void(*appCallback)(int), bool wait, const unsigned char *encKey, bool storeGlobal, int timeoutMinutes);
    
    void finishedDataOperation( CodedDataInfo *dataInfo, int status );
};

#endif /*STORAGEREQUESTMANAGER_HPP_*/
