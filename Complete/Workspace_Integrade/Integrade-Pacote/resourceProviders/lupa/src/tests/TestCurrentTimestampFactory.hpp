#ifndef TESTCURRENTTIMESTAMPFACTORY_HPP_
#define TESTCURRENTTIMESTAMPFACTORY_HPP_

#include "../CurrentTimestampFactory.hpp"
#include <vector>

using namespace std;

class TestCurrentTimestampFactory : public CurrentTimestampFactory {
public:
	virtual Timestamp getCurrentTimestamp();
	void setTimestampsToReturn(vector<Timestamp> sequence);
	
private:
	vector<Timestamp> sequenceToReturn;
	unsigned int currentPosition;
};

#endif /*TESTCURRENTTIMESTAMPFACTORY_HPP_*/
