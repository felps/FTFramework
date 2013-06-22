#ifndef _IDAENCODER_H_
#define _IDAENCODER_H_

#include "IDADefinitions.h"
#include "IDAAuxVectors.h"

class IDAEncoder
{
public:
	IDAEncoder(){}
	virtual ~IDAEncoder(){}
    
    virtual void encodeData(unsigned char *data, int dataSize, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, enc_t **encData, long *codedSize) = 0;
};

//------------------------------------------------------------------------------

class IDAEncoderRabin : public IDAEncoder {

    class IDAAuxVectors *idaAuxVectors;

public:

    IDAEncoderRabin();
    ~IDAEncoderRabin();

    virtual void encodeData(unsigned char *data, int dataSize, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, enc_t **encData, long *codedSize);
};

//------------------------------------------------------------------------------

class IDAEncoderIdentity : public IDAEncoder {

    class IDAAuxVectors *idaAuxVectors;

public:

    IDAEncoderIdentity();
    ~IDAEncoderIdentity();

    virtual void encodeData(unsigned char *data, int dataSize, int nSlices, int nExtra, IDAAuxVectors *idaAuxVectors, enc_t **encData, long *codedSize);
    
};

//------------------------------------------------------------------------------

#endif /*_IDAENCODER_H_*/
