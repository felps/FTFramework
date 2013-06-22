#ifndef THREAD_HPP
#define THREAD_HPP

#ifdef WIN32
#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x400
#endif
#endif

#ifdef WIN32
#include <Windows.h>
#endif

#ifdef POSIX
#include <pthread.h>
#include <time.h>
#endif

#ifdef WIN32
	typedef void * (*THREAD_FUNCTION)(void *);
	//typedef unsigned long (__stdcall *THREAD_FUNCTION)(void *);
	//typedef void * (__stdcall *THREAD_FUNCTION)(void *);
#endif
#ifdef POSIX
	//typedef unsigned long (*THREAD_FUNCTION)(void *) __attribute__((stdcall));
	typedef void * (*THREAD_FUNCTION)(void *);
#endif


	class Thread{

		public: 

			virtual void detach() = 0;
			virtual void exit() = 0;
			virtual void yield() = 0;
			virtual void start() = 0;
			virtual ~Thread(){}

			static void sleep(int seconds){
				#ifdef WIN32
					Sleep(seconds * 1000);
				#endif
				#ifdef POSIX
						
					struct timespec tspec;
					tspec.tv_sec = seconds;
					tspec.tv_nsec = 0;

					nanosleep(&tspec, NULL);
				#endif
			}
	};

#endif//THREAD_HPP
