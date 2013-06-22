#ifndef FRAGMENTUPLOADTHREAD_HPP_
#define FRAGMENTUPLOADTHREAD_HPP_

#include "AdrDataTransferStub.hpp"
#include "DataStorageManager.hpp"
#include "DataReaderThread.hpp"
#include "CodedDataInfo.hpp"
#include "utils/c++/CdrmRequestsStub.hpp"

#include <string>
#include <vector>
using namespace std;

class FragmentUploadThread
{
    //string ipAddress;
    //short portNumber;
    int neededFragments;
    
    unsigned char *fileKey;
    unsigned char *fragmentKey;
    
    //char *encData;
    int fragmentSize;
    int fragmentNumber;

    //long *availableBytes;
    int sockfd; // Socket connected to the ADR
    bool isCacheCopy;
    
    int *remainingUploads;
    int timeoutMinutes;
    
    DataReaderThread *dataReaderThread;
    
    FragmentUploadThread( int sockfd, unsigned char *fileKey, unsigned char *fragmentKey_, DataReaderThread *dataReaderThread_, int fragmentSize_, int fragmentNumber_, int nCached, int *remainingUploads_, int neededFragments_, int timeoutMinutes_);

    static int uploadFileToLocalCache( CodedDataInfo *dataInfo, vector<int> & notStoredFragments, vector<char *> & fragmentHashList );
    
public:
	
    static void *run( void *ptr );
    
    static void *launchUploadThread ( void *ptr );
    
    void performUpload();
    
    static void configureUploadThreads( CdrmRequestsStub *cdrmRequestsStub_, AdrDataTransferStub *adrDataTransferStub_, DataStorageManager *dataStorageManager_ );
    
	virtual ~FragmentUploadThread();
};

#endif /*FRAGMENTUPLOADTHREAD_HPP_*/
