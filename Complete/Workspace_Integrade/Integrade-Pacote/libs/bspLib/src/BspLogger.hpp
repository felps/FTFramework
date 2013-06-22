#ifndef BSPLOGGER_H_
#define BSPLOGGER_H_

#include <fstream>
#include <string>

using namespace std;

class BspLogger {
    
private:
    ofstream *out;
public:
	BspLogger();
	virtual ~BspLogger();
    
    void debug(const string & message);
};

extern BspLogger bspLogger;

#endif /*BSPLOGGER_H_*/
