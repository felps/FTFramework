#ifndef FRAGMENTOPERATIONTHREAD_HPP_
#define FRAGMENTOPERATIONTHREAD_HPP_

#include "CodedDataInfo.hpp"
#include "AdrDataTransferStub.hpp"
#include "DataRemovalRenewalManager.hpp"

#include <vector>
using namespace std;

class FragmentOperationThread
{
    
	int sockfd;
    char *hexKey;      
    CodedDataInfo *dataInfo;
    int fragmentNumber;
    
    FragmentOperationThread( int sockfd_, char *hexKey_, int fragmentNumber_, CodedDataInfo *dataInfo_ );
    
public:
	virtual ~FragmentOperationThread();
    
    static void *run( void *ptr );
    
    /**
     * Used only in the experiments
     */
    static int setAllowedAdrs(vector<string> & allowedAdrs);
    
    static void *launchOperationThread( void *ptr );
    
    static void configureOperationThreads( AdrDataTransferStub *adrDataTransferStub_, DataRemovalRenewalManager *dataRetrievalManager_ );
    
    void performOperation();
};

#endif /*FRAGMENTDOWNLOADTHREAD_HPP_*/
