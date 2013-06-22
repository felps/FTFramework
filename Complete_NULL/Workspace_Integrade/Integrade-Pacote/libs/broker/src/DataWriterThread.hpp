#ifndef DATAWRITERTHREAD_HPP_
#define DATAWRITERTHREAD_HPP_

#define READER_BUFFER_SIZE 512*1024 

#include "ida/IDAImpl.h"

#include <pthread.h>
#include <openssl/sha.h>
#include <openssl/evp.h>

class DataWriterThread {
	
	int outputFileHandler;
	unsigned char *outputData;
	long outputDataSize;	
	
	unsigned char *outputDataKey;
	unsigned char *codedDataKey;
	unsigned char **fragmentKey;
	
	long fragmentSize;
	long totalReadBytes;
    
	unsigned char *outputBuffer;
	unsigned char *interBuffer;
    unsigned char **inputBuffer;
    unsigned char **idaBuffer;
    
    int allocatedBufferSize;
    int usedBufferSize;    
    
    int remainingDownloads;
    int numberOfDownloads;
    int numberOfRequired;
    int iteration;
    
    IDAImpl *idaImpl;
    long *bytesEncoded;
    int idaFragmentSize;
    int idaUsedSize;
    int *fragmentNumbers;
    
    const unsigned char *encKey;
    SHA_CTX *shaCtx;    
    SHA_CTX *codedShaCtx;
    SHA_CTX **fragmentShaCtx;
    
    EVP_CIPHER_CTX *evpCtx;

    pthread_mutex_t *writerMutex;
    pthread_cond_t  *writerCond;   
    pthread_cond_t  *keyCond;    
    pthread_cond_t  *stepCond;

    DataWriterThread( int inputFileHandler_, unsigned char *inputData_, long inputDataSize_, const unsigned char * enckey_ );
    
    static void *run( void *ptr );
    
    void writeData();
    
    static void launchThread ( DataWriterThread *dataWriterThread );
    
public:
	
    virtual ~DataWriterThread();
        
    //void setNumberofDownloads (int numberOfUploads_);
    void configureFileDownload (long fileSize, long fragmentSize, int numberOfDownloads_, int numberOfRequired_, int *fragmentNumbers);
    
    void setInputBufferUpdated(int numberOfBytesRead, int iteration_, int fragmentNumber );
    
    unsigned char *getInputBuffer(int * outputBufferSize, int fragmentNumber, int iteration_ );
    
    unsigned char *getOutputData();
    
    unsigned char *getOutputDataKey( );
    
    unsigned char *getFragmentKey(int fragmentNumber);
    
    //unsigned char *getIdaDataKey( int fragmentNumber );
    
    static DataWriterThread *createDataWriterThread ( unsigned char *inputData, long inputDataSize, const unsigned char * enckey );
    
    static DataWriterThread *createDataWriterThreadToFile ( const char *filePath, long fileSize_, const unsigned char * enckey );
    
};

// Does it make sense to maintain a single thread?

#endif /*IDAENCODINGTHREAD_HPP_*/
