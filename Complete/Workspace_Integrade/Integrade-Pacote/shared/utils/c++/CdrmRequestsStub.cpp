#include <cassert>
#include <cstdlib>

extern "C" {
#include <lua.h>
//#include <lualib.h>
#include <lauxlib.h>
}

#include "LuaUtils.hpp"
#include "OrbUtils.hpp"
#include "StringUtils.hpp"
#include "NameServiceStub.hpp"
#include "NoSuchConfigException.hpp"

#include "CdrmRequestsStub.hpp"

#include <iostream>
#include <fstream>
using std::cerr;
using std::endl; 
using std::ifstream; 

  //----------------------------------------------------------------------
  CdrmRequestsStub::CdrmRequestsStub(const Config & config, int keySize_) {

    this->keySize = keySize_;
    string serverNameLocation;
    string cosNamingIdlPath;   
    string orbPath;
    string ckpReposManagerIdl;
    try {
        serverNameLocation = config.getConf("serverNameRef");
        cosNamingIdlPath = config.getConf("cosNamingIdlPath");   
        orbPath = config.getConf("orbPath");
        ckpReposManagerIdl = config.getConf("cdrmIdlPath");       
    }
    catch (NoSuchConfigException e) {
        cerr << "[CRITICAL] CdrmRequestsStub::CdrmRequestsStub -> field from config file not found! Exiting..." << endl;
        cerr << e.what() << endl;
        exit(-1);
    }
    
    this->stubMutex = new pthread_mutex_t;
    pthread_mutex_init(this->stubMutex, NULL);
    //*stubMutex = PTHREAD_MUTEX_INITIALIZER;
    
    //struct lua_State *ccdrmRequestsState = lua_open();
    struct lua_State *cdrmRequestsState = luaL_newstate();
    OrbUtils::initLuaState(cdrmRequestsState);
    OrbUtils::loadOrb(cdrmRequestsState, orbPath);
    OrbUtils::loadIdl(cdrmRequestsState, ckpReposManagerIdl);

    NameServiceStub nameServiceStub(lua_open(),serverNameLocation,cosNamingIdlPath);

    // Gets GRM ior 
    string ckpReposManagerIor = nameServiceStub.resolve("CdrmRequests");
    if ( ckpReposManagerIor.compare("") == 0 ) {
        cerr << "[CRITICAL] CdrmRequestsStub::CdrmRequestsStub -> Could not resolve CdrmRequests. Exiting..." << endl;
        exit(-1);
    }
    
    OrbUtils::instantiateProxy (cdrmRequestsState, ckpReposManagerIor, "IDL:br/usp/ime/oppstore/corba/CdrmRequests:1.0", "cdrmRequestsProxy");
  
    state = cdrmRequestsState;
    
  }

  //--------------------------------------------------------------------------------
  void CdrmRequestsStub::setFragmentStorageFinished( const int & requestNumber, const vector<int> & notStoredFragmentList,
                                                     const vector<char *> & fragmentHashList, char * fileKey ){

    pthread_mutex_lock( stubMutex );
    
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushstring(state, "setFragmentStorageFinished");
    lua_gettable(state, -2);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushnumber(state, requestNumber);             
    // Sets notStoredFragmentList
    lua_newtable(state);
    for (unsigned int i=0; i<notStoredFragmentList.size(); i++) {
        int tmpIndex = LuaUtils::convertStackIndex(state, -1);
        lua_pushnumber(state, i+1);
        lua_pushnumber(state, notStoredFragmentList[i]);
        lua_settable(state, tmpIndex);
    }
    // Sets fragmentHashList
    lua_newtable(state);
    for (unsigned int i=0; i<fragmentHashList.size(); i++) {
        int tmpIndex = LuaUtils::convertStackIndex(state, -1);
        lua_pushnumber(state, i+1);
        lua_pushlstring(state, fragmentHashList[i], keySize);
        lua_settable(state, tmpIndex);
    }
    lua_pushlstring(state, fileKey, keySize);
        
    if (lua_pcall(state, 5, 0, 0) != 0){
        lua_getglobal(state, "tostring");
        lua_insert(state, -2);
        lua_pcall(state, 1, 1, 0);
        cerr << "[ERROR] CdrmRequestsStub::setFragmentStorageFinished->Lua error: " << endl
             << lua_tostring(state, -1) << " error " << endl;
        lua_pop(state, 1);    	
    }
  
    lua_pop(state, 1);//removing proxy
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );    
  }

  //--------------------------------------------------------------------------------
  int CdrmRequestsStub::requestFileStorage (char * & fileKey, const vector<char *> & fragmentKeyList, 
                                            int fileSize, const vector<int> & fragmentSizeList, int neededFragments,
                                            const string & accessBrokerIor, int timeoutMinutes, bool storeGlobal) {
  	
    pthread_mutex_lock( stubMutex );

    int stackTop = lua_gettop(state);
    
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushstring(state, "requestFileStorage");
    lua_gettable(state, -2);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushlstring(state, fileKey, keySize);       
    lua_newtable(state);
    for (unsigned int i=0; i<fragmentKeyList.size(); i++) {
        int tmpIndex = LuaUtils::convertStackIndex(state, -1);
        lua_pushnumber(state, i+1);
        lua_pushlstring(state, fragmentKeyList[i], keySize);
        lua_settable(state, tmpIndex);
    }
    lua_pushnumber(state, fileSize);
    lua_newtable(state);
    for (unsigned int i=0; i<fragmentSizeList.size(); i++) {
        int tmpIndex = LuaUtils::convertStackIndex(state, -1);
        lua_pushnumber(state, i+1);
        lua_pushnumber(state, fragmentSizeList[i]);
        lua_settable(state, tmpIndex);
    }
    lua_pushnumber(state, neededFragments);
    lua_pushstring(state, accessBrokerIor.c_str());
    lua_pushnumber(state, timeoutMinutes);
    (storeGlobal) ? lua_pushboolean(state, 1) : lua_pushboolean(state, 0);
    
    //sleep(1);
    
    if (lua_pcall(state, 9, 1, 0) != 0) {
      lua_getglobal(state, "tostring");
      lua_insert(state, -2);
      lua_pcall(state, 1, 1, 0);
      cerr << "[ERROR] CdrmRequestsStub::requestFileStorage -> OiL error: " << endl
           << lua_tostring(state, -1) << " error " << endl;
      lua_pop(state, 1);
    }

    int response = (int)lua_tonumber(state, -1);
    lua_pop(state, 2);//removing proxy + result
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );
        
    return response;
  }
  
  //--------------------------------------------------------------------------------
  int CdrmRequestsStub::requestFileRetrieval(char * & fileKey, const string & accessBrokerIor) {

    pthread_mutex_lock( stubMutex );

    int stackTop = lua_gettop(state);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushstring(state, "requestFileRetrieval");
    lua_gettable(state, -2);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushlstring(state, fileKey, keySize);
    lua_pushstring(state, accessBrokerIor.c_str());

    if (lua_pcall(state, 3, 1, 0) != 0){
      cerr << "[ERROR] CdrmRequestsStub::requestFileRetrieval->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }

    int updateStatus = (int)lua_tonumber(state, -1);
    lua_pop(state, 2);//removing proxy + result
    assert(stackTop == lua_gettop(state));

    pthread_mutex_unlock( stubMutex );
        
    return updateStatus;
  }
  
  //--------------------------------------------------------------------------------
  int CdrmRequestsStub::requestFileRemoval(char * & fileKey, const string & accessBrokerIor) {

    pthread_mutex_lock( stubMutex );

    int stackTop = lua_gettop(state);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushstring(state, "requestFileRemoval");
    lua_gettable(state, -2);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushlstring(state, fileKey, keySize);
    lua_pushstring(state, accessBrokerIor.c_str());

    if (lua_pcall(state, 3, 1, 0) != 0){
      cerr << "[ERROR] CdrmRequestsStub::requestFileRetrieval->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }

    int updateStatus = (int)lua_tonumber(state, -1);
    lua_pop(state, 2);//removing proxy + result
    assert(stackTop == lua_gettop(state));

    pthread_mutex_unlock( stubMutex );
        
    return updateStatus;
  }

  //--------------------------------------------------------------------------------
  int CdrmRequestsStub::requestFileLeaseRenewal(char * & fileKey, const string & accessBrokerIor, int timeoutMinutes) {

    pthread_mutex_lock( stubMutex );

    int stackTop = lua_gettop(state);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushstring(state, "requestFileLeaseRenewal");
    lua_gettable(state, -2);
    lua_getglobal(state, "cdrmRequestsProxy");
    lua_pushlstring(state, fileKey, keySize);
    lua_pushstring(state, accessBrokerIor.c_str());
    lua_pushnumber(state, timeoutMinutes);

    if (lua_pcall(state, 4, 1, 0) != 0){
      cerr << "[ERROR] CdrmRequestsStub::requestFileRetrieval->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }

    int updateStatus = (int)lua_tonumber(state, -1);
    lua_pop(state, 2);//removing proxy + result
    assert(stackTop == lua_gettop(state));

    pthread_mutex_unlock( stubMutex );
        
    return updateStatus;
  }

