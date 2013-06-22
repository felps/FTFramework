#include "FileManipulationNotifier.hpp"
#include "OppStoreUtils.hpp"

#include <cstring>
#include <iostream>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
using namespace std;

FileManipulationNotifier *fileManipulationNotifier;

FileManipulationNotifier::FileManipulationNotifier( AdrManagerStub & stub, const string & fifoName, int ckpReposId ) : adrManagerStub_ (stub) {
    this->fifoName_ = fifoName;    
    this->ckpReposId_ = ckpReposId;
}

FileManipulationNotifier::~FileManipulationNotifier() {
    
}

void FileManipulationNotifier::notifierLoop () {
    
    const int bufferSize = 5000;
    const int keyBufferSize = 1024;
    const int binaryKeySize = OppStoreUtils::binaryKeySize;
    char *readBuffer = new char[bufferSize];
    
    ssize_t notificationType = 0; // 1 = store, 2 = remove, 3 = renew
    ssize_t nBytesWritten = 0;
    ssize_t keySize = 0;
    char *hexKey = new char[keyBufferSize];
    char *binaryKey = new char[binaryKeySize];
    ssize_t timeOutMinutes = 0;
     
    std::cout << "Starting notifier loop. "  << sizeof(ssize_t) << std::endl;
             
    /**
     * Reads the number of bytes written to the disk from the FIFO file 'fifoName'
     */ 
    while (true) {
        
        int fifofd = open( fifoName_.c_str(), O_RDONLY );
        int nbytes = read(fifofd, readBuffer, bufferSize);
        //cerr << "nBytesRead=" << nbytes << endl;
        
        //for ( int pos=0; pos < nbytes; pos += sizeof(ssize_t) )  {
        for ( int pos=0; pos < nbytes; )  {

            memcpy(&notificationType, &(readBuffer[pos]), sizeof(ssize_t));
            pos += sizeof(ssize_t);                        
           	memcpy(&nBytesWritten, &(readBuffer[pos]), sizeof(ssize_t));
           	pos += sizeof(ssize_t);
           	memcpy(&timeOutMinutes, &(readBuffer[pos]), sizeof(ssize_t));
           	pos += sizeof(ssize_t);
           	memcpy(&keySize, &(readBuffer[pos]), sizeof(ssize_t));
           	pos += sizeof(ssize_t);
           	memcpy(hexKey, &(readBuffer[pos]), keySize);
           	pos += keySize;
           	
           OppStoreUtils::convertHexToBinary( hexKey, binaryKey );               
            
            if (notificationType == 1) // storage                
            	adrManagerStub_.setFragmentStored(ckpReposId_, binaryKey, binaryKeySize, nBytesWritten, timeOutMinutes);            
            
            else if (notificationType == 2) // removal
            	adrManagerStub_.setFragmentRemoved(ckpReposId_, binaryKey, binaryKeySize, nBytesWritten);
            	
            else if (notificationType == 3) // renewal
                adrManagerStub_.setFragmentLeaseRenewed(ckpReposId_, binaryKey, binaryKeySize, timeOutMinutes);            
        }             
    }
    
    delete[] readBuffer;
    delete[] hexKey;
    //delete[] numberBuffer;
}

void *FileManipulationNotifier::run( void *ptr ) {
    fileManipulationNotifier->notifierLoop();
    return NULL;
}


void FileManipulationNotifier::launchFileManipulationNotifier( AdrManagerStub & stub, const string & fifoName, int ckpReposId ) {
    
    fileManipulationNotifier = new FileManipulationNotifier( stub, fifoName, ckpReposId );

    pthread_t thread1;
    pthread_create( &thread1, NULL, FileManipulationNotifier::run, NULL);
    pthread_detach(thread1);
}
