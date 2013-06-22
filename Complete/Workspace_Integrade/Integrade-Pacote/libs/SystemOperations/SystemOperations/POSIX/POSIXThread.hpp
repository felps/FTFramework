#ifdef POSIX
#ifndef POSIXTHREAD_HPP
#define POSIXTHREAD_HPP

#include <pthread.h>
#include "Thread.hpp"

class POSIXThread: public Thread{

	public: 

		virtual void detach(){ pthread_detach(_thread); } 
		virtual void exit(){ pthread_exit(NULL); }
		virtual void yield() {pthread_yield(); } 
		virtual void start() {
			pthread_create(&_thread, 
				NULL,//creation attributes
				_threadFunction, 
				_arguments); 
		}

		POSIXThread(THREAD_FUNCTION threadFunction, void * arguments){
			_threadFunction = threadFunction;
			_arguments = arguments;
		}

		~POSIXThread(){}

	private:

		pthread_t _thread;
		THREAD_FUNCTION _threadFunction;
		void * _arguments;
};

#endif//POSIXTHREAD_HPP
#endif//POSIX
