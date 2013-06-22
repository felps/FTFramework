#ifndef OPPSTOREUTILS_HPP_
#define OPPSTOREUTILS_HPP_

#include <string>
using namespace std;

class OppStoreUtils
{
public:
	OppStoreUtils();
	virtual ~OppStoreUtils();

    const static int binaryKeySize = 20;
        
    static void convertBinaryToHex(const char *binaryKey, char *hexKey);
    
    static void convertHexToBinary(const char *hexKey, char *binaryKey);
    
    static void generateRandomKey(unsigned char *binaryKey);
    
    static bool extractIpAddress(const string & adrAddress, string & ipAddress, short & port);
    
    static void printHexKey(char *hexKey, ostream & out);
    
    static void sleep(int miliseconds);
    
    static void waitVar(int miliseconds, int *var);
    
    static void resetClock();
    
    static long timeInMillis();    
};

#endif /*OPPSTOREUTILS_HPP_*/
