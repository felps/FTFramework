#ifndef FRAGMENTDOWNLOADTHREAD_HPP_
#define FRAGMENTDOWNLOADTHREAD_HPP_

#include "CodedDataInfo.hpp"
#include "AdrDataTransferStub.hpp"
#include "DataRetrievalManager.hpp"
#include "DataWriterThread.hpp"

#include <vector>
using namespace std;

class FragmentDownloadThread
{
    
	int sockfd;
    string ipAddress;
    short portNumber;
    
    const char *fileKey;
    char *hashKey;
    int neededFragments;
    
    //CodedDataInfo *dataInfo;
    
    DataWriterThread *dataWriterThread;
    
    int fragmentSize;
    int fragmentPos;
    int fragmentNumber;
    
    int *remainingDownloads;
    int *nRecovered;
    
    FragmentDownloadThread( int sockfd_, string ipAddress_, short portNumber_, int fragmentNumber_,  int nCaches, CodedDataInfo *dataInfo_, int fragmentPos_, int *nRecovered_ );
    
    static int downloadDataFromCache( CodedDataInfo *dataInfo );
    
public:
	virtual ~FragmentDownloadThread();
    
    static void *run( void *ptr );
    
    /**
     * Used only in the experiments
     */
    static int setAllowedAdrs(vector<string> & allowedAdrs);
    
    static void *launchDownloadThread( void *ptr );
    
    static void configureDownloadThreads( AdrDataTransferStub *adrDataTransferStub_, DataRetrievalManager *dataRetrievalManager_ );
    
    void performDownload();
};

#endif /*FRAGMENTDOWNLOADTHREAD_HPP_*/
