#ifndef SystemAnalyzer_HPP
#define SystemAnalyzer_HPP

#include "ResourceData.hpp"

class SystemAnalyzer {
public:
	//TODO: arrumar um jeito melhor de definir esse destrutor sem dar warning
	virtual ~SystemAnalyzer() {}; 

	/** Returns the average usage of the last COLLECT_INTERVAL seconds */
	virtual ResourceData getResourceData() = 0;

};

#endif
