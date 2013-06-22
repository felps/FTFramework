#include "IDAEncoder.h"

#include <cassert>
#include <iostream>
using namespace std;


IDAEncoderIdentity::IDAEncoderIdentity()
{
}

IDAEncoderIdentity::~IDAEncoderIdentity()
{
}

/**
 * TODO: Current Impl considers dataSize and nSlices are exactly divisible
 * */
void IDAEncoderIdentity::encodeData(unsigned char *data, int dataSize, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, enc_t **encData, long *codedSize) {

    //enc_t **auxVectors = idaAuxVectors->generateIdentityG(nSlices, nExtra);

    enc_t **auxVectors = idaAuxVectors->getAuxVectors(nSlices, nExtra);
    if (auxVectors == 0)
    	auxVectors = idaAuxVectors->generateIdentityG(nSlices, nExtra);

    int nSegments = (dataSize%nSlices == 0) ? dataSize/nSlices : dataSize/nSlices+1;

    for (int i=0; i<nSlices + nExtra; i++)
        codedSize[i] = 0;

//    enc_t **encData = new enc_t *[nSlices + nExtra];
//    for (int i=0; i<nSlices + nExtra; i++)
//        encData[i] = new enc_t[nSegments];

    for (int i=0; i<nSlices; i++) // encData[i]
        for (int j=0; j<nSegments; j++) // c(i,k) = a(i).data(j*m)
            encData[i][j] = data[(j*nSlices)+i];
        
//    for (int i=nSlices; i<nSlices + nExtra; i++) // encData[i]
//        for (int j=0; j<nSegments; j++) { // c(i,k) = a(i).data(j*m)
//            enc_t encDataTemp = 0;
//            for (int k=0; k<nSlices ; k++) {
//                encDataTemp = psum(encDataTemp, pmul(auxVectors[i][k],data[(j*nSlices)+k])); // %p ?
//            }
//            encData[i][j] = encDataTemp; // %p ?   
//        }     

    for (int segment=0; segment<nSegments; segment++) { // c(i,k) = a(i).data(j*m)
        for (int i=nSlices; i<nSlices + nExtra; i++) { // encData[i]
            enc_t encDataTemp = 0;
            for (int k=0; k<nSlices ; k++) { 
                encDataTemp = psum(encDataTemp, pmul(auxVectors[i][k],data[(segment*nSlices)+k])); // %p ?
            }
            encData[i][segment] = encDataTemp; // %p ?   
        }
             
        if (segment % 100000 == 0 && segment != 0)
            for (int frag=0; frag < nSlices + nExtra; frag++) 
                codedSize[frag] = segment;                    
    }        

    for (int i=0; i<nSlices + nExtra; i++)
        codedSize[i] = nSegments;
                
}
