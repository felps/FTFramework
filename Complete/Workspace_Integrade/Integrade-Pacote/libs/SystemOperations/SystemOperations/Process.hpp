#ifndef PROCESS_HPP
#define PROCESS_HPP

#include <string>
#include "ProcessDataTypes.hpp" 



class Process{

	public:

		virtual int killProcess() = 0;
		virtual ProcessStatus getProcessStatus() = 0;

		PROCESS_ID processID(){ return _processID; }
		void processID(PROCESS_ID aProcessID){ _processID = aProcessID; }

		virtual ~Process(){}; 

	private:

		PROCESS_ID _processID;

};

#endif//PROCESS_HPP
