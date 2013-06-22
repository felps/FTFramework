#ifndef CkpReposStub_HPP
#define CkpReposStub_HPP

#include <string>
#include <vector>
#include <pthread.h>
#include "utils/c++/Config.hpp"
struct lua_State;

using std::string;
using std::vector;

struct CkpInfo {
    vector<string> checkpointKey;
    vector<int> checkpointNumber;    
};

// IMPORTANT: This class is not multithread-safe. Users of this class must ensure that
// 'state' is safely isolated when concurrently accessed
class CkpReposManagerStub{

private:
  struct lua_State * state;
  pthread_mutex_t * stubMutex;

public:
  CkpReposManagerStub(const Config & config);

  void setCheckpointStored( const string & executionId, const string & checkpointKey, int checkpointNumber );
  
  CkpInfo getCheckpointingInformation(const string & executionId);                        
};

#endif // CkpReposStub_HPP


