extern "C" {
#include <pthread.h>
#include <lualib.h>
#include <lauxlib.h>
#include <lua.h>

}

#include <cassert>
#include "AccessBrokerSkeleton.hpp"
#include "OppStoreUtils.hpp"
#include "BrokerLogger.hpp"

#include <sstream>
 
AccessBrokerSkeleton * AccessBrokerSkeleton::singleInstance_ = NULL;

//-----------------------------------------------------------------------
AccessBrokerSkeleton & AccessBrokerSkeleton::init
( FileRetrievalServerInterface *fileRetrieval, FileStorageServerInterface *fileStorage, FileRemovalRenewalServerInterface *fileRemoval, const Config & config){

    if(AccessBrokerSkeleton::singleInstance_ == NULL){        
        AccessBrokerSkeleton::singleInstance_ = new AccessBrokerSkeleton(fileRetrieval, fileStorage, fileRemoval, config);
        pthread_t thread;
        pthread_create(&thread, NULL, (void * (*)(void *))serverSideSetup, (void *) NULL );
        pthread_detach(thread);
    }
    return *AccessBrokerSkeleton::singleInstance_;
}

//-----------------------------------------------------------------------
void * AccessBrokerSkeleton::serverSideSetup(void * ptr){
 
    brokerLogger.debug("Calling AccessBrokerSkeleton::serverSideSetup");   
    
    lua_State *state = AccessBrokerSkeleton::singleInstance_->serverSideState;

    LuaUtils::getField(state,"oil.run");
    if(lua_pcall(state, 0, 0, 0) != 0){
      lua_getglobal(state, "tostring");
      lua_insert(state, -2);
      lua_pcall(state, 1, 1, 0);
      cerr << "[ERROR] AccessBrokerSkeleton::serverSideSetup -> OiL error: " << endl
           << lua_tostring(state, -1) << " error " << endl;
      lua_pop(state, 1);
    }
    return NULL;
}

//---------------------------------------------------------------------
AccessBrokerSkeleton::AccessBrokerSkeleton( FileRetrievalServerInterface *fileRetrieval, FileStorageServerInterface *fileStorage, FileRemovalRenewalServerInterface *fileRemoval, const Config & config) {

    this->fileRetrieval_ = fileRetrieval;
    this->fileStorage_   = fileStorage;
    this->fileRemoval_   = fileRemoval;
    
    //serverSideState = lua_open();
    serverSideState = luaL_newstate();
    OrbUtils::initLuaState(serverSideState);
    OrbUtils::loadOrb(serverSideState, config.getConf("orbPath"));
    OrbUtils::loadIdl(serverSideState, config.getConf("cdrmIdlPath"));

    lua_register(serverSideState, "uploadFragmentsWrapper", uploadFragments);
    lua_register(serverSideState, "downloadFragmentsWrapper", downloadFragments);
    lua_register(serverSideState, "removeFragmentsWrapper", removeFragments);
    lua_register(serverSideState, "renewFragmentLeasesWrapper", renewFragmentLeases);
    lua_register(serverSideState, "setFileStorageRequestCompletedWrapper", setFileStorageRequestCompleted);
    lua_register(serverSideState, "setFileStorageRequestFailedWrapper", setFileStorageRequestFailed);
    lua_register(serverSideState, "setFileRetrievalRequestFailedWrapper", setFileRetrievalRequestFailed);

    //servant implementation
    luaL_dostring(serverSideState, " brokerImpl = {"
                        " uploadFragments = function (self, requestId, adrAddresses)"
                        "   uploadFragmentsWrapper(requestId, adrAddresses)"
                        " end,"
                        " downloadFragments = function (self, requestId, adrAddresses, fragmentKeyList, fileSize, fragmentSizeList, nNeededFragments)"
                        "   downloadFragmentsWrapper(requestId, adrAddresses, fragmentKeyList, fileSize, fragmentSizeList, nNeededFragments)"
                        " end,"
    					" removeFragments = function (self, requestId, adrAddresses, fragmentKeyList)"
            			"   removeFragmentsWrapper(requestId, adrAddresses, fragmentKeyList)"
            			" end,"
    					" renewFragmentLeases = function (self, requestId, adrAddresses, fragmentKeyList, timeout)"
						"   renewFragmentLeasesWrapper(requestId, adrAddresses, fragmentKeyList, timeout)"
						" end,"
                        " setFileStorageRequestCompleted = function (self, requestId)"
                        "   setFileStorageRequestCompletedWrapper(requestId)"
                        " end,"
    					" setFileStorageRequestFailed = function (self, requestId)"
            			"   setFileStorageRequestFailedWrapper(requestId)"
            			" end,"
                        " setFileRetrievalRequestFailed = function(self, requestId)"
                        "   setFileRetrievalRequestFailedWrapper(requestId)"
                        " end,"
                        " }"
                );

    luaL_dostring(serverSideState, "brokerServant = oil.createservant(brokerImpl, 'IDL:br/usp/ime/oppstore/corba/AccessBroker:1.0')");
    brokerIor_ = OrbUtils::getIor(serverSideState, "brokerServant");
    
    { 
        ostringstream logStr;
        logStr << "AccessBrokerSkeleton::serverSideSetup::LRM IOR: " << brokerIor_; 
        brokerLogger.debug( logStr.str() ); 
    }                                

}

//---------------------------------------------------------------------
int AccessBrokerSkeleton::setFileRetrievalRequestFailed(struct lua_State * state){
    int n = lua_gettop(state);
    assert(n == 1);
        
    int requestId = int(lua_tonumber(state, 1));
    AccessBrokerSkeleton::singleInstance_->fileRetrieval_->setFileRetrievalRequestFailed(requestId);
    
    //cout << "AccessBrokerSkeleton::setFileRetrievalRequestFailed requestId=" << requestId << endl;    
    //lua_pop(state, n);
    
    return 0;
}

//---------------------------------------------------------------------
int AccessBrokerSkeleton::setFileStorageRequestCompleted(struct lua_State * state){
    int n = lua_gettop(state);
    assert(n == 1);
    
    int requestId = int(lua_tonumber(state, 1));
    AccessBrokerSkeleton::singleInstance_->fileStorage_->setFileStorageRequestCompleted(requestId);

    //cout << "AccessBrokerSkeleton::setFileStorageRequestCompleted requestId=" << requestId << endl;
    //lua_pop(state, n);
    //cout << "AccessBrokerSkeleton::fileStorageRequestCompleted finished " << lua_gettop(state) << endl;
        
    return 0;
}

//---------------------------------------------------------------------
int AccessBrokerSkeleton::setFileStorageRequestFailed(struct lua_State * state){
    int n = lua_gettop(state);
    assert(n == 1);
            
    int requestId = int(lua_tonumber(state, 1));
    //cout << "AccessBrokerSkeleton::setFileStorageRequestFailed requestId=" << requestId << endl;
    AccessBrokerSkeleton::singleInstance_->fileStorage_->setFileStorageRequestFailed(requestId);

    //cout << "AccessBrokerSkeleton::setFileStorageRequestCompleted requestId=" << requestId << endl;
    //lua_pop(state, n);
    //cout << "AccessBrokerSkeleton::fileStorageRequestCompleted finished " << lua_gettop(state) << endl;
        
    return 0;
}

//---------------------------------------------------------------------
int AccessBrokerSkeleton::uploadFragments(struct lua_State * state){
    
    brokerLogger.debug("AccessBrokerSkeleton::uploadFragments called.");

    int n = lua_gettop(state);
    assert(n == 2);

    int requestId = int(lua_tonumber(state, 1));
    vector<string> adrAddresses = LuaUtils::extractStringSequence(state, 2);
    AccessBrokerSkeleton::singleInstance_->fileStorage_->uploadFragments(requestId, adrAddresses);    
    
    //lua_pop(state, n);
    //cout << "AccessBrokerSkeleton::uploadFragments finished " << lua_gettop(state) << endl;
    return 0;
}

//---------------------------------------------------------------------
int AccessBrokerSkeleton::downloadFragments(struct lua_State * state){
    
    brokerLogger.debug("AccessBrokerSkeleton::downloadFragments called.");
                
    int n = lua_gettop(state);
    assert(n == 6);
                
    int requestId = int(lua_tonumber(state, 1));
    
    vector<string> adrAddresses    = LuaUtils::extractStringSequence(state, 2);
    vector<char *> fragmentKeyList = LuaUtils::extractOctetArraySequence(state, 3, OppStoreUtils::binaryKeySize);
    int dataSize                   = int(lua_tonumber(state, 4));
    vector<int> fragmentSizeList   = LuaUtils::extractIntegerSequence(state, 5);
    int nNeededFragments           = int(lua_tonumber(state, 6));

    AccessBrokerSkeleton::singleInstance_->fileRetrieval_->downloadFragments(requestId, adrAddresses, fragmentKeyList, dataSize, fragmentSizeList, nNeededFragments);    
    
    //lua_pop(state, n);
    //cout << "AccessBrokerSkeleton::downloadFragments finished " << lua_gettop(state) << endl;
    
    return 0;
}

//---------------------------------------------------------------------
int AccessBrokerSkeleton::removeFragments(struct lua_State * state){
    
    brokerLogger.debug("AccessBrokerSkeleton::removeFragments called.");
                
    int n = lua_gettop(state);
    assert(n == 3);
                
    int requestId = int(lua_tonumber(state, 1));    
    vector<string> adrAddresses    = LuaUtils::extractStringSequence(state, 2);
    vector<char *> fragmentKeyList = LuaUtils::extractOctetArraySequence(state, 3, OppStoreUtils::binaryKeySize);

    AccessBrokerSkeleton::singleInstance_->fileRemoval_->removeFragments(requestId, adrAddresses, fragmentKeyList);    
    
    //lua_pop(state, n);
    //cout << "AccessBrokerSkeleton::downloadFragments finished " << lua_gettop(state) << endl;
    
    return 0;
}

//---------------------------------------------------------------------
int AccessBrokerSkeleton::renewFragmentLeases(struct lua_State * state){
    
    brokerLogger.debug("AccessBrokerSkeleton::renewFragmentLeases called.");
                
    int n = lua_gettop(state);
    assert(n == 4);
                
    int requestId = int(lua_tonumber(state, 1));    
    vector<string> adrAddresses    = LuaUtils::extractStringSequence(state, 2);
    vector<char *> fragmentKeyList = LuaUtils::extractOctetArraySequence(state, 3, OppStoreUtils::binaryKeySize);
    int timeout = int(lua_tonumber(state, 4));

    AccessBrokerSkeleton::singleInstance_->fileRemoval_->renewFragmentLeases(requestId, adrAddresses, fragmentKeyList, timeout);    
    
    //lua_pop(state, n);
    //cout << "AccessBrokerSkeleton::downloadFragments finished " << lua_gettop(state) << endl;
    
    return 0;
}

//---------------------------------------------------------------------
