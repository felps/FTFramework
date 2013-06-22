#ifdef POSIX
#ifndef POSIXMUTEX_HPP
#define POSIXMUTEX_HPP

#include "Mutex.hpp"
#include <pthread.h>
#include <errno.h>

class POSIXMutex: public Mutex{

	public:

		void lock(){ pthread_mutex_lock(&_mutex); }
		void unlock(){ pthread_mutex_unlock(&_mutex); }
		bool trylock(){ 
			if(pthread_mutex_trylock(&_mutex) == EBUSY)
				return false;
			return true;
		}
		POSIXMutex(){ pthread_mutex_init(&_mutex, NULL); }
		~POSIXMutex(){ pthread_mutex_destroy(&_mutex); }

	private:

	pthread_mutex_t _mutex;

};
#endif//POSIXMUTEX_HPP
#endif//POSIX
