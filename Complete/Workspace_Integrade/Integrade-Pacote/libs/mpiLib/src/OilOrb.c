/* c libraries
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
 #ifdef WIN32
#include <windows.h>
#include <process.h>
 #else
#include <unistd.h>
#include <pthread.h>
 #endif 


/* local libraries
 */
#include "OilOrb.h"
#include "NameServiceStub.h"
#include "LuaUtils.h"

#define STOPING 0
#define RUNNING 1

/* Using WINDOWS
 */
 #ifndef WIN32
#define MUTEX pthread_mutex_t
#define LOCK_INIT(a) pthread_mutex_init(a, NULL)
#define LOCK pthread_mutex_lock
#define UNLOCK pthread_mutex_unlock
#define SLEEP usleep
#define THREAD pthread_t
#define DWORD void *
#define WINAPI 
#define LPVOID void *
#define T_FREE(a) pthread_exit(a)
 #else
#define MUTEX CRITICAL_SECTION
#define LOCK_INIT(a) InitializeCriticalSection(a)
#define LOCK EnterCriticalSection
#define UNLOCK LeaveCriticalSection
#define THREAD HANDLE
#define SLEEP Sleep
#define T_FREE(a) CloseHandle(*a)
 #endif
 
DWORD WINAPI serverSideSetupWrapper( LPVOID lpParam );

typedef struct OIL_
{
	lua_State * state;
	int status;
	THREAD * thread;
	char * name;
	MUTEX plock;
} OIL;
typedef OIL * OIL_t;

static int MAX_ORB = 10;
static OIL_t * orbs = NULL;
static int INDEX = 0;

/*
 */
int oilSetMaxCreateOrbs(int x) 
{
	if (x <= 0)
		return OIL_PARAM_ERR;
		
	MAX_ORB = x;
	return OIL_SUCCESS;
}

/*
 */
lua_State * oilLuaState(OIL_ORB reference)
{
	OIL_t orb;
	
	if (reference == NULL)
		return NULL;
	
	orb = (OIL_t) reference;
		
	return orb->state;
}

/*
 */
int oilLoadOrb(char * name, OIL_ORB * reference) 
{	
	int oil_err = OIL_SUCCESS;
	OIL_t orb = NULL;

	if (orbs == NULL)
	{
		orbs = (OIL_t*) malloc (sizeof(OIL_t) * MAX_ORB);
		INDEX = 0;
	}
	else if (INDEX >= MAX_ORB)
		return OIL_UINIT_ERR;
	
	orb = (OIL_t) malloc(sizeof(OIL));
	
	orb->state = luaL_newstate ();
	
	oil_err = utilsInitLuaState (orb->state);
	if (oil_err != OIL_SUCCESS)
		goto fail;
	
	oil_err = utilsLoadOrb(orb->state);
	if (oil_err != OIL_SUCCESS)
		goto fail;
		
	orb->status = STOPING;
	orb->thread = NULL;
	orb->name = name;
	*reference = orb;
	
	orbs[INDEX++] = orb;
	
 exit:
 	return oil_err;
 	
 fail:
 	free(orb);
 	orb = NULL;
 	goto exit;	
}

/*
 */
int oilLoadIdl(OIL_ORB reference, char * idlPath) 
{
	OIL_t orb;
	
	if (reference == NULL)
		return OIL_PARAM_ERR;
	
	orb = (OIL_t) reference;
	
	return utilsLoadIdl(orb->state, idlPath);
}

/*
 */
int oilCreateProxy(OIL_ORB reference, char * ior, char * interfaceName, char * proxyName)
{	
	OIL_t orb;
	
	if (reference == NULL)
		return OIL_PARAM_ERR;
	
	orb = (OIL_t) reference;
	
	return utilsInstantiateProxy(orb->state, ior, interfaceName, proxyName);
}

/*
 */
int oilCreateServant(OIL_ORB reference, char * nameServant, IDLMAP_t servant, int f_number, char * idlDescription, char * ref)
{
	char luaImpl[500] = "\0";
	char functionWrapper[500] = "\0";
	char params[500] = "\0";
	char aux[500] = "\0";
	int i, j;
	OIL_t orb;
	
	if (reference == NULL)
		return OIL_PARAM_ERR;
	
	orb = (OIL_t) reference;
	
	sprintf(aux, " %s = {", orb->name);
	strcat(luaImpl, aux);
	
	for (i = 0; i < f_number; i++) 
	{
		functionWrapper[0] = '\0';
		sprintf(functionWrapper, "%sWrapper", servant[i].nameFunction);

		lua_register(orb->state, functionWrapper, servant[i].function);
		
		params[0] = '\0';
		for (j = 0; j < servant[i].paramNumber; j++) 
		{
			if (j != 0)
				strcat(params, ", ");
			
			strcat(params, servant[i].paramNames[j]);
		}
		
		sprintf(aux, " %s = function (self, %s)   %s (%s) end,", servant[i].nameFunction, params, functionWrapper, params);
		strcat(luaImpl, aux);
	}
	
	strcat(luaImpl, " }");
	
	/*Servant Implementation*/
	luaL_dostring(orb->state, luaImpl);

	luaImpl[0] = '\0';
	sprintf(luaImpl, "%s = oil.createservant(%s,'%s')", nameServant, orb->name, idlDescription);
	
	luaL_dostring(orb->state, luaImpl);
	
	return utilsGetIor(orb->state, nameServant, ref);
}

/* 
 */
int oilListen(OIL_ORB reference, void ** tr)
{
	int err;
	OIL_t orb;
	
	if (reference == NULL)
		return OIL_PARAM_ERR;
	
	orb = (OIL_t) reference;
	
	if (!oilIsAlive(reference)) 
	{
 		THREAD thread;
		LOCK(&orb->plock);

 #ifndef WIN32
		err = pthread_create (&thread, NULL,(void * (*)(void *)) serverSideSetupWrapper, reference);
		if (err != 0)
			return OIL_SYSTEM_ERR;
		pthread_detach(thread);
 #else
 		thread = CreateThread( NULL, 0, serverSideSetupWrapper, reference, 0, NULL);  
    	if ( thread == NULL)
        	return OIL_SYSTEM_ERR;
 #endif
 		
 		LOCK(&orb->plock);
 		SLEEP(1000);
 		UNLOCK(&orb->plock);
 		
 		if (!oilIsAlive(reference))
 			return OIL_INTERNAL_ERR;
 			
 		orb->thread = &thread;
 		*tr = &thread;
	}
	
 	return OIL_SUCCESS;
}

/*
 */
DWORD WINAPI serverSideSetupWrapper( LPVOID reference )
{
	lua_State * state;
	OIL_t orb;
	
	orb = (OIL_t) reference;
	state = orb->state;
	luaGetField(state,"oil.run");
	orb->status = RUNNING;
	
	UNLOCK(&orb->plock);
	
	if(lua_pcall(state, 0, 0, 0) != 0) 
	{
		fprintf(stderr, "Lua error: oil.run -> %s\n", lua_tostring(state, -1));
		lua_pop(state, 1);
	}
	
	orb->status = STOPING;
	return NULL;
}


/* 
 */
int oilIsAlive(OIL_ORB reference)
{
	OIL_t orb;
	
	if (reference == NULL)
		return OIL_PARAM_ERR;
	
	orb = (OIL_t) reference;
	
	return (orb != NULL && orb->status);
}

/*
 */
int oilStop(OIL_ORB reference)
{
	OIL_t orb;
	
	if (reference == NULL)
		return OIL_PARAM_ERR;
	
	orb = (OIL_t) reference;
	
	T_FREE(orb->thread);
	return OIL_SUCCESS;
}

/*
 */
int oilDestroy(OIL_ORB reference)
{
	OIL_t orb;
	int i;
	
	if (reference == NULL)
		return OIL_PARAM_ERR;
	
	oilStop(reference);
	
	orb = (OIL_t) reference;
	
	lua_close(orb->state);
	
	for (i = 0; i < INDEX; i++)
		if (orbs[i] == orb)
			orbs[i] = NULL;
			
	free(orb);
	
	return OIL_SUCCESS;
}

/*
 */
int oilDestroyAll()
{	
	int i;
	
	for (i = 0; i < INDEX; i++) 
	{
		if (orbs[i] == NULL)
			continue;
	
		oilStop(orbs[i]);
	
		lua_close(orbs[i]->state);
		orbs[i] = NULL;
			
		free(orbs[i]);
	}
	
	return OIL_SUCCESS;
}
