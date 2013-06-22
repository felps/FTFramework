#ifndef CKPREPOSITORYUPDATER_HPP_
#define CKPREPOSITORYUPDATER_HPP_

#include "utils/c++/AdrManagerStub.hpp"
#include "CRDataServer.hpp"

class AdrStatusUpdater
{
private:
    int updateInterval;
    int ckpReposId;
    string outputDir;
    AdrManagerStub & adrManagerStub;
        
    AdrStatusUpdater(AdrManagerStub & stub, int ckpReposId, int updateInterval, const string & outputDir);       
    static void *run( void *ptr );
    
    void removeFiles(vector<char *> removalList);
    void updateLoop ();
    
public:
	virtual ~AdrStatusUpdater();
    
    static void launchCkpRepositoryUpdater( AdrManagerStub & stub, int ckpReposId, int updateInterval, const string & outputDir );    
};

#endif /*CKPREPOSITORYUPDATER_HPP_*/
