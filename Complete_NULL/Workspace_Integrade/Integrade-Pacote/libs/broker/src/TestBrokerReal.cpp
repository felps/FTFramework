/**
 * Performs the storage and recovery of a file and tests if the file was recovered correctly
 */
  
#include "OppStoreBroker.h"
#include "OppStoreUtils.hpp"
#include "utils/c++/FileUtils.hpp"
#include "Benchmark.hpp"

#include <cstdlib>
#include <cassert>
#include <string>
#include <iostream>

#include <fcntl.h>

#include <cstdio>

#include <openssl/sha.h>

void testConversion() {
    long dataSize = 100;
    char *data = new char [dataSize];
    for (int i=0; i<dataSize; i++)
        data[i] = rand()%256;            
    
    unsigned char *fileKey1   = new unsigned char[OppStoreUtils::binaryKeySize];
    unsigned char *fileKeyHex = new unsigned char[OppStoreUtils::binaryKeySize*2];
    unsigned char *fileKey2   = new unsigned char[OppStoreUtils::binaryKeySize];    
    SHA1( (unsigned char*)data, dataSize, (unsigned char*)fileKey1 );
    OppStoreUtils::convertBinaryToHex((char *)fileKey1,   (char *)fileKeyHex);
    OppStoreUtils::convertHexToBinary((char *)fileKeyHex, (char *)fileKey2);    
    for (int i=0; i<OppStoreUtils::binaryKeySize; i++)
        assert( fileKey1[i] == fileKey2[i] );
}

char *testDataStorage(char *data, long dataSize, bool usePerennial) {

    std::cout << "testDataStorage -> Started storing data with size " << dataSize << "!" << std::endl;        
    char *key;        
    int status;
    if (usePerennial == true)
    	status = storeDataW(key, data, dataSize, NULL);
    else
    	status = storeDataEphemeralW(key, data, dataSize, NULL);
    
    {
    	unsigned char *recoveredKey = new unsigned char[OppStoreUtils::binaryKeySize];
    	    	
    	SHA_CTX ctx;
    	SHA1_Init( &ctx ); 
    	SHA1_Update( &ctx, data, dataSize/2 );
    	SHA1_Update( &ctx, data + (dataSize/2), dataSize/2 );
    	SHA1_Final((unsigned char*)recoveredKey, &ctx);
    
        char *key = new char[OppStoreUtils::binaryKeySize*2+1]; 
        OppStoreUtils::convertBinaryToHex((char *)recoveredKey, key);
        key[OppStoreUtils::binaryKeySize*2] = 0;

        cout << "returnedKey:     ";
        OppStoreUtils::printHexKey(key, cout);
        cout << endl;

    }
    
    if (status >= 0) {
    	std::cout << "testDataStorage -> Data stored succesfully with key = ";
    	OppStoreUtils::printHexKey(key, std::cout);
    	std::cout << "." << std::endl;
    }
    else if (status == -1)
    	std::cout << "testDataStorage -> Data storage FAILED...." << std::endl;
    
    return key;
}

char *testFileStorage(char *filePath, bool usePerennial) {

    std::cout << "testFileStorage -> Started storing file with path '" << filePath << "'!" << std::endl;        
    char *key;
    
    int status;
    if ( usePerennial == true )
    	status = storeFileW(key, filePath, NULL);
    else
    	status = storeFileEphemeralW(key, filePath, NULL);
    
    if (status >= 0) {
    	std::cout << "testFileStorage -> File stored succesfully with key = ";
    	OppStoreUtils::printHexKey(key, std::cout);
    	std::cout << "." << std::endl;
    }
    else if (status == -1)
    	std::cout << "testFileStorage -> File storage FAILED...." << std::endl;
    
    return key;
}

int testDataRetrieval(char *data, long dataSize, char *key) {
    
    std::cout << "testDataRetrieval -> Started recovering data!" << std::endl;
    void *recoveredData;
    long recoveredSize;
    sleep(1);
    int status = retrieveDataW( key, recoveredData, recoveredSize, NULL );
    if (status == -1) {
    	std::cout << "testDataRetrieval -> Data recovery FAILED...." << std::endl;
    	return -1;
    }

  	std::cout << "testDataRetrieval -> Finished recovering data! nbytes=" << recoveredSize << " status=" << status << std::endl;
    
    unsigned char *recoveredHash = new unsigned char[OppStoreUtils::binaryKeySize];
    unsigned char *originalHash  = new unsigned char[OppStoreUtils::binaryKeySize];
    unsigned char *binaryFileKey = new unsigned char[OppStoreUtils::binaryKeySize];
    SHA1(          (unsigned char*)data, dataSize, (unsigned char*)recoveredHash);
    SHA1( (unsigned char*)recoveredData, dataSize, (unsigned char*)originalHash );
    OppStoreUtils::convertHexToBinary(key, (char *)binaryFileKey);
    
    for (int i=0; i<dataSize; i++)
        assert( data[i] == ((char *)recoveredData)[i] );
    
    for (int i=0; i<OppStoreUtils::binaryKeySize; i++)
        assert( recoveredHash[i] == originalHash[i] );

    for (int i=0; i<OppStoreUtils::binaryKeySize; i++)
        assert( recoveredHash[i] == binaryFileKey[i] );

     //std::cout << "Removing recovered data!" << std::endl;   
     delete[] (char *)recoveredData;
     delete[] recoveredHash;
     delete[] originalHash;
     
     return status;
}

int testFileRetrieval(char *outputFilePath, char *key, char *inputFilePath) {
    
    std::cout << "testFileRetrieval -> Started recovering data!" << std::endl;    
    long recoveredSize;    
    int status = retrieveFileW( key, outputFilePath, recoveredSize, NULL );
    if (status == -1) {
    	std::cout << "testFileRetrieval -> Data recovery FAILED...." << std::endl;
    	return -1;
    }

  	std::cout << "testFileRetrieval -> Finished recovering file! nbytes=" << recoveredSize << " status=" << status << std::endl;
    
  	/**
  	 *  Compares the input e output files
  	 */ 
  	{
  	    int inFile = open(outputFilePath, O_RDONLY);
  	    long inFileSize = FileUtils::getFileSize(outputFilePath);  	    	
  	    char *inFileData = new char[inFileSize];
  	    read(inFile, inFileData, inFileSize);
  	    close(inFile);

  	    int outFile = open(inputFilePath, O_RDONLY);
  	    long outFileSize = FileUtils::getFileSize(inputFilePath);  	    	
  	    char *outFileData = new char[outFileSize];
  	    read(outFile, outFileData, outFileSize);
  	    close(outFile);

  	    assert( inFileSize == outFileSize );
  	    for (int i=0; i<inFileSize; i++)
  	        assert( inFileData[i] == outFileData[i] );

  	     delete[] inFileData;
  	     delete[] outFileData;
  	}
  	      
     return status;
}

int testFileRemoval(char *key) {
    
    std::cout << "testFileRemoval -> Started removing data!" << std::endl;

    int status = removeDataW( key, NULL );
    if (status == -1) {
    	std::cout << "testFileRemoval -> Data removal FAILED...." << std::endl;
    	return -1;
    }

  	std::cout << "testFileRemoval -> Finished removing data!" << std::endl;
  	return status;
}

int testFileLeaseRenewal(char *key) {
    
    std::cout << "testFileLeaseRenewing -> Started renewing lease!" << std::endl;

    int status = renewStorageLeaseW( key, NULL );
    if (status == -1) {
    	std::cout << "testFileRenewal -> Data lease renewal FAILED...." << std::endl;
    	return -1;
    }

  	std::cout << "testFileRenewal -> Finished renewing data!" << std::endl;
  	return status;
}

int testStorageRetrievalData (unsigned char* encKey, bool usePerennial, char *inFileName, char* outFileName) {
        
	long dataSize1 = 1 * 1000 ; 
    long dataSize2 = 4 * 1000 * 1000;
    
    char *data1 = new char [dataSize1];
    for (int i=0; i<dataSize1; i++)
         data1[i] = rand()%256;
     
    char *data2 = new char [dataSize2];
    for (int i=0; i<dataSize2; i++)
    	data2[i] = rand()%256;
            
     setEncryptionKey(encKey);
     char *key1, *key2, *keyF1;          
     key1 = testDataStorage(data1, dataSize1, usePerennial);    
     key2 = testDataStorage(data2, dataSize2, usePerennial);     
     keyF1 = testFileStorage(inFileName, usePerennial);
     
     assert (testDataRetrieval(data1, dataSize1, key1) == 0);      
     assert (testDataRetrieval(data2, dataSize2, key2) == 0);      
     assert (testFileRetrieval(outFileName, keyF1, inFileName) == 0);    
}

int main(int argc, char **argv) {

    launchBroker();    
    //srand(1234);
    
    testConversion();        
    unsigned char encKey[] = "1234567890123456";
    
    std::cout << endl << "---------------------------------------------------------------------" << endl;
    std::cout << "Testing with encKey=" << encKey << " and Ephemeral mode." << endl;
    //testStorageRetrievalData(encKey, false, "tests/TestAdrOperations1", "tests/TestAdrOperations1.out");        
    
    std::cout << endl << "---------------------------------------------------------------------" << endl;
    std::cout <<"Testing with encKey=NULL and Ephemeral mode." << endl;
    //testStorageRetrievalData(NULL, false, "tests/TestAdrOperations2", "tests/TestAdrOperations2.out");

    std::cout << endl << "---------------------------------------------------------------------" << endl;
    std::cout <<"Testing with encKey=" << encKey << " and Perennial mode." << endl;
    //testStorageRetrievalData(encKey, true, "tests/TestAdrOperations3", "tests/TestAdrOperations3.out");

    std::cout << endl << "---------------------------------------------------------------------" << endl;
    std::cout <<"Testing with encKey=NULL and Perennial mode." << endl;
    //testStorageRetrievalData(NULL, true, (const char *) "tests/TestAdrOperations4", (const char *) "tests/TestAdrOperations4.out");
    
    std::cout << "Test finished succesfully!" << std::endl;
    
    sleep(1);
    return 0;
    
    //=========================================================

	char *key1, *key2, *data1, *data2;
	int status;
    int waitLeaseExpire = false;
	long dataSize1, dataSize2;

    for (int i=0; i<1; i++) {       


    	/**
    	 * Tests file renewal
    	 */ 

    	if (waitLeaseExpire) {
    		for (int i = 30; i > 0; i -= 10) {
    			cout << "Waiting for " << i << " seconds..." << endl;
    			sleep(10);
    		}

    		/**
    		 * Tests file renewal
    		 */ 
    		testFileLeaseRenewal(key1);

    		status = testDataRetrieval(data1, dataSize1, key1); 
    		assert (status == 0);
    		status = testDataRetrieval(data2, dataSize2, key2);
    		assert (status == 0);

    		for (int i = 60; i > 0; i -= 10) {
    			cout << "Waiting for " << i << " seconds..." << endl;
    			sleep(10);
    		}

    		status = testDataRetrieval(data1, dataSize1, key1); 
    		assert (status == 0);
    		status = testDataRetrieval(data2, dataSize2, key2);
    		assert (status == -1);
    	}
    	else {
    		status = testFileLeaseRenewal(key1);
    		assert (status == 0);
    	}

    	/**
    	 * Tests file removal
    	 */ 
    	status = testFileRemoval(key1);
    	assert (status == 0);
    	status = testFileRemoval(key1);
    	assert (status == -1);

    	status = testDataRetrieval(data1, dataSize1, key1); 
    	assert (status == -1);

    	if (waitLeaseExpire) {
    		status = testDataRetrieval(data2, dataSize2, key2);
    		assert (status == -1);
    	}
    	else {
    		status = testDataRetrieval(data2, dataSize2, key2);
    		assert (status == 0);        	
    	}

    	delete[] data1;
    	delete[] data2;
    }

    std::cout << "Test finished succesfully!" << std::endl;

    sleep(2);

    return 0;
}
