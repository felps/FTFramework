#ifndef FILEMANIPULATIONNOTIFIER_HP_
#define FILEMANIPULATIONNOTIFIER_HP_

#include "utils/c++/AdrManagerStub.hpp"

#include <string>
using namespace std;

class FileManipulationNotifier
{

    int ckpReposId_;        
    string fifoName_;
    AdrManagerStub & adrManagerStub_;
        
    FileManipulationNotifier( AdrManagerStub & stub, const string & fifoName, int ckpReposId );       
    static void *run( void *ptr );
    
    void notifierLoop ();
    
public:
    virtual ~FileManipulationNotifier();
    
    static void launchFileManipulationNotifier( AdrManagerStub & stub, const string & fifoName, int ckpReposId );    
};

#endif /*FILEMANIPULATIONNOTIFIER_HPP_*/
