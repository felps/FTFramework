//
// C++ Implementation: Lupa
//
// Description: 
//
//
// Author: Danilo Conde <danconde@danconde-laptop>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//

#include "Lupa.hpp"
#include "KMeansClusteringAlgorithm.hpp"

#include "LupaConstants.hpp"

Lupa::Lupa() {
   /* Initializes the log filename with the hostname of the computer */
   initLupaLogFileName();
   
   logger = new RequestLogger();
	DataCollector *collector = new DataCollector();
	usagePredictor = new UsagePredictor(new KMeansClusteringAlgorithm(), collector);
	collector->startCollectingData();
}

bool Lupa::canRunGridApplication(double freeCpuRequired, double freeMemoryRequired, int hours) {
	ResourceData rd (freeCpuRequired, freeMemoryRequired);
	Timestamp timestamp;
   bool answer = usagePredictor->canRunGridApplication(timestamp, rd, hours);
	logger->log(timestamp, rd, answer);

	return answer;
}

vector<double> Lupa::getPrediction(resource r, int hours) {
   Timestamp timestamp;
   return usagePredictor->getPrediction(timestamp, r, hours);
}
