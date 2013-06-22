/**
 * Performs the storage and recovery of a file and tests if the file was recovered correctly
 */
  
#include "OppStoreBroker.h"
#include "OppStoreUtils.hpp"
#include "Benchmark.hpp"
#include "AdrDataTransferStub.hpp"

#include <cstdlib>
#include <cassert>
#include <string>
#include <iostream>

#include <cstdio>

#include <openssl/sha.h>

AdrDataTransferStub adrDataTransferStub1;
string adrIpAddress;
short adrPort;

int testFileRenewal(char *key, int timeout) {
	
	int sockfd = adrDataTransferStub1.connectToServer(adrIpAddress, adrPort);
	return adrDataTransferStub1.renewFragmentLease(sockfd, key, OppStoreUtils::binaryKeySize*2, timeout);
		
}

int testFileRemoval(char *key) {
	
	int sockfd = adrDataTransferStub1.connectToServer(adrIpAddress, adrPort);
	return adrDataTransferStub1.removeFragment(sockfd, key, OppStoreUtils::binaryKeySize*2);
		
}

int testFileStorage(char *key, char *data, long dataSize, int timeout) {
	
	int sockfd = adrDataTransferStub1.connectToServer(adrIpAddress, adrPort);
	int writtenBytes = adrDataTransferStub1.transferData(sockfd, key, OppStoreUtils::binaryKeySize*2, data, dataSize, timeout);
	
	if (writtenBytes == dataSize) return 0;
	else return -1;
	
}

int testFileRetrieval(const char *key, char *originalData, int originalDataSize) {
	
	long availableBytes;
	char *recoveredData = new char[originalDataSize];
	
	int sockfd = adrDataTransferStub1.connectToServer(adrIpAddress, adrPort);
	int recoveredDataSize = 
		adrDataTransferStub1.readData(sockfd, key, OppStoreUtils::binaryKeySize*2, recoveredData, originalDataSize, &availableBytes);
            
	if (recoveredDataSize == originalDataSize) {	
		for (int i=0; i<originalDataSize; i++)
			assert( originalData[i] == ((char *)recoveredData)[i] );
            
		delete[] (char *)recoveredData;
		return 0;
	}
	
    return -1;
}

char *createKey() {
	
	char *binaryKey = new char [OppStoreUtils::binaryKeySize];
	for (int i=0; i<OppStoreUtils::binaryKeySize; i++)
		binaryKey[i] = rand()%256;
	
	char *hexKey = new char [OppStoreUtils::binaryKeySize*2];
	OppStoreUtils::convertBinaryToHex((char *)binaryKey, hexKey);
	delete[] binaryKey;
	
	return hexKey;
}

char *createDataArray(long dataSize) {
	
	char *data = new char [dataSize];
	for (int i=0; i<dataSize; i++)
		data[i] = rand()%256;
	return data;
}


int main(int argc, char **argv) {

    launchBroker();

    //srand(12345);

    int dataSize = 3000;
    int timeout = 1;
    adrIpAddress = "127.0.1.1";
    adrPort = atoi(argv[1]);
    	
    for (int i=0; i<1; i++) {
    	
    	char *data1 = createDataArray(dataSize);
    	char *data2 = createDataArray(dataSize);
    	char *data3 = createDataArray(dataSize);

    	char *key1 = createKey();
    	char *key2 = createKey();
    	char *key3 = createKey();    	
        
        assert( testFileStorage(key1, data1, dataSize, timeout) == 0);
        assert( testFileStorage(key2, data2, dataSize, timeout) == 0);
        assert( testFileStorage(key3, data3, dataSize, timeout) == 0);
        
        assert( testFileRetrieval(key1, data1, dataSize) == 0);
        assert( testFileRetrieval(key2, data2, dataSize) == 0);
        assert( testFileRetrieval(key3, data3, dataSize) == 0);                        
        
        assert( testFileRemoval(key1) == 0);
        assert( testFileRemoval(key1) == -1);

        assert( testFileRetrieval(key1, data1, dataSize) == -1);
        assert( testFileRetrieval(key2, data2, dataSize) == 0);
        assert( testFileRetrieval(key3, data3, dataSize) == 0);                        

        for (int i = 50; i > 0; i -= 10) {
        	cout << "Waiting for " << i << " seconds..." << endl;
        	sleep(10);
        }

        assert( testFileRenewal(key1, timeout) == -1);
        assert( testFileRenewal(key3, timeout) == 0);        

        assert( testFileRetrieval(key1, data1, dataSize) == -1);
        assert( testFileRetrieval(key2, data2, dataSize) == 0);
        assert( testFileRetrieval(key3, data3, dataSize) == 0);                        

        for (int i = 50; i > 0; i -= 10) {
        	cout << "Waiting for " << i << " seconds..." << endl;
        	sleep(10);
        }
        
        assert( testFileRetrieval(key1, data1, dataSize) == -1);
        assert( testFileRetrieval(key2, data2, dataSize) == -1);
        assert( testFileRetrieval(key3, data3, dataSize) == 0);                        

        for (int i = 50; i > 0; i -= 10) {
        	cout << "Waiting for " << i << " seconds..." << endl;
        	sleep(10);
        }

        assert( testFileRetrieval(key1, data1, dataSize) == -1);
        assert( testFileRetrieval(key2, data2, dataSize) == -1);
        assert( testFileRetrieval(key3, data3, dataSize) == -1);
        
        assert( testFileRemoval(key1) == -1);
        assert( testFileRemoval(key2) == -1);
        assert( testFileRemoval(key3) == -1);
        
        delete[] data1;
        delete[] data2;
        delete[] data3;
        
        delete[] key1;
        delete[] key2;
        delete[] key3;

    }

    std::cout << "Test finished succesfully!" << std::endl;

    sleep(2);

    return 0;
}
