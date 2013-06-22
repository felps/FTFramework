#include <openssl/evp.h>
#include <cassert>
#include <iostream>
#include "OppStoreUtils.hpp"

using namespace std;

/*
  Testar encrypt and decrypt na mesma função [OK]
  Colocar funções na OppStoreUtils.cpp e testar [OK]
  Implementar criptografia no OppStore
    - Adicionar método que permite transmitir dados com criptografia
*/ 
 
/* Example from man page: EVP_EncryptInit(3) */
int testRC4() {

        /* Allow enough space in output buffer for additional block */
        unsigned char inbuf[1024], outbuf[1024 + EVP_MAX_BLOCK_LENGTH];
        int inlen=1024, outlen=0, outlenTmp;
        
        for (int i=0; i<1024;i++)
            inbuf[i] = rand()%256;

        unsigned char key[] = "1234567890123456";
                
        /* Don't set key or IV because we will modify the parameters */
        EVP_CIPHER_CTX ctx;        
        EVP_CIPHER_CTX_init(&ctx);
        EVP_EncryptInit_ex(&ctx, EVP_rc4(), NULL, key, NULL);
        //EVP_CipherInit_ex(&ctx, EVP_rc4(), NULL, NULL, NULL, 1);
        //EVP_CIPHER_CTX_set_key_length(&ctx, 10);        
        /* We finished modifying parameters so now we can set key and IV */
        //EVP_CipherInit_ex(&ctx, NULL, NULL, key, iv, 1);
        
        if(!EVP_EncryptUpdate(&ctx, outbuf, &outlenTmp, inbuf, inlen)) {
            /* Error */
            EVP_CIPHER_CTX_cleanup(&ctx);
            return 0;
        }
        outlen += outlenTmp;

        if(!EVP_EncryptFinal_ex(&ctx, outbuf, &outlenTmp)) {
            /* Error */
            EVP_CIPHER_CTX_cleanup(&ctx);
            return 0;
        }
        outlen += outlenTmp;
        
        EVP_CIPHER_CTX_cleanup(&ctx);
        
        
        unsigned char finalbuf[1024];     
        int flen;  
        
        /* Don't set key or IV because we will modify the parameters */
        EVP_CIPHER_CTX ctx2;        
        EVP_CIPHER_CTX_init(&ctx2);
        EVP_DecryptInit_ex(&ctx2, EVP_rc4(), NULL, key, NULL);

        if(!EVP_DecryptUpdate(&ctx2, finalbuf, &flen, outbuf, outlen)) {
            /* Error */
            EVP_CIPHER_CTX_cleanup(&ctx2);
            return 0;
        }

        if(!EVP_DecryptFinal_ex(&ctx2, finalbuf, &flen)) {
            /* Error */
            EVP_CIPHER_CTX_cleanup(&ctx2);
            return 0;
        }
        
        EVP_CIPHER_CTX_cleanup(&ctx2);

        for (int i=0; i<1024;i++)
            assert (inbuf[i] == finalbuf[i]);               
        
        return 1;
}

int main(int argc, char **argv) {
    
    testRC4();
    
    int fileSize = 10241273;
    unsigned char *file = new unsigned char[fileSize];        
    for (int i=0; i<fileSize; i++)
        file[i] = rand()%256;

    cerr << "Testing RC4..." << endl;
    
    unsigned char key[] = "1234567890123456";
    
    unsigned char *codedFile     = new unsigned char[fileSize];        
    unsigned char *recoveredFile = new unsigned char[fileSize];    
        
    assert ( OppStoreUtils::encryptRC4(key,      file, fileSize,     codedFile) );
    assert ( OppStoreUtils::decryptRC4(key, codedFile, fileSize, recoveredFile) );
            
    for (int i=0; i<fileSize;i++)
        assert (file[i] == recoveredFile[i]);                   
    
    cerr << "Test of RC4 was succesfull!" << endl;
    
    return 0;
}
