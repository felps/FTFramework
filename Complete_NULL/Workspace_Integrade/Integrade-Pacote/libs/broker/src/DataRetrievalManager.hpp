#ifndef DATARETRIEVALMANAGER_HPP_
#define DATARETRIEVALMANAGER_HPP_

#include <map>

#include "utils/c++/CdrmRequestsStub.hpp"
#include "AccessBrokerSkeleton.hpp"
#include "AdrDataTransferStub.hpp"
#include "CodedDataInfo.hpp"

class DataRetrievalManager : public FileRetrievalServerInterface {
    
private:
    CdrmRequestsStub *cdrmRequestsStub_ ;
    string brokerIor_; 
    map<int, CodedDataInfo *> requestInfoMap;
    AdrDataTransferStub *adrDataTransferStub_;
    
public:
	DataRetrievalManager();
	virtual ~DataRetrievalManager();
    
    void setCdrmRequestStub(CdrmRequestsStub *cdrmRequestsStub, AdrDataTransferStub * adrDataTransferStub);       
    void retrieveData( CodedDataInfo *dataInfo );
    
    CodedDataInfo *getCodedDataInfo ( int requestId );
    
    void downloadFragments ( int requestId, vector<string> adrAddresses, vector<char *> fragmentKeyList, int dataSize, vector<int> fragmentSizeList, int nNeededFragments );
    void setFileRetrievalRequestFailed ( int requestId );    
};

#endif /*DATARETRIEVALMANAGER_HPP_*/
