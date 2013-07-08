#include "DataStorageManager.hpp"
#include "FragmentUploadThread.hpp"
#include "OppStoreUtils.hpp"
#include "Benchmark.hpp"
#include "BrokerLogger.hpp"

#include <sstream>

DataStorageManager::DataStorageManager()
{
}

DataStorageManager::~DataStorageManager()
{
}

void DataStorageManager::setCdrmRequestStub( CdrmRequestsStub *cdrmRequestsStub, AdrDataTransferStub *adrDataTransferStub ) {
 
    this->cdrmRequestsStub_    = cdrmRequestsStub;
    this->brokerIor_           = AccessBrokerSkeleton::singleInstance().getIor();  
    this->adrDataTransferStub_ = adrDataTransferStub;
    
    FragmentUploadThread::configureUploadThreads(cdrmRequestsStub, adrDataTransferStub, this);
}
       
void DataStorageManager::storeData( CodedDataInfo *dataInfo, bool storeGlobal ) {
        
    int requestId = cdrmRequestsStub_->requestFileStorage(
                        dataInfo->fileKey, dataInfo->fragmentKeyList, dataInfo->dataSize, dataInfo->fragmentSizeList, dataInfo->neededFragments,
                        this->brokerIor_, dataInfo->timeoutMinutes, storeGlobal);
    requestInfoMap[requestId] = dataInfo;

    { 
        ostringstream logStr;
        logStr << "Requested data storage with requestId=" << requestId << ".";
        brokerLogger.debug( logStr.str() ); 
    }        

}
    
CodedDataInfo *DataStorageManager::getCodedDataInfo ( int requestId ) {
    
    CodedDataInfo *dataInfo = NULL;
    for (int i=0; i<10 && dataInfo == NULL; i++) {
        if ( requestInfoMap.find(requestId) != requestInfoMap.end() )
            dataInfo = requestInfoMap[requestId];
        else
            OppStoreUtils::sleep(100*(i+1));
    }               
    return dataInfo;
}
        
void DataStorageManager::uploadFragments ( int requestId, vector<string> adrAddresses ) {

    { 
        ostringstream logStr;
        logStr << "Received upload fragment message! requestId=" << requestId << "."; 
        brokerLogger.debug( logStr.str() ); 
    }                            

    CodedDataInfo *dataInfo = this->getCodedDataInfo( requestId ); 
    dataInfo->adrAddresses.assign( adrAddresses.begin(), adrAddresses.end() );
                            
    /**
     * Uploads fragment data, launching one thread per fragment.
     */
    int * requestIdPtr = new int;
    *requestIdPtr = requestId;
    //FragmentUploadThread::launchUploadThread(requestIdPtr);
    pthread_t thread1;
    pthread_create( &thread1, NULL, FragmentUploadThread::launchUploadThread, requestIdPtr );
    pthread_detach(thread1);

}    
        
void DataStorageManager::setFileStorageRequestCompleted( int requestId ) {

    { 
        ostringstream logStr;
        logStr << "File storage completed for request " << requestId << "."; 
        brokerLogger.debug( logStr.str() ); 
    }                                

    /**
     * Waits the method storeData to put dataInfo into requestInfoMap 
     */ 
    for (int i=0; i<10 && requestInfoMap.find(requestId) == requestInfoMap.end(); i++)
    	sleep(1);    

    CodedDataInfo *dataInfo = requestInfoMap.find(requestId)->second;
    if (dataInfo->operationCallback != NULL)
        dataInfo->operationCallback->finishedDataOperation(dataInfo, 0);

}

void DataStorageManager::setFileStorageRequestFailed( int requestId ) {

    { 
        ostringstream logStr;
        logStr << "File storage FAILED for request " << requestId << "."; 
        brokerLogger.debug( logStr.str() ); 
    }                                

    /**
     * Waits the method storeData to put dataInfo into requestInfoMap 
     */ 
    for (int i=0; i<10 && requestInfoMap.find(requestId) == requestInfoMap.end(); i++)
    	sleep(1);    

    CodedDataInfo *dataInfo = requestInfoMap.find(requestId)->second;
    if (dataInfo->operationCallback != NULL)
        dataInfo->operationCallback->finishedDataOperation(dataInfo, -1);

}
