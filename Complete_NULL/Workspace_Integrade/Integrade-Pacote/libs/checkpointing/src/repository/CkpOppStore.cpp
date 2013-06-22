#include "../CkpLogger.hpp"
#include "CkpOppStore.hpp"
#include "OppStoreBroker.h"
#include <sys/time.h>

#include <iostream>
#include <sstream>
using namespace std;

CkpOppStore::CkpOppStore(const Config & ckpConfig) {
    
    try {
        execId_ = ckpConfig.getConf("execId");
    }
    catch(...) { // NoSuchConfigException
        struct timeval tv;
        gettimeofday(&tv, NULL);
        ostringstream timeStr;
        timeStr << tv.tv_sec << ":" << tv.tv_usec; 
        execId_ = timeStr.str();
        cerr << "WARNING! could not determine executionId from config file. Using random execution id \"" << execId_ << "\"" << endl;
    }
   
    Config brokerConfig("broker.conf"); 
    ckpReposManagerStub = new CkpReposManagerStub( brokerConfig );
    
    launchBroker();
}

//---------------------------------------------------------------------------
CkpOppStore::~CkpOppStore() { }

//---------------------------------------------------------------------------
int CkpOppStore::getLastCkpNumber() { 
            
    CkpInfo ckpInfo = ckpReposManagerStub->getCheckpointingInformation(execId_);
    this->ckpInfo_ = ckpInfo;
    if (ckpInfo.checkpointNumber.size() > 0) {
        int lastCkpNumber = ckpInfo.checkpointNumber.front();
        return lastCkpNumber;
    }
    
    ckpLogger.debug("No stored checkpoint was found");
    return -1;
}    

//---------------------------------------------------------------------------
void CkpOppStore::saveData(void *data, long dataSize, int ckpNumber) {

    ckpLogger.debug("Saving ckp data!");
    
    /**
     * Saves checkpointing data using OppStore's broker 
     */
    char *key;
    storeDataEphemeralW( key, data, dataSize, NULL );
    
    ckpLogger.debug("Finished saving ckp data!!!");
    ostringstream logStr;
    logStr << "key="  << string(key) << " ckpNumber=" << ckpNumber << " dataSize=" << dataSize;
    ckpLogger.debug( logStr.str() );
    
    ckpReposManagerStub->setCheckpointStored(execId_, key, ckpNumber);    
    ckpLogger.debug("CkpReposManager notified.");
}

//---------------------------------------------------------------------------

void CkpOppStore::recoverCkpData(void * & data, long & dataSize, int ckpNumber) {

    ckpLogger.debug("Recovering ckp data!!!");
    string key = this->ckpInfo_.checkpointKey.back().c_str();

    if ( this->ckpInfo_.checkpointKey.size() > 0 )
        retrieveDataW( key.c_str(), data, dataSize, NULL );
    else
        ckpLogger.debug("ERROR!!!! Tyring to recover checkpoint data without a valid key!");
    
    ckpLogger.debug("Finished recovering ckp data!!!");
    ckpLogger.debug("key=" + key);
}
