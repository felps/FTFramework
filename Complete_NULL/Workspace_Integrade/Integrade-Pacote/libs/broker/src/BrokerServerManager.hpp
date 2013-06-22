#ifndef BROKERSERVERMANAGER_HPP_
#define BROKERSERVERMANAGER_HPP_

#include "AdrDataTransferStub.hpp"
#include "utils/c++/CdrmRequestsStub.hpp"
#include "AccessBrokerSkeleton.hpp"
#include "DataStorageManager.hpp"
#include "DataRetrievalManager.hpp"
#include "DataRemovalRenewalManager.hpp"

class BrokerServerManager
{
    DataStorageManager   *storageManager;
    DataRetrievalManager *retrievalManager;
    DataRemovalRenewalManager *removalManager;
    
public:
	BrokerServerManager();
	virtual ~BrokerServerManager();
    
    DataStorageManager *getStorageManager();
    DataRetrievalManager *getRetrievalManager();
    DataRemovalRenewalManager *getRemovalRenewalManager();
};

#endif /*BROKERSERVERMANAGER_HPP_*/
