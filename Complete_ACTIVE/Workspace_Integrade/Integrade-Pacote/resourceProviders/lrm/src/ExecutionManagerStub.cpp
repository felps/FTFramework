#include <cassert>

extern "C" {
#include <lua.h>
#include <lualib.h>
#include <lauxlib.h>
}

#include "utils/c++/LuaUtils.hpp"
#include "utils/c++/OrbUtils.hpp"
#include "utils/c++/StringUtils.hpp"

#include "ExecutionManagerStub.hpp"

#include <iostream>
using std::cerr;
using std::endl; 


  //----------------------------------------------------------------------
  ExecutionManagerStub::ExecutionManagerStub(lua_State * aState,
                     const string & execManagerIor):state(aState){
    //FIXME: This assumes that state ALREADY loaded the IDL for the GRM. Due to
    //O2's current design it is not possible to load the GRM IDL separately.
    OrbUtils::instantiateProxy (state, execManagerIor, "IDL:clusterManagement/ExecutionManager:1.0",
                               "execManagerProxy");
  }

  //--------------------------------------------------------------------------------
  void ExecutionManagerStub::setProcessExecutionStarted
  (const string & applicationId, const string & processId, const string & lrmIor, 
   const int & restartId, const string & executionId) {

    int stackTop = lua_gettop(state);
    lua_getglobal(state, "execManagerProxy");
    lua_pushstring(state, "setProcessExecutionStarted");
    lua_gettable(state, -2);
    lua_getglobal(state, "execManagerProxy");
    lua_pushstring(state, lrmIor.c_str());       
    lua_pushstring(state, executionId.c_str()); 
    lua_pushnumber(state, restartId);
    lua_newtable(state);
    LuaUtils::setFieldOnTable(state, "requestId", applicationId.c_str(), -1);
    LuaUtils::setFieldOnTable(state, "processId", processId.c_str(), -1);
    if (lua_pcall(state, 5, 0, 0) != 0){
      cerr << "[ERROR] ExecutionManagerStub::setProcessExecutionStarted->Lua error: "
           << lua_tostring(state, -1) << endl;
      lua_pop(state, 1);
    }

    lua_pop(state, 1);//removing execManagerProxy
    assert(stackTop == lua_gettop(state));
  }

  //--------------------------------------------------------------------------------
  int ExecutionManagerStub::setProcessExecutionFinished
  (const string & applicationId, const string & processId, const int & restartId, 
		  int exitStatus, int executionState, const vector<string> & outputFiles) {

    int flag = -1;
    int stackTop = lua_gettop(state);
    lua_getglobal(state, "execManagerProxy");
    lua_pushstring(state, "setProcessExecutionFinished");
    lua_gettable(state, -2);
    lua_getglobal(state, "execManagerProxy");
    lua_pushnumber(state, restartId);

    lua_newtable(state);
    LuaUtils::setFieldOnTable(state, "requestId", applicationId.c_str(), -1);
    LuaUtils::setFieldOnTable(state, "processId", processId.c_str(), -1);

    // Puts the filenames in the array
    lua_newtable(state);
    for(unsigned int i = 0; i < outputFiles.size(); i++){
    	lua_pushstring(state, outputFiles[i].c_str());
    	lua_rawseti(state, -2, i+1);    	
    }
    
    lua_pushnumber(state, exitStatus);    
    lua_pushnumber(state, executionState);
    
    if (lua_pcall(state, 6, 1, 0) != 0){ // if (lua_pcall(state, 5, 1, 0) != 0){
       cerr << "[ERROR] ExecutionManagerStub::setProcessExecutionFinished->Lua error: "
            << lua_tostring(state, -1) << endl;
       lua_pop(state, 1); // removing error
    }
    else {
    	flag = (int)lua_tonumber(state, -1);
	lua_pop(state, 1); // removing result
    }

    lua_pop(state, 1);//removing asctProxy
    assert(stackTop == lua_gettop(state));

    return flag;
  }
