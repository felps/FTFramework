#ifdef POSIX

#include "POSIXUtils.hpp"

#include <iostream>
#include <errno.h>

	void POSIXUtils::printError(const std::string & functionName, const std::string & what){
		std::cerr << "POSIXOperations::"
				  << functionName
				  << ": "
				  << what 
				  << " failed. Error code: <"
				  << errno
				  << ">. Error Description: <"
				  << strerror(errno)
				  << ">"
				  << std::endl;
	}

	void POSIXUtils::printError(const std::string & functionName, const std::string & what, const std::string & target){
		std::cerr << "POSIXOperations::"
				  << functionName
				  << ": "
				  << what 
				  << " failed on <"
				  << target
				  << ">. Error code: <"
				  << errno
				  << ">. Error Description: <"
				  << strerror(errno)
				  << ">"
				  << std::endl;
	}
#endif
