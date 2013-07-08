#include <jni.h>
#include <iostream>
#include "OppStoreBrokerJava.h"
#include "../OppStoreBroker.h"
#include "../OppStoreUtils.hpp"

JNIEXPORT void JNICALL
Java_br_usp_ime_oppstore_broker_OppStoreBroker_print(JNIEnv *env, jobject obj) {

  cout << "JNI oppstore interface called successfully!" << endl;
  return;
}

JNIEXPORT jint JNICALL 
Java_br_usp_ime_oppstore_broker_OppStoreBroker_removeDataW_1(JNIEnv *env, jobject obj, jstring javaKey) {

  char *key = new char[OppStoreUtils::binaryKeySize*2];
  int len = env->GetStringLength(javaKey);
  if (len != OppStoreUtils::binaryKeySize*2)
	  return -2;
  
  env->GetStringUTFRegion(javaKey, 0, len, key);
  
  cout << "Removing file with key '";
  OppStoreUtils::printHexKey(key, cout);
  cout << "'." << endl;
  
  int status = removeDataW(key, NULL);

  delete[] key;
  return status;
}

JNIEXPORT jstring JNICALL 
Java_br_usp_ime_oppstore_broker_OppStoreBroker_storeFileW_1(JNIEnv *env, jobject obj, jstring filePathJ) {

    char *key = new char[OppStoreUtils::binaryKeySize*2 + 1];
    key[OppStoreUtils::binaryKeySize*2] = 0;
    
    int len = env->GetStringLength(filePathJ);
    char *filePath = new char[len+1];    
    env->GetStringUTFRegion(filePathJ, 0, len, filePath);
    filePath[len]=0;

    int status = storeFileW(key, filePath, NULL);

    if (status == 0)
    	return env->NewStringUTF(key);
    else
    	return NULL;
}

JNIEXPORT jint JNICALL
Java_br_usp_ime_oppstore_broker_OppStoreBroker_retrieveFileW_1(JNIEnv *env, jobject obj, jstring javaKey, jstring javaPath) {

	char *key = new char[OppStoreUtils::binaryKeySize*2];
	int keyLen = env->GetStringLength(javaKey);
	if (keyLen != OppStoreUtils::binaryKeySize*2) return -2;
	env->GetStringUTFRegion(javaKey, 0, keyLen, key);

    int nameLen = env->GetStringLength(javaPath);
    char *filePath = new char[nameLen+1];    
    env->GetStringUTFRegion(javaPath, 0, nameLen, filePath);
    filePath[nameLen]=0;
    
	long fileSize;
	int status = retrieveFileW( key, filePath, fileSize, NULL);
	
	return fileSize;
}

