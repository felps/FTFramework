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

#include "AdrManagerStub.hpp"

#include <iostream>
#include <fstream>
using std::cerr;
using std::endl; 
using std::ifstream; 

  //----------------------------------------------------------------------
  AdrManagerStub::AdrManagerStub(const Config & config) {

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
        cerr << "[CRITICAL] AdrManagerStub::AdrManagerStub -> field from config file not found! Exiting..." << endl;
        cerr << e.what() << endl;
        exit(-1);
    }
    
    //struct lua_State *ckpReposManagerState = lua_open();
    struct lua_State *ckpReposManagerState = luaL_newstate();
    OrbUtils::initLuaState(ckpReposManagerState);
    OrbUtils::loadOrb(ckpReposManagerState, orbPath);
    OrbUtils::loadIdl(ckpReposManagerState, ckpReposManagerIdl);

    NameServiceStub nameServiceStub(lua_open(),serverNameLocation,cosNamingIdlPath);

    // Gets GRM ior 
    string ckpReposManagerIor = nameServiceStub.resolve("AdrManager");
    if ( ckpReposManagerIor.compare("") == 0 ) {
        cerr << "[CRITICAL] AdrManagerStub::AdrManagerStub -> Could not resolve AdrManager. Exiting..." << endl;
        exit(-1);
    }
    
    OrbUtils::instantiateProxy (ckpReposManagerState, ckpReposManagerIor, "IDL:br/usp/ime/oppstore/corba/AdrManager:1.0", "adrManagerProxy");
  
    state = ckpReposManagerState;
    
    stubMutex = new pthread_mutex_t;
    pthread_mutex_init(stubMutex, NULL);

  }

  //--------------------------------------------------------------------------------
  int AdrManagerStub::registerAdr (const string & address, const int & freeStorageSpace, const double & meanUptime, const double & meanIdleness) {
  	
    pthread_mutex_lock( stubMutex );
   
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushstring(state, "registerAdr");
    lua_gettable(state, -2);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushstring(state, address.c_str());       
    lua_pushnumber(state, freeStorageSpace);
    lua_pushnumber(state, meanUptime);
    lua_pushnumber(state, meanIdleness);
    
    if (lua_pcall(state, 5, 1, 0) != 0) {
      cerr << "[ERROR] CkpReposStub::registerAdr->Lua error: " << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }

    int adrId = (int)lua_tonumber(state, -1);
    lua_pop(state, 2);//removing proxy + result
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );
        
    return adrId;
  }

  //--------------------------------------------------------------------------------
  void AdrManagerStub::adrStatusChanged 
  (const int & adrId, const int & freeStorageSpaceChange, const double & meanUptimeChange, const double & meanIdlenessChange) {

    pthread_mutex_lock( stubMutex );
    
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushstring(state, "adrStatusChanged");
    lua_gettable(state, -2);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushnumber(state, adrId);         
    lua_pushnumber(state, freeStorageSpaceChange);    
    lua_pushnumber(state, meanUptimeChange);    
    lua_pushnumber(state, meanIdlenessChange);
    if (lua_pcall(state, 5, 0, 0) != 0){
      cerr << "[ERROR] AdrManagerStub::adrStatusChanged->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }
  
    lua_pop(state, 1);//removing proxy
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );    
  }
  
  //--------------------------------------------------------------------------------  
  void AdrManagerStub::setFragmentStored (const int & adrId, char * & fragmentKey, const int & keySize, const int & fragmentSize, const int & timeoutMinutes) {
	  
    pthread_mutex_lock( stubMutex );
        
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushstring(state, "setFragmentStored");
    lua_gettable(state, -2);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushnumber(state, adrId);
    lua_pushlstring(state, fragmentKey, keySize);
    lua_pushnumber(state, fragmentSize);
    lua_pushnumber(state, timeoutMinutes);

    if (lua_pcall(state, 5, 0, 0) != 0){
      cerr << "[ERROR] AdrManagerStub::setFragmentStored->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }
           
    lua_pop(state, 1);//removing proxy 
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );
  }

  //--------------------------------------------------------------------------------  
  void AdrManagerStub::setFragmentRemoved (const int & adrId, char * & fragmentKey, const int & keySize, const int & fragmentSize) {
	  
    pthread_mutex_lock( stubMutex );
        
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushstring(state, "setFragmentRemoved");
    lua_gettable(state, -2);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushnumber(state, adrId);
    lua_pushlstring(state, fragmentKey, keySize);
    lua_pushnumber(state, fragmentSize);

    if (lua_pcall(state, 4, 0, 0) != 0){
      cerr << "[ERROR] AdrManagerStub::setFragmentStored->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }
           
    lua_pop(state, 1);//removing proxy 
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );
  }

  //--------------------------------------------------------------------------------  
  void AdrManagerStub::setFragmentLeaseRenewed (const int & adrId, char * & fragmentKey, const int & keySize, const int & timeoutMinutes) {
	  
    pthread_mutex_lock( stubMutex );
        
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushstring(state, "setFragmentLeaseRenewed");
    lua_gettable(state, -2);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushnumber(state, adrId);
    lua_pushlstring(state, fragmentKey, keySize);
    lua_pushnumber(state, timeoutMinutes);

    if (lua_pcall(state, 4, 0, 0) != 0){
      cerr << "[ERROR] AdrManagerStub::setFragmentStored->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }
           
    lua_pop(state, 1);//removing proxy 
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );
  }

  //--------------------------------------------------------------------------------
  int AdrManagerStub::adrKeepAlive (const int & adrId) {

    pthread_mutex_lock( stubMutex );

    int stackTop = lua_gettop(state);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushstring(state, "adrKeepAlive");
    lua_gettable(state, -2);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushnumber(state, adrId);

    if (lua_pcall(state, 2, 1, 0) != 0){
      cerr << "[ERROR] AdrManagerStub::adrKeepAlive->Lua error: "
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
  vector<char *> AdrManagerStub::getFragmentRemovalList(int adrId, int keyLength) {

    pthread_mutex_lock( stubMutex );
    
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushstring(state, "getFragmentRemovalList");
    lua_gettable(state, -2);
    lua_getglobal(state, "adrManagerProxy");
    lua_pushnumber(state, adrId);       

    if (lua_pcall(state, 2, 1, 0) != 0){
      cerr << "[ERROR] CkpReposManagerStub::getFragmentRemovalList->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }

    vector<char *> fragmentKeyList = LuaUtils::extractOctetArraySequence(state, -1, keyLength);
    
    lua_pop(state, 2);//removing proxy + result
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );
        
    return fragmentKeyList;
  }
