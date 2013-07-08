#ifndef FILERETRIEVALINTERFACE_HPP_
#define FILERETRIEVALINTERFACE_HPP_

#include <vector>
#include <string>

using namespace std;

class FileRetrievalServerInterface {

protected:
    virtual ~FileRetrievalServerInterface() {};

public:
        
    virtual void downloadFragments ( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList, int dataSize, vector<int> fragmentSizeList, int nNeededFragments ) = 0;
    
    virtual void setFileRetrievalRequestFailed ( int requestId ) = 0;    
};

#endif /*FILESTORAGEINTERFACE_HPP_*/
