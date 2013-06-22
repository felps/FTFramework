#include "utils/c++/CdrmRequestsStub.hpp"
#include "AccessBrokerSkeleton.hpp"
#include "OppStoreUtils.hpp"

#include <cstdlib>
#include <cassert>
#include <string>
#include <iostream>

#include <openssl/sha.h>

class FileRetrievalServer : public FileRetrievalServerInterface {

    int correctRequestId_;
    int incorrectRequestId_;
    CdrmRequestsStub *cdrmRequestsStub_;    

public:    

    void testRetrieval( CdrmRequestsStub *cdrmRequestsStub ) {
        
        cdrmRequestsStub_ = cdrmRequestsStub;
        
        cout << "Launching retrieval test!" << endl;
        
        const char *fileKey = "12345678901234567890";        
        string brokerIor = AccessBrokerSkeleton::singleInstance().getIor();
        correctRequestId_ = cdrmRequestsStub_->requestFileRetrieval(fileKey, brokerIor);        
        
        cout << "Correct file retrieval sent to CDRM!" << endl;

        //sleep(1);
        
        const char *fileKey2 = "AAAAAAAAAAAAAAAAAAAA";        
        incorrectRequestId_ = cdrmRequestsStub_->requestFileRetrieval(fileKey2, brokerIor);
        
        cout << "Incorrect file retrieval sent to CDRM!" << endl;        
        
    }
    
    void downloadFragments ( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList, vector<int> fragmentSizeList ) {
        
        cout << "Received download fragment message!" << endl;
                
        assert (correctRequestId_ == requestId);
        for (unsigned int i=0; i<adrAddresses.size(); i++)
            cout << adrAddresses[i] << " ";
        cout << endl;
        
    };
    
    void setFileRetrievalRequestFailed ( int requestId ) {
    
        cout << "Received file storage request failed!" << endl;
        assert (incorrectRequestId_ == requestId);
    
    };    
};

class FileStorageServer : public FileStorageServerInterface {
    
    int requestId_;
    CdrmRequestsStub *cdrmRequestsStub_;
    
public:
    FileStorageServer() {}
    
    void testStorage(CdrmRequestsStub *cdrmRequestsStub) {
        
        cdrmRequestsStub_ = cdrmRequestsStub;
        
        cout << "Launching storage test!" << endl;
        
        const char *fileKey = "12345678901234567890";
        vector<char *> fileKeyList;
        fileKeyList.push_back("a234567890123456789a");
        fileKeyList.push_back("t234567890123456789t");
        fileKeyList.push_back("Z234567890123456789Z");
        
        vector<int> fileSizeList;
        fileSizeList.push_back(100);
        fileSizeList.push_back(100);
        fileSizeList.push_back(100);
        string brokerIor = AccessBrokerSkeleton::singleInstance().getIor();
        
        requestId_ = cdrmRequestsStub_->requestFileStorage(fileKey, fileKeyList, fileSizeList, brokerIor);        
        
        cout << "File storage request sent to CDRM!" << endl;
    }
    
    void uploadFragments ( int requestId, vector<string> adrAddresses ) {
        
        cout << "Received upload fragment message!" << endl;
                
        assert (requestId_ == requestId);
        for (unsigned int i=0; i<adrAddresses.size(); i++)
            cout << adrAddresses[i] << " ";
        cout << endl;
        
        vector<int> notStoredFragments;
        notStoredFragments.push_back(0);
        
        cdrmRequestsStub_->setFragmentStorageFinished(requestId, notStoredFragments);
            
    }    
    
    void setFileStorageRequestCompleted( int requestId ) {
        
        assert (requestId_ == requestId);
        cout << "File storage completed!" << endl;
        
    }
};

void testFileStorage() {
    
    char data[50];
    ulong dataSize = 50;
    char md[OppStoreUtils::binaryKeySize];
    
    for (uint i=0; i<dataSize; i++)
        data[i] = rand()%256; 
    
    SHA1( (unsigned char*)data, dataSize, (unsigned char*)md);
    
    char hexKey[ OppStoreUtils::binaryKeySize*2 ];
    OppStoreUtils::convertBinaryToHex(md, hexKey);
    
    cout << "hexKey: ";
    for (int i=0; i<OppStoreUtils::binaryKeySize*2 ; i++)        
        cout << hexKey[i];
    cout << endl;           
}

void testStringSplit( string adrAddress ) {
        
    string ipAddress;
    short portNumber;
    
    OppStoreUtils::extractIpAddress(adrAddress, ipAddress, portNumber);
    cout << ipAddress << ":" << portNumber << endl;
}

int main(int argc, char **argv) {

    srand(12345);
    for (int i=0; i<100; i++)
        testFileStorage();
        
    testStringSplit("192.168.155.1:123");            
    testStringSplit("cerebellum:123");
        
    return 0; 
    
    FileRetrievalServer *fileRetrieval = new FileRetrievalServer();
    FileStorageServer   *fileStorage   = new FileStorageServer();
    
    Config config("broker.conf");
    AccessBrokerSkeleton::init(fileRetrieval, fileStorage, config);
    
    //    sleep(1);
    
    CdrmRequestsStub *cdrmRequestsStub = new CdrmRequestsStub(config, 20);    
    fileStorage->testStorage(cdrmRequestsStub);
    
    sleep(1);
    
    fileRetrieval->testRetrieval(cdrmRequestsStub);
        
    sleep(10);
    
    return 0;
}
