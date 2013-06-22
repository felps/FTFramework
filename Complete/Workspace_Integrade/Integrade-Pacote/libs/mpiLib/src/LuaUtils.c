/* c libraries
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* local libraries
 */
#include "LuaUtils.h"

/*
 */
void luaGetField(lua_State * state, const char * name) {
	const char *end = strchr(name, '.');
	lua_pushvalue(state, LUA_GLOBALSINDEX);
	
	while (end) 
	{
		lua_pushlstring(state, name, end - name);
		lua_gettable(state, -2);
		lua_remove(state, -2);
		
		if (lua_isnil(state, -1)) 
			return;
			
		name = end+1;
		end = strchr(name, '.');
	}
	
	lua_pushstring(state, name);
	lua_gettable(state, -2);
	lua_remove(state, -2);
}

/*
 */
void luaSetFieldOnTable(struct lua_State * state, const char * key, const char * value, int tableIndexOnStack)
{
	int tmpIndex = luaConvertStackIndex(state, tableIndexOnStack);
	lua_pushstring(state, key);
	lua_pushstring(state, value);
	lua_settable(state, tmpIndex);
}

/*
 */
int luaConvertStackIndex(struct lua_State * state, int stackIndex)
{
	int tmpIndex = stackIndex;
	
	if(tmpIndex < 0)
		tmpIndex = lua_gettop(state) + tmpIndex + 1;
		
	return tmpIndex;
}

int luaGetIntFromTable(struct lua_State * state, const char * key)
{
	int value;
	
	lua_pushstring(state, key);
	lua_gettable(state, -2);
	value = (int) lua_tonumber(state,-1);
	lua_pop(state, 1);
	return value;
}

char * luaGetStringFromTable(struct lua_State * state, const char* key)
{
	char * str;
	lua_pushstring(state, key);
	lua_gettable(state, -2);
	str = (char*) malloc(sizeof(char) * (lua_strlen(state,-1) + 1));
	strcpy(str, lua_tostring(state,-1));
	lua_pop(state, 1);
	return str;
}
