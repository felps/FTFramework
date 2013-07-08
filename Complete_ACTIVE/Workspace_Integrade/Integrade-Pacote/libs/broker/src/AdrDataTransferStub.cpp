#include "AdrDataTransferStub.hpp"
#include "utils/c++/SocketUtils.hpp"
#include "utils/c++/StringUtils.hpp"
#include "BrokerLogger.hpp"
#include "OppStoreUtils.hpp"

#include <sstream>
#include <unistd.h>
#include <fcntl.h>
#include <cassert>
#include <cstring>

using namespace std;

//===========================================================================
// CkpRepositoryStub
//===========================================================================

int AdrDataTransferStub::connectToServer(string ipAddress, short port) {

  // Fills serverAddress with the server address and port
  struct sockaddr_in serverAddress;
  bzero((char *) &serverAddress, sizeof(serverAddress));
  serverAddress.sin_family      = AF_INET;
  serverAddress.sin_addr.s_addr = inet_addr(ipAddress.c_str());  
  serverAddress.sin_port        = htons(port);
 
  // Opens a TCP socket
  int sockfd = socket(AF_INET, SOCK_STREAM, 0);
  if ( sockfd < 0 ) {
    std::cerr << "AdrDataTransferStub::connectToServer --> cannot open stream socket." << std::endl;
    return -1;
  }

  // Connects to server
  int connectFlag = connect (sockfd, (struct sockaddr *)&serverAddress, sizeof(serverAddress));
  if ( connectFlag < 0 ) {
    std::cerr << "AdrDataTransferStub::connectToServer --> cannot connect to server." << std::endl;
    return -1;
  }
   
  return sockfd;
}

//---------------------------------------------------------------------------
// delete(11) -> keySize -> key
int AdrDataTransferStub::removeFragment(int sockfd, const char *fragmentId, int fragmentIdSize) {
	
	// Indicates that a removal operation will be performed
    SocketUtils::writeUint32(sockfd, (long)11); 

    // Transfer the fragmentId
    SocketUtils::writeUint32(sockfd, (long)fragmentIdSize);
    SocketUtils::writeToStream(sockfd, fragmentId, fragmentIdSize);

    // Gets the removal status
    long removalStatus = SocketUtils::readUint32(sockfd);
    
    close (sockfd);
    return removalStatus;
}

//---------------------------------------------------------------------------

// renew(21) -> keySize -> key
int AdrDataTransferStub::renewFragmentLease(int sockfd, const char *fragmentId, int fragmentIdSize, int timeoutMinutes) {

	// Indicates that a renew operation will be performed
    SocketUtils::writeUint32(sockfd, (long)21); 

    // Transfer the fragmentId
    SocketUtils::writeUint32(sockfd, (long)fragmentIdSize);
    SocketUtils::writeToStream(sockfd, fragmentId, fragmentIdSize);

    // Transfer the timeout
    SocketUtils::writeUint32(sockfd, (long)timeoutMinutes);    

    // Gets the renew status
    long renewStatus = SocketUtils::readUint32(sockfd);
    
    close (sockfd);
    return renewStatus;
}

//---------------------------------------------------------------------------

// storeFile(3) -> fragmentIdSize -> fragmentId -> pathSize -> fragmentPath -> timeoutMinutes 
int AdrDataTransferStub::storeLocalFile(int sockfd, const char *fragmentId, int fragmentIdSize, const char *fragmentPath, int pathSize, int timeoutMinutes) {
	
	// Indicates that a store file operation will be performed
    SocketUtils::writeUint32(sockfd, (long)3); 

    // Transfer the fragmentId
    SocketUtils::writeUint32(sockfd, (long)fragmentIdSize);
    SocketUtils::writeToStream(sockfd, fragmentId, fragmentIdSize);

    // Transfer the pathName
    SocketUtils::writeUint32(sockfd, (long)pathSize);
    SocketUtils::writeToStream(sockfd, fragmentPath, pathSize);
    
    // Transfer the timeout
    SocketUtils::writeUint32(sockfd, (long)timeoutMinutes);    

    // Gets the storage status
    long storageStatus = SocketUtils::readUint32(sockfd);
    
    close (sockfd);
    return storageStatus;
}

//---------------------------------------------------------------------------
  
int AdrDataTransferStub::performDataTransfer(int sockfd, const void *data, long nbytes) {

    // sending a new fragment
    SocketUtils::writeUint32(sockfd, (long)1); // Indicates that a store will be performed

    // Transfer data to repository
    SocketUtils::writeUint32(sockfd, (long)nbytes);
    SocketUtils::writeToStream(sockfd, data, nbytes);
    
    return 0;
}

int AdrDataTransferStub::performDataTransferInc(int sockfd, const void *data, long nbytes, long *availableBytes) {

    // sending a new fragment
    SocketUtils::writeUint32(sockfd, (long)1); // Indicates that a store will be performed

    // Transfer data to repository
    SocketUtils::writeUint32(sockfd, (long)nbytes);
    SocketUtils::writeToStreamInc(sockfd, data, nbytes, availableBytes);
    
    return 0;
}

int AdrDataTransferStub::performDataTransferDataReader(int sockfd, DataReaderThread *dataReaderThread, long nbytes, int fragmentNumber) {

    // sending a new fragment
    SocketUtils::writeUint32(sockfd, (long)1); // Indicates that a store will be performed

    // Transfer data to repository
    SocketUtils::writeUint32(sockfd, (long)nbytes);

    int iteration = 0;
    int outputBufferSize = 0;
    unsigned char *outputBuffer = dataReaderThread->getOutputBuffer( &outputBufferSize, iteration++, fragmentNumber );
    while (outputBufferSize > 0 && outputBuffer != NULL) {    	    	
    	SocketUtils::writeToStream(sockfd, outputBuffer, outputBufferSize);
    	outputBuffer = dataReaderThread->getOutputBuffer( &outputBufferSize, iteration++, fragmentNumber );
    }

    return 0;
}

int AdrDataTransferStub::finishDataTransfer(int sockfd, const char *fragmentId, int fragmentIdSize, int timeoutMinutes) {

  // Transfer the fragmentId
  SocketUtils::writeUint32(sockfd, (long)fragmentIdSize);
  SocketUtils::writeToStream(sockfd, fragmentId, fragmentIdSize);
  
  // Transfer the timeout
  SocketUtils::writeUint32(sockfd, (long)timeoutMinutes);
  
  // get the number of bytes written
  long writeCompleted = SocketUtils::readUint32(sockfd);
  
  close (sockfd);

  return writeCompleted;
}

//---------------------------------------------------------------------------
  
int AdrDataTransferStub::transferData
(int sockfd, const char *fragmentId, int fragmentIdSize, const void *data, long nbytes, int timeoutMinutes) {

    performDataTransfer(sockfd, data, nbytes);
    return finishDataTransfer(sockfd, fragmentId, fragmentIdSize, timeoutMinutes);
    
}

//---------------------------------------------------------------------------
//int sockfd, DataReaderThread *dataReaderThread, long nbytes, int fragmentNumber
int AdrDataTransferStub::readDataToWriter(int sockfd, DataWriterThread *writerThread, int fragmentNumber, const char *fragmentId, int fragmentIdSize, int dataSize) {

	long availableBytes = 0;
    
	// Data retrieval operation 
	SocketUtils::writeUint32(sockfd, (long)2);

	// Transfer the executionId
	SocketUtils::writeUint32(sockfd, (long)fragmentIdSize);
	SocketUtils::writeToStream(sockfd, fragmentId, fragmentIdSize);

	// Reads the file data from the stream to 'data'
	long storedSize = SocketUtils::readUint32(sockfd);	

	if (dataSize <= 0 || storedSize <= 0 || storedSize != dataSize )
		return -1;
	
	//long remainingBytes = dataSize;
		
    int iteration = 0;
    int inputBufferSize = 0;
    unsigned char *inputBuffer = writerThread->getInputBuffer(&inputBufferSize, fragmentNumber, iteration );
    while (inputBufferSize > 0 && inputBuffer != NULL) {
    	
    	SocketUtils::readFromStream(sockfd, inputBuffer, (long)inputBufferSize, &availableBytes);
    	writerThread->setInputBufferUpdated(availableBytes, iteration++, fragmentNumber);
    	inputBuffer = writerThread->getInputBuffer(&inputBufferSize, fragmentNumber, iteration );    	
    }
	
	close (sockfd);  

	{
		std::ostringstream logStr;
		logStr << "AdrDataTransferStub::readData -> Finished recovering data!!! nbytes=" << availableBytes; 
		brokerLogger.debug( logStr.str() );        
	}    

	return storedSize;
}

//---------------------------------------------------------------------------

int AdrDataTransferStub::readData(int sockfd, const char *fragmentId, int fragmentIdSize, void * data, int dataSize, long *availableBytes) {

  //int sockfd = this->connectToServer(ipAddress, portNumber);
  //if (sockfd < 0) return -1;
    
  // Asking for checkpoint 'CkpNumber' 
  SocketUtils::writeUint32(sockfd, (long)2);
  
  // Transfer the executionId
  SocketUtils::writeUint32(sockfd, (long)fragmentIdSize);
  SocketUtils::writeToStream(sockfd, fragmentId, fragmentIdSize);

  // Reads the file data from the stream to 'data'
  long storedSize = SocketUtils::readUint32(sockfd);

  if (dataSize > 0 && storedSize > 0 )
	  SocketUtils::readFromStream(sockfd, data, (long)dataSize, availableBytes);
  close (sockfd);  
  
  {
        std::ostringstream logStr;
        logStr << "AdrDataTransferStub::readData -> Finished recovering data!!! nbytes=" << *availableBytes; 
        brokerLogger.debug( logStr.str() );        
  }    
  
  return storedSize;
}

//---------------------------------------------------------------------------

