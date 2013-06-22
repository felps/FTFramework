#ifndef MUTEXFACTORY_HPP
#define MUTEXFACTORY_HPP

#include "Mutex.hpp"

#ifdef WIN32
	#include "Windows/WindowsMutex.hpp"
#endif
#ifdef POSIX
	#include "POSIX/POSIXMutex.hpp"
#endif

class MutexFactory{

	public:

		static Mutex * createMutex(){
			#ifdef WIN32
				return new WindowsMutex();
			#endif
			#ifdef POSIX
				return new POSIXMutex();
			#endif
		}

};


#endif//MUTEXFACTORY_HPP
