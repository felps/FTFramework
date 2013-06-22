#ifndef REQUESTLOGGER_HPP
#define REQUESTLOGGER_HPP

/**
	@author Danilo Conde <danconde@danconde-laptop>
*/

#include "Timestamp.hpp"
#include "ResourceData.hpp"
#include "LupaConstants.hpp"
#include "RequestLog.hpp"

#include <fstream>

class RequestLogger {
public:
	RequestLogger(char* fileName = REQUEST_LOG_FILE_NAME);
	~RequestLogger();

	void log (Timestamp timestamp, ResourceData rd, bool answer);

private:
	ofstream *logFileOutputStream;
	char *logFileName;	
};

#endif
