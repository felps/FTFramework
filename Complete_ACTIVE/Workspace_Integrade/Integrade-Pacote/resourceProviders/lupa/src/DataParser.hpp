#ifndef DATAPARSER_HPP_
#define DATAPARSER_HPP_

#include "UsageData.hpp"

class DataParser {
public:
	vector<UsageData*> *parseFile (const char* fileName);
	vector<UsageData*> *parseFile (const char* fileName, Timestamp initial);
};

#endif /*DATAPARSER_HPP_*/
