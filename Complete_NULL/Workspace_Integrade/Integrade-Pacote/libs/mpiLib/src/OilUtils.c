/* c libraries
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <unistd.h>

/* local libraries
 */
#include "OilUtils.h"
#include "OilOrb.h"
#include "LuaUtils.h"

/*
 */
int utilsInitLuaState(lua_State * state) 
{
	luaL_openlibs(state);

	luaopen_socket_core(state);
	return OIL_SUCCESS;
}

/*
 */
int utilsLoadOrb(lua_State * state)
{
	int initialStackSize = lua_gettop(state);
	luapreload_oilall(state);
	lua_pushliteral(state,"require");
	lua_rawget(state, LUA_GLOBALSINDEX);
	assert(lua_isfunction(state, -1));
	lua_pushliteral(state, "oil");
	
	if(lua_pcall(state, 1, 0, 0) != 0)
	{
		fprintf(stderr, "Lua error: oil.load -> %s\n", lua_tostring(state, -1));
		lua_pop(state, 1);
		return OIL_INTERNAL_ERR;
	}
	
	assert(initialStackSize == lua_gettop(state));
	return OIL_SUCCESS;
}

/*
 */
int utilsLoadIdl(lua_State * state, char * idlPath)
{
	int initialStackSize = lua_gettop(state);

	luaGetField(state, "oil.loadidlfile");
	lua_pushstring(state, idlPath);
	
	if (lua_pcall(state, 1, 0, 0) != 0)
	{
		fprintf(stderr, "Lua error: oil.loadidlfile -> %s\n", lua_tostring(state, -1));
		lua_pop(state, 1);
		return OIL_INTERNAL_ERR; 
	}

	assert(initialStackSize == lua_gettop(state));
	return OIL_SUCCESS;
}

/*
 */
int utilsInstantiateProxy(lua_State * state, char * ior, char * interfaceName, char * proxyName)
{
	int initialStackSize = lua_gettop(state);

	luaGetField(state,"oil.createproxy"); /*IOR on stack*/
	lua_pushstring(state, ior);
	lua_pushstring(state, interfaceName);

	if(lua_pcall(state, 2, 1, 0) != 0)
	{
		lua_getglobal(state, "tostring");
		lua_insert(state, -2);
		lua_pcall(state, 1, 1, 0);
		fprintf(stderr, "Lua error: oil.createproxy -> %s\n", lua_tostring(state, -1));
		lua_pop(state, 1);
		return OIL_INTERNAL_ERR;
	}
	else
		lua_setglobal(state, proxyName);
	
	assert(initialStackSize == lua_gettop(state));
	return OIL_SUCCESS;
}

/*
 */
int utilsGetIor(lua_State * state, const char * objectName, char * referencia)
{
	int initialStackSize = lua_gettop(state);

	lua_getglobal(state, objectName);
	lua_pushstring(state, "_ior");
	lua_gettable(state, -2);
	lua_getglobal(state, objectName);

	if(lua_pcall(state, 1, 1, 0) != 0)
	{
		fprintf(stderr, "Lua error: oil.getior -> %s\n", lua_tostring(state, -1));
		lua_pop(state, 1);
		return OIL_INTERNAL_ERR;
	}

	strcpy (referencia, lua_tostring(state, -1));

	lua_pop(state, 2);/*servant, ior*/
	assert(initialStackSize == lua_gettop(state));
	return OIL_SUCCESS;
}
