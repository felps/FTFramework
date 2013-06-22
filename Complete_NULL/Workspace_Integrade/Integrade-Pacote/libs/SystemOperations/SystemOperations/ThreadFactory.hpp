#ifndef THREADFACTORY_HPP
#define THREADFACTORY_HPP

#include "Thread.hpp"
#ifdef WIN32
#include "Windows/WindowsThread.hpp"
#endif
#ifdef POSIX
#include "POSIX/POSIXThread.hpp"
#endif

	class ThreadFactory{

		public: 

			static Thread * createThread(THREAD_FUNCTION threadFunction, void * arguments){
			#ifdef WIN32
				return new WindowsThread(threadFunction, arguments);
			#endif
			#ifdef POSIX
				return new POSIXThread(threadFunction, arguments);
			#endif
			}
	};
#endif//THREADFACTORY_HPP
