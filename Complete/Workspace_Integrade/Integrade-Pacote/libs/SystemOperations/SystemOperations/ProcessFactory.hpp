#ifndef PROCESSFACTORY_HPP 
#define PROCESSFACTORY_HPP

#ifdef WIN32
#include "Windows/WindowsProcess.hpp" 
#endif
#ifdef POSIX
#include "POSIX/POSIXProcess.hpp" 
#endif

#include <string>

	class ProcessFactory{

		public:

			static Process * createProcess(const std::string & startupDirectory, 
				const std::string & applicationPath, const std::string &  args){

			#ifdef WIN32
				return WindowsProcess::createProcess(startupDirectory, applicationPath, args);
			#endif
			#ifdef POSIX
				return POSIXProcess::createProcess(startupDirectory, applicationPath, args);
			#endif
			}

	};

#endif//PROCESSFACTORY_HPP 
