#ifndef RETRIEVALREQUESTIDA_HPP_
#define RETRIEVALREQUESTIDA_HPP_

#include "ida/IDAImpl.h"
#include "CodedDataInfo.hpp"
#include "DataRetrievalManager.hpp"

class RetrievalRequestIda : public DataOperationCallback {
    
    DataRetrievalManager *dataRetrievalManager_;
    IDAImpl *idaImpl;
    bool dataInfoFreed;
    
    void releaseMemory( CodedDataInfo *dataInfo );
    
public:
	RetrievalRequestIda(DataRetrievalManager *dataRetrievalManager);
	virtual ~RetrievalRequestIda();
    
    int retrieveData (const char *id, const char *outputPath, void * & data, long & dataSize, void(*appCallback)(int), bool wait, const unsigned char *encKey);
    
    void finishedDataOperation( CodedDataInfo *dataInfo, int status );
    
};

#endif /*RETRIEVALREQUESTIDA_HPP_*/
