#include "TestCurrentTimestampFactory.hpp"

void TestCurrentTimestampFactory::setTimestampsToReturn(vector<Timestamp> sequence) {
	sequenceToReturn = sequence;
	currentPosition = 0;
}

Timestamp TestCurrentTimestampFactory::getCurrentTimestamp() {
	if (currentPosition >= sequenceToReturn.size())
		return Timestamp();
	return sequenceToReturn.at(currentPosition++);
}
