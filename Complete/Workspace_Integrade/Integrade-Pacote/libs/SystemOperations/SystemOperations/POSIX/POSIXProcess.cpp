#ifdef POSIX
#include "POSIXProcess.hpp"

#include "StringTokenizer.hpp"
#include "CharArrayArrayBeautifier.hpp"
#include "POSIXUtils.hpp"
#include "SystemOperationException.hpp"

#include <sys/stat.h>
#include <sys/wait.h>
#include <sstream>
#include <errno.h>

	int POSIXProcess::killProcess(){
	    int killStatus = kill(processID(), SIGTERM);
		if(killStatus != 0){
			POSIXUtils::printError("killProcess", "kill");
			std::ostringstream buf;
			buf << processID();
			throw SystemOperationException(errno, 
				"POSIXProcess::killProcess: could not kill process: " + buf.str() + ". Error description: " + strerror(errno));
		}
		return killStatus;
	}

	Process * POSIXProcess::createProcess(const std::string & startupDirectory, 
		const std::string & applicationPath, const std::string & args){

	    int pid = fork();

	    if (pid == 0) {  // child
	        nice(NICE_AMOUNT);

			//Step 1: chdir
			int chdirStatus = chdir(startupDirectory.c_str());
			if (chdirStatus == -1) {
				POSIXUtils::printError("createProcess", "chdir");
				throw SystemOperationException(errno, 
					"POSIXProcess::createProcess: could not chdir: " + startupDirectory + ". Error description: " + strerror(errno));
			}

			//Step 2: chmod +RWX the executable
			int chmodStatus =  chmod(applicationPath.c_str(), S_IRUSR | S_IWUSR | S_IXUSR);
			if (chmodStatus == -1) {
				POSIXUtils::printError("createProcess", "chmod");
				throw SystemOperationException(errno, 
					"POSIXProcess::createProcess: could not chmod: " + applicationPath + ". Error description: " + strerror(errno));
			}

			//Step 3: redirect STDOUT/STDERR. Close STDIO
			FILE * errfile = freopen("stderr", "w", stderr);
			FILE * outfile = freopen("stdout", "w", stdout);
			setvbuf(errfile, NULL, _IONBF, 0);
			setvbuf(outfile, NULL, _IONBF, 0);
			fclose(stdin);

			//Step 4: exec the application
			//TODO: Check if apppath can be the fullpath; otherwise we need to get the last token
	        StringTokenizer st(args);
		    CharArrayArrayBeautifier btf(st.countTokens() + 2);
       		btf.add(applicationPath.c_str());

			while (st.hasMoreTokens())
				btf.add(st.nextToken().c_str());

			int execStatus = execv(applicationPath.c_str(), btf.getArray());
		    if (execStatus == -1) {
				POSIXUtils::printError("createProcess", "exec");
				throw SystemOperationException(errno, 
					"POSIXProcess::createProcess: could not execv: " + applicationPath + ". Error description: " + strerror(errno));
			}
		} // child
    
		return new POSIXProcess(pid);
	}

	ProcessStatus POSIXProcess::getProcessStatus(){

		 int processStatus = 0;
		 int waitpidStatus = waitpid(processID(), &processStatus, WNOHANG);

		 if(waitpidStatus == 0)
			 return RUNNING;
		 if(waitpidStatus < 0){
			 std::ostringstream buf;
			 buf << processID();
			 POSIXUtils::printError("getProcessStatus", "waitpid", buf.str());
			 return UNKNOWN;
		 }
		 else{
			 if(WIFEXITED(processStatus))
				 return NORMAL_EXIT;
			 if(WIFSIGNALED(processStatus)){
				if((WTERMSIG(processStatus) == SIGKILL) || (WTERMSIG(processStatus) == SIGTERM))
					return KILLED;
				else
					return ABNORMAL_EXIT;
			 }
		 }

		 return UNKNOWN;
	}

#endif//POSIX
