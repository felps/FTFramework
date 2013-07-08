#ifndef _IDAIMPL_H_
#define _IDAIMPL_H_

#include <vector>
using namespace std;

class IDAImpl
{
    
    class IDAEncoder *idaEncoder;
    class IDADecoder *idaDecoder;    
    class IDAAuxVectors *idaAuxVectors;    
    
    void createTables ();
        
public:

    IDAImpl();
    virtual ~IDAImpl();

    
    void encodeData(unsigned char *data, int dataSize, int nSlices, int nExtra, unsigned char**encData, long *codedSize);
    
    unsigned char *decodeDataIntoBuffer(unsigned char *outputBuffer, unsigned char **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra);
    unsigned char *decodeData(unsigned char **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra);
    unsigned char *decodeDataInc(unsigned char **data, int dataSize, int *sliceNumbers, int nSlices, int nExtra, vector<long *> & availableBytes);
       
    static IDAImpl *getInstance(); 
};

#endif //_IDAIMPL_H_
