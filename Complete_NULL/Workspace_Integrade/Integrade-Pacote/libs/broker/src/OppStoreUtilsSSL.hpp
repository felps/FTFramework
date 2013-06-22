#ifndef OPPSTOREUTILSSSL_HPP_
#define OPPSTOREUTILSSSL_HPP_

#include <string>
using namespace std;

class OppStoreUtilsSSL
{
public:
	OppStoreUtilsSSL();
	virtual ~OppStoreUtilsSSL();

    static int encryptRC4(const unsigned char *key, const unsigned char *inputData, int inputSize, unsigned char *outputData);
    
    static int decryptRC4(const unsigned char *key, const unsigned char *inputData, int inputSize, unsigned char *outputData);
};

#endif /*OPPSTOREUTILS_HPP_*/
