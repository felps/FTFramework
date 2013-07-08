#ifdef WIN32
#ifndef WINDOWSCONDITIONVARIABLE_HPP
#define WINDOWSCONDITIONVARIABLE_HPP

#include <Windows.h>
#include "ConditionVariable.hpp"

class WindowsConditionVariable: public ConditionVariable{

	public:
	
		WindowsConditionVariable::WindowsConditionVariable(){ _event = CreateEvent(NULL, true, false, NULL); }
		~WindowsConditionVariable(){ CloseHandle(_event); }
		void wait(){ 
			WaitForSingleObject(_event, INFINITE);
			ResetEvent(_event); 
		}
		void signal(){ SetEvent(_event); }
		void broadcast(){ SetEvent(_event); }


	private:

		HANDLE _event;

};


#endif//WINDOWSCONDITIONVARIABLE_HPP
#endif//WIN32
