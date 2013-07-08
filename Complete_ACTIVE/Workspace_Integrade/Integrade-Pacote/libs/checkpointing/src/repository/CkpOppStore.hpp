#ifndef CKPOPPSTORE_HPP_
#define CKPOPPSTORE_HPP_

#include "../CkpStore.hpp"

class CkpOppStore : public CkpStore {

  /** The id of the running process */
  string execId_;  
  
  CkpReposManagerStub *ckpReposManagerStub;
    
  CkpInfo ckpInfo_;
  
  virtual void saveData(void *data, long nbytes, int ckpNumber);
  
public:
  CkpOppStore(const Config & ckpConfig);
  ~CkpOppStore();
  
  virtual int getLastCkpNumber();
  
  virtual void recoverCkpData(void * & data, long & dataSize, int ckpNumber);
};

//-----------------------------------------------------------------------------

#endif /*CKPOPPSTORE_HPP_*/
