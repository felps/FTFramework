#ifndef OILORB_H_
#define OILORB_H_

#include "OilUtils.h"

/* Error state codes
 */
#define OIL_SUCCESS 0
#define OIL_PARAM_ERR 1
#define OIL_SYSTEM_ERR 2
#define OIL_INTERNAL_ERR 3
#define OIL_UINIT_ERR 4

/* Struct to map idl functions
 */
typedef struct IDLMAP_ {
	char * nameFunction;
	char ** paramNames;
	int paramNumber;
	lua_CFunction function;
} IDLMAP;
typedef IDLMAP * IDLMAP_t;

typedef void * OIL_ORB;

/*
 */
int oilSetMaxCreateOrbs(int x);

/* Returns the lua state of the orb oil
 */
lua_State * oilLuaState(OIL_ORB reference);

/* Load oil orb
 */
int oilLoadOrb(char * name, OIL_ORB * reference);

/* Load idl CORBA
 * 
 * idlPath - the idl path
 */
int oilLoadIdl(OIL_ORB reference, char * idlPath);

/* Create proxy
 * 
 * ior - reference to remote object
 * idlDescription - interface name of object in idl
 * proxyName - name proxy to object 
 */
int oilCreateProxy(OIL_ORB reference, char * ior, char * idlDescription, char * proxyName);

/* Create the remote object
 * 
 * nameOrb - orb name
 * nameServant - name of the remote object
 * servant - functions of the servant
 * f_number - number of functions in "servant"
 * idlDescription - interface name of object in idl
 * ref - output parameter, reference of the remote object created 
 */
int oilCreateServant(OIL_ORB reference, char * nameServant, IDLMAP_t servant, int f_number, char * idlDescription, char * ref);

/* Init oil orb server
 * 
 * tr - thread instantiate oil
 */
int oilListen(OIL_ORB reference, void ** tr);

/*
 */
int oilStop(OIL_ORB reference);

/* Test if oil orb is alive
 */
int oilIsAlive(OIL_ORB reference);

/*
 */
int oilDestroy(OIL_ORB reference);

/*
 */
int oilDestroyAll();

#endif /*OILORB_H_*/
