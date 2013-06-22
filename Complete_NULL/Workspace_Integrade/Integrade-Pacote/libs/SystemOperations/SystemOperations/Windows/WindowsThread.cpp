#include "WindowsThread.hpp"

	unsigned long __stdcall WindowsThread::WrapVoidFunction(void * functionHolder){

		THREAD_FUNCTION funct = 
			((FunctionHolder *)functionHolder)->function();

		void * arguments =
			((FunctionHolder *)functionHolder)->arguments();
			
		(*funct)(arguments);

		//delete functionHolder;
		return 0;
	}

	void WindowsThread::start(){

		_threadHandle = CreateThread(NULL,//no security attributes
						0,//Default stack size
						WindowsThread::WrapVoidFunction,//this is the function pointer
						functionHolder,//parameters
						0,//no creation flags, lauch thread after the function exits)
						NULL);//Do not "return" the thread id

		if(_threadHandle == NULL)
			throw SystemOperationException(GetLastError(), 
			"WindowsThread::WindowsThread: could not create thread. Error description: " +
			WindowsUtils::getErrorDescription(GetLastError()));
	}
