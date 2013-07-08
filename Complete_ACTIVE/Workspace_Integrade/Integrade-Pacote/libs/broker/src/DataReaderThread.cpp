#include "DataReaderThread.hpp"
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

map<int, DataReaderThread *> dataReaderThreadMap;
int nextDataReaderId = 1;

DataReaderThread::DataReaderThread( int inputFileHandler_, unsigned char *inputData_, long inputDataSize_, const unsigned char * enckey_ ) {

	this->inputFileHandler = inputFileHandler_;
	this->inputData = inputData_;
	this->inputDataSize = inputDataSize_;
	this->inputDataKey = NULL;
	this->codedDataKey = NULL;
	this->idaDataKey = NULL;
	this->encKey = enckey_;
    
	this->allocatedBufferSize = READER_BUFFER_SIZE;
	this->idaBuffer = NULL;
	this->outputBuffer  = NULL;
	if (inputFileHandler > 0)
		this->inputBuffer  = new unsigned char[this->allocatedBufferSize];
	if (this->encKey != NULL)
		this->interBuffer = new unsigned char[this->allocatedBufferSize];			
	        
    this->usedBufferSize = -1;
    this->iteration = -2; // -1 is for setting numberOfUploads
    
    //this->isReading = false;
    this->shaCtx = new SHA_CTX;
    this->codedShaCtx = new SHA_CTX;
    this->evpCtx = new EVP_CIPHER_CTX;
    
    this->numberOfUploads = -1;
    this->remainingUploads = this->numberOfUploads;
    this->remainingUploads = -1;
    this->idaImpl = NULL;
    this->bytesEncoded = NULL;
    this->idaFragmentSize = -1;
    this->idaUsedSize = -1;
        	
    this->readerMutex = new pthread_mutex_t; //
    pthread_mutex_init (this->readerMutex, NULL);
    this->readerCond  = new pthread_cond_t;
    pthread_cond_init (this->readerCond, NULL);
    this->keyCond  = new pthread_cond_t;
    pthread_cond_init (this->keyCond, NULL);
    //this->isReadingCond  = new pthread_cond_t;
    //pthread_cond_init (this->isReadingCond, NULL);
    this->stepCond = new pthread_cond_t;
    pthread_cond_init (this->stepCond, NULL);
}

DataReaderThread::~DataReaderThread() {
	
	delete this->shaCtx;
	delete this->evpCtx;
	delete this->codedShaCtx;
	if (inputFileHandler > 0)
		delete[] this->inputBuffer;
	
	if (this->outputBuffer != NULL && (inputFileHandler > 0 || this->encKey != NULL || this->idaImpl != NULL) ) {
		for (int i=0; i<this->numberOfUploads; i++)
			delete[] this->outputBuffer[i];
		delete[] this->outputBuffer;
	}

	if (this->idaImpl != NULL) {		
		for (int i=0; i<this->numberOfUploads; i++) {
			if (this->idaDataKey[i] != NULL) delete[] this->idaDataKey[i];
			if (this->idaShaCtx[i] != NULL) delete[] this->idaShaCtx[i];
			delete[] this->idaBuffer[i];
		}
		delete[] this->idaBuffer;
		delete[] this->idaDataKey;
		delete[] this->idaShaCtx;
	}

	if (this->encKey != NULL)
		delete[] this->interBuffer;			
    
    if (this->inputDataKey != NULL) 
    	delete[] this->inputDataKey;
    
    delete this->readerMutex; //
    delete this->readerCond;
    delete this->keyCond;
    delete this->stepCond;
}

void DataReaderThread::setNumberofUploads (int numberOfUploads_) {

	pthread_mutex_lock( this->readerMutex );
		this->numberOfUploads = numberOfUploads_;
		this->remainingUploads = this->numberOfUploads;
		this->numberOfRequired = 1;
		this->iteration++;
		
		this->outputBuffer = new unsigned char *[this->numberOfUploads];
		if (inputFileHandler > 0 || this->encKey != NULL ) {
			for (int i=0; i<this->numberOfUploads; i++)
				this->outputBuffer[i] = new unsigned char[this->allocatedBufferSize];
		}
		
		pthread_cond_broadcast( this->stepCond );
	pthread_mutex_unlock( this->readerMutex );
	
}

void DataReaderThread::setNumberofIdaUploads (int numberOfUploads_, int numberOfRequired_) {

	pthread_mutex_lock( this->readerMutex );
		this->numberOfUploads = numberOfUploads_;
		this->remainingUploads = this->numberOfUploads;
		this->numberOfRequired = numberOfRequired_;
		this->iteration++;				

	    this->idaFragmentSize = (this->allocatedBufferSize % numberOfRequired_ == 0) ? 
	    		this->allocatedBufferSize/numberOfRequired_ : this->allocatedBufferSize/numberOfRequired_+1;     
	    
	    this->idaBuffer = new unsigned char *[this->numberOfUploads];
		this->outputBuffer = new unsigned char *[this->numberOfUploads];
		this->idaShaCtx = new SHA_CTX *[this->numberOfUploads];
		for (int i=0; i<this->numberOfUploads; i++) {
			this->idaBuffer[i] = new unsigned char[this->idaFragmentSize];
			this->outputBuffer[i] = new unsigned char[this->idaFragmentSize];
			this->idaShaCtx[i] = new SHA_CTX;
		}	   	    	

	    this->bytesEncoded = new long[this->numberOfUploads];
						
		idaImpl = IDAImpl::getInstance();
		
		pthread_cond_broadcast( this->stepCond );
	pthread_mutex_unlock( this->readerMutex );
	
}


void DataReaderThread::readData() {

	brokerLogger.debug("Started reading data!");
	
	long totalReadBytes = 0;	
	this->usedBufferSize = 1;	
            
    EVP_CIPHER_CTX_init(this->evpCtx);
    EVP_EncryptInit_ex(this->evpCtx, EVP_rc4(), NULL, this->encKey, NULL);
	
	//cerr << "DataReaderThread::readThread -> locking" << endl;
	pthread_mutex_lock( this->readerMutex );
	//this->isReading = true;

	while (this->iteration == -2)
		pthread_cond_wait( this->stepCond, this->readerMutex );

	SHA1_Init(this->shaCtx);
	SHA1_Init(this->codedShaCtx);
    if (this->idaImpl != NULL)
    	for (int i=0; i<this->numberOfUploads; i++)
    		SHA1_Init(this->idaShaCtx[i]);

    while ( this->usedBufferSize > 0 && totalReadBytes < this->inputDataSize ) {    	    	

    	/**
    	 *  Reads data into inputBuffer
    	 */
    	if (inputFileHandler > 0) {
    		this->usedBufferSize = read(this->inputFileHandler, this->inputBuffer, this->allocatedBufferSize);    		
    	}
    	else {
    		this->inputBuffer = this->inputData + totalReadBytes;

    		if ( this->inputDataSize - totalReadBytes < this->allocatedBufferSize )    			
    			this->usedBufferSize = this->inputDataSize - totalReadBytes;
    		else
    			this->usedBufferSize = this->allocatedBufferSize;
    	}
    	if (this->usedBufferSize == 0) continue;
    	totalReadBytes += this->usedBufferSize;
    	    	
    	{
    	ostringstream logStr;
    	logStr << "Finished reading " << this->usedBufferSize << " bytes. Waiting for FragmentDownloadThread."; 
    	brokerLogger.debug( logStr.str() );    	
    	}    
    	
    	/**
    	 *  Encrypts the data or swap inputData into outputBuffer.
    	 */ 
    	int outputSize;
    	if (encKey == NULL)	this->interBuffer = this->inputBuffer;
    	else EVP_EncryptUpdate(this->evpCtx, this->interBuffer, &outputSize, this->inputBuffer, this->usedBufferSize);
    	//OppStoreUtilsSSL::encryptRC4(encKey, this->inputBuffer, this->usedBufferSize, this->outputBuffer);

    	/**
    	 *  Encodes data using IDA
    	 */ 
    	if (idaImpl != NULL)
    		idaImpl->encodeData(this->interBuffer, this->usedBufferSize, this->numberOfRequired, this->numberOfUploads-this->numberOfRequired, this->idaBuffer, this->bytesEncoded);
    	    	 
	    this->idaUsedSize = (this->usedBufferSize % this->numberOfRequired == 0) ? 
	    		this->usedBufferSize/this->numberOfRequired: this->usedBufferSize/this->numberOfRequired+1;     

	    //cout << this->usedBufferSize << " " << this->idaUsedSize << " " << *this->bytesEncoded << endl;;
    	/**
    	 *  Evaluates SHA-1 from outputBuffer
    	 */
    	SHA1_Update( this->shaCtx, this->inputBuffer, this->usedBufferSize );
    	SHA1_Update( this->codedShaCtx, this->interBuffer, this->usedBufferSize );    	
        if (this->idaImpl != NULL)
        	for (int i=0; i<this->numberOfUploads; i++)
        		SHA1_Update( this->idaShaCtx[i], this->idaBuffer[i], this->idaUsedSize );
    	
    	this->iteration++;
    	
    	/**
    	 *  Waits until encoder or AdrStub finishes using the previous outputBuffer 
    	 */      	
    	//pthread_cond_broadcast( this->isReadingCond );    	
    	pthread_cond_broadcast( this->stepCond );
    	pthread_cond_wait( this->readerCond, this->readerMutex );    	
    
    	//this->isReading = true;
    	brokerLogger.debug("DataReaderThread released!");
    }

    //cerr << "DataReaderThread::readThread -> braodcast isReading" << endl;
    this->iteration++;
    //pthread_cond_broadcast( this->isReadingCond );
    pthread_cond_broadcast( this->stepCond );
    this->usedBufferSize = 0;
    
    /**
     * Evaluates the inputKey and notifies any thread that was waiting to key the inputKey
     */     
    this->inputDataKey = new unsigned char[OppStoreUtils::binaryKeySize];
    SHA1_Final(this->inputDataKey, this->shaCtx);	    
    
    this->codedDataKey = new unsigned char[OppStoreUtils::binaryKeySize];
    SHA1_Final(this->codedDataKey, this->codedShaCtx);
    
    if (this->idaImpl != NULL) {
    	this->idaDataKey = new unsigned char *[this->numberOfUploads];
    	for (int i=0; i<this->numberOfUploads; i++) {
    	    this->idaDataKey[i] = new unsigned char[OppStoreUtils::binaryKeySize];
    	    SHA1_Final(this->idaDataKey[i], this->idaShaCtx[i]);    		
    	}
    }
    
    EVP_CIPHER_CTX_cleanup(this->evpCtx);
    
    //cerr << "DataReaderThread::readThread -> broadcast keyCond" << endl;
    pthread_cond_broadcast( this->keyCond );

    {
    char *key = new char[OppStoreUtils::binaryKeySize*2+1]; 
    OppStoreUtils::convertBinaryToHex( (char *)this->inputDataKey, key );
    key[OppStoreUtils::binaryKeySize*2] = 0;

    cout << "ReaderInput:  ";
    OppStoreUtils::printHexKey(key, cout);
    cout << endl;
    }

    {
    char *key = new char[OppStoreUtils::binaryKeySize*2+1]; 
    OppStoreUtils::convertBinaryToHex( (char *)this->codedDataKey, key );
    key[OppStoreUtils::binaryKeySize*2] = 0;

    cout << "ReaderCoded:  ";
    OppStoreUtils::printHexKey(key, cout);
    cout << endl;
    }

	{
	ostringstream logStr;
	logStr << "Finished reading the total of " << totalReadBytes << " bytes of " << this->inputDataSize << "!!!!!"; 
	brokerLogger.debug( logStr.str() );    	
	}
    //cout << "Finished reading " << totalReadBytes << " bytes of " << this->inputDataSize << "." << endl;
    
	
	//cerr << "DataReaderThread::readThread -> unlocking" << endl;
    pthread_mutex_unlock( this->readerMutex );
        
    return;
}


unsigned char *DataReaderThread::getInputDataKey( ) {

	//cerr << "DataReaderThread::getInputDataKey -> locking" << endl;
	pthread_mutex_lock( this->readerMutex );
	if (this->inputDataKey == NULL)
		pthread_cond_wait( this->keyCond, this->readerMutex );
	//cerr << "DataReaderThread::getInputDataKey -> unlocking" << endl;
	pthread_mutex_unlock( this->readerMutex );	
	
	return this->inputDataKey;
}

unsigned char *DataReaderThread::getCodedDataKey( ) {

	pthread_mutex_lock( this->readerMutex );
	if (this->codedDataKey == NULL)
		pthread_cond_wait( this->keyCond, this->readerMutex );	
	pthread_mutex_unlock( this->readerMutex );	
	
	return this->codedDataKey;
}

unsigned char *DataReaderThread::getIdaDataKey( int fragmentNumber_ ) {

	//sleep(1);
	pthread_mutex_lock( this->readerMutex );
	if (this->idaDataKey == NULL)
		pthread_cond_wait( this->keyCond, this->readerMutex );	
	pthread_mutex_unlock( this->readerMutex );	
	
    {
    char *key = new char[OppStoreUtils::binaryKeySize*2+1]; 
    OppStoreUtils::convertBinaryToHex( (char *)this->idaDataKey[fragmentNumber_], key );
    key[OppStoreUtils::binaryKeySize*2] = 0;

    cout << "ReaderCoded:  ";
    OppStoreUtils::printHexKey(key, cout);
    cout << " " << fragmentNumber_ << endl;
    }

	return this->idaDataKey[fragmentNumber_];
}

unsigned char *DataReaderThread::getOutputBuffer( int * outputBufferSize_, int iteration_, int fragmentNumber_ ) {
		
	//while (isReading == false) sleep(1);
	
	
	pthread_mutex_lock( this->readerMutex );
		
	while (this->iteration < iteration_)
		pthread_cond_wait( this->stepCond, this->readerMutex );
	
	//if (this->isReading == false)		
	//	pthread_cond_wait( this->isReadingCond, this->readerMutex );

	if (this->usedBufferSize <= 0) {
		*outputBufferSize_ = 0;		
		pthread_mutex_unlock( this->readerMutex );
		return NULL;
	}

	/**
	 *  Swap interBuffer and outputBuffer
	 */
	if (idaImpl != NULL)
		memcpy(this->outputBuffer[fragmentNumber_], this->idaBuffer[fragmentNumber_], this->idaUsedSize);
	else if (inputFileHandler > 0 || this->encKey != NULL)
		memcpy(this->outputBuffer[fragmentNumber_], this->interBuffer, this->usedBufferSize);
	else
		this->outputBuffer[fragmentNumber_] = this->interBuffer;
	
	/**
	 * Sets the getOutputBuffer results
	 */ 
    unsigned char *buffer = this->outputBuffer[fragmentNumber_];
    if (idaImpl != NULL) *outputBufferSize_ = this->idaUsedSize;
    else *outputBufferSize_ = this->usedBufferSize;    

	{
	ostringstream logStr;
	logStr << "Returning " << *outputBufferSize_ << " bytes from DataReaderThread outputBuffer to fragment " << fragmentNumber_ << "."; 
	brokerLogger.debug( logStr.str() );    	
	}

    /**
   	 *  Allows the DataReaderThread to overwrite inputData.  
   	 */
	this->remainingUploads--;	
	if (this->remainingUploads == 0) {
		//this->isReading = false;
		this->remainingUploads = this->numberOfUploads;
		pthread_cond_broadcast( this->readerCond );		
	}
    
    pthread_mutex_unlock( this->readerMutex );    
    return buffer;
}

void *DataReaderThread::run( void *ptr ) {
 
	//cout << "Starting DataReaderThread." << endl;
    int *threadId = (int *)ptr;
    DataReaderThread *dataReaderThread = dataReaderThreadMap[*threadId];
    dataReaderThread->readData();
    
    dataReaderThreadMap.erase(*threadId);
    delete threadId;
    
    return NULL;   
}

DataReaderThread *DataReaderThread::createDataReaderThread ( unsigned char *inputData_, long inputDataSize_, const unsigned char * encKey_ ) {

	int inputFileHandler_ = -1;
	DataReaderThread *dataReaderThread = new DataReaderThread( inputFileHandler_, inputData_, inputDataSize_, encKey_ );
	DataReaderThread::launchThread( dataReaderThread );

	return dataReaderThread;
}

DataReaderThread *DataReaderThread::createDataReaderThreadFromFile ( const char *filePath, long fileSize_, const unsigned char * encKey_ ) {

    int inputFileHandler_ = open( filePath, O_RDONLY );
    if (inputFileHandler_ > 0) {     	

    	DataReaderThread *dataReaderThread = new DataReaderThread( inputFileHandler_, NULL, fileSize_, encKey_ );
    	DataReaderThread::launchThread( dataReaderThread );

    	return dataReaderThread;
    }
    
    return NULL;    
}

void DataReaderThread::launchThread ( DataReaderThread *dataReaderThread ) {

    int *threadId = new int;
    *threadId = nextDataReaderId++;
    dataReaderThreadMap[*threadId] = dataReaderThread;
    
    pthread_t thread1;
    pthread_create( &thread1, NULL, DataReaderThread::run, threadId );
    pthread_detach(thread1);		
}

