#ifdef WIN32
#ifndef WINDOWSTHREAD_HPP
#define WINDOWSTHREAD_HPP

//For TryEnterCriticalSection
#ifdef WIN32
#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x400
#endif
#endif


#include <Windows.h>

#include "Thread.hpp"
#include "SystemOperationException.hpp"
#include "WindowsUtils.hpp"


	class FunctionHolder{

		private:

			THREAD_FUNCTION _function;
			void * _arguments;

		public:

			FunctionHolder(THREAD_FUNCTION function, void * arguments) : 
			  _function(function), _arguments(arguments) {}

			THREAD_FUNCTION function(){ return _function; }
			void * arguments(){ return _arguments; }



	};

	class WindowsThread: public Thread{

		public:

			void detach(){}
			void exit(){ ExitThread(0); }
			void yield(){ SwitchToThread(); } 


			WindowsThread(THREAD_FUNCTION threadFunction, void * arguments){
				functionHolder = new FunctionHolder(threadFunction, arguments);
			}

			~WindowsThread(){ 
				CloseHandle(_threadHandle);
				delete functionHolder;
			}

			void start();

		private:

			HANDLE _threadHandle;
			FunctionHolder * functionHolder;

			static unsigned long __stdcall WrapVoidFunction(void * functionHolder);

	};

#endif//WINDOWSTHREAD_HPP
#endif//WIN32
