#ifdef POSIX
#ifndef POSIXCONDITIONVARIABLE_HPP
#define POSIXCONDITIONVARIABLE_HPP

#include <pthread.h>
#include "ConditionVariable.hpp"

class POSIXConditionVariable: public ConditionVariable{

		public:

			virtual void wait(){
			
			    pthread_mutex_lock(& _mutex);
					while (! _condition)
						pthread_cond_wait(&_conditionVariable, & _mutex);
					_condition = false;
				pthread_mutex_unlock(&_mutex);
			}

			virtual void signal(){
			
			    pthread_mutex_lock(&_mutex);
				    _condition = true;
					pthread_cond_signal(&_conditionVariable);
				pthread_mutex_unlock(&_mutex);
			}

			virtual void broadcast(){
			
				pthread_mutex_lock(&_mutex);
					_condition = true;
					pthread_cond_broadcast(&_conditionVariable);
				pthread_mutex_unlock(&_mutex);
			}

			POSIXConditionVariable(){
				_condition = false;
				pthread_mutex_init(&_mutex, NULL);
				pthread_cond_init(&_conditionVariable, NULL);
	
			}


			~POSIXConditionVariable(){}
		
		private:

			pthread_mutex_t  _mutex;
			pthread_cond_t _conditionVariable;
			bool _condition;
};

#endif//POSIXCONDITIONVARIABLE_HPP
#endif//POSIX
