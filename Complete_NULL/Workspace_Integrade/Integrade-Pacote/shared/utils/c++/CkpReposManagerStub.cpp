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

#include "CkpReposManagerStub.hpp"

#include <iostream>
#include <fstream>
using std::cerr;
using std::endl; 
using std::ifstream; 

  //----------------------------------------------------------------------
  CkpReposManagerStub::CkpReposManagerStub(const Config & config) {

    string serverNameLocation;
    string cosNamingIdlPath;   
    string orbPath;
    string ckpReposManagerIdl;
    try {
        serverNameLocation = config.getConf("serverNameRef");
        cosNamingIdlPath = config.getConf("cosNamingIdlPath");   
        orbPath = config.getConf("orbPath");
        ckpReposManagerIdl = config.getConf("ckpReposManagerIdlPath");       
    }
    catch (NoSuchConfigException e) {
        cerr << "[CRITICAL] CkpReposManagerStub::CkpReposManagerStub -> field from config file not found! Exiting..." << endl;
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
    string ckpReposManagerIor = nameServiceStub.resolve("CkpReposManager");
    if ( ckpReposManagerIor.compare("") == 0 ) {
        cerr << "[CRITICAL] CkpReposStub::CkpReposStub -> Could not resolve CkpReposManager. Exiting..." << endl;
        exit(-1);
    }
    
    OrbUtils::instantiateProxy (ckpReposManagerState, ckpReposManagerIor, "IDL:clusterManagement/CkpReposManager:1.0", "ckpReposManagerProxy");
  
    state = ckpReposManagerState;
    
    stubMutex = new pthread_mutex_t;
    pthread_mutex_init(stubMutex, NULL);

  }

  //--------------------------------------------------------------------------------
  void CkpReposManagerStub::setCheckpointStored( const string & executionId, const string & checkpointKey, int checkpointNumber ) {

    pthread_mutex_lock( stubMutex );
    
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "ckpReposManagerProxy");
    lua_pushstring(state, "setCheckpointStored");
    lua_gettable(state, -2);
    lua_getglobal(state, "ckpReposManagerProxy");    
    lua_pushstring(state, executionId.c_str());       
    lua_pushstring(state, checkpointKey.c_str());
    lua_pushnumber(state, checkpointNumber);    
    if (lua_pcall(state, 4, 0, 0) != 0){
      cerr << "[ERROR] CkpReposStub::notifyCkpStored->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }
  
    lua_pop(state, 1);//removing proxy
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );    
  }
  
  //--------------------------------------------------------------------------------
  CkpInfo CkpReposManagerStub::getCheckpointingInformation(const string & executionId) {

    pthread_mutex_lock( stubMutex );
    
    //cerr << "getCheckpointingInformation" << endl;
    
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "ckpReposManagerProxy");
    lua_pushstring(state, "getCheckpointingInformation");
    lua_gettable(state, -2);
    lua_getglobal(state, "ckpReposManagerProxy");
    lua_pushstring(state, executionId.c_str());       

    //cerr << "getCheckpointingInformation CALLING" << endl;

    if (lua_pcall(state, 2, 1, 0) != 0) {
      cerr << "[ERROR] CkpReposManagerStub::getCheckpointingInformation->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }

    //cerr << "getCheckpointingInformation RESPONSE" << endl;
    
    CkpInfo ckpInfo;        
    
    lua_pushstring(state, "checkpointKey");
    lua_gettable(state, -2);
    ckpInfo.checkpointKey = LuaUtils::extractStringSequence(state, -1);
    lua_pop(state, 1);

    lua_pushstring(state, "checkpointNumber");
    lua_gettable(state, -2);
    ckpInfo.checkpointNumber = LuaUtils::extractIntegerSequence(state, -1);
    lua_pop(state, 1);
        
    //cerr << "getCheckpointingInformation OK1" << endl;
            
    lua_pop(state, 2);//removing proxy + result
    assert(stackTop == lua_gettop(state));
    
    pthread_mutex_unlock( stubMutex );

    //cerr << "getCheckpointingInformation OK2" << endl;
        
    return ckpInfo;
  }

  //--------------------------------------------------------------------------------
