// CRDataServer.cpp

#include "CRDataServer.hpp"
#include "utils/c++/SocketUtils.hpp"
#include "utils/c++/FileUtils.hpp"
// gethostbyname

#include <fcntl.h>

#include <cstdlib>
#include <cstring>
#include <cassert>
#include <iostream>
#include <fstream>
#include <sstream>

#include <sys/types.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include <strings.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

struct DataInfo {
  char *data;  
  long dataSize;
  char *execId;
  long execIdSize;  
  int ckpNumber;
  int fragmentNumber;
};

//------------------------------------------------------------------------------
CRDataServer::CRDataServer(AdrManagerStub & stub, const int & port, string & storagePath) : adrManager(stub) {
    
    this->port = port;
    this->maxPort = this->port + 100;
  
    // sets and creates output dir
    storagePath += "/";
    this->outputDir = storagePath.c_str();
    mkdir(outputDir.c_str(), 0700);
    ostringstream rmCmd;
    rmCmd << "rm " << this->outputDir << "/*.adr " << this->outputDir << "/adrWrite.fifo -f";
    system(rmCmd.str().c_str());
        
    // obtains the ipAddress of the host
    system("hostname -i > machineIp.dat");
    ifstream ipFile("machineIp.dat");
    ipFile >> this->ipAddress;
    ipFile.close();
      
    // Initializes Server  
    this->setupServer();      
}

//------------------------------------------------------------------------------
CRDataServer::CRDataServer(AdrManagerStub & stub, string ipAddress, const int & port, string & storagePath) : adrManager(stub) {

    this->port = port;
    this->maxPort = this->port + 100;
      
    // sets and creates output dir
    storagePath += "/";
    this->outputDir = storagePath.c_str();
    mkdir(outputDir.c_str(), 0700);
    ostringstream rmCmd;
    rmCmd << "rm " << this->outputDir << "/*.adr -f";
    system(rmCmd.str().c_str());
        
    // obtains the ipAddress of the host
    this->ipAddress = ipAddress;
      
    // Initializes Server  
    this->setupServer();      
}

void CRDataServer::notifyAdrManager(ssize_t notification, ssize_t nWrittenBytes, ssize_t timeout, ssize_t fragmentIdSize, char *fragmentId) {
	
	int fifoBufferSize = 4*sizeof(ssize_t) + fragmentIdSize;
	char *fifoBuffer = new char[ fifoBufferSize ];	
	memcpy( fifoBuffer,                   &notification,   sizeof(ssize_t) );
	memcpy( fifoBuffer+1*sizeof(ssize_t), &nWrittenBytes,  sizeof(ssize_t) );
	memcpy( fifoBuffer+2*sizeof(ssize_t), &timeout,        sizeof(ssize_t) );
	memcpy( fifoBuffer+3*sizeof(ssize_t), &fragmentIdSize, sizeof(ssize_t) );
	memcpy( fifoBuffer+4*sizeof(ssize_t), fragmentId,      fragmentIdSize );                                     
	int fifofd = open( fifoName_.c_str(), O_WRONLY );
	write( fifofd, fifoBuffer, fifoBufferSize );        
	delete[] fifoBuffer;           
}


//------------------------------------------------------------------------------
void CRDataServer::storeData(int sockfd) {
	
    std::ostringstream tempFileName;
    tempFileName << outputDir << getpid() << ".adr";
    int tempfd = open(tempFileName.str().c_str(), O_WRONLY | O_CREAT, 0600);
    
    // Reads the file data from the stream to 'data'
    long fragmentDataSize = (long)SocketUtils::readUint32(sockfd);
    //assert (fragmentDataSize < 5 * 1024 * 1024);
    int nWrittenBytes = SocketUtils::readFromStreamToFile(sockfd, fragmentDataSize, tempfd);        
    close(tempfd);
    
    // Reads the fragmentId from the stream to 'fragmentId'
    int fragmentIdSize = (long)SocketUtils::readUint32(sockfd);        
    char *fragmentId = new char[fragmentIdSize+1];
    fragmentId[fragmentIdSize]=0; // Marks the end of the string
    SocketUtils::readFromStream(sockfd, fragmentId, fragmentIdSize, NULL);

    // Reads the timeout from the stream to 'timeout'
    int timeout = (long)SocketUtils::readUint32(sockfd);
    //std::cout << "timeOut=" << timeout << "." << std::endl;

    // Saves data to filesystem -----------------------
    std::ostringstream fragmentFileName;
    fragmentFileName << outputDir << fragmentId << ".adr";
    rename(tempFileName.str().c_str(), fragmentFileName.str().c_str());
    // ------------------------------------------------
        
    std::cout << "Data written to " << fragmentFileName.str() << " nbytes=" << nWrittenBytes << " of " << fragmentDataSize << "." << std::endl;

    /**
     *  Notifies the adrManager about the newly stored fragment
     */
    if (nWrittenBytes > 0) {
    	ssize_t notification = 1;
    	notifyAdrManager(notification, nWrittenBytes, timeout, fragmentIdSize, fragmentId);    	
    }
    
    delete[] fragmentId;
    SocketUtils::writeUint32(sockfd, nWrittenBytes);
}

//------------------------------------------------------------------------------
void CRDataServer::recoverData(int sockfd) {
	
    // Reads the executionId from the stream to 'data'
    long fragmentIdSize = (long)SocketUtils::readUint32(sockfd);        
    char *fragmentId = new char[fragmentIdSize+1];
    fragmentId[fragmentIdSize]=0;      
    SocketUtils::readFromStream(sockfd, fragmentId, fragmentIdSize, NULL);
    
    // Opens the correct file and put in buffer --------
    std::ostringstream fragmentFileName;
    fragmentFileName << outputDir << fragmentId << ".adr";    
    
    long fragmentDataSize = 0;
    char *fragmentData = 0;
    
    int inFile = open(fragmentFileName.str().c_str(), O_RDONLY);
    if (inFile > 0) { 
    	fragmentDataSize = FileUtils::getFileSize(fragmentFileName.str().c_str());
    	
    	fragmentData = new char[fragmentDataSize];
    	read(inFile, fragmentData, fragmentDataSize);
    	
        std::cout << "Returning Data from " << fragmentFileName.str() << " dataSize=" << fragmentDataSize << std::endl;
            
        // Writes the number of bytes of the file
        SocketUtils::writeUint32(sockfd, (long)fragmentDataSize);
        // Transfer data to repository
        if (fragmentDataSize > 0) {
        	SocketUtils::writeToStream(sockfd, fragmentData, fragmentDataSize);
        	delete[] fragmentData;
        }
    }
    else {
        std::cerr << "Couldn't open file " << fragmentFileName.str() << "." << std::endl;
        
        // Writes error code into the stream
        SocketUtils::writeUint32(sockfd, -1);        
    }

    // -------------------------------------------------
    
    delete[] fragmentId;

}

//------------------------------------------------------------------------------
void CRDataServer::storeFilePath(int sockfd) {
	//fragmentIdSize -> fragmentId -> pathSize -> fragmentPath -> timeoutMinutes
	
    // Reads the 'executionId' from the stream 
    long fragmentIdSize = (long)SocketUtils::readUint32(sockfd);        
    char *fragmentId = new char[fragmentIdSize+1];
    fragmentId[fragmentIdSize]=0;      
    SocketUtils::readFromStream(sockfd, fragmentId, fragmentIdSize, NULL);

    // Reads the 'path' from the stream
    long pathSize = (long)SocketUtils::readUint32(sockfd);        
    char *fragmentPath = new char[pathSize+1];
    fragmentPath[pathSize]=0;      
    SocketUtils::readFromStream(sockfd, fragmentPath, pathSize, NULL);

    // Reads the timeout from the stream to 'timeout'
    int timeout = (long)SocketUtils::readUint32(sockfd);
    
    /**
     * Creates the hardlink and obtains the size of the fragment from the file system
     */     
    std::ostringstream fragmentFileName;
    fragmentFileName << outputDir << fragmentId << ".adr";
    int linkStatus = link(fragmentPath, fragmentFileName.str().c_str());
    int fragmentDataSize = FileUtils::getFileSize(fragmentFileName.str().c_str());     
    
    cout << "CRDataServer::storeData -> storing file path " << fragmentFileName.str() << "." << endl;
    
    /**
     *  Notifies the adrManager about the newly stored fragment
     */
    if (linkStatus == 0) {
    	ssize_t notification = 1;
    	notifyAdrManager(notification, fragmentDataSize, timeout, fragmentIdSize, fragmentId);    	
    }

    /**
     * returns the renew status
     */
    delete[] fragmentId;  
    SocketUtils::writeUint32(sockfd, linkStatus);

}

//------------------------------------------------------------------------------
void CRDataServer::removeData(int sockfd) {

    // Reads the executionId from the stream to 'data'
    long fragmentIdSize = (long)SocketUtils::readUint32(sockfd);        
    char *fragmentId = new char[fragmentIdSize+1];
    fragmentId[fragmentIdSize]=0;      
    SocketUtils::readFromStream(sockfd, fragmentId, fragmentIdSize, NULL);

    /**
     * Obtains the size and removes the fragment from the file system
     */     
    std::ostringstream fragmentFileName;
    fragmentFileName << outputDir << fragmentId << ".adr";    
    int fragmentDataSize = FileUtils::getFileSize(fragmentFileName.str().c_str());    
	int removalStatus = unlink(fragmentFileName.str().c_str());
    
	cout << "CRDataServer::removeData -> removing file " << fragmentFileName.str() << "." << endl;
	
    /**
     *  Notifies the adrManager about the newly stored fragment
     */
	if (removalStatus == 0) {
		ssize_t notification = 2;
    	notifyAdrManager(notification, fragmentDataSize, 0, fragmentIdSize, fragmentId);
    	std::cout << "Removed fragment from " << fragmentFileName.str() << "." << std::endl;
	}

    /**
     * returns the removal status
     */
    delete[] fragmentId;
    SocketUtils::writeUint32(sockfd, removalStatus);
}

//------------------------------------------------------------------------------
void CRDataServer::renewData(int sockfd) {

    // Reads the executionId from the stream to 'data'
    long fragmentIdSize = (long)SocketUtils::readUint32(sockfd);        
    char *fragmentId = new char[fragmentIdSize+1];
    fragmentId[fragmentIdSize]=0;      
    SocketUtils::readFromStream(sockfd, fragmentId, fragmentIdSize, NULL);

    // Reads the timeout from the stream to 'timeout'
    int timeout = (long)SocketUtils::readUint32(sockfd);
   
    std::ostringstream fragmentFileName;
    fragmentFileName << outputDir << fragmentId << ".adr";    
	int inFile = open(fragmentFileName.str().c_str(), O_RDONLY);	
	int status = -1;
	
	cout << "CRDataServer::renewData -> renewing file " << fragmentFileName.str() << "." << endl; 
	
    /**
     *  Notifies the adrManager about the newly stored fragment
     */
	if (inFile >= 0) {
		ssize_t notification = 3;
		notifyAdrManager(notification, 0, timeout, fragmentIdSize, fragmentId);
		close (inFile);
		status = 0;
	}

    /**
     * returns the renew status
     */
	delete[] fragmentId;
    SocketUtils::writeUint32(sockfd, status);
}

//------------------------------------------------------------------------------
void CRDataServer::readStream(int sockfd) {

  long methodType = SocketUtils::readUint32(sockfd);

  // Store data --> fragmentIdSize(int) fragmentId(char*) fragmentSize(int) fragmentData(char*) timeout(int) | nWrittenBytes(int)
  if (methodType == 1)
	  this->storeData(sockfd);
  
  // Recover stored data --> fragmentIdSize(int) fragmentId(char*) fragmentSize(int) fragmentData(char*)
  else if (methodType == 2)
	  this->recoverData(sockfd);

  // Store file path data --> fragmentIdSize(int) fragmentId(char*) pathSize(int) filePath(char*) timeout(int)
  else if (methodType == 3)
	  this->storeFilePath(sockfd);

  // Delete stored data --> fragmentIdSize(int) fragmentId(char*) | status (int)
  else if (methodType == 11)
	  this->removeData(sockfd);

  // Renew stored data lease --> fragmentIdSize(int) fragmentId(char*) timeout(int) | status (int)  
  else if (methodType == 21)
	  this->renewData(sockfd);

}

//------------------------------------------------------------------------------

void CRDataServer::setupServer() {
  
  // Open a TCP socket
  this->sockfd_ = socket(AF_INET, SOCK_STREAM, 0);
  if ( sockfd_ < 0 )
    std::cerr << "CRDataServer::startServer --> cannot open stream socket." << std::endl;

  // Binds the server address
  struct sockaddr_in serverAddress;
  bzero((char *) &serverAddress, sizeof(serverAddress));
  serverAddress.sin_family      = AF_INET;
  serverAddress.sin_addr.s_addr = htonl(INADDR_ANY);  
  serverAddress.sin_port        = htons(port);
  
  bool bound = false;
  while (bound == false && port < maxPort) {  
    if ( bind(sockfd_, (struct sockaddr *)&serverAddress, sizeof(serverAddress)) == 0 )
      bound = true;
    else
      serverAddress.sin_port = htons(++port);
  }
            
  ostringstream oppStoreAddress;
  oppStoreAddress << this->ipAddress << ":" << this->port;
  int availableSpace = FileUtils::getAvailableDiskSpace(this->outputDir.c_str());
  this->ckpReposId = adrManager.registerAdr(oppStoreAddress.str(), availableSpace, 1.0, 1.0);
  
  ostringstream fifoNameStream;
  fifoNameStream << this->outputDir << "/adrWrite.fifo";
  this->fifoName_ = fifoNameStream.str(); 
  mkfifo( this->fifoName_.c_str(), S_IRUSR | S_IWUSR );
  //std::cerr << "ERROR! Could not create FIFO file " << this->fifoName_ << "." << std::endl;
  
  std::cout << "ckpRepository: Listening on ip:" << ipAddress      << " port:" << port << std::endl;      
  std::cout << "ckpRepository: started with id " << ckpReposId     << "."      << std::endl;  
  std::cout << "ckpRepository: saving data at '" << outputDir      << "'."     << std::endl;
  std::cout << "ckpRepository: availableSpace="  << availableSpace << "."      << std::endl;
   
  listen(sockfd_, 128);          
}

//----------------------------------------------------------------------------

void CRDataServer::startServer() {
    
  while(true) {
    
    // Accepts the connection from a client
    struct sockaddr_in clientAddress;
    socklen_t clientAddressLength = sizeof(clientAddress);
    int newsockfd = 
      accept (sockfd_, (struct sockaddr *)&clientAddress, &clientAddressLength);
    
    if (newsockfd < 0)
      std::cerr << "CRDataServer::startServer --> error accepting connection." << std::endl;
    
    // Forks a new process for concurrent processing
    int pidMid = fork();
    if (pidMid == 0) {  //mid
      int pidBotton = fork();
      if (pidBotton == 0) {  //son
        close (sockfd_); // close original socket
        this->readStream(newsockfd);
        exit(0);
      }
      else 
	    exit(0);
    }
    else
        wait(NULL);

    // Parent process: close the new socket
    close (newsockfd); 
  }
}

//------------------------------------------------------------------------------
