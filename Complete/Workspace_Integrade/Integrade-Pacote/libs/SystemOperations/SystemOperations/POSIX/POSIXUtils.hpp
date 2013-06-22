#ifdef POSIX
#ifndef POSIXUTILS_HPP
#define POSIXUTILS_HPP

#include <string>

class POSIXUtils{

	public:

		static void printError(const std::string & functionName, const std::string & what);
		static void printError(const std::string & functionName, const std::string & what, const std::string & target);




};

#endif//POSIXUTILS_HPP
#endif//POSIX
