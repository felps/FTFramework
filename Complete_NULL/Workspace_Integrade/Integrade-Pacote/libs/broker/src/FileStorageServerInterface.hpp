#ifndef FILESTORAGEINTERFACE_HPP_
#define FILESTORAGEINTERFACE_HPP_

#include <vector>
#include <string>

using namespace std;

class FileStorageServerInterface {
protected:
    virtual ~FileStorageServerInterface() {};

public:
        
    virtual void uploadFragments ( int requestId, vector<string> adrAddresses ) = 0;
    
    virtual void setFileStorageRequestCompleted( int requestId ) = 0;    
    
    virtual void setFileStorageRequestFailed( int requestId ) = 0;
};

#endif /*FILESTORAGEINTERFACE_HPP_*/
