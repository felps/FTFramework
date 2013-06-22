//
// C++ Interface: Lupa
//
// Description: 
//
//
// Author: Danilo Conde <danconde@danconde-laptop>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//

#ifndef LUPA_HPP
#define LUPA_HPP

#include "UsagePredictor.hpp"
#include "RequestLogger.hpp"

#include <vector>

using namespace std;

class Lupa {
public:
	Lupa();

	bool canRunGridApplication(double freeCpuRequired = 0.0f, double freeMemoryRequired = 32/*000*/, int hours = DEFAULT_PREDICTION_HOURS);
	vector<double> getPrediction(resource r, int hours);

private:
	UsagePredictor *usagePredictor;
	RequestLogger *logger; 
};

#endif
