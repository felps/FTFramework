#include "ClusteringAlgorithm.hpp"

Cluster* ClusteringAlgorithm::getClosestCluster(UsageData* ud, vector<Cluster*>* clusters, resource r, int startIndex, int endIndex) {
	Cluster *closest = NULL;
	double minDistance = 0.0f, distance;
	vector<Cluster*>::iterator iterator;

//	assert (clusters != NULL);
//	assert (clusters->size() > 0);
	
	for (iterator = clusters->begin(); iterator != clusters->end(); iterator++) {
		distance = getDistanceElementToCluster (ud, *iterator, r, startIndex, endIndex);
		if (distance < minDistance || closest == NULL) {
			minDistance = distance;
			closest = *iterator;
		}
	}
	
	return closest;
}

double ClusteringAlgorithm::getDistanceElementToCluster(UsageData* ud, Cluster* cluster, resource r, int startIndex, int endIndex) {
	return ud->distance (&cluster->getRepresentativeElement(), r, startIndex, endIndex);
}
