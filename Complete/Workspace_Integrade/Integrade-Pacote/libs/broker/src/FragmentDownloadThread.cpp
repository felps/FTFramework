#include "FragmentDownloadThread.hpp"
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

/**
 * Used only in the experiments
 */
set<string> allowedAdrsSet;

map<int, FragmentDownloadThread *> downloadThreadMap;
int nextDownloadThreadId = 1;

AdrDataTransferStub *adrDataTransferStubDownload;
DataRetrievalManager *dataRetrievaManager;

FragmentDownloadThread::FragmentDownloadThread
( int sockfd_, string ipAddress_, short portNumber_, int fragmentNumber_, int nCaches, CodedDataInfo *dataInfo_, int fragmentPos_, int *nRecovered_ )
{
    
	this->sockfd = sockfd_;
    //this->dataInfo = dataInfo_;
    this->ipAddress = ipAddress_;
    this->portNumber = portNumber_;    
    this->fragmentPos = fragmentPos_;        
    this->nRecovered = nRecovered_;
    this->fragmentNumber = fragmentNumber_;
    if (dataInfo_->neededFragments > 1)
    	this->fragmentNumber -= nCaches;

    this->dataWriterThread = dataInfo_->dataWriterThread;
    this->fragmentSize = dataInfo_->fragmentSizeList[fragmentNumber_];
    this->hashKey = dataInfo_->fragmentKeyList[fragmentNumber_];
    this->remainingDownloads = dataInfo_->remainingDownloads;
    this->fileKey = dataInfo_->fileKey;
    this->neededFragments = dataInfo_->neededFragments;
}

FragmentDownloadThread::~FragmentDownloadThread()
{
}

void FragmentDownloadThread::configureDownloadThreads( AdrDataTransferStub *adrDataTransferStub_, DataRetrievalManager *dataRetrievalManager_ ) {
    adrDataTransferStubDownload  = adrDataTransferStub_;
    dataRetrievaManager = dataRetrievalManager_;
}

void FragmentDownloadThread::performDownload() {

    { 
        ostringstream logStr;
        logStr << "Downloading fragment from " << ipAddress << ":" << portNumber;
        brokerLogger.debug( logStr.str() ); 
    }              

    //Benchmark::getCurrentBenchmark()->fragmentStart[fragmentPos] = OppStoreUtils::timeInMillis();
  
    /**
     * Generates the hexadeciaml key
     */  
    char *hexKey = new char[ OppStoreUtils::binaryKeySize*2  ];
    OppStoreUtils::convertBinaryToHex((char *)hashKey, hexKey);
       
    { 
        ostringstream logStr;
        logStr << "Downloading fragment from " << ipAddress << ":" << portNumber << " key=";
        OppStoreUtils::printHexKey(hexKey, logStr); 
        logStr << " fragmentSize=" << fragmentSize << ".";
        brokerLogger.debug( logStr.str() ); 
    }              

    //void * fragmentData = dataInfo->encData[fragmentPos];
    //int tmpSize = adrDataTransferStubDownload->readData(sockfd, hexKey, OppStoreUtils::binaryKeySize*2, fragmentData, fragmentSize, dataInfo->availableBytes[fragmentPos]);
    int tmpSize = adrDataTransferStubDownload->readDataToWriter(sockfd, dataWriterThread, fragmentNumber, hexKey, OppStoreUtils::binaryKeySize*2, fragmentSize);
    delete[] hexKey;

    //void * fragmentData = dataInfo->dataReaderThread->getInput
    	
    /**
     * Checks if fragment size and hash are correct
     */
    if (fragmentSize == tmpSize) { 
        ostringstream logStr;
        logStr << "Downloaded " << tmpSize << " bytes from fragment " << fragmentNumber << "."; 
        brokerLogger.debug( logStr.str() ); 
    }        
    else {
    	(*nRecovered)--;
    	(*remainingDownloads)--;
    	delete[] hashKey;
    	
        ostringstream logStr;
        logStr << "Error downloading fragment " << fragmentNumber << "."; 
        brokerLogger.debug( logStr.str() ); 

    	return;
    }
        
//    char *fragmentKey = new char[OppStoreUtils::binaryKeySize];
//    SHA1( (unsigned char *)fragmentData, fragmentSize, (unsigned char *)fragmentKey);     

    unsigned char *fragmentKey = dataWriterThread->getFragmentKey(fragmentNumber);     

    {
    char *hexKey = new char[ OppStoreUtils::binaryKeySize*2 ];
    OppStoreUtils::convertBinaryToHex((char *)fragmentKey, hexKey);
    cout << "fragmentKey: ";
    OppStoreUtils::printHexKey( hexKey, cout );
    cout << endl;
    }

    {
    char *hexKey = new char[ OppStoreUtils::binaryKeySize*2 ];
    OppStoreUtils::convertBinaryToHex((char *)hashKey, hexKey);
    cout << "hashKey:     ";
    OppStoreUtils::printHexKey( hexKey, cout );
    cout << endl;
    }

    int hashSize = OppStoreUtils::binaryKeySize;
    if (this->neededFragments == 1) hashSize--; // Using replication
    for (int keyPos=0; keyPos<hashSize; keyPos++) {
    	//cout << keyPos << " " << (int)hashKey[keyPos] << " " << (int)fragmentKey[keyPos] << endl;
        if ( (unsigned char)hashKey[keyPos] != fragmentKey[keyPos] ) {
            cerr << "ERROR: checksum for fragment does not match at position " << keyPos << endl;
            cerr << "with fragmentNumber " << fragmentNumber << " at position " << fragmentPos << " ipAddress=" << ipAddress << ":" << portNumber << "." << endl;
            exit(-1); 
        }
    }
        
    delete[] fragmentKey;
    delete[] hashKey;
                                
    /**
     * Puts the checked data into the dataInfo for later decoding
     */  
    //dataInfo->recoveredFragmentIndexes[fragmentPos] = fragmentNumber;   
    //Benchmark::getCurrentBenchmark()->fragmentFinish[fragmentPos] = OppStoreUtils::timeInMillis();    
    { 
        ostringstream logStr;
        logStr << "Finished downloading fragment number " << fragmentNumber << ". Remaining downloads=" << (*remainingDownloads)-1 << "."; 
        brokerLogger.debug( logStr.str() ); 
    }        
    (*remainingDownloads)--;
        
   
}

void *FragmentDownloadThread::run( void *ptr ) {
 
    int *threadId = (int *)ptr;
    FragmentDownloadThread *downloadThread = downloadThreadMap[*threadId];
    downloadThread->performDownload();
        
    downloadThreadMap.erase(*threadId);
    delete threadId;

    return NULL;
}


// 143.107.1.1:9003
int FragmentDownloadThread::setAllowedAdrs(vector<string> & allowedAdrs) {
    
    allowedAdrsSet.clear();
    for (uint fragment=0; fragment < allowedAdrs.size(); fragment++) {
        allowedAdrsSet.insert( allowedAdrs[fragment] );
    }
    
    return 0;
}

int FragmentDownloadThread::downloadDataFromCache( CodedDataInfo *dataInfo ) {
	
	int tmpNeededFragments = dataInfo->neededFragments;
	dataInfo->neededFragments = 1;	
    
	int fragment=0;
    string ipAddress;
    short portNumber;
    bool validIpAddress = OppStoreUtils::extractIpAddress(dataInfo->adrAddresses[fragment], ipAddress, portNumber);

    if (validIpAddress == false) return -1;

    int sockfd = adrDataTransferStubDownload->connectToServer(ipAddress, portNumber);
    if (sockfd >= 0) {

        if (dataInfo->filePath == NULL) {
        	unsigned char *outputData = new unsigned char[dataInfo->dataSize];
        	dataInfo->dataWriterThread = DataWriterThread::createDataWriterThread( outputData, dataInfo->dataSize, dataInfo->encKey );
        }
        else
        	dataInfo->dataWriterThread = DataWriterThread::createDataWriterThreadToFile( dataInfo->filePath, dataInfo->dataSize, dataInfo->encKey );
    	
        dataInfo->dataWriterThread->configureFileDownload(dataInfo->dataSize, dataInfo->fragmentSizeList[0], 1, 1, NULL);

        ostringstream logStr;
        logStr << "Downloading from repository " << dataInfo->adrAddresses[fragment] << " fragment=" << fragment << "."; 
        brokerLogger.debug( logStr.str() ); 
                      
        int nRecovered = 0;
        int nCached = 0;
        /**
         * Launches the fragment download thread.
         */ 
        FragmentDownloadThread *downloadThread = 
            new FragmentDownloadThread(sockfd, ipAddress, portNumber, fragment, nCached, dataInfo, nRecovered, &nRecovered );                     
        downloadThread->performDownload();

        //int *threadId = new int;
        //*threadId = nextDownloadThreadId++;
        //downloadThreadMap[*threadId] = downloadThread;

        //pthread_t thread1;
        //pthread_create( &thread1, NULL, FragmentDownloadThread::run, threadId );
        //pthread_detach(thread1);
        
        //nRecovered++;
        
        dataInfo->neededFragments = tmpNeededFragments;
    }
    else {
    	ostringstream logStr;
        logStr << "Error connecting to repository " << dataInfo->adrAddresses[fragment] << " fragment=" << fragment << "."; 
        brokerLogger.debug( logStr.str() );
        return -1;
    }            
    
    return 0;
}

void *FragmentDownloadThread::launchDownloadThread( void *ptr ) {
	
    int requestId = *( int *)ptr;
    delete (int *)ptr;
    
    CodedDataInfo *dataInfo = dataRetrievaManager->getCodedDataInfo(requestId );    
    dataInfo->remainingDownloads = new int;
    *(dataInfo->remainingDownloads) = dataInfo->neededFragments; // adrAddresses.size();
    dataInfo->encData.resize(dataInfo->neededFragments);
    dataInfo->fragmentSizeList.resize(dataInfo->neededFragments);
    dataInfo->recoveredFragmentIndexes.resize(dataInfo->neededFragments);
    dataInfo->availableBytes.resize(dataInfo->neededFragments);    

    /**
     * Download data from local ADR, if available
     */ 
    int cacheStatus = -1;
	if (dataInfo->neededFragments > 1 && dataInfo->fragmentSizeList[0] == dataInfo->dataSize) {
		cout << "Download cached file from address " << dataInfo->adrAddresses[0] << "." << endl;
		cacheStatus = FragmentDownloadThread::downloadDataFromCache(dataInfo);	
		dataInfo->data = dataInfo->dataWriterThread->getOutputData();
	}
    
	if (cacheStatus < 0) {
    
		if (dataInfo->filePath == NULL) {
			unsigned char *outputData = new unsigned char[dataInfo->dataSize];
			dataInfo->dataWriterThread = DataWriterThread::createDataWriterThread( outputData, dataInfo->dataSize, dataInfo->encKey );
		}
		else
			dataInfo->dataWriterThread = DataWriterThread::createDataWriterThreadToFile( dataInfo->filePath, dataInfo->dataSize, dataInfo->encKey );

		int nRecovered = 0;         
		int nCached=0;
		for (uint fragment=0; fragment < dataInfo->adrAddresses.size() && nRecovered < dataInfo->neededFragments; fragment++) {

			if ( allowedAdrsSet.size() > 0 && allowedAdrsSet.find(dataInfo->adrAddresses[fragment]) == allowedAdrsSet.end() ) {
				ostringstream logStr;
				logStr << "Discarding repository " << dataInfo->adrAddresses[fragment] << "."; 
				brokerLogger.debug( logStr.str() );              
				continue;
			}                        

			string ipAddress;
			short portNumber;
			bool validIpAddress = OppStoreUtils::extractIpAddress(dataInfo->adrAddresses[fragment], ipAddress, portNumber);

			if (validIpAddress == false) continue;

			int sockfd = adrDataTransferStubDownload->connectToServer(ipAddress, portNumber);
			if (sockfd >= 0) {

				ostringstream logStr;
				logStr << "Downloading from repository " << dataInfo->adrAddresses[fragment] << " fragment=" << fragment << "."; 
				brokerLogger.debug( logStr.str() ); 

				//dataInfo->encData[nRecovered] = new char[ dataInfo->fragmentSizeList[fragment] ];     
				dataInfo->recoveredFragmentIndexes[nRecovered] = fragment - nCached;
				dataInfo->availableBytes[nRecovered] = new long;
				*(dataInfo->availableBytes[nRecovered]) = 0;              
				//Benchmark::getCurrentBenchmark()->fragmentAddress[nRecovered] = dataInfo->adrAddresses[fragment];

				/**
				 * Launches the fragment download thread.
				 */ 
				FragmentDownloadThread *downloadThread = 
					new FragmentDownloadThread(sockfd, ipAddress, portNumber, fragment, nCached, dataInfo, nRecovered, &nRecovered );
				int *threadId = new int;
				*threadId = nextDownloadThreadId++;
				downloadThreadMap[*threadId] = downloadThread;

				pthread_t thread1;
				pthread_create( &thread1, NULL, FragmentDownloadThread::run, threadId );
				pthread_detach(thread1);

				nRecovered++;
			}
			else {
				ostringstream logStr;
				logStr << "Error connecting to repository " << dataInfo->adrAddresses[fragment] << " fragment=" << fragment << "."; 
				brokerLogger.debug( logStr.str() );         	
			}            
		}            

		int *fragmentNumbers = NULL;
		if (dataInfo->neededFragments > 1) {
			fragmentNumbers = new int[dataInfo->neededFragments];
			for (int i=0; i<dataInfo->neededFragments; i++)
				fragmentNumbers[i] = dataInfo->recoveredFragmentIndexes[i];
		}

		if (nRecovered == dataInfo->neededFragments) {    
			dataInfo->dataWriterThread->configureFileDownload(dataInfo->dataSize, dataInfo->fragmentSizeList[0], nRecovered, nRecovered, fragmentNumbers);

			OppStoreUtils::waitVar(100, dataInfo->remainingDownloads);
			dataInfo->data = dataInfo->dataWriterThread->getOutputData();
		}

		if (fragmentNumbers != NULL)
			delete[] fragmentNumbers;
	}
	
    if (dataInfo->operationCallback != NULL)    	
    	dataInfo->operationCallback->finishedDataOperation(dataInfo, 0);
    else 
    	dataInfo->operationCallback->finishedDataOperation(dataInfo, -1);    	
        
    return NULL;        
}
