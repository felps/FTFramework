#include "DataWriterThread.hpp"
#include "OppStoreUtils.hpp"
#include "OppStoreUtilsSSL.hpp"
#include "BrokerLogger.hpp"

#include <map>
#include <iostream>
#include <sstream>
#include <fcntl.h>
#include <cassert>
#include <cstring>
using namespace std;

map<int, DataWriterThread *> dataWriterThreadMap;
int nextDataWriterId = 1;

DataWriterThread::DataWriterThread( int outputFileHandler_, unsigned char *inputData_, long inputDataSize_, const unsigned char * enckey_ ) {

	this->outputFileHandler = outputFileHandler_;
	this->outputData = inputData_;
	this->outputDataSize = inputDataSize_;
	this->outputDataKey = NULL;
	this->codedDataKey = NULL;
	this->fragmentKey = NULL;
	this->encKey = enckey_;
	this->totalReadBytes = 0;
    
	this->allocatedBufferSize = READER_BUFFER_SIZE;
	this->idaBuffer = NULL;
	this->inputBuffer  = NULL;
	if (outputFileHandler > 0)
		this->outputBuffer  = new unsigned char[this->allocatedBufferSize];
	if (this->encKey != NULL)
		this->interBuffer = new unsigned char[this->allocatedBufferSize];	
	        
    this->usedBufferSize = -1;
    this->iteration = -1;
    
    //this->isReading = false;
    this->shaCtx = new SHA_CTX;
    this->codedShaCtx = new SHA_CTX;
    this->evpCtx = new EVP_CIPHER_CTX;
    
    this->numberOfDownloads = -1;
    this->remainingDownloads = -1;
    this->idaImpl = NULL;
    this->bytesEncoded = NULL;
    this->idaFragmentSize = -1;
    this->idaUsedSize = -1;
        	
    this->writerMutex = new pthread_mutex_t; //
    pthread_mutex_init (this->writerMutex, NULL);
    this->writerCond  = new pthread_cond_t;
    pthread_cond_init (this->writerCond, NULL);
    this->keyCond  = new pthread_cond_t;
    pthread_cond_init (this->keyCond, NULL);
    //this->isReadingCond  = new pthread_cond_t;
    //pthread_cond_init (this->isReadingCond, NULL);
    this->stepCond = new pthread_cond_t;
    pthread_cond_init (this->stepCond, NULL);
}

DataWriterThread::~DataWriterThread() {
	
	delete this->shaCtx;
	delete this->evpCtx;
	delete this->codedShaCtx;
	if (outputFileHandler > 0)
		delete[] this->outputBuffer;
	
	if (this->inputBuffer != NULL && (outputFileHandler > 0 || this->encKey != NULL || this->idaImpl != NULL) ) {
		for (int i=0; i<this->numberOfDownloads; i++)
			delete[] this->inputBuffer[i];
		delete[] this->inputBuffer;
	}

	if (this->idaImpl != NULL) {		
		for (int i=0; i<this->numberOfDownloads; i++) {
			if (this->fragmentKey[i] != NULL) delete[] this->fragmentKey[i];
			if (this->fragmentShaCtx[i] != NULL) delete[] this->fragmentShaCtx[i];
			delete[] this->idaBuffer[i];
		}
		delete[] this->idaBuffer;
		delete[] this->fragmentKey;
		delete[] this->fragmentShaCtx;
	}

	if (this->encKey != NULL)
		delete[] this->interBuffer;			
    
    if (this->outputDataKey != NULL) 
    	delete[] this->outputDataKey;
    
    delete this->writerMutex; //
    delete this->writerCond;
    delete this->keyCond;
    delete this->stepCond;
}

void DataWriterThread::configureFileDownload (long fileSize_, long fragmentSize_, int numberOfDownloads_, int numberOfRequired_, int *fragmentNumbers_) {

	pthread_mutex_lock( this->writerMutex );
	{	
		this->numberOfDownloads = numberOfDownloads_;
		this->remainingDownloads = this->numberOfDownloads;
		this->numberOfRequired = numberOfRequired_;
		this->outputDataSize = fileSize_;
		this->fragmentSize = fileSize_;
		this->iteration++;
		
		// Currently, the broker assumes numberOfRequired == numberOfDownloads
		assert (this->numberOfRequired == this->numberOfDownloads);

		if (this->outputFileHandler <= 0) {
			this->outputData = new unsigned char[this->outputDataSize];
			this->outputBuffer = this->outputData;
		}
		
		if (encKey == NULL)
			this->interBuffer = this->outputBuffer;
			
		this->inputBuffer = new unsigned char *[this->numberOfDownloads];		

		if (numberOfRequired_ == 1) {									
			for (int i=0; i<this->numberOfDownloads; i++) {
				if (outputFileHandler > 0 || this->encKey != NULL )
					this->inputBuffer[i] = new unsigned char[this->allocatedBufferSize];
				else 
					this->inputBuffer[i] = this->outputBuffer;
			}			
		}
		else { // Using IDA
			
			this->fragmentNumbers = fragmentNumbers_;
		    this->idaFragmentSize = (this->allocatedBufferSize % this->numberOfRequired == 0) ? 
		    		this->allocatedBufferSize/this->numberOfRequired: this->allocatedBufferSize/this->numberOfRequired+1;
		    
			this->fragmentShaCtx = new SHA_CTX *[this->numberOfDownloads];
			for (int i=0; i<this->numberOfDownloads; i++)
				this->fragmentShaCtx[i] = new SHA_CTX;
			    
			this->idaBuffer = new unsigned char *[this->numberOfDownloads];
			for (int i=0; i<this->numberOfDownloads; i++) {
				this->idaBuffer[i] = new unsigned char[this->idaFragmentSize];
				this->inputBuffer[i] = new unsigned char[this->idaFragmentSize];
			}

			this->bytesEncoded = new long[this->numberOfDownloads];
			idaImpl = IDAImpl::getInstance();
		}

		pthread_cond_broadcast( this->stepCond );
	}
	pthread_mutex_unlock( this->writerMutex );

}


void DataWriterThread::writeData() {

	brokerLogger.debug("Started reading data!");
			
	this->usedBufferSize = 1;	
            
    EVP_CIPHER_CTX_init(this->evpCtx);
    EVP_DecryptInit_ex(this->evpCtx, EVP_rc4(), NULL, this->encKey, NULL);

	pthread_mutex_lock( this->writerMutex );
		
	while (this->iteration < 0) // Becomes 0 when the dataWriterThread is configured
		pthread_cond_wait( this->stepCond, this->writerMutex );
		
	SHA1_Init(this->shaCtx);
	SHA1_Init(this->codedShaCtx);
	if (idaImpl != NULL)
		for (int i=0; i<this->numberOfDownloads; i++)
			SHA1_Init(this->fragmentShaCtx[i]);

	if (this->remainingDownloads > 0)
		pthread_cond_wait( this->writerCond, this->writerMutex );			
	
    while ( this->usedBufferSize > 0 && this->totalReadBytes < this->outputDataSize ) {    	    	

    	//if (this->outputFileHandler <= 0)
    	//	this->outputBuffer = this->outputData + this->totalReadBytes;    	

    	/**
    	 *  Decodes the data using IDA
    	 *  TODO: Needs to check which is the size when using IDA
    	 */ 
    	if (idaImpl != NULL) {
        	this->usedBufferSize = this->allocatedBufferSize;
    		if (this->outputDataSize - this->totalReadBytes < this->allocatedBufferSize)
    			this->usedBufferSize = this->outputDataSize - this->totalReadBytes;
    		idaImpl->decodeDataIntoBuffer(this->interBuffer, this->idaBuffer, this->usedBufferSize, this->fragmentNumbers, this->numberOfRequired, this->numberOfDownloads-this->numberOfRequired);
    	}
    	    	 
	    //this->idaUsedSize = (this->usedBufferSize % this->numberOfRequired == 0) ? 
	    //		this->usedBufferSize/this->numberOfRequired: this->usedBufferSize/this->numberOfRequired+1;

    	/**
    	 *  Decrypts the data if a key is provided.
    	 */ 
    	int outputSize; 
    	if (encKey != NULL) 
    		EVP_DecryptUpdate(this->evpCtx, this->outputBuffer, &outputSize, this->interBuffer, this->usedBufferSize);
    	else
    		this->interBuffer = this->outputBuffer;

    	/**
    	 *  Evaluates SHA-1 from outputBuffer
    	 */
    	SHA1_Update( this->shaCtx, this->outputBuffer, this->usedBufferSize );
    	SHA1_Update( this->codedShaCtx, this->interBuffer, this->usedBufferSize );
    	if (idaImpl != NULL)
    		for (int i=0; i<this->numberOfDownloads; i++)
    			SHA1_Update( this->fragmentShaCtx[i], this->idaBuffer[i], this->idaUsedSize );

    	this->totalReadBytes += this->usedBufferSize;
    	this->iteration++;
    	this->remainingDownloads = this->numberOfDownloads;

    	/**
    	 *  Writes data from outputBuffer
    	 */
    	if (outputFileHandler > 0)
    		int nWrittenBytes = write(this->outputFileHandler, this->outputBuffer, this->usedBufferSize);    		    	
    	else
    		this->outputBuffer = this->outputData + this->totalReadBytes;
    	
    	if (encKey == NULL && outputFileHandler <= 0) 
    		this->interBuffer = this->outputData + this->totalReadBytes;
    	
    	if (idaBuffer == NULL && outputFileHandler <= 0 && this->encKey == NULL )
    		for (int i=0; i<this->numberOfDownloads; i++)
    			this->inputBuffer[i] = this->outputData + this->totalReadBytes;
     	    	
    	{
    	ostringstream logStr;
    	logStr << "Finished reading " << this->usedBufferSize << " bytes. Waiting for FragmentDownloadThread."; 
    	brokerLogger.debug( logStr.str() );    	
    	}

    	/**
    	 *  Waits until encoder or AdrStub finishes using the previous outputBuffer 
    	 */      	    	
    	if (this->totalReadBytes < this->outputDataSize) {
    		pthread_cond_broadcast( this->stepCond );
    		pthread_cond_wait( this->writerCond, this->writerMutex );
    		brokerLogger.debug("DataWriterThread released!");
    	}    	
    	
    }
        
    this->iteration++;    
    pthread_cond_broadcast( this->stepCond );
    
    /**
     * Evaluates the inputKey and notifies any thread that was waiting to key the inputKey
     */     
    this->outputDataKey = new unsigned char[OppStoreUtils::binaryKeySize];
    SHA1_Final(this->outputDataKey, this->shaCtx);
    this->codedDataKey = new unsigned char[OppStoreUtils::binaryKeySize];
    SHA1_Final(this->codedDataKey, this->codedShaCtx);
    if (this->idaImpl != NULL) {
    	this->fragmentKey = new unsigned char *[this->numberOfDownloads];
    	for (int i=0; i<this->numberOfDownloads; i++) {
    	    this->fragmentKey[i] = new unsigned char[OppStoreUtils::binaryKeySize];
    	    SHA1_Final(this->fragmentKey[i], this->fragmentShaCtx[i]);
    	}
    }
    EVP_CIPHER_CTX_cleanup(this->evpCtx);        

	{
	ostringstream logStr;
	logStr << "Finished reading the total of " << totalReadBytes << " bytes of " << this->outputDataSize << " with key ";
    char *key = new char[OppStoreUtils::binaryKeySize*2]; 
    OppStoreUtils::convertBinaryToHex( (char *)this->outputDataKey, key );    
	OppStoreUtils::printHexKey(key, logStr);
	brokerLogger.debug( logStr.str() );    	
	}
    	
	//cerr << "DataWriterThread::readThread -> unlocking" << endl;
    pthread_cond_broadcast( this->keyCond );
    pthread_mutex_unlock( this->writerMutex );
        
    return;
}

unsigned char *DataWriterThread::getInputBuffer( int * inputBufferSize_, int fragmentNumber_, int iteration_) {

	pthread_mutex_lock( this->writerMutex );
		
	while (this->iteration < iteration_)
		pthread_cond_wait( this->stepCond, this->writerMutex ); 

	if (this->outputDataSize - this->totalReadBytes > this->allocatedBufferSize)		
		*inputBufferSize_ = this->allocatedBufferSize;
	else
		*inputBufferSize_ = this->outputDataSize - this->totalReadBytes;
	
	if (idaImpl != NULL)
		*inputBufferSize_ = (*inputBufferSize_ % this->numberOfRequired == 0) ? 
				*inputBufferSize_/this->numberOfRequired : *inputBufferSize_/this->numberOfRequired+1;
	
	unsigned char *tmpBuffer = this->inputBuffer[fragmentNumber_];
	
	pthread_mutex_unlock( this->writerMutex );
	
	return tmpBuffer;
	
    //unsigned char *buffer = this->inputBuffer[fragmentNumber_];
    //if (idaImpl != NULL) *inputBufferSize_ = this->idaUsedSize;
    //else *inputBufferSize_ = this->usedBufferSize;    

}

void DataWriterThread::setInputBufferUpdated(int numberOfBytesRead, int iteration_, int fragmentNumber_ ) {
			
	pthread_mutex_lock( this->writerMutex );
		
	while (this->iteration < iteration_)
		pthread_cond_wait( this->stepCond, this->writerMutex );
	
	/**
	 *  Swap interBuffer and inputBuffer
	 */
	if (idaImpl != NULL) {
		memcpy(this->idaBuffer[fragmentNumber_], this->inputBuffer[fragmentNumber_], numberOfBytesRead);
		this->idaUsedSize = numberOfBytesRead;
		this->usedBufferSize = numberOfBytesRead;
	}
	else {
		memcpy(this->interBuffer, this->inputBuffer[fragmentNumber_], numberOfBytesRead);
		this->usedBufferSize = numberOfBytesRead;
	}

	{
	ostringstream logStr;
	logStr << "DataWriterThread::setInputBufferUpdated -> Updated " << numberOfBytesRead << " bytes from fragment " << fragmentNumber_ << "."; 
	brokerLogger.debug( logStr.str() );    	
	}

	this->remainingDownloads--;
	if (this->remainingDownloads == 0)
		pthread_cond_broadcast( this->writerCond );
    
    pthread_mutex_unlock( this->writerMutex );    
    
}

unsigned char *DataWriterThread::getOutputDataKey( ) {
	
	pthread_mutex_lock( this->writerMutex );
	if (this->outputDataKey == NULL)
		pthread_cond_wait( this->keyCond, this->writerMutex );

	pthread_mutex_unlock( this->writerMutex );	
	
	return this->outputDataKey;
}

unsigned char *DataWriterThread::getFragmentKey(int fragmentNumber) {
	
	pthread_mutex_lock( this->writerMutex );
	if (this->outputDataKey == NULL)
		pthread_cond_wait( this->keyCond, this->writerMutex );

	pthread_mutex_unlock( this->writerMutex );	
	
	if (this->numberOfRequired > 1)
		return this->fragmentKey[fragmentNumber];
	else
		return this->codedDataKey;
}

unsigned char *DataWriterThread::getOutputData( ) {

	pthread_mutex_lock( this->writerMutex );
	if (this->outputDataKey == NULL)
		pthread_cond_wait( this->keyCond, this->writerMutex );

	pthread_mutex_unlock( this->writerMutex );	
	
	return this->outputData;
}


// TODO: We need to evaluate the fragment key. Can be done by the AdrDataTransferStub 

//================================================================================
// The methods below are used to launch the DataWriterThread
//================================================================================


void *DataWriterThread::run( void *ptr ) {
 
	//cout << "Starting DataWriterThread." << endl;
    int *threadId = (int *)ptr;
    DataWriterThread *dataWriterThread = dataWriterThreadMap[*threadId];
    dataWriterThread->writeData();
    
    dataWriterThreadMap.erase(*threadId);
    delete threadId;
    
    return NULL;   
}

DataWriterThread *DataWriterThread::createDataWriterThread ( unsigned char *outputData_, long dataSize_, const unsigned char * encKey_ ) {

	int inputFileHandler_ = -1;
	DataWriterThread *dataWriterThread = new DataWriterThread( inputFileHandler_, outputData_, dataSize_, encKey_ );
	DataWriterThread::launchThread( dataWriterThread );

	return dataWriterThread;
}

DataWriterThread *DataWriterThread::createDataWriterThreadToFile ( const char *outputFilePath, long dataSize_, const unsigned char * encKey_ ) {

    int outputFileHandler_ = open( outputFilePath, O_WRONLY | O_CREAT, 0600 );
    if (outputFileHandler_ > 0) {     	

    	DataWriterThread *dataWriterThread = new DataWriterThread( outputFileHandler_, NULL, dataSize_, encKey_ );
    	DataWriterThread::launchThread( dataWriterThread );

    	return dataWriterThread;
    }
    
    return NULL;    
}

void DataWriterThread::launchThread ( DataWriterThread *dataWriterThread ) {

    int *threadId = new int;
    *threadId = nextDataWriterId++;
    dataWriterThreadMap[*threadId] = dataWriterThread;
    
    pthread_t thread1;
    pthread_create( &thread1, NULL, DataWriterThread::run, threadId );
    pthread_detach(thread1);		
}

