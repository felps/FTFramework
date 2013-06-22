#ifndef STORAGEREQUESTIDA_HPP_
#define STORAGEREQUESTIDA_HPP_

#include "ida/IDAImpl.h"
#include "CodedDataInfo.hpp"
#include "DataStorageManager.hpp"
#include "ida/IDAEncodingThread.hpp"

class StorageRequestIda : public DataOperationCallback {
    
    int totalFragments_;
    int neededFragments_;
    DataStorageManager *dataStorageManager_;
    IDAImpl *idaImpl;
    
    IDAEncodingThread *encodingThread;
    
    void releaseMemory( CodedDataInfo *dataInfo );        
    
public:
	StorageRequestIda(int neededFragments, int totalFragments, DataStorageManager *dataStorageManager);
	virtual ~StorageRequestIda();
   
    int storeData (char * & key, const char *inputPath, void *data, long dataSize, void(*appCallback)(int), bool wait, const unsigned char *encKey, bool storeGlobal, int timeoutMinutes);
    
    void finishedDataOperation( CodedDataInfo *dataInfo, int status );
};

#endif /*STORAGEREQUESTMANAGER_HPP_*/
