#include "RegularProcess.hpp"
#include "BspProxyStubPool.hpp"
#include "BspProxyImpl.hpp"
#include "DrmaManager.hpp"
#include "BsmpManager.hpp"
#include "utils/c++/GuardedVariable.hpp"
#include "utils/c++/Condition.hpp"
#include "BspLogger.hpp"

#include <sstream>
#include <iostream>

  RegularProcess::RegularProcess(BspProxyStubPool * stubPool,
                                 DrmaManager * drmaManager,
                                 BsmpManager * bsmpManager,
                                 const string & processZeroIor,
                                 int processId):
                                 BaseProcess(stubPool, drmaManager, bsmpManager),
                                 processZeroIor_(processZeroIor),
                                 processId_(processId){

  }

  //-----------------------------------------------------------------------
  void RegularProcess::registerOtherProcessIors(map<int, string> processInfo){


    for(map<int, string>::iterator it = processInfo.begin(); it != processInfo.end(); it++)
      if((*it).first != myPid)//FIXME: o zero poderia mandar uma lista "certinha", ao custo
        stubPool_->createNewProxy((*it).first,(*it).second);//de  ter de mandar n listas !=s
    globalInitDone.signal();

  }//method

  //-----------------------------------------------------------------------
  void RegularProcess::bspLocalSynch(){

    stubPool_->bspSynch(myPid, superstep_.value());
    synchDone.wait();
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
  void RegularProcess::bspSynchDone(){ synchDone.signal(); }



  //-----------------------------------------------------------------------
  void RegularProcess::bspBegin(){
    
    {
        ostringstream logStr;
        logStr << "Sending --> pid=" << processId_; 
        bspLogger.debug( logStr.str() );        
    }            
    stubPool_->createNewProxy(0, processZeroIor_);    
    stubPool_->registerRemoteIor(BspProxyImpl::getInstance().getIor(), processId_);
    
    globalInitDone.wait();
  }










