#ifndef DATAREADERTHREAD_HPP_
#define DATAREADERTHREAD_HPP_

#define READER_BUFFER_SIZE 512*1024 

#include "ida/IDAImpl.h"

#include <pthread.h>
#include <openssl/sha.h>
#include <openssl/evp.h>

class DataReaderThread {
	
	int inputFileHandler;
	unsigned char *inputData;
	long inputDataSize;	
	unsigned char *inputDataKey;
	unsigned char *codedDataKey;
	unsigned char **idaDataKey;
	
	//bool isReading;
    
	unsigned char *inputBuffer;
	unsigned char *interBuffer;
    unsigned char **outputBuffer;
    unsigned char **idaBuffer;
    int allocatedBufferSize;
    int usedBufferSize;    
    
    int remainingUploads;
    int numberOfUploads;
    int numberOfRequired;
    int iteration;
    
    IDAImpl *idaImpl;
    long *bytesEncoded;
    int idaFragmentSize;
    int idaUsedSize;
    
    const unsigned char *encKey;
    SHA_CTX *shaCtx;    
    SHA_CTX *codedShaCtx;
    SHA_CTX **idaShaCtx;
    
    EVP_CIPHER_CTX *evpCtx;

    pthread_mutex_t *readerMutex;
    pthread_cond_t  *readerCond;
    
    pthread_cond_t  *keyCond;    
    pthread_cond_t  *stepCond;

    DataReaderThread( int inputFileHandler_, unsigned char *inputData_, long inputDataSize_, const unsigned char * enckey_ );
    
    static void *run( void *ptr );
    
    void readData();
    
    static void launchThread ( DataReaderThread *dataReaderThread );
    
public:
	
    virtual ~DataReaderThread();
        
    void setNumberofUploads (int numberOfUploads_);
    void setNumberofIdaUploads (int numberOfUploads_, int numberOfRequired_);
    
    unsigned char *getOutputBuffer(int * outputBufferSize, int iteration_, int fragmentNumber );
    
    unsigned char *getInputDataKey( );
    
    unsigned char *getCodedDataKey( );
    
    unsigned char *getIdaDataKey( int fragmentNumber );
    
    static DataReaderThread *createDataReaderThread ( unsigned char *inputData, long inputDataSize, const unsigned char * enckey );
    
    static DataReaderThread *createDataReaderThreadFromFile ( const char *filePath, long fileSize_, const unsigned char * enckey );
    
};

// Does it make sense to maintain a single thread?

#endif /*IDAENCODINGTHREAD_HPP_*/
