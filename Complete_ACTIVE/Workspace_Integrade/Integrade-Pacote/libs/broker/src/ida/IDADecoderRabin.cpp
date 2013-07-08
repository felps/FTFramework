#include "IDADecoder.h"

#include <cassert>
#include <iostream>
using namespace std;

IDADecoderRabin::IDADecoderRabin()
{
}

IDADecoderRabin::~IDADecoderRabin()
{
}

unsigned char *IDADecoderRabin::decodeData(enc_t *outputBuffer, enc_t **codedData, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors) {

    if (idaAuxVectors->getAuxVectors(nSlices, nExtra) == 0)    
        idaAuxVectors->generateVectors(nSlices+nExtra, nSlices); 
    enc_t **auxInverse = idaAuxVectors->calculateInverse(sliceNumbers, nSlices);
    
    int nSegments = (dataSize/nSlices==0) ? dataSize/nSlices : dataSize/nSlices+1;
    unsigned char *decodedData = outputBuffer;
    if (decodedData == NULL) 
    	decodedData = new unsigned char[dataSize];
    
    for (int i=0; i<nSegments; i++) { 
        unsigned char *decodedDataTemp = decodedData + i*nSlices;
        for (int j=0; j<nSlices; j++) {
            enc_t d = 0;
            for (int k=0; k<nSlices; k++)            
                 d = psum(d, pmul(auxInverse[j][k], codedData[k][i])); // %p ?
            decodedDataTemp[j] = d; // %p ?
        }
    }
       
    return decodedData;
}

unsigned char *IDADecoderRabin::decodeDataInc(enc_t **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, vector<long *> & availableBytes) {
    return decodeData(NULL, data, dataSize, sliceNumbers, nSlices, nExtra, idaAuxVectors);
}
