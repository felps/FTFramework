#include "FragmentOperationThread.hpp"
#include "OppStoreUtils.hpp"
#include "Benchmark.hpp"
#include "utils/c++/Config.hpp"
#include "utils/c++/NoSuchConfigException.hpp"
#include "BrokerLogger.hpp"

#include <sstream>
#include <openssl/sha.h>
#include <cassert>

#include <set>
#include <iostream>
#include <string>
using namespace std;

map<int, FragmentOperationThread *> operationThreadMap;
int nextOperationThreadId = 1;

AdrDataTransferStub *adrDataTransferStubOperation;
DataRemovalRenewalManager *dataRemovalManager;

FragmentOperationThread::FragmentOperationThread( int sockfd_, char *hexKey_, int fragmentNumber_, CodedDataInfo *dataInfo_ )
{
    
	this->sockfd = sockfd_;
    this->dataInfo = dataInfo_;    
    this->hexKey = hexKey_;    
    this->fragmentNumber = fragmentNumber_;    
        
}

FragmentOperationThread::~FragmentOperationThread()
{
}

void FragmentOperationThread::configureOperationThreads( AdrDataTransferStub *adrDataTransferStub_, DataRemovalRenewalManager *dataRetrievalManager_ ) {
	adrDataTransferStubOperation  = adrDataTransferStub_;
	dataRemovalManager = dataRetrievalManager_;
}

void FragmentOperationThread::performOperation() {

	int status = -1;
	if (dataInfo->operationType == OPP_REMOVAL)   
		status = adrDataTransferStubOperation->removeFragment(sockfd, hexKey, OppStoreUtils::binaryKeySize*2);
	else if (dataInfo->operationType == OPP_RENEWAL)   
		status = adrDataTransferStubOperation->renewFragmentLease(sockfd, hexKey, OppStoreUtils::binaryKeySize*2, dataInfo->timeoutMinutes);	

	delete[] hexKey;                               
	(*dataInfo->remainingDownloads)--;

	ostringstream logStr;
	logStr << "Finished operationing fragment number " << fragmentNumber << ". Remaining operations=" << (*dataInfo->remainingDownloads) << "."; 
	brokerLogger.debug( logStr.str() );                        

}

void *FragmentOperationThread::run( void *ptr ) {
 
    int *threadId = (int *)ptr;
    FragmentOperationThread *operationThread = operationThreadMap[*threadId];
    operationThread->performOperation();
        
    operationThreadMap.erase(*threadId);
    delete threadId;

    return NULL;
}


void *FragmentOperationThread::launchOperationThread( void *ptr ) {
	
    int requestId = *( int *)ptr;
    delete (int *)ptr;    
         
    CodedDataInfo *dataInfo = dataRemovalManager->getCodedDataInfo(requestId );
    int nFragments = dataInfo->adrAddresses.size();
    dataInfo->remainingDownloads = new int;
    *(dataInfo->remainingDownloads) = nFragments;    

    {
    	ostringstream logStr;
    	logStr << "FragmentOperationThread::launchOperationThread called for requestId " << requestId << " nFragments=" << nFragments << "."; 
    	brokerLogger.debug( logStr.str() );         	
    }

    for (int fragment=0; fragment < nFragments ; fragment++) {

        string ipAddress;
        short portNumber;
        bool validIpAddress = OppStoreUtils::extractIpAddress(dataInfo->adrAddresses[fragment], ipAddress, portNumber);

        if (validIpAddress == false) continue;

        int sockfd = adrDataTransferStubOperation->connectToServer(ipAddress, portNumber);
        if (sockfd >= 0) {

            char *hexKey = new char[ OppStoreUtils::binaryKeySize*2  ];
            OppStoreUtils::convertBinaryToHex((char *)dataInfo->fragmentKeyList[fragment], hexKey);
               
            ostringstream logStr;
            logStr << "Operationing fragment from " << ipAddress << ":" << portNumber << " fragment=" << fragment << " key=";
            OppStoreUtils::printHexKey(hexKey, logStr); 
            brokerLogger.debug( logStr.str() );               

	        /**
	         * Launches the fragment operation thread.
	         */ 
	        FragmentOperationThread *operationThread = new FragmentOperationThread(sockfd, hexKey, fragment, dataInfo);
	        int *threadId = new int;
	        *threadId = nextOperationThreadId++;
	        operationThreadMap[*threadId] = operationThread;
	             
	        pthread_t thread1;
	        pthread_create( &thread1, NULL, FragmentOperationThread::run, threadId );
	        pthread_detach(thread1);
	        
        }
        else {
        	ostringstream logStr;
            logStr << "Error connecting to repository " << dataInfo->adrAddresses[fragment] << " fragment=" << fragment << "."; 
            brokerLogger.debug( logStr.str() );         	
        }
            
    }        
    
    OppStoreUtils::waitVar(100, dataInfo->remainingDownloads);
    
    if (nFragments > 0)
    	dataInfo->operationCallback->finishedDataOperation(dataInfo, 0);
    else
    	dataInfo->operationCallback->finishedDataOperation(dataInfo, -1);
        
    return NULL;        
}
