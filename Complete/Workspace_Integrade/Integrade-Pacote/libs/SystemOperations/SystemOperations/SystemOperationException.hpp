#ifndef SYSTEMOPERATIONEXCEPTION_HPP
#define SYSTEMOPERATIONEXCEPTION_HPP

#include <string>

class SystemOperationException{

	private:

		int errorCode_;
		std::string errorDescription_;

	public:
		
		int errorCode() { return errorCode_; }
		const std::string & errorDescription() { return errorDescription_; }

		SystemOperationException(const int aErrorCode, const std::string & aErrorDescription): 
		errorCode_(aErrorCode), errorDescription_(aErrorDescription){
		}

		std::string toString();
};

#endif//SYSTEMOPERATIONEXCEPTION_HPP
