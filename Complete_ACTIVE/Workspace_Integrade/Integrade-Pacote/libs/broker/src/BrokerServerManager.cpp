#include "BrokerServerManager.hpp"

BrokerServerManager::BrokerServerManager() {
    
    storageManager   = new DataStorageManager();
    retrievalManager = new DataRetrievalManager();
    removalManager   = new DataRemovalRenewalManager();
    
    Config config("broker.conf");
    AccessBrokerSkeleton::init(retrievalManager, storageManager, removalManager, config);    
    CdrmRequestsStub *cdrmRequestsStub = new CdrmRequestsStub(config, 20);
    AdrDataTransferStub *adrDataTransferStub = new AdrDataTransferStub();
    
    storageManager->setCdrmRequestStub(cdrmRequestsStub, adrDataTransferStub);    
    retrievalManager->setCdrmRequestStub(cdrmRequestsStub, adrDataTransferStub);
    removalManager->setCdrmRequestStub(cdrmRequestsStub, adrDataTransferStub);
}

BrokerServerManager::~BrokerServerManager() {
    
}

DataStorageManager *BrokerServerManager::getStorageManager() {
    return storageManager;
}

DataRemovalRenewalManager *BrokerServerManager::getRemovalRenewalManager() {
    return removalManager;
}


DataRetrievalManager *BrokerServerManager::getRetrievalManager() {
    return retrievalManager;
}
