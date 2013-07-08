#ifndef BROKERLOGGER_H_
#define BROKERLOGGER_H_

#include <fstream>
#include <string>

using namespace std;

class BrokerLogger {
    
private:
    ofstream *out;
public:
	BrokerLogger();
	virtual ~BrokerLogger();
    
    void debug(const string & message);
};

extern BrokerLogger brokerLogger;

#endif /*BROKERLOGGER_H_*/
