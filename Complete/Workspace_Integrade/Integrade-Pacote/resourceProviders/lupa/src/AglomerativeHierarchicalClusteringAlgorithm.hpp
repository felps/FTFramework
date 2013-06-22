#ifndef AGLOMERATIVEHIERARCHICALCLUSTERINGALGORITHM_
#define AGLOMERATIVEHIERARCHICALCLUSTERINGALGORITHM_

#include "ClusteringAlgorithm.hpp"	

class AglomerativeHierarchicalClusteringAlgorithm : public ClusteringAlgorithm {
public:
	vector<UsageData> analyzeData (vector<UsageData*>* unclassifiedData);
};

#endif /*AGLOMERATIVEHIERARCHICALCLUSTERINGALGORITHM_*/
