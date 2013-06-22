#include "SystemOperationException.hpp"
#include <sstream>


	std::string SystemOperationException::toString(){

		std::ostringstream buf;
		buf << "Code: ";
		buf << errorCode_;
		buf << " Description: ";
		buf << errorDescription_;
		return buf.str();
	}
