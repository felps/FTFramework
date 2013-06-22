#include "KMeansClusteringAlgorithm.hpp"
#include <ostream>
#include <cstdlib>

//#define __K_DEBUG true

void printClusters(vector<Cluster*> clusters) {
	unsigned int i;
	for (i = 0; i < clusters.size(); i++)
		cerr << "[" << i << "]: " << clusters[i]->getNumberOfElements() << "\t";
	cerr << endl;
}


vector<Cluster*> KMeansClusteringAlgorithm::analyzeData (vector<UsageData*>* unclassifiedData, resource r) {
	vector<Cluster*> clusters;
	Cluster* cluster;
	UsageData* ud;
	int random;

	unsigned int i;
	// check if there are more than k unclassified objects
	
	// aleatorizacao dos dados
	srand(unsigned (time(0)));
	for (i = 0; i < unclassifiedData->size(); i++) {
		random = rand() % unclassifiedData->size();	
		ud = (*unclassifiedData)[i];
		(*unclassifiedData)[i] = (*unclassifiedData)[random];
		(*unclassifiedData)[random] = ud;
	}
	ud = NULL;

	// build k clusters with the first k unclassified objects
	for (i = 0; i < K; i++) {
		cluster = new Cluster();
		cluster->addUsageData(unclassifiedData->at(i));
		clusters.push_back(cluster);
	}

#ifdef __K_DEBUG
	cerr << "KMeans: first step" << endl;
	printClusters(clusters);
#endif
	
	// add the remaining n-k elements to the clusters
	while (i < unclassifiedData->size()) {
		cluster = getClosestCluster (unclassifiedData->at(i), &clusters, r, 0, 2 * SAMPLES_PER_DAY);
		cluster->addUsageData(unclassifiedData->at(i));  
		i++;
	} 
	
#ifdef __K_DEBUG
	cerr << "KMeans: second step" << endl;
	printClusters(clusters);
#endif
	
	
#ifdef __K_DEBUG
	cerr << "KMeans: third step" << endl;
#endif
	// move elements to their closest clusters until no more moves are needed
	int iterationsAllowed = 2 * unclassifiedData->size(); // avoids infinite loop when it does not converge
	bool converged = false;
	while (!converged && iterationsAllowed > 0) {
		converged = true;
		for (i = 0; i < unclassifiedData->size(); i++) {
			ud = unclassifiedData->at(i);  
			cluster = getClosestCluster(ud, &clusters, r, 0, 2 * SAMPLES_PER_DAY);
			if (cluster != ud->getCluster()) {
				ud->getCluster()->removeUsageData(ud);
				cluster->addUsageData(ud);
				converged = false;
			}
		}
		if (!converged) {
			iterationsAllowed--;
#ifdef __K_DEBUG
			printClusters(clusters);
#endif
		}
	}

#ifdef __K_DEBUG
	if (iterationsAllowed == 0)
		cout << "KMeans: maximum iterations allowed" << endl;
#endif	

#ifdef __DEBUG_OUTPUT
	//cout << "LUPA: " << r << " clusters = ";
	//printClusters(clusters);
#endif
	return clusters;
} 


#undef __K_DEBUG
