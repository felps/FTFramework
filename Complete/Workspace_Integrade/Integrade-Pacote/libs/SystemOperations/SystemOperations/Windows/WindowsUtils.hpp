#ifndef WINDOWSUTILS_HPP
#define WINDOWSUTILS_HPP

#include <string>

class WindowsUtils{

	private:

		static const int ERROR_BUFFER_SIZE = 1024;

	public:

		
		static std::string getErrorDescription(int errorCode);

};

#endif//WINDOWSUTILS_HPP
