#ifndef CURRENTTIMESTAMPFACTORY_H_
#define CURRENTTIMESTAMPFACTORY_H_

#include "Timestamp.hpp"

class CurrentTimestampFactory {
public: 
	virtual ~CurrentTimestampFactory() {}
	virtual Timestamp getCurrentTimestamp();
};

#endif /*CURRENTTIMESTAMPFACTORY_H_*/
