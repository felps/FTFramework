#ifndef WINDOWSPROCESS_HPP
#define WINDOWSPROCESS_HPP

#include "Process.hpp"
#include "ProcessDataTypes.hpp"
#include <string>

class WindowsProcess : public Process{

	public:

		//static int killProcess(PROCESS_ID pid);

		static Process * createProcess(const std::string & startupDirectory, const std::string & applicationPath, const std::string &  args);

		//static ProcessStatus getProcessStatus(PROCESS_ID pid);

		int killProcess();
		ProcessStatus getProcessStatus();

		~WindowsProcess();

	private:

		WindowsProcess(PROCESS_ID aProcessID){ processID(aProcessID); }

};

#endif//WINDOWSPROCESS_HPP