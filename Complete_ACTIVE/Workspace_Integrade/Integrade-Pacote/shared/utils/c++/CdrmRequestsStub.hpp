#ifndef CdrmRequestsStub_HPP
#define CdrmRequestsStub_HPP

#include <string>
#include <vector>
#include <pthread.h>
#include "utils/c++/Config.hpp"
struct lua_State;

using namespace std;

class CdrmRequestsStub{

private:
    struct lua_State * state;
    pthread_mutex_t * stubMutex;
    int keySize;

public:
    CdrmRequestsStub(const Config & config, int keySize);

    void setFragmentStorageFinished( const int & requestNumber, const vector<int> & notStoredFragmentList, 
    								 const vector<char *> & fragmentHashList, char * fileKey);

    int requestFileStorage(char * & fileKey, const vector<char *> & fragmentKeyList, int fileSize, const vector<int> & fragmentSizeList, int neededFragments,
                           const string & accessBrokerIor, int timeoutMinutes, bool storeGlobal);
                
    int requestFileRetrieval(char * & fileKey, const string & accessBrokerIor);
    
    int requestFileRemoval(char * & fileKey, const string & accessBrokerIor);
    
    int requestFileLeaseRenewal(char * & fileKey, const string & accessBrokerIor, int timeoutMinutes);
};

#endif // CkpReposStub_HPP


