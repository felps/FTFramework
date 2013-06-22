//
// C++ Implementation: RequestLogger
//
// Description: 
//
//
// Author: Danilo Conde <danconde@danconde-laptop>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "RequestLogger.hpp"

RequestLogger::RequestLogger(char *fileName) {
	logFileName = fileName;
	logFileOutputStream = new ofstream(logFileName, ios::app); /* opens the file in append mode */
}


RequestLogger::~RequestLogger() {
	logFileOutputStream->close();
}


void RequestLogger::log(Timestamp t, ResourceData rd, bool answer) {
	RequestLog rl(t, rd, answer);
	*logFileOutputStream << rl << endl;
}
