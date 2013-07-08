/* c libraries
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

/* local libraries
 */
#include "NameServiceStub.h"
#include "OilUtils.h"
#include "OilOrb.h"
#include "LuaUtils.h"

/* Realize bind to servant name previusly created in orb oil
 * 
 * Input param:
 * reference - reference to orb oil
 * nameNsProxy - the name of proxy comunicator wiht name service
 * name - publish name of the object
 * servantname - name of the object created (servant)
 * 
 * Return param:
 * error code
 */
int nspBind(OIL_ORB reference, char * nameNsProxy, char * name, char * servantName)
{
	int oil_err = OIL_UINIT_ERR;

	lua_State * state = oilLuaState(reference);
	int stackTop = lua_gettop(state);
	
	oil_err = OIL_SUCCESS;
	
	lua_getglobal(state, nameNsProxy);
	lua_pushstring(state, "bind");
	lua_gettable(state, -2);
	lua_getglobal(state, nameNsProxy);

	lua_newtable(state);
	lua_pushnumber(state,1);
	lua_newtable(state);
	luaSetFieldOnTable(state, "id", name, -1);
	luaSetFieldOnTable(state, "kind", "", -1);
	lua_settable(state,-3);

	lua_getglobal(state, servantName);    

	if (lua_pcall(state, 3, 0, 0) != 0){
		lua_getglobal(state, "tostring");
		lua_insert(state, -2);
		lua_pcall(state, 1, 1, 0);
		fprintf(stderr, "Lua error: oil.bind -> %s\n", lua_tostring(state, -1));
		lua_pop(state, 1);
		return OIL_INTERNAL_ERR;
	}
	
	lua_pop(state, 1);
	assert(stackTop == lua_gettop(state));
 	
	return oil_err;
}

/* Realize unbid of the object created in orb oil
 * 
 * Input param:
 * reference - reference to orb oil
 * nameNsProxy - the name of proxy comunicator wiht name service
 * name - publish name of the object
 * 
 * Return param:
 * error code
 */
int nspUnbind(OIL_ORB reference, char * nameNsProxy, char * name)
{
	int oil_err = OIL_UINIT_ERR;

	lua_State * state = oilLuaState(reference);
	int stackTop = lua_gettop(state);
	
	oil_err = OIL_SUCCESS;
	
	lua_getglobal(state, nameNsProxy);
	lua_pushstring(state, "unbind");
	lua_gettable(state, -2);
	lua_getglobal(state, nameNsProxy);
    
	lua_newtable(state);
	lua_pushnumber(state,1);
	lua_newtable(state);
	luaSetFieldOnTable(state, "id", name, -1);
	luaSetFieldOnTable(state, "kind", "", -1);
	lua_settable(state,-3);
    
	if (lua_pcall(state, 2, 0, 0) != 0)
	{
		lua_getglobal(state, "tostring");
		lua_insert(state, -2);
		lua_pcall(state, 1, 1, 0);
		fprintf(stderr, "Lua error: oil.unbid -> %s\n", lua_tostring(state, -1));	
		lua_pop(state, 1);
		return OIL_INTERNAL_ERR;
	}
 	
	lua_pop(state, 1);

	assert(stackTop == lua_gettop(state));
	
	return oil_err;
}

/* Resolve name of the object in service name especified
 * 
 * Input param:
 * reference - reference to orb oil
 * nameNsProxy - the name of proxy comunicator wiht name service
 * registeredName - publish name of the object
 * 
 * Output param:
 * reference - ior of the object referred
 * 
 * Return param:
 * error code
 */
int nspResolve(OIL_ORB orb, char * nameNsProxy, char * registeredName, char * reference)
{
	int oil_err = OIL_UINIT_ERR;

	lua_State * state = oilLuaState(orb);
	int stackTop = lua_gettop(state);
	int resolveReturnStatus;

	oil_err = OIL_SUCCESS;
	
	lua_getglobal(state, nameNsProxy);
	lua_pushstring(state, "resolve");
	lua_gettable(state, -2);
	lua_getglobal(state, nameNsProxy);
	
	lua_newtable(state);
	lua_pushnumber(state,1);
	lua_newtable(state);
	luaSetFieldOnTable(state, "id", registeredName, -1);
	luaSetFieldOnTable(state, "kind", "", -1);
	lua_settable(state,-3);
 
	resolveReturnStatus = lua_pcall(state, 2, 1, 0);
	
	if (resolveReturnStatus == 0) 
	{     
		luaGetField(state, "oil.ior.encode");
		lua_insert(state,-2); /*swap -1 and -2 position*/
    
		if (lua_pcall(state, 1, 1, 0) != 0)
		{
			lua_getglobal(state, "tostring");
			lua_insert(state, -2);
			lua_pcall(state, 1, 1, 0);
			fprintf(stderr, "Lua error: oil.ior.encode -> %s\n", lua_tostring(state, -1));  
			lua_pop(state, 1);
			return OIL_INTERNAL_ERR;
		}
        
        reference[0] = '\0';
        strcpy(reference, lua_tostring(state,-1));
		lua_pop(state,1);      
	}        
	else { /* Error resolving name!*/
		lua_getglobal(state, "tostring");
		lua_insert(state, -2);
		lua_pcall(state, 1, 1, 0);
		printf("Error: %s\n", lua_tostring(state, -1));
		fprintf(stderr, "Lua error: oil.resolve -> %s\n", lua_tostring(state, -1));
		lua_pop(state, 1);
		return OIL_INTERNAL_ERR;
	}
 
	lua_pop(state,1); /* Removes nameServerProxy from the stack*/
	assert(stackTop == lua_gettop(state));
	
	return oil_err;
}

