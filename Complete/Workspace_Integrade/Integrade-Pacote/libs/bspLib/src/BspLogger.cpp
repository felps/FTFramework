#include "BspLogger.hpp"

#include <iostream>
using namespace std;

BspLogger::BspLogger() {
    out = new ofstream ("bsp.log", ios_base::app);
    //cout << "Logging bsp events at 'bsp.log'." << endl;
}

BspLogger::~BspLogger()
{
}

void BspLogger::debug(const string & message) {
    *out << "DEBUG: " << message << endl << flush;
}

BspLogger bspLogger;
