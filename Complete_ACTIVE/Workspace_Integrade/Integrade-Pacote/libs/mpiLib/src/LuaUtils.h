#ifndef LUAUTILS_H_
#define LUAUTILS_H_

/* lua libraries
 */
#include <lualib.h>
#include <lua.h>
#include <lauxlib.h>
#include <oilall.h>
#include <luasocket.h>

void luaGetField(lua_State * state, const char *name);

void luaSetFieldOnTable(struct lua_State * state, const char * key, const char * value, int tableIndexOnStack);

int luaConvertStackIndex(struct lua_State * state, int stackIndex);

int luaGetIntFromTable(struct lua_State * state, const char * key);

char * luaGetStringFromTable(struct lua_State * state, const char* key);

#endif /*LUAUTILS_H_*/
