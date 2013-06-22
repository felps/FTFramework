#ifndef CODEDDATAINFO_HPP_
#define CODEDDATAINFO_HPP_

#include <string>
#include <vector>
#include <pthread.h>
#include "DataReaderThread.hpp"
#include "DataWriterThread.hpp"
using namespace std;

#define OPP_REMOVAL 1
#define OPP_RENEWAL 2

class DataOperationCallback {
public:    
    virtual ~DataOperationCallback() {}
    virtual void finishedDataOperation( class CodedDataInfo *dataInfo, int status ) = 0;
};

class CodedDataInfo {
public:
    char *fileKey;
    vector<char *> encData;

    DataReaderThread *dataReaderThread;
    DataWriterThread *dataWriterThread;
    const char *filePath;
    
    // Used to control the amount of bytes already coded/downloaded
    vector<long *> availableBytes;

    vector<int> fragmentSizeList;
    vector<char *> fragmentKeyList;
    vector<string> adrAddresses;
    
    // Used by RetrievalRequest
    vector<int> recoveredFragmentIndexes;    
    int neededFragments;
    void *data;
    long dataSize;
    
    int status; // Indicates if the storage or retrieval request was succesfull
    
    int timeoutMinutes;
    
    const unsigned char *encKey;
    
    int *remainingUploads;
    int *remainingDownloads;
    int *remainingCodings;
    
    int operationType;
    
    DataOperationCallback *operationCallback;
    void (*appCallback) (int);
    pthread_mutex_t *callerMutex;
    pthread_cond_t  *callerCond;
    
    CodedDataInfo() : appCallback (NULL), callerMutex(NULL), callerCond(NULL) {}  
};

#endif /*CODEDDATAINFO_HPP_*/
