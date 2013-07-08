#ifndef DATASTORAGEMANAGER_HPP_
#define DATASTORAGEMANAGER_HPP_

#include <map>

#include "utils/c++/CdrmRequestsStub.hpp"
#include "AccessBrokerSkeleton.hpp"
#include "AdrDataTransferStub.hpp"
#include "CodedDataInfo.hpp"

class DataStorageManager : public FileStorageServerInterface {

private:
    CdrmRequestsStub *cdrmRequestsStub_ ;
    string brokerIor_; 
    map<int, CodedDataInfo *> requestInfoMap;
    AdrDataTransferStub *adrDataTransferStub_;
    
public:
	DataStorageManager();
	virtual ~DataStorageManager();
    
    void setCdrmRequestStub(CdrmRequestsStub *cdrmRequestsStub, AdrDataTransferStub * adrDataTransferStub);
       
    void storeData( CodedDataInfo *dataInfo, bool storeGlobal );
    
    CodedDataInfo *getCodedDataInfo ( int requestId );
    /**
     * From the FileStorageServerInterface interface. Called by the CDRM when it already has the adrAddresses to store the fragments.
     */
    void uploadFragments ( int requestId, vector<string> adrAddresses );

    
    
    /**
     * From the FileStorageServerInterface interface. Called by the CDRM when the storage of the FFI has finished.
     */
    void setFileStorageRequestCompleted( int requestId );
    
    /**
     * From the FileStorageServerInterface interface. Called by the CDRM when the storage of the FFI has failed.
     */
    void setFileStorageRequestFailed( int requestId );

};

#endif /*DATASTORAGEMANAGER_HPP_*/
