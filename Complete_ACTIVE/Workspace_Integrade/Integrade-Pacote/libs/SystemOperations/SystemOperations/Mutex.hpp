#ifndef MUTEX_HPP
#define MUTEX_HPP

class Mutex{

public:

	virtual void lock() = 0;
	virtual void unlock() = 0;
	virtual bool trylock() = 0;
	virtual ~Mutex(){}

};
#endif//MUTEX_HPP
