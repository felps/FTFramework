#ifndef AccessBrokerSkeleton_HPP
#define AccessBrokerSkeleton_HPP

#include "FileStorageServerInterface.hpp"
#include "FileRetrievalServerInterface.hpp"
#include "FileRemovalRenewalServerInterface.hpp"

#include "utils/c++/Config.hpp"
#include "utils/c++/OrbUtils.hpp"
#include "utils/c++/LuaUtils.hpp"
#include "utils/c++/StringUtils.hpp"

#include <string>
#include <iostream>
using namespace std;

class AccessBrokerSkeleton{

private:

    //Fields--------------------------------------------------------------------------
    struct lua_State * serverSideState; /**< lua_state representing the server side*/
    FileRetrievalServerInterface * fileRetrieval_;
    FileStorageServerInterface   * fileStorage_;
    FileRemovalRenewalServerInterface * fileRemoval_;
    string brokerIor_;
    static AccessBrokerSkeleton * singleInstance_; /**< singleton */

    //Methods-------------------------------------------------------------------------
    AccessBrokerSkeleton( FileRetrievalServerInterface *fileRetrieval, FileStorageServerInterface *fileStorage, FileRemovalRenewalServerInterface *fileRemoval, const Config & config );

    /**
     * Launches the OiL server.
     */
    static void * serverSideSetup(void * ptr);

    static int uploadFragments(struct lua_State * state);

    static int setFileStorageRequestCompleted(struct lua_State * state);
    
    static int setFileStorageRequestFailed(struct lua_State * state);

    static int downloadFragments(struct lua_State * state);

    static int setFileRetrievalRequestFailed(struct lua_State * state);
    
    static int removeFragments(struct lua_State * state);
    
    static int renewFragmentLeases(struct lua_State * state);

public:

    static AccessBrokerSkeleton & init( FileRetrievalServerInterface *fileRetrieval, FileStorageServerInterface *fileStorage, FileRemovalRenewalServerInterface *fileRemoval, const Config & config );
    
    static AccessBrokerSkeleton & singleInstance(){ return *AccessBrokerSkeleton::singleInstance_; }    
    const string & getIor() const{ return brokerIor_; }

};

#endif//AccessBrokerSkeleton_HPP

