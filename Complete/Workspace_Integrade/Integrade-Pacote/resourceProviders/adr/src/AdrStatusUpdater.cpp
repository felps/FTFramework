#include "AdrStatusUpdater.hpp"
#include "OppStoreUtils.hpp"
#include "utils/c++/FileUtils.hpp"
#include <iostream>
#include <sstream>
#include <pthread.h>

AdrStatusUpdater *adrStatusUpdater;

AdrStatusUpdater::AdrStatusUpdater
( AdrManagerStub & stub, int ckpReposId, int updateInterval, const string & outputDir ) : adrManagerStub( stub )
{
    this->outputDir = outputDir;
    this->updateInterval = updateInterval;
    this->ckpReposId = ckpReposId;
}

AdrStatusUpdater::~AdrStatusUpdater()
{
}

void AdrStatusUpdater::removeFiles(vector<char *> removalList) {
    
    int numberOfFiles = removalList.size();
    std::cerr << "AdrStatusUpdater -> removing " << numberOfFiles << " fragments." << std::endl;        
    
    char hexKey[OppStoreUtils::binaryKeySize*2 + 1];
    for (int fileIndex=0; fileIndex < numberOfFiles; fileIndex++) {
        OppStoreUtils::convertBinaryToHex( removalList[fileIndex], hexKey );
        hexKey[OppStoreUtils::binaryKeySize*2] = 0; // Marks end of string
        std::ostringstream ckpFileName;
        ckpFileName << this->outputDir << hexKey << ".adr";        
        int status = unlink(ckpFileName.str().c_str());
        if (status == -1)
            std::cerr << "AdrStatusUpdater -> Error removing file '" << ckpFileName.str() << "'." << std::endl;
    }
    
}

void AdrStatusUpdater::updateLoop ( ) {
    
    while (true) {
        sleep(updateInterval);

        //int availableSpace = FileUtils::getAvailableDiskSpace( this->outputDir.c_str() );        
        int updateStatus = adrManagerStub.adrKeepAlive( this->ckpReposId );
        //std::cerr << "AdrStatusUpdater::updateLoop -> updateStatus=" << updateStatus << " availableSpace=" << availableSpace << "." << std::endl;
        if (updateStatus == 1)
            this->removeFiles( adrManagerStub.getFragmentRemovalList( this->ckpReposId, OppStoreUtils::binaryKeySize ) );
        else if (updateStatus == -1)
            std::cerr << "AdrStatusUpdater::updateLoop -> Error updating status." << std::endl;
    }
}

void *AdrStatusUpdater::run( void *ptr ) {
    adrStatusUpdater->updateLoop();
    return NULL;
}

void AdrStatusUpdater::launchCkpRepositoryUpdater( AdrManagerStub & stub, int ckpReposId, int updateInterval, const string & outputDir ) {

    adrStatusUpdater = new AdrStatusUpdater( stub, ckpReposId, updateInterval, outputDir );

    pthread_t thread1;
    pthread_create( &thread1, NULL, AdrStatusUpdater::run, NULL);
    pthread_detach(thread1);
}
