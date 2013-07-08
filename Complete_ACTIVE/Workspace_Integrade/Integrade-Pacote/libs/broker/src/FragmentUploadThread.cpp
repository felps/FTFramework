#include "FragmentUploadThread.hpp"
#include "OppStoreUtils.hpp"
#include "Benchmark.hpp"
#include "BrokerLogger.hpp"

#include <sstream>
#include <openssl/sha.h>
#include <map>
#include <iostream>
#include <cassert>
using namespace std;

map<int, FragmentUploadThread *> uploadThreadMap;
int nextUploadThreadId = 1;

CdrmRequestsStub *cdrmRequestsStub;
AdrDataTransferStub *adrDataTransferStubUpload;
DataStorageManager *dataStorageManager;

void FragmentUploadThread::configureUploadThreads( CdrmRequestsStub *cdrmRequestsStub_, AdrDataTransferStub *adrDataTransferStub_, DataStorageManager *dataStorageManager_ ) {

    cdrmRequestsStub = cdrmRequestsStub_;
    adrDataTransferStubUpload = adrDataTransferStub_;
    dataStorageManager = dataStorageManager_;

}


FragmentUploadThread::FragmentUploadThread
( int sockfd, unsigned char *fileKey_, unsigned char *fragmentKey_, DataReaderThread *dataReaderThread_, int fragmentSize_, int fragmentNumber_, int nCached, int *remainingUploads_, int neededFragments_, int timeoutMinutes_)
{

	this->dataReaderThread = dataReaderThread_;
	this->timeoutMinutes = timeoutMinutes_;
	this->neededFragments = neededFragments_;
    this->sockfd = sockfd;
    this->fileKey = fileKey_;
    
    this->fragmentKey = fragmentKey_;
    
    this->fragmentSize = fragmentSize_;    
    this->fragmentNumber = fragmentNumber_ - nCached;
    this->isCacheCopy = (nCached == 0) ? true : false; 
    
    this->remainingUploads = remainingUploads_;

    //this->encData = encData_;
    //this->ipAddress = ipAddress_;
    //this->portNumber = portNumber_;
    //this->availableBytes = availableBytes_;
}

FragmentUploadThread::~FragmentUploadThread() {}

void FragmentUploadThread::performUpload() {

    Benchmark::getCurrentBenchmark()->fragmentStart[fragmentNumber] = OppStoreUtils::timeInMillis();
            
    adrDataTransferStubUpload->performDataTransferDataReader(sockfd, dataReaderThread, fragmentSize, fragmentNumber);
        
    /**
     * Generates the hexadecimal key
     */  
    //SHA1( (unsigned char *)encData, fragmentSize, fragmentKey);
    if (this->neededFragments == 1) { // Using replication
    	const unsigned char *inputKey = dataReaderThread->getInputDataKey();
    	const unsigned char *codedKey = dataReaderThread->getCodedDataKey();
    	for (int pos=0; pos < OppStoreUtils::binaryKeySize; pos++) {
    		this->fragmentKey[pos] = codedKey[pos]; 
    		this->fileKey[pos] = inputKey[pos];
    	}
      	this->fragmentKey[OppStoreUtils::binaryKeySize-1] = (unsigned char)this->fragmentNumber;
    }
    else {
    	const unsigned char *inputKey = dataReaderThread->getInputDataKey();
    	const unsigned char *codedKey = dataReaderThread->getIdaDataKey(fragmentNumber);
    	for (int pos=0; pos < OppStoreUtils::binaryKeySize; pos++) {
    		this->fragmentKey[pos] = codedKey[pos]; 
    		this->fileKey[pos] = inputKey[pos];
    	}      	   
    }
    
    char *hexKey = new char[ OppStoreUtils::binaryKeySize*2 ];
    OppStoreUtils::convertBinaryToHex((char *)fragmentKey, hexKey);
    adrDataTransferStubUpload->finishDataTransfer(sockfd, hexKey, OppStoreUtils::binaryKeySize*2, this->timeoutMinutes);
    delete[] hexKey;

    //OppStoreUtils::printHexKey( hexKey, cout );   
    
    (*remainingUploads)--;
    { 
        ostringstream logStr;
        logStr << "remainingUploads=" << *remainingUploads << "."; 
        brokerLogger.debug( logStr.str() ); 
    }        

    if (this->isCacheCopy)
    	delete this->dataReaderThread;
    Benchmark::getCurrentBenchmark()->fragmentFinish[fragmentNumber] = OppStoreUtils::timeInMillis();
}

void *FragmentUploadThread::run( void *ptr ) {
 
    int *threadId = (int *)ptr;
    FragmentUploadThread *uploadThread = uploadThreadMap[*threadId];
    uploadThread->performUpload();
    
    uploadThreadMap.erase(*threadId);
    delete threadId;

    return NULL;
}

int FragmentUploadThread::uploadFileToLocalCache( CodedDataInfo *dataInfo, vector<int> & notStoredFragments, vector<char *> & fragmentHashList ) {	

	DataReaderThread* dataReaderThread = NULL;
    if (dataInfo->filePath == NULL)
    	dataReaderThread = DataReaderThread::createDataReaderThread((unsigned char *)dataInfo->data, dataInfo->dataSize, dataInfo->encKey);
    else
    	dataReaderThread = DataReaderThread::createDataReaderThreadFromFile(dataInfo->filePath, dataInfo->dataSize, dataInfo->encKey);
    dataReaderThread->setNumberofUploads(1);
    
	int fragment=0;
	
    string ipAddress;
    short portNumber;
    bool validIpAddress = OppStoreUtils::extractIpAddress(dataInfo->adrAddresses[fragment], ipAddress, portNumber);
    
    if (validIpAddress == false) {
    	notStoredFragments.push_back( fragment );
    	(*(dataInfo->remainingUploads))--;
    	//delete dataReaderThread;
    	return -1;
    }
                  
    /**
     * Launches the fragment upload thread.
     */
    int sockfd = adrDataTransferStubUpload->connectToServer(ipAddress, portNumber);
    if (sockfd >= 0) {        
    	ostringstream logStr;
        logStr << "DataStorageManager -> uploading fragment to " << ipAddress << ":" << portNumber << ".";
        brokerLogger.debug( logStr.str() );

        unsigned char *fragmentKey = new unsigned char[OppStoreUtils::binaryKeySize];
        fragmentHashList.push_back( (char *)fragmentKey );

        int neededFragments = 1;
    	FragmentUploadThread *uploadThread = // alterar encData e availableBytes p/ referência para DataReaderThread
    		new FragmentUploadThread( sockfd, (unsigned char *)dataInfo->fileKey, fragmentKey, dataReaderThread, 
    				dataInfo->fragmentSizeList[fragment], fragment, 0, dataInfo->remainingUploads, 
    				neededFragments, dataInfo->timeoutMinutes );

        int *threadId = new int;
        *threadId = nextUploadThreadId++;
        uploadThreadMap[*threadId] = uploadThread;

        pthread_t thread1;
        pthread_create( &thread1, NULL, FragmentUploadThread::run, threadId );
        pthread_detach(thread1);
        //(*(dataInfo->remainingUploads))--;
    }
    else {
    	ostringstream logStr;
        logStr << "DataStorageManager -> could not connect to " << ipAddress << ":" << portNumber << "."; 
        brokerLogger.debug( logStr.str() );
        
    	notStoredFragments.push_back( fragment );
    	(*(dataInfo->remainingUploads))--;
    	//delete dataReaderThread;
    	return -1;
    }
    
    //delete dataReaderThread;
    return 0;
}


void *FragmentUploadThread::launchUploadThread ( void *ptr ) {

    int requestId = *(int *)ptr;
    delete (int *)ptr;
    CodedDataInfo *dataInfo = dataStorageManager->getCodedDataInfo(requestId );
    
    vector<int> notStoredFragments;
    vector<char *> fragmentHashList;

    int nAddresses = dataInfo->adrAddresses.size();
    dataInfo->remainingUploads = new int;
    *(dataInfo->remainingUploads) = nAddresses;
    
    if (dataInfo->filePath == NULL)
    	dataInfo->dataReaderThread = DataReaderThread::createDataReaderThread((unsigned char *)dataInfo->data, dataInfo->dataSize, dataInfo->encKey);
    else
    	dataInfo->dataReaderThread = DataReaderThread::createDataReaderThreadFromFile(dataInfo->filePath, dataInfo->dataSize, dataInfo->encKey);

    Benchmark *benchmark = Benchmark::getCurrentBenchmark();
    benchmark->nFragmentTimes = nAddresses;
    benchmark->fragmentStart  = new long[nAddresses];
    benchmark->fragmentFinish = new long[nAddresses];
    benchmark->fragmentAddress.resize(nAddresses);
    
    cout << "uploadAddresses: ";
    for (int fragment=0; fragment < nAddresses; fragment++)
    	cout << dataInfo->adrAddresses[fragment] << " ";
    cout << endl;
    
	if ( dataInfo->fragmentSizeList[0] == dataInfo->dataSize ) {
		cout << "Performing replica upload for address " << dataInfo->adrAddresses[0] << "." << endl;    				
		FragmentUploadThread::uploadFileToLocalCache(dataInfo, notStoredFragments, fragmentHashList);    		
	}
	else
		(*(dataInfo->remainingUploads))--;

    int nCached=1;
    int nAcceptedUploads=0;
    for (int fragment=1; fragment < nAddresses; fragment++) {

        string ipAddress;
        short portNumber;
        bool validIpAddress = OppStoreUtils::extractIpAddress(dataInfo->adrAddresses[fragment], ipAddress, portNumber);
        
        if (validIpAddress == false) {
        	notStoredFragments.push_back( fragment );
        	(*(dataInfo->remainingUploads))--;
        	continue;
        }

        //Benchmark::getCurrentBenchmark()->fragmentAddress[fragment] = dataInfo->adrAddresses[fragment];
                       
        /**
         * Launches the fragment upload thread.
         */
        int sockfd = adrDataTransferStubUpload->connectToServer(ipAddress, portNumber);
        if (sockfd >= 0) {        
        	ostringstream logStr;
            logStr << "DataStorageManager -> uploading fragment to " << ipAddress << ":" << portNumber << ".";
            brokerLogger.debug( logStr.str() );

            unsigned char *fragmentKey = new unsigned char[OppStoreUtils::binaryKeySize];
            fragmentHashList.push_back( (char *)fragmentKey );

        	FragmentUploadThread *uploadThread = // alterar encData e availableBytes p/ referência para DataReaderThread
        		new FragmentUploadThread( sockfd, (unsigned char *)dataInfo->fileKey, fragmentKey, dataInfo->dataReaderThread, 
        				dataInfo->fragmentSizeList[fragment], fragment, nCached, dataInfo->remainingUploads, 
        				dataInfo->neededFragments, dataInfo->timeoutMinutes );

            int *threadId = new int;
            *threadId = nextUploadThreadId++;
            uploadThreadMap[*threadId] = uploadThread;

            pthread_t thread1;
            pthread_create( &thread1, NULL, FragmentUploadThread::run, threadId );
            pthread_detach(thread1);
            nAcceptedUploads++;
        }
        else {
        	ostringstream logStr;
            logStr << "DataStorageManager -> could not connect to " << ipAddress << ":" << portNumber << "."; 
            brokerLogger.debug( logStr.str() );
            
        	notStoredFragments.push_back( fragment );
        	(*(dataInfo->remainingUploads))--;
        	continue;
        }
    }
    
    if (nAcceptedUploads > 0) {
    	
    	if (dataInfo->neededFragments == 1)  // Using replication
    		dataInfo->dataReaderThread->setNumberofUploads(nAcceptedUploads);
    	else
    		dataInfo->dataReaderThread->setNumberofIdaUploads(nAcceptedUploads, *&dataInfo->neededFragments);
    }

    OppStoreUtils::waitVar(100, dataInfo->remainingUploads);    
        
    cdrmRequestsStub->setFragmentStorageFinished(requestId, notStoredFragments, fragmentHashList, dataInfo->fileKey);
    
    // TODO: free space from fragment Hash List?
    
    return NULL;
}

