#ifndef REMOVALREQUESTIDA_HPP_
#define REMOVALREQUESTIDA_HPP_

#include "CodedDataInfo.hpp"
#include "DataRemovalRenewalManager.hpp"

class RemovalRenewalRequest : public DataOperationCallback {
    
    DataRemovalRenewalManager *dataRemovalRenewalManager_;
    
    void releaseMemory( CodedDataInfo *dataInfo );        
    
public:
	RemovalRenewalRequest(DataRemovalRenewalManager *dataRemovalRenewalManager);
	virtual ~RemovalRenewalRequest();
   
    int removeData (char * & key, void(*appCallback)(int), bool wait);
    
    int renewDataLease (char * & key, void(*appCallback)(int), bool wait, int timeoutMinutes);
    
    void finishedDataOperation( CodedDataInfo *dataInfo, int status );
};

#endif /*STORAGEREQUESTMANAGER_HPP_*/
