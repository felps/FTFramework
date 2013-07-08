#include <cassert>
#include <cstdlib>
#include <unistd.h>
#include <sys/time.h>
#include <openssl/sha.h>

#include "OppStoreUtils.hpp"
#include <iostream>

OppStoreUtils::OppStoreUtils() {
	unsigned int seed = (unsigned int) time(NULL);
	seed = seed << 16;
	seed += getpid();	
	
	srand(seed);
}

OppStoreUtils::~OppStoreUtils() {}

void OppStoreUtils::convertBinaryToHex(const char *binaryKey, char *hexKey) {
    
    for (int i=0; i < 2*binaryKeySize; i++) {
        unsigned char posData = binaryKey[i/2];
        (i%2 == 0) ? hexKey[i] = posData >> 4 : hexKey[i] = posData & 0x0F;
       
        //cout << i << ": " << (int)(md[i/2]) << " -> " <<  (int) fragmentKey[i] << " posData=" << (int)posData << endl;     
        assert( ( hexKey[i] < 16 ) && ( 0 <= hexKey[i] ) );
        // ASCII: '0' -> 48, 'A' -> 65    
        (hexKey[i] < 10) ? hexKey[i] += 48 : hexKey[i] += 55; 
    }        
}

void OppStoreUtils::generateRandomKey(unsigned char *binaryKey) {
    	
	char hostname[256];		
    for (int i=0; i < 256; i++)
        hostname[i] = rand() % 256;
    gethostname(hostname, 256);
    
    SHA1( (unsigned char *)hostname, 256, binaryKey );
}


void OppStoreUtils::convertHexToBinary(const char *hexKey, char *binaryKey) {
    
    for (int i=0; i < 2*binaryKeySize; i++) {
        
        unsigned char posData = hexKey[i];
        // ASCII: '0' -> 48, 'A' -> 65    
        (posData < 65) ? posData -= 48 : posData -= 55; 
        
        (i%2 == 0) ? binaryKey[i/2] = posData << 4 : binaryKey[i/2] += posData;
    }        
    
}

bool OppStoreUtils::extractIpAddress(const string & adrAddress, string & ipAddress, short & portNumber) {

    uint separatorPos = adrAddress.find(':');
    if( separatorPos == string::npos || separatorPos <= 0 && separatorPos >= adrAddress.length() )
    	return false;
    int portNumberSize = adrAddress.length() - (separatorPos+1);
        
    ipAddress = adrAddress.substr(0, separatorPos);
    portNumber = atoi( adrAddress.substr(separatorPos+1, portNumberSize).c_str() );
    assert(portNumber > 0);
    
    return true;
}

void OppStoreUtils::printHexKey(char *hexKey, ostream & out) {

    for (int i=0; i < 2*binaryKeySize; i++)
        out << hexKey[i];        
}

void OppStoreUtils::sleep(int miliseconds) {

        struct timespec sleepTime;
        sleepTime.tv_sec  = 0;
        sleepTime.tv_nsec = miliseconds * 1000 * 1000;
        
        nanosleep(&sleepTime, NULL);            
}

void OppStoreUtils::waitVar(int miliseconds, int *var) {

        struct timespec sleepTime;
        sleepTime.tv_sec  = 0;
        sleepTime.tv_nsec = miliseconds * 1000 * 1000;
        
        while (*var > 0)
            nanosleep(&sleepTime, NULL);            
}

struct timeval initial_tv;

void OppStoreUtils::resetClock() {
    gettimeofday(&initial_tv, NULL);    
}

long OppStoreUtils::timeInMillis() {
        
        struct timeval tv;
        gettimeofday(&tv, NULL);
        
        long timeMillis = ( tv.tv_sec - initial_tv.tv_sec ) * 1000 + ( tv.tv_usec - initial_tv.tv_usec ) / 1000 ;
        
        return timeMillis;
}
