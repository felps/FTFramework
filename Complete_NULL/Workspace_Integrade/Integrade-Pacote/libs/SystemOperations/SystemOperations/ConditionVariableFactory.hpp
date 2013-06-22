#ifndef CONDITIONVARIABLEFACTORY_HPP
#define CONDITIONVARIABLEFACTORY_HPP

#include "ConditionVariable.hpp"

#ifdef WIN32
	#include "Windows/WindowsConditionVariable.hpp"
#endif
#ifdef POSIX
	#include "POSIX/POSIXConditionVariable.hpp"
#endif

class ConditionVariableFactory{

	public:

		static ConditionVariable * createConditionVariable(){
			#ifdef WIN32
				return new WindowsConditionVariable();
			#endif
			#ifdef POSIX
				return new POSIXConditionVariable();
			#endif
		}
};


#endif//CONDITIONVARIABLEFACTORY_HPP
