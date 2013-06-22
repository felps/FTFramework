#ifndef CLUSTERANALYZER_HPP_
#define CLUSTERANALYZER_HPP_

#include "ClusteringAlgorithm.hpp"
#include "DataCollector.hpp"

#include <vector>

using namespace std;

class ClusterAnalyzer {
public:
	ClusterAnalyzer(ClusteringAlgorithm *clAlgorithm, DataCollector *dtCollector);
	Cluster* findUsagePattern (resource r, int startIndex, int endIndex, UsageData* ud = NULL);
	vector< vector<Cluster*> > updateClusters(vector<UsageData*>* uds = NULL);
	vector< vector<Cluster*> > updateClustersIfNeeded(Timestamp timestamp);
	bool hasClusters();
	
private:
	ClusteringAlgorithm *clusteringAlgorithm;
	DataCollector *dataCollector;
	vector< vector<Cluster*> > usagePatterns;
	//Timestamp lastClusterUpdate;
	time_t lastClusterUpdate; // storing a time_t because the instatiation of new Timestamps may break the tests since they use the TestTimestampFactory

	void loadClustersFromFile();
	bool needsClusterUpdate(Timestamp timestamp);
	//void startUpdateThread();
};

#endif /*CLUSTERANALYZER_HPP_*/
