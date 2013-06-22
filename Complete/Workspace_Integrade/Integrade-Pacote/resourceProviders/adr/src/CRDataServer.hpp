// CRDataServer.hpp

#ifndef CR_DATA_SERVER_HPP
#define CR_DATA_SERVER_HPP

#include "utils/c++/AdrManagerStub.hpp"
#include <string>

class CRDataServer {

private:
  AdrManagerStub & adrManager;
  short port;  
  std::string ipAddress;  
  std::string outputDir;
  int ckpReposId;
  
  string fifoName_;
  
  short maxPort;
  
  int sockfd_;      
  
  void storeData(int sockfd);
  void recoverData(int sockfd);
  void storeFilePath(int sockfd);
  void removeData(int sockfd);
  void renewData(int sockfd);

  void readStream(int sockfd);
  void setupServer();
  void notifyAdrManager(ssize_t notification, ssize_t nWrittenBytes, ssize_t timeout, ssize_t fragmentIdSize, char *fragmentId);
  
public:
  CRDataServer(AdrManagerStub & stub, const int & port, string & storagePath);
  CRDataServer(AdrManagerStub & stub, string ipAddress, const int & port, string & storagePath);
  
  
  string getIpAddress () {return ipAddress;}
  int getPort () {return port;}
  
  string & getFifoName () {return fifoName_;}
  string & getOutputDir () {return outputDir;}
  int getCkpReposId () {return ckpReposId;}
  
  void startServer();
};


#endif // CR_DATA_SERVER_HPP
