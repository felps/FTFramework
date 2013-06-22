#ifndef CONDITIONVARIABLE_HPP
#define CONDITIONVARIABLE_HPP

class ConditionVariable{

	public:

		virtual void wait() = 0;
		virtual void signal() = 0;
		virtual void broadcast() = 0; 
		virtual ~ConditionVariable(){}

};


#endif//CONDITIONVARIABLE_HPP
