#ifndef USAGEPREDICTOR_HPP_
#define USAGEPREDICTOR_HPP_

#include "ClusteringAlgorithm.hpp"
#include "DataCollector.hpp"
#include "ClusterAnalyzer.hpp"

using namespace std;

class UsagePredictor { 
public:

	UsagePredictor (ClusteringAlgorithm *clAlgorithm, DataCollector *dtCollector);
	//float minCpuRequired = 0.5f, long minMemoryRequired = 10240
	bool canRunGridApplication(Timestamp timestamp, ResourceData rd, int hours);
	bool canRunGridApplication(Timestamp timestamp, ResourceData rd, UsageData *recentDataSample, int hours);
	vector<double> getPrediction(Timestamp timestamp, resource r, int hours, UsageData *recentDataSample);
	vector<double> getPrediction(Timestamp timestamp, resource r, int hours);
		
private:
	ClusterAnalyzer *clusterAnalyzer;	
	DataCollector *dataCollector;
};

#endif /*USAGEPREDICTOR_HPP_*/
