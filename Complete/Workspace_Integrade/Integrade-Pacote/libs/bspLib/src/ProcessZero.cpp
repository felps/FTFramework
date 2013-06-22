#include "ProcessZero.hpp"
#include "BspProxyStubPool.hpp"
#include "DrmaManager.hpp"
#include "BsmpManager.hpp"
#include "utils/c++/GuardedVariable.hpp"
#include "utils/c++/Condition.hpp"
#include "BspLogger.hpp"

#include <sstream>
#include <string>
#include <iostream>

    ProcessZero::ProcessZero(BspProxyStubPool * stubPool,
                             DrmaManager * drmaManager,
                             BsmpManager * bsmpManager):
                             BaseProcess(stubPool, drmaManager, bsmpManager) {

      myPid = 0;
      pthread_mutex_init(&bspSynchLock, NULL);

    }


  //-----------------------------------------------------------------------
  void ProcessZero::bspBegin() {
    
    if (totalNumProcs > 1)
        globalInitDone.wait();
         
    bspLogger.debug("ProcessZero::bspBegin -> All processes are ready. Starting execution.");    
  }

  //-----------------------------------------------------------------------
  void ProcessZero::registerRemoteIor(string ior, int processPid){

    //pthread_mutex_lock(&bspSynchLock); // NOT Necessary    
    //std::cerr << "ProcessZero::registerRemoteIor" << std::endl;

    int nRegisteredProc = nextPid.inc();    
    stubPool_->createNewProxy(processPid, ior);
    //stubPool_->takeYourPid(processPid);

    {
        ostringstream logStr;
        logStr << "Receiving (" << myPid << ") --> pid=" << processPid << ". Remaining " 
               << totalNumProcs - nRegisteredProc - 2 << " of " << totalNumProcs - 1; 
        bspLogger.debug( logStr.str() );        
    }
        
    if (nRegisteredProc == totalNumProcs - 2){
      
      //std::cerr << "BspProxyImpl::registerRemoteIor --> All processes registered " << std::endl;          
      
      for (int i = 1; i < totalNumProcs; i++)
        stubPool_->registerOtherProcessIors(i);
      globalInitDone.signal();
    }
    //pthread_mutex_unlock(&bspSynchLock); // NOT Necessary    
  }

  //-----------------------------------------------------------------------
  void ProcessZero::bspLocalSynch(){

    synchedProcecess.inc();
    pthread_mutex_lock(&bspSynchLock);

    if((synchedProcecess.value() == totalNumProcs)){
      synchedProcecess.reset();
      for(int i = 1; i < totalNumProcs; i++)
        stubPool_->bspSynchDone(i);
      pthread_mutex_unlock(&bspSynchLock);
    }
    else{
      pthread_mutex_unlock(&bspSynchLock);
      //cerr << "Waitin' in ProcessZero::bspLocalSynch()" << endl << endl;
      synchDone.wait();
    }
    drmaManager_->processPendingOperations();
    bsmpManager_->processPendingOperations();
    
    {
        ostringstream logStr;
        logStr << "########### END OF SUPERSTEP  " << superstep_.value() << " ##########"; 
        bspLogger.debug( logStr.str() );        
    }
    
    superstep_.inc();
  }

  //-----------------------------------------------------------------------
  void ProcessZero::bspSynch(){

    synchedProcecess.inc();
    pthread_mutex_lock(&bspSynchLock);
      if ((synchedProcecess.value() == totalNumProcs) ){
        synchedProcecess.reset();

        for (int i = 1; i < totalNumProcs; i++)
          stubPool_->bspSynchDone(i);
        synchDone.signal();
      }
    pthread_mutex_unlock(&bspSynchLock);
  }

