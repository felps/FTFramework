#include "BspCentral.hpp"
#include "BspProxyImpl.hpp"
#include "BspInfo.hpp"
#include "ExecutionManagerStub.hpp"
#include "utils/c++/Config.hpp"
#include "utils/c++/StringUtils.hpp"
#include "utils/c++/NoSuchConfigException.hpp"
#include "BaseProcess.hpp"
#include "ProcessZero.hpp"
#include "RegularProcess.hpp"
#include "BspLogger.hpp"

#include <sstream>
#include <iostream>
#include <fstream>
#include <cstdio>
#include <cstdlib>

  BspCentral::BspCentral(){

    Config asctConfig("asct.conf");
    // { IMPI: changed name from "bspExecution.conf" to "execution.conf"
    Config bspConfig("execution.conf");
    // } IMPI
    // Obtains an Execution Manager reference from the Name Service
    string serverNameRef;
    string CosNamingIdlPath; 

    try {
        serverNameRef=asctConfig.getConf("serverNameRef");
        CosNamingIdlPath=asctConfig.getConf("cosNamingIdlPath");
    }
    catch(NoSuchConfigException e) {
        cout << e.what() << endl;
    }

    struct lua_State * clientSideState = lua_open();    
    OrbUtils::initLuaState(clientSideState);
    nameServiceStub = new NameServiceStub(clientSideState,serverNameRef,CosNamingIdlPath);
    string execManagerIor=nameServiceStub->resolve("EM");     

    stubPool  = new BspProxyStubPool(asctConfig);

    DataConverters *dataConverter = new DataConverters(DataConverters::getProcessorArchitecture());
    drmaManager = new DrmaManager(stubPool, dataConverter);
    bsmpManager = new BsmpManager(stubPool, dataConverter);
    BspProxyImpl::init(drmaManager, bsmpManager, asctConfig);

    /** Obtains Process Zero IOR */
    BspInfo *bspInfo = NULL;
    try{
    	ExecutionManagerStub execManagerStub(asctConfig.getConf("orbPath"),
                                         asctConfig.getConf("resourceManagementIdlPath"),
                                         execManagerIor);
  
    	bspInfo = execManagerStub.registerBspNode(bspConfig.getConf("applicationId"),
						  bspConfig.getConf("processId"),
						  BspProxyImpl::getInstance().getIor());

    }catch(NoSuchConfigException e)
    {
              cout << e.what() << endl;
    }

    int processId = -1;
    try {
    	processId = atoi(bspConfig.getConf("processId").c_str());           
    } 
    catch(NoSuchConfigException e) {
      // { IMPI: changed name from "bspExecution.conf" to "execution.conf"
      cerr << "Could not obtain processId from file execution.conf." << endl;
      exit(-1);              
      // } IMPI

        exit(-1);              
    }

    if(bspInfo->isProcessZero()){
      assert (bspInfo->processZeroIor().compare(BspProxyImpl::getInstance().getIor()) == 0);
      process = new ProcessZero(stubPool, drmaManager, bsmpManager);
    }
    else{
      assert (bspInfo->processZeroIor().compare(BspProxyImpl::getInstance().getIor()) != 0);              
      process = new RegularProcess(stubPool, drmaManager, bsmpManager, 
                                   bspInfo->processZeroIor(), processId);
    }
       
    try{ 
    process->setTotalNumProcs(StringUtils::string2int(bspConfig.getConf("numExecs")));
    }catch(NoSuchConfigException e)
    {
              cout << e.what() << endl;
    }


    drmaManager->setBaseProcess(process);
    bsmpManager->setBaseProcess(process);    
    BspProxyImpl::getInstance().setBaseProcess(process);
    process->setMyPid(processId);
    
    bspLogger.debug("BSP process succesfully initialized.");
    
    delete bspInfo;

  }







