#include "CRDataServer.hpp"
#include "AdrStatusUpdater.hpp"
#include "FileManipulationNotifier.hpp"
#include "utils/c++/NoSuchConfigException.hpp"

#include <cstdlib>
#include <string>
#include <iostream>

int main(int argc, char **argv) {

    Config config("adr.conf");
    AdrManagerStub adrManagerStub(config);
    int updateInterval = 5; // in seconds

    bool ipFromConf = false;
    try {        
        config.getConf("ipAddress"); 
        ipFromConf = true;               
    }
    catch (NoSuchConfigException e) {}
    
    CRDataServer *dataServer = NULL;    
    try {        
        string port = config.getConf("port");
        string storagePath = config.getConf("storagePath");
        if (ipFromConf == false)
            dataServer = new CRDataServer(adrManagerStub, atoi(port.c_str()), storagePath);
        else
            dataServer = new CRDataServer(adrManagerStub, config.getConf("ipAddress"), atoi(port.c_str()), storagePath);
        AdrStatusUpdater::launchCkpRepositoryUpdater(adrManagerStub, dataServer->getCkpReposId(), updateInterval, dataServer->getOutputDir());
        FileManipulationNotifier::launchFileManipulationNotifier( adrManagerStub, dataServer->getFifoName(), dataServer->getCkpReposId() );
                                 
        dataServer->startServer();
        delete dataServer;                
    }
    catch (NoSuchConfigException e) {
        std::cerr << "[CRITICAL] AdrLauncher::main -> field 'port' from config file not found! Exiting..." << std::endl;
        exit(-1);
    }

    return 0;
}
