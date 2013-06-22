#ifndef DATAREMOVALMANAGER_HPP_
#define DATAREMOVALMANAGER_HPP_

#include <map>

#include "utils/c++/CdrmRequestsStub.hpp"
#include "AccessBrokerSkeleton.hpp"
#include "AdrDataTransferStub.hpp"
#include "CodedDataInfo.hpp"

class DataRemovalRenewalManager : public FileRemovalRenewalServerInterface {
    
private:
    CdrmRequestsStub *cdrmRequestsStub_ ;
    string brokerIor_; 
    map<int, CodedDataInfo *> requestInfoMap;
    AdrDataTransferStub *adrDataTransferStub_;
    
public:
	DataRemovalRenewalManager();
	virtual ~DataRemovalRenewalManager();
    
    void setCdrmRequestStub(CdrmRequestsStub *cdrmRequestsStub, AdrDataTransferStub * adrDataTransferStub);       
    void removeData( CodedDataInfo *dataInfo );
    void renewDataLease( CodedDataInfo *dataInfo );
    
    CodedDataInfo *getCodedDataInfo ( int requestId );
    
    void removeFragments ( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList );
    void renewFragmentLeases ( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList, int timeout );
    void setFileRemovalRenewalRequestFailed ( int requestId );    
};

#endif /*DATARETRIEVALMANAGER_HPP_*/
