#include "IDAEncodingThread.hpp"
#include "../Benchmark.hpp"
#include "../OppStoreUtils.hpp"

#include <map>
#include <iostream>
using namespace std;

map<int, IDAEncodingThread *> encodingThreadMap;
int nextEncodingThreadId = 1;

IDAEncodingThread::IDAEncodingThread( int *remainingCodings_, unsigned char *data_, int dataSize_, int nSlices_, int nExtra_ )
{
    this->data     = data_;
    this->dataSize = dataSize_;
    this->nSlices  = nSlices_;
    this->nExtra   = nExtra_;
    this->remainingCodings = remainingCodings_;
    this->idaImpl          = IDAImpl::getInstance();
    
    int nSegments = (dataSize%nSlices == 0) ? dataSize/nSlices : dataSize/nSlices+1;     
    this->encData = new unsigned char *[nSlices+nExtra];
    for (int i=0; i<nSlices+nExtra; i++)
        encData[i] = new unsigned char[nSegments];
    this->bytesEncoded = new long[nSlices+nExtra];
    
}

IDAEncodingThread::~IDAEncodingThread()
{
    delete[] this->bytesEncoded;
    delete[] this->encData;
}

void IDAEncodingThread::performEncoding() {
    
    Benchmark::getCurrentBenchmark()->idaStart = OppStoreUtils::timeInMillis();    
    idaImpl->encodeData(data, dataSize, nSlices, nExtra, encData, bytesEncoded);
    Benchmark::getCurrentBenchmark()->idaFinish = OppStoreUtils::timeInMillis();

    *remainingCodings = 0;
    Benchmark::getCurrentBenchmark()->requestFree = OppStoreUtils::timeInMillis();
}

int IDAEncodingThread::getFragmentData( int fragmentNumber, unsigned char * & fragmentData, long * & nbytes ) {
    fragmentData = encData[fragmentNumber];
    nbytes = &(bytesEncoded[fragmentNumber]);
    
    return 0;
}

void *IDAEncodingThread::run( void *ptr ) {
 
    int *threadId = (int *)ptr;
    IDAEncodingThread *encodingThread = encodingThreadMap[*threadId];
    encodingThread->performEncoding();
    
    encodingThreadMap.erase(*threadId);
    delete threadId;
    
    return NULL;   
}
    
IDAEncodingThread *IDAEncodingThread::launchEncodingThread ( int *remainingCodings, unsigned char *data, int dataSize, int nSlices, int nExtra ) {

    *remainingCodings = 1;
                
    IDAEncodingThread *encodingThread = new IDAEncodingThread(remainingCodings, data, dataSize, nSlices, nExtra);
    int *threadId = new int;
    *threadId = nextEncodingThreadId++;
    encodingThreadMap[*threadId] = encodingThread;
    
    pthread_t thread1;
    pthread_create( &thread1, NULL, IDAEncodingThread::run, threadId );
    pthread_detach(thread1);

    return encodingThread;
}
