#include "IDAImpl.h"

#include "IDAEncoder.h"
#include "IDADecoder.h"
#include "IDAAuxVectors.h"

// defined in "IDADefinitions.h"
enc_t *mulTable;
enc_t *mulList;
enc_t *iMulList;

IDAImpl instance;

//--------------------------------

IDAImpl::IDAImpl()
{
    idaEncoder    = new IDAEncoderIdentity();
    idaDecoder    = new IDADecoderIdentity();    
//    idaEncoder    = new IDAEncoderRabin();
//    idaDecoder    = new IDADecoderRabin();   
    idaAuxVectors = new IDAAuxVectors();    
    
    createTables();
}

IDAImpl::~IDAImpl()
{
    if (idaEncoder) delete idaEncoder;
    if (idaDecoder) delete idaDecoder;
    if (idaAuxVectors) delete idaAuxVectors;
}

IDAImpl *IDAImpl::getInstance() {
    return &instance;
}
    
//------------------------------------------------------------------------------

void IDAImpl::createTables () {
    mulTable = new enc_t[FIELD_SIZE*FIELD_SIZE];    
    mulList  = new enc_t[FIELD_SIZE];
    iMulList = new enc_t[FIELD_SIZE];    
    mulList[0]=0; mulList[1]=0; iMulList[0]=1;    
    for (int i=1, x=1; i<FIELD_SIZE-1; i++) {        
        x = x << 1; // Primitive poynomial is "x"
        if (x>=FIELD_SIZE) x = x^IRREDUCILBE_POLYNOMIAL;                 
        mulList[x] = i;        
        iMulList[i] = x;                        
    }
       
    /** Populates the mulTable */
    for (int i=1; i<FIELD_SIZE; i++) mulTable[i*FIELD_SIZE] = 0;   
    for (int j=1; j<FIELD_SIZE; j++) mulTable[j] = 0;              
    for (int i=1; i<FIELD_SIZE; i++) { 
        for (int j=1; j<FIELD_SIZE; j++) { 
            mulTable[i*FIELD_SIZE+j] = pmul1(i,j);   
        }
    }  
}

//------------------------------------------------------------------------------

void IDAImpl::encodeData(unsigned char *data, int dataSize, int nSlices, int nExtra, unsigned char **encData, long *codedSize) {
    idaEncoder->encodeData(data, dataSize, nSlices, nExtra, idaAuxVectors, encData, codedSize);
}

//------------------------------------------------------------------------------

unsigned char *IDAImpl::decodeDataIntoBuffer(unsigned char *outputBuffer, unsigned char **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra) {
    return idaDecoder->decodeData(outputBuffer, data, dataSize, sliceNumbers, nSlices, nExtra, idaAuxVectors);
}

//------------------------------------------------------------------------------

unsigned char *IDAImpl::decodeData(unsigned char **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra) {
    return idaDecoder->decodeData(NULL, data, dataSize, sliceNumbers, nSlices, nExtra, idaAuxVectors);
}

//------------------------------------------------------------------------------

unsigned char *IDAImpl::decodeDataInc(unsigned char **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, vector<long *> & availableBytes) {
    return idaDecoder->decodeDataInc(data, dataSize, sliceNumbers, nSlices, nExtra, idaAuxVectors, availableBytes);
}

//------------------------------------------------------------------------------
