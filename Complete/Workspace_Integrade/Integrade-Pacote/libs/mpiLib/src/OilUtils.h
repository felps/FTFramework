#ifndef OILUTILS_H_
#define OILUTILS_H_

/* lua libraries
 */
#include <lualib.h>
#include <lauxlib.h>
#include <oilall.h>
#include <luasocket.h>

/* Init lua state loding required libraries
 * Enviroment variable "LUA_HOME" have necessary
 * 
 * state - lua state
 * 
 * Code errors:
 * OIL_SYSTEM_ERR - LUA_HOME not defined
 */
int utilsInitLuaState(lua_State * state);

int utilsLoadOrb(lua_State * state);

int utilsLoadIdl(lua_State * state, char * idlPath);

int utilsInstantiateProxy(lua_State * state, char * ior, char * interfaceName, char * proxyName);

int utilsGetIor(lua_State * state, const char * objectName, char * referencia);

#endif /*OILUTILS_H_*/
