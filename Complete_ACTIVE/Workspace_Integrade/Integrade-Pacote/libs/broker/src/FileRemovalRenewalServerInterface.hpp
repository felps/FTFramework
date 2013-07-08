#ifndef FILEREMOVALINTERFACE_HPP_
#define FILEREMOVALINTERFACE_HPP_

#include <vector>
using namespace std;

class FileRemovalRenewalServerInterface {

protected:
    virtual ~FileRemovalRenewalServerInterface() {};

public:
        
    virtual void removeFragments ( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList) = 0;
    
    virtual void renewFragmentLeases ( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList, int timeout ) = 0;
};

#endif /*FILREMOVALINTERFACE_HPP_*/
