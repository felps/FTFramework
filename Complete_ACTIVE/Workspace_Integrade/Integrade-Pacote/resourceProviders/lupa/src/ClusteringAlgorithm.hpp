#ifndef CLUSTERINGALGORITHM_HPP_
#define CLUSTERINGALGORITHM_HPP_

#include "UsageData.hpp"
#include "Cluster.hpp"
#include "LupaConstants.hpp"

#include <vector>

using namespace std;

class ClusteringAlgorithm {
public:

	virtual ~ClusteringAlgorithm() {};

	// analyzes unclassified data and returns a vector with prototypes for each cluster indentified 
	virtual vector<Cluster*> analyzeData (vector<UsageData*>* unclassifiedData, resource r) = 0;

	virtual Cluster* getClosestCluster(UsageData* ud, vector<Cluster*>* clusters, resource r, int startIndex, int endIndex);
	virtual double getDistanceElementToCluster(UsageData* ud, Cluster* cluster, resource r, int startIndex, int endIndex);
};

#endif /*CLUSTERINGALGORITHM_HPP_*/
