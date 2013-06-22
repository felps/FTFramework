#ifndef AdrManagerStub_HPP
#define AdrManagerStub_HPP

#include <string>
#include <vector>
#include <pthread.h>
#include "utils/c++/Config.hpp"
struct lua_State;

using std::string;
using std::vector;

class AdrManagerStub{

private:
  struct lua_State * state;
  pthread_mutex_t * stubMutex;

public:
  AdrManagerStub(const Config & config);

  int registerAdr (const string & address, const int & freeStorageSpace, const double & meanUptime, const double & meanIdleness);
  
  void adrStatusChanged (const int & adrId, const int & freeStorageSpaceChange, const double & meanUptimeChange, const double & meanIdlenessChange);

  void setFragmentStored (const int & adrId, char * & fragmentKey, const int & keySize, const int & fragmentSize, const int & timeoutMinutes);
  
  void setFragmentRemoved (const int & adrId, char * & fragmentKey, const int & keySize, const int & fragmentSize);
  
  void setFragmentLeaseRenewed (const int & adrId, char * & fragmentKey, const int & keySize, const int & timeoutMinutes); 
                        
  int adrKeepAlive (const int & adrId);
  
  vector<char *> getFragmentRemovalList(int adrId, int keyLength);  
                        
};

#endif // CkpReposStub_HPP


