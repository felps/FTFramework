#ifndef OPPSTOREBROKER_H_
#define OPPSTOREBROKER_H_

#include <vector>
#include <string>

#ifdef __cplusplus
extern "C"{
#endif

int launchBroker();

void setEncryptionKey(const unsigned char *encKey);

int storeDataEphemeral(char * & key, void *data, long dataSize, void(*appCallback)(int));

int storeDataEphemeralW(char * & key, void *data, long dataSize, void(*appCallback)(int));

int storeFileEphemeral(char * & key, const char *filePath, void(*appCallback)(int));

int storeFileEphemeralW(char * & key, const char *filePath, void(*appCallback)(int));

int storeData(char * & key, void *data, long dataSize, void(*appCallback)(int));

int storeDataW(char * & key, void *data, long dataSize, void(*appCallback)(int));

int storeFile(char * & key, const char *filePath, void(*appCallback)(int));

int storeFileW(char * & key, const char *filePath, void(*appCallback)(int));

//-----------------------------------------------------------------------------------

int retrieveDataW(const char * key, void * & data, long & dataSize, void(*appCallback)(int));

int retrieveFileW(const char * key, const char *filePath, long & fileSize, void(*appCallback)(int));

int removeDataW(char * & key, void(*appCallback)(int));

int renewStorageLeaseW(char * & key, void(*appCallback)(int));

/**
 * Used only for experiments
 */
int setAllowedAdrs(std::vector<std::string> & allowedAdrs);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /*OPPSTOREBROKER_H_*/
