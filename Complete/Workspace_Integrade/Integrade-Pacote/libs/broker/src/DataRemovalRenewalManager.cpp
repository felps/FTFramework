#include "DataRemovalRenewalManager.hpp"
#include "OppStoreUtils.hpp"
#include "FragmentOperationThread.hpp"
#include "Benchmark.hpp"
#include "BrokerLogger.hpp"

#include <sstream>
#include <openssl/sha.h>

DataRemovalRenewalManager::DataRemovalRenewalManager() {   
}

DataRemovalRenewalManager::~DataRemovalRenewalManager() {   
}

void DataRemovalRenewalManager::setCdrmRequestStub(CdrmRequestsStub *cdrmRequestsStub, AdrDataTransferStub * adrDataTransferStub){

    this->cdrmRequestsStub_    = cdrmRequestsStub;
    this->brokerIor_           = AccessBrokerSkeleton::singleInstance().getIor();  
    this->adrDataTransferStub_ = adrDataTransferStub;
    
    FragmentOperationThread::configureOperationThreads(adrDataTransferStub, this);
}

CodedDataInfo *DataRemovalRenewalManager::getCodedDataInfo ( int requestId ) {
    
    CodedDataInfo *dataInfo = NULL;
    for (int i=0; i<10 && dataInfo == NULL; i++) {
        if ( requestInfoMap.find(requestId) != requestInfoMap.end() )
            dataInfo = requestInfoMap[requestId];
        else
            OppStoreUtils::sleep(100*(i+1));
    }               
    return dataInfo;
}

void DataRemovalRenewalManager::removeData( CodedDataInfo *dataInfo ) {

    int requestId = cdrmRequestsStub_->requestFileRemoval(dataInfo->fileKey, this->brokerIor_);
    requestInfoMap[requestId] = dataInfo;

    { 
        ostringstream logStr;
        logStr << "Requested data removal with requestId=" << requestId << "."; 
        brokerLogger.debug( logStr.str() ); 
    }                                

}

void DataRemovalRenewalManager::renewDataLease( CodedDataInfo *dataInfo ) {

    int requestId = cdrmRequestsStub_->requestFileLeaseRenewal(dataInfo->fileKey, this->brokerIor_, dataInfo->timeoutMinutes);
    requestInfoMap[requestId] = dataInfo;

    { 
        ostringstream logStr;
        logStr << "Requested data lease renewal with requestId=" << requestId << "."; 
        brokerLogger.debug( logStr.str() ); 
    }                                

}


//====================================================================================

void DataRemovalRenewalManager::removeFragments( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList ) {

    { 
        ostringstream logStr;
        logStr << "Received remove fragment message for request=" << requestId << " numberOfAdrs=" << adrAddresses.size() << ".";
        brokerLogger.debug( logStr.str() ); 
    }                                

    CodedDataInfo *dataInfo = getCodedDataInfo(requestId);
    dataInfo->adrAddresses.assign( adrAddresses.begin(), adrAddresses.end() );
    dataInfo->fragmentKeyList.assign( fragmentKeyList.begin(), fragmentKeyList.end() );
    dataInfo->operationType = OPP_REMOVAL;
            
    /**
     * Uploads fragment data, launching one thread per fragment.
     */
    int * requestIdPtr = new int;
    *requestIdPtr = requestId;    
    
    pthread_t thread1;
    pthread_create( &thread1, NULL, FragmentOperationThread::launchOperationThread, requestIdPtr );
    pthread_detach(thread1);
}

void DataRemovalRenewalManager::renewFragmentLeases( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList, int timeout ) {

    { 
        ostringstream logStr;
        logStr << "Received renewal fragment message for request " << requestId << ".";
        brokerLogger.debug( logStr.str() ); 
    }                                

    CodedDataInfo *dataInfo = getCodedDataInfo(requestId);
    dataInfo->adrAddresses.assign( adrAddresses.begin(), adrAddresses.end() );
    dataInfo->fragmentKeyList.assign( fragmentKeyList.begin(), fragmentKeyList.end() );
    dataInfo->operationType = OPP_RENEWAL;
            
    /**
     * Uploads fragment data, launching one thread per fragment.
     */
    int * requestIdPtr = new int;
    *requestIdPtr = requestId;
    //FragmentDownloadThread::launchDownloadThread(requestIdPtr);
    
    pthread_t thread1;
    pthread_create( &thread1, NULL, FragmentOperationThread::launchOperationThread, requestIdPtr );
    pthread_detach(thread1);

}
