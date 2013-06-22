#ifndef NAMESERVICESTUB_H_
#define NAMESERVICESTUB_H_

/* lua libraries
 */
#include <lualib.h>
#include <lauxlib.h>
#include <oilall.h>
#include <luasocket.h>

/* local bibraries
 */
#include "OilOrb.h"

typedef void * NAME_SERVICE;

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
int nspBind(OIL_ORB reference, char * nameNsProxy, char * name, char * servantName);

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
int nspUnbind(OIL_ORB reference, char * nameNsProxy, char * name);

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
int nspResolve(OIL_ORB orb, char * nameNsProxy, char * registeredName, char * reference);

#endif /*NAMESERVICESTUB_H_*/
