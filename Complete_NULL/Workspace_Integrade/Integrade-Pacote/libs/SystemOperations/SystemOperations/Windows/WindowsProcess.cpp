#include "WindowsProcess.hpp"
#include <Windows.h>
#include "WindowsUtils.hpp"
#include <tchar.h>
#include <sstream>
#include <iostream>
#include "SystemOperationException.hpp"

	int WindowsProcess::killProcess(){
	
		int returnCode = TerminateProcess(processID(), 1);
		if(returnCode != 0)
			returnCode = 0;
		else
			returnCode = GetLastError();
		
		//TODO: alguem usa ReturnCode? Mudar para exception?
		return returnCode;
	}

	//INFO: This assumes that
	//Startup directory exists
	Process * WindowsProcess::createProcess(const std::string & startupDirectory, const std::string & applicationPath, const std::string & args){

		//DONE: We need to standardize on wether user char * or char ** for the arguments. 
		//Windows need char *, linux need char **. Maybe we will use char ** and 
		//DONE: We need to decide if the APIs will be char * based or std::string based

		STARTUPINFO startupInfo;
		PROCESS_INFORMATION processInfo;
		ZeroMemory( &processInfo, sizeof(processInfo));
		ZeroMemory(&startupInfo, sizeof(startupInfo));
		startupInfo.cb = sizeof(startupInfo);

		
		

		SECURITY_ATTRIBUTES securityAttr;
		securityAttr.bInheritHandle = TRUE;
		securityAttr.lpSecurityDescriptor = NULL;
		securityAttr.nLength = sizeof(SECURITY_ATTRIBUTES);

		//Redirect IO to STDOUT and STDERR files
		startupInfo.dwFlags = STARTF_USESTDHANDLES;
		std::stringstream filepathBuffer;
		filepathBuffer << startupDirectory;
		filepathBuffer << "\\stderr";

		HANDLE stderrHandle =
			CreateFile(filepathBuffer.str().c_str(),//PATH
			FILE_WRITE_DATA,//Read only
			FILE_SHARE_READ,//Allow sharing in read mode
			&securityAttr,//No security attributes
			CREATE_ALWAYS,//Create even if it exists 
			FILE_FLAG_WRITE_THROUGH,
			NULL);

		if(stderrHandle == INVALID_HANDLE_VALUE)
			throw SystemOperationException(GetLastError(), 
			"WindowsProcess::createProcess: could not redirect the standard error output. \
			Error description: " + WindowsUtils::getErrorDescription(GetLastError()));
			


		filepathBuffer.str("");

		filepathBuffer << startupDirectory;
		filepathBuffer << "\\stdout";

		HANDLE stdoutHandle =
			CreateFile(filepathBuffer.str().c_str(),//PATH
			FILE_WRITE_DATA,//Read only
			FILE_SHARE_READ,//Allow sharing in read mode
			&securityAttr,//No security attributes
			CREATE_ALWAYS,//Create even if it exists 
			FILE_FLAG_WRITE_THROUGH,
			NULL);

		if(stdoutHandle == INVALID_HANDLE_VALUE)
			throw SystemOperationException(GetLastError(), 
			"WindowsProcess::createProcess: could not redirect the standard output. \
			Error description: " + WindowsUtils::getErrorDescription(GetLastError()));

		startupInfo.hStdInput = GetStdHandle(STD_INPUT_HANDLE);
		startupInfo.hStdError = stderrHandle;
		startupInfo.hStdOutput = stdoutHandle;
		
		startupInfo.dwFlags = STARTF_USESTDHANDLES;

		
		BOOL result = CreateProcess((char *)applicationPath.c_str(),//Path to the executable
			(char *)args.c_str(),//Command line (Just the arguments)
			NULL,//No security attributes
			NULL,//No thread attributes
			TRUE,//Needed for STDOUT/STDERR redirection
			0,//No priority specification
			NULL,//No environment
			(char *)startupDirectory.c_str(),
			&startupInfo,
			&processInfo);

		if(result == 0)
			throw SystemOperationException(GetLastError(), 
			"WindowsProcess::createProcess: could not create process. \
			Error description: " + WindowsUtils::getErrorDescription(GetLastError()));
			

		WindowsProcess * windowsProcess = new WindowsProcess(processInfo.hProcess);
		return windowsProcess;
	}

	WindowsProcess::~WindowsProcess(){
		CloseHandle(processID());
	}

	ProcessStatus WindowsProcess::getProcessStatus(){
		unsigned long code;
		BOOL status = GetExitCodeProcess(processID(), &code);
		switch(code){
			case STILL_ACTIVE:
				return RUNNING;
				break;
			case 0:
				return NORMAL_EXIT;
				break;
			case 1:
				return KILLED;
				break;
			default:
				return ABNORMAL_EXIT;
				break;
		}
	}