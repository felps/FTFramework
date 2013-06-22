#ifndef IDAENCODINGTHREAD_HPP_
#define IDAENCODINGTHREAD_HPP_
#include "IDAImpl.h"

class IDAEncodingThread
{
    unsigned char *data;
    int dataSize;
    int nSlices;
    int nExtra;
    
    unsigned char **encData;
    long *bytesEncoded;
    
    int *remainingCodings;
    
    IDAImpl *idaImpl;    
    
    IDAEncodingThread(int *remainingCodings_, unsigned char *data_, int dataSize_, int nSlices_, int nExtra_);
    
    static void *run( void *ptr );
    
    void performEncoding();
    
public:
	
    virtual ~IDAEncodingThread();
        
    int getFragmentData( int fragmentNumber, unsigned char * & fragmentData, long * & nbytes );
    
    static IDAEncodingThread *launchEncodingThread ( int *remainingCodings, unsigned char *data, int dataSize, int nSlices, int nExtra );
    
};

// Does it make sense to maintain a single thread?

#endif /*IDAENCODINGTHREAD_HPP_*/
