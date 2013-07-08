#include "BrokerLogger.hpp"

#include <iostream>
using namespace std;

BrokerLogger::BrokerLogger() {
    out = new ofstream ("broker.log", ios_base::app);
    //cout << "Logging broker events at 'broker.log'." << endl;
}

BrokerLogger::~BrokerLogger()
{
}

void BrokerLogger::debug(const string & message) {
    *out << "DEBUG: " << message << endl << flush;
}

BrokerLogger brokerLogger;
