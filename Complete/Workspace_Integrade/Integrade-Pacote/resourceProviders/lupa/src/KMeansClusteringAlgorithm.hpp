#ifndef KMEANSCLUSTERINGALGORITHM_HPP_
#define KMEANSCLUSTERINGALGORITHM_HPP_

#include "ClusteringAlgorithm.hpp"
#include "Cluster.hpp"
#include "UsageData.hpp"
#include "LupaConstants.hpp"

class KMeansClusteringAlgorithm : public ClusteringAlgorithm {
public:
	vector<Cluster*> analyzeData (vector<UsageData*>* unclassifiedData, resource r);

};

#endif /*KMEANSCLUSTERINGALGORITHM_HPP_*/
