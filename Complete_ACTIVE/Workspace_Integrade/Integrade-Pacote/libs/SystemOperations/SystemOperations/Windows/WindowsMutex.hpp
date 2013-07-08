#ifdef WIN32
#ifndef WINDOWSMUTEX_HPP
#define WINDOWSMUTEX_HPP

//For TryEnterCriticalSection
#ifdef WIN32
#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x400
#endif
#endif

#include <Windows.h>

#include "Mutex.hpp"



class WindowsMutex: public Mutex{

public:

	void lock(){ EnterCriticalSection(&_criticalSection); }
	void unlock(){ LeaveCriticalSection(&_criticalSection); }
	bool trylock() { 
		if(TryEnterCriticalSection(&_criticalSection) == 0)
			return false;
		return true; 
	}
	WindowsMutex(){ 
		InitializeCriticalSection(&_criticalSection); }
	~WindowsMutex(){DeleteCriticalSection(&_criticalSection);  }

private:

	CRITICAL_SECTION _criticalSection;

};
#endif//WINDOWSMUTEX_HPP
#endif//WIN32
