
#ifndef _IDADECODER_H_
#define _IDADECODER_H_

#include "IDADefinitions.h"
#include "IDAAuxVectors.h"
#include <vector>

class IDADecoder
{

public:
	IDADecoder(){}
	virtual ~IDADecoder(){}
    
    virtual unsigned char *decodeData(enc_t *outputBuffer, enc_t **codedData, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors)=0;
    virtual unsigned char *decodeDataInc(enc_t **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, std::vector<long *> & availableBytes)=0;
};

class IDADecoderRabin : public IDADecoder {

    class IDAAuxVectors *idaAuxVectors;

public:
    IDADecoderRabin();
    ~IDADecoderRabin();
    
    unsigned char *decodeData(enc_t *outputBuffer, enc_t **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors);
    
    unsigned char *decodeDataInc(enc_t **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, std::vector<long *> & availableBytes);
};

class IDADecoderIdentity : public IDADecoder {

    class IDAAuxVectors *idaAuxVectors;

public:
    IDADecoderIdentity();
    ~IDADecoderIdentity();
    
    unsigned char *decodeData(enc_t *outputBuffer, enc_t **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors);
    
    unsigned char *decodeDataInc(enc_t **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, std::vector<long *> & availableBytes);
};


#endif /*_IDAENCODER_H_*/
