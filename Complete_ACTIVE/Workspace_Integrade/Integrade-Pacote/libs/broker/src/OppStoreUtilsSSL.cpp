#include <cassert>
#include <ctime>
#include <sys/time.h>
#include <openssl/evp.h>

#include "OppStoreUtilsSSL.hpp"
#include <iostream>

OppStoreUtilsSSL::OppStoreUtilsSSL() {}

OppStoreUtilsSSL::~OppStoreUtilsSSL() {}

int OppStoreUtilsSSL::encryptRC4(const unsigned char *key, const unsigned char *inputData, int inputSize, unsigned char *outputData) {

        assert(key[16]==0);                
                
        EVP_CIPHER_CTX ctx;        
        EVP_CIPHER_CTX_init(&ctx);
        EVP_EncryptInit_ex(&ctx, EVP_rc4(), NULL, key, NULL);

        int outputSize;
        if(!EVP_EncryptUpdate(&ctx, outputData, &outputSize, inputData, inputSize)) {
            /* Error */
            EVP_CIPHER_CTX_cleanup(&ctx);
            return 0;
        }
        EVP_CIPHER_CTX_cleanup(&ctx);
        assert (outputSize==inputSize);
        
        return 1;
}

int OppStoreUtilsSSL::decryptRC4(const unsigned char *key, const unsigned char *inputData, int inputSize, unsigned char *outputData) {

        assert(key[16]==0);
                
        EVP_CIPHER_CTX ctx;        
        EVP_CIPHER_CTX_init(&ctx);
        EVP_DecryptInit_ex(&ctx, EVP_rc4(), NULL, key, NULL);

        int outputSize;
        if(!EVP_DecryptUpdate(&ctx, outputData, &outputSize, inputData, inputSize)) {
            /* Error */
            EVP_CIPHER_CTX_cleanup(&ctx);
            return 0;
        }        
        EVP_CIPHER_CTX_cleanup(&ctx);
        assert (outputSize==inputSize);
        
        return 1;
}

