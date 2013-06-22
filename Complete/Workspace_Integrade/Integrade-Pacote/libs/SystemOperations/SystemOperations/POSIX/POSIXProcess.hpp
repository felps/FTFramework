#ifdef POSIX
#ifndef POSIXPROCESS_HPP
#define POSIXPROCESS_HPP

#include "ProcessDataTypes.hpp"
#include "Process.hpp"

#include <string>

class POSIXProcess : public Process{

	private:

		static const int NICE_AMOUNT = 100;
		


		POSIXProcess(PROCESS_ID aProcessID){ processID(aProcessID); }


	public:

		int killProcess();

		static Process * createProcess(const std::string & startupDirectory, const std::string & applicationPath, const std::string &  args);

		ProcessStatus getProcessStatus();

};

#endif//POSIXPROCESS_HPP
#endif//POSIX
