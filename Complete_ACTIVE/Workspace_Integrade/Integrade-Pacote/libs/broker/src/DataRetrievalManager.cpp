#include "DataRetrievalManager.hpp"
#include "OppStoreUtils.hpp"
#include "FragmentDownloadThread.hpp"
#include "Benchmark.hpp"
#include "BrokerLogger.hpp"

#include <sstream>
#include <openssl/sha.h>

DataRetrievalManager::DataRetrievalManager() {
    
}

DataRetrievalManager::~DataRetrievalManager() {
    
}

void DataRetrievalManager::setCdrmRequestStub(CdrmRequestsStub *cdrmRequestsStub, AdrDataTransferStub * adrDataTransferStub){

    this->cdrmRequestsStub_    = cdrmRequestsStub;
    this->brokerIor_           = AccessBrokerSkeleton::singleInstance().getIor();  
    this->adrDataTransferStub_ = adrDataTransferStub;
    
    FragmentDownloadThread::configureDownloadThreads(adrDataTransferStub, this);
}

CodedDataInfo *DataRetrievalManager::getCodedDataInfo ( int requestId ) {
    
    CodedDataInfo *dataInfo = NULL;
    for (int i=0; i<10 && dataInfo == NULL; i++) {
        if ( requestInfoMap.find(requestId) != requestInfoMap.end() )
            dataInfo = requestInfoMap[requestId];
        else
            OppStoreUtils::sleep(100*(i+1));
    }               
    return dataInfo;
}

void DataRetrievalManager::retrieveData( CodedDataInfo *dataInfo ) {

    int requestId = cdrmRequestsStub_->requestFileRetrieval(dataInfo->fileKey, this->brokerIor_);
    requestInfoMap[requestId] = dataInfo;

    { 
        ostringstream logStr;
        logStr << "Requested data retrieval with requestId=" << requestId << "."; 
        brokerLogger.debug( logStr.str() ); 
    }                                

}

void DataRetrievalManager::downloadFragments
( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList, int dataSize, vector<int> fragmentSizeList, int nNeededFragments ) {

    { 
        ostringstream logStr;
        logStr << "Received download fragment message for request " << requestId << " with nNeededFragments=" << nNeededFragments << ".";
        brokerLogger.debug( logStr.str() ); 
    }                                

    CodedDataInfo *dataInfo = getCodedDataInfo(requestId);
    dataInfo->dataSize = dataSize;
    dataInfo->neededFragments = nNeededFragments;
    dataInfo->adrAddresses.assign( adrAddresses.begin(), adrAddresses.end() );
    dataInfo->fragmentKeyList.assign( fragmentKeyList.begin(), fragmentKeyList.end() );
    dataInfo->fragmentSizeList.assign( fragmentSizeList.begin(), fragmentSizeList.end() );
            
    if (fragmentSizeList.size() > 0) 
        Benchmark::getCurrentBenchmark()->fragmentSize = fragmentSizeList[0];         
    Benchmark::getCurrentBenchmark()->fragmentSize = dataSize;        
                            
    /**
     * Uploads fragment data, launching one thread per fragment.
     */
    int * requestIdPtr = new int;
    *requestIdPtr = requestId;     
    //FragmentDownloadThread::launchDownloadThread(requestIdPtr);
    pthread_t thread1;
    pthread_create( &thread1, NULL, FragmentDownloadThread::launchDownloadThread, requestIdPtr );
    pthread_detach(thread1);
}

void DataRetrievalManager::setFileRetrievalRequestFailed ( int requestId ) {
    
    { 
        ostringstream logStr;
        logStr << "Data retrieval request " << requestId << " failed."; 
        brokerLogger.debug( logStr.str() ); 
    }                                

    /**
     * Waits the method retrieveData to put dataInfo into requestInfoMap 
     */ 
    for (int i=0; i<10 && requestInfoMap.find(requestId) == requestInfoMap.end(); i++)
    	sleep(1);    
    
    CodedDataInfo *dataInfo = requestInfoMap.find(requestId)->second;    
    if (dataInfo->operationCallback != NULL)
        dataInfo->operationCallback->finishedDataOperation(dataInfo, -1);    
} 
