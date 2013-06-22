#ifndef _ADRDATATRANSFERSTUB_HPP_
#define _ADRDATATRANSFERSTUB_HPP_

#include <string>
#include "DataReaderThread.hpp"
#include "DataWriterThread.hpp"
using namespace std;

class AdrDataTransferStub {

    int sockfdTemp;      

public:
    AdrDataTransferStub() : sockfdTemp(0){}
  
    /**
     * Connects to the ADR.
     * Must be called before calling any other operation of the stub. 
     */
    int connectToServer(string ipAddress, short port);
  
    /**
     * Removes a fragment stored in the ADR.
     */
    int removeFragment(int sockfd, const char *fragmentId, int fragmentIdSize);

    /**
     * Renews the lease of an already stored file.
     */
    int renewFragmentLease(int sockfd, const char *fragmentId, int fragmentIdSize, int timeoutMinutes);

    /**
     * Creates a copy of a file in the local file system in an ADR located in the same machine.
     */
    int storeLocalFile(int sockfd, const char *fragmentId, int fragmentIdSize, const char *fragmentPath, int pathSize, int timeoutMinutes);
    
    /**
     * Transfer the fragment stored in 'data' to ipAddress:portNumber.
     * Returns the number of transfered bytes or -1 if a failure occurs;
     */  
    int transferData(int sockfd, const char *fragmentId, int fragmentIdSize, const void *data, long nbytes, int timeoutMinutes);
      
    int performDataTransfer(int sockfd, const void *data, long nbytes);
    int performDataTransferInc(int sockfd, const void *data, long nbytes, long *availableBytes); 
    int performDataTransferDataReader(int sockfd, DataReaderThread *dataReaderThread, long nbytes, int fragmentNumber);
    	
    int finishDataTransfer(int sockfd, const char *fragmentId, int fragmentIdSize, int timeoutMinutes);
      
    /**
     * Reads the fragment with with id 'fragmentId' from ipAddress:portNumber and puts into 'data'.
     * Returns the number of bytes read or -1 if a failure occurs;
     */
    int readData(int sockfd, const char *fragmentId, int fragmentIdSize, void * data, int dataSize, long *availableBytes);
    int readDataToWriter(int sockfd, DataWriterThread *writerThread, int fragmentNumber, const char *fragmentId, int fragmentIdSize, int dataSize);
  
};

#endif /*_ADRDATATRANSFER_HPP_*/
