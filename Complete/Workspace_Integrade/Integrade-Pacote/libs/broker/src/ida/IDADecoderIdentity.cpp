#include "IDADecoder.h"

#include <cassert>
#include <iostream>
#include <iomanip>
using namespace std;

IDADecoderIdentity::IDADecoderIdentity()
{
}

IDADecoderIdentity::~IDADecoderIdentity()
{
}

// TODO: someone has to delete data!
unsigned char *IDADecoderIdentity::decodeData(enc_t *outputBuffer, enc_t **codedData, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors) {
    
    if (idaAuxVectors->getAuxVectors(nSlices, nExtra) == 0)    
        idaAuxVectors->generateIdentityG(nSlices, nExtra);                  
    enc_t **auxInverse = idaAuxVectors->calculateInverse(sliceNumbers, nSlices);
  
    int nSegments = (dataSize%nSlices==0) ? dataSize/nSlices : dataSize/nSlices+1;    
    unsigned char *decodedData = outputBuffer;
    if (decodedData == NULL) 
    	decodedData = new unsigned char[dataSize];
  
    int *posVector = new int[nSlices];
    for (int i=0; i<nSlices; i++) {
        int usedSum = 0;
        int usedPos = -1;
        for (int j=0; j<nSlices; j++)
            if (auxInverse[i][j] > 0) {
                usedSum += auxInverse[i][j];
                usedPos = j;
            }
        if (usedSum == 1) posVector[i] = usedPos;
        else posVector[i] = -1;
        
    }
  
    // IdaDecoderIdentity   
    for (int segment=0; segment<nSegments; segment++) { 
        unsigned char *decodedDataTemp = decodedData + segment*nSlices;
        for (int slicePos=0; slicePos<nSlices; slicePos++) {
            if (posVector[slicePos] > 0)
               decodedDataTemp[slicePos] = codedData[posVector[slicePos]][segment];
            else {
                enc_t d = 0;
                for (int k=0; k<nSlices; k++)            
                    d = psum(d, pmul(auxInverse[slicePos][k], codedData[k][segment])); // %p ?
                decodedDataTemp[slicePos] = d; // %p ?
            }
        }
    }
    
    delete[] posVector;
    for (int i=0; i<nSlices*2; i++)
        delete[] auxInverse[i];
    delete[] auxInverse;               
       
    return decodedData;
}

unsigned char *IDADecoderIdentity::decodeDataInc(enc_t **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, vector<long *> & availableBytes) {

    if (idaAuxVectors->getAuxVectors(nSlices, nExtra) == 0)    
        idaAuxVectors->generateIdentityG(nSlices, nExtra);                  
    enc_t **auxInverse = idaAuxVectors->calculateInverse(sliceNumbers, nSlices);

    int nSegments = (dataSize%nSlices==0) ? dataSize/nSlices : dataSize/nSlices+1;    
    unsigned char *decodedData = new unsigned char[dataSize];
  
    int *posVector = new int[nSlices];
    for (int i=0; i<nSlices; i++) {
        int usedSum = 0;
        int usedPos = -1;
        for (int j=0; j<nSlices; j++)
            if (auxInverse[i][j] > 0) {
                usedSum += auxInverse[i][j];
                usedPos = j;
            }
        if (usedSum == 1) posVector[i] = usedPos;
        else posVector[i] = -1;        
    }
  
    struct timespec sleepTime;
    sleepTime.tv_sec  = 0;
    sleepTime.tv_nsec = 100 * 1000 * 1000;
    long currentAvailable = 0;
    unsigned int availPos = 0;
    long smallestAvailable = 2147483647;
   
//    while (currentAvailable == 0) {        
//        smallestAvailable = 2147483647;
//        for (availPos = 0; availPos < availableBytes.size(); availPos++)
//            if (*(availableBytes[availPos]) < smallestAvailable)
//                smallestAvailable = *(availableBytes[availPos]);
//        currentAvailable = smallestAvailable;
//        
//        if (currentAvailable > 0) break;
//        nanosleep(&sleepTime, NULL);  
//    }          
                 
    // IdaDecoderIdentity   
    for (int segment=0; segment<nSegments; segment++) { 
        
        while ( (currentAvailable > segment) == false ) {
            
            smallestAvailable = 2147483647;
            for (availPos = 0; availPos < availableBytes.size(); availPos++)
                if (*(availableBytes[availPos]) < smallestAvailable)
                    smallestAvailable = *(availableBytes[availPos]);
            currentAvailable = smallestAvailable;
            
            if (currentAvailable > segment) break;
            nanosleep(&sleepTime, NULL);                     
        }                  
        
        unsigned char *decodedDataTemp = decodedData + segment*nSlices;
        for (int slicePos=0; slicePos<nSlices; slicePos++) {
            if (posVector[slicePos] > 0)
               decodedDataTemp[slicePos] = data[posVector[slicePos]][segment];
            else {
                enc_t d = 0;
                for (int k=0; k<nSlices; k++)            
                    d = psum(d, pmul(auxInverse[slicePos][k], data[k][segment])); // %p ?
                decodedDataTemp[slicePos] = d; // %p ?
            }
        }
        
//        while ( segment >= (currentAvailable-2) ) {
//            
//            smallestAvailable = 2147483647;
//            for (availPos = 0; availPos < availableBytes.size(); availPos++)
//                if (*(availableBytes[availPos]) < smallestAvailable)
//                    smallestAvailable = *(availableBytes[availPos]);
//            currentAvailable = smallestAvailable;
//            
//            if (currentAvailable > segment) break;
//            nanosleep(&sleepTime, NULL);                     
//        }                  
        
    }
    
    //cout << "Finished Decoding!" << endl << flush ;
    
    delete[] posVector;
    for (int i=0; i<nSlices*2; i++)
        delete[] auxInverse[i];
    delete[] auxInverse;               
    
    //cout << "Deleted decoding memory!" << endl << flush ;
       
    return decodedData;
}
