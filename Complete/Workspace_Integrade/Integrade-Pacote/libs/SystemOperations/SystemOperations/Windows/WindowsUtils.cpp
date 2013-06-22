#include "WindowsUtils.hpp"
#include <Windows.h>

std::string WindowsUtils::getErrorDescription(int errorCode){

		char buf [ERROR_BUFFER_SIZE]; 
		ZeroMemory(buf, ERROR_BUFFER_SIZE);
		int foo = FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM
			| FORMAT_MESSAGE_IGNORE_INSERTS | FORMAT_MESSAGE_MAX_WIDTH_MASK,
			NULL, errorCode, NULL, buf, ERROR_BUFFER_SIZE, NULL);
			
		if(foo == 0)
			return std::string("Error description is unavailable");
		
		return std::string(buf);
}