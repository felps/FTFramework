#include "UsageData.hpp"
#include "Cluster.hpp"

#include <vector>
#include <math.h>

UsageData::UsageData() {
	initialize();
}

UsageData::UsageData(Timestamp t, UsageData ud) {
	// the first part of the new object corresponds to the second part of the given UsageData
	initialize(t);
	int i;
	vector<ResourceData> oldData = ud.getData();
	for (i = 0; i < SAMPLES_PER_DAY; i++) {
		data[i] = oldData[i + SAMPLES_PER_DAY];	
	} 
}

UsageData::UsageData(Timestamp t) {
	initialize(t);
	//cerr << "UsageData::UsageData - Criou UsageData: " << date.formattedPrint() << endl;
}

void UsageData::initialize(Timestamp t) {
	initialize();
	date = t;
}

void UsageData::initialize() {
	data = vector<ResourceData>(2*SAMPLES_PER_DAY);
}

vector<ResourceData>& UsageData::getData() {
	return data;
}

Timestamp UsageData::getDate() {
	return date;	
}

void UsageData::simplePrint() {
	unsigned int i;
	cout << "UsageData (" << date.formattedPrint() << ")" << endl;
	for (i = 0; i < data.size(); i++) 
		if (data[i].isValid())
			cout << i << ": " << data[i] << endl; 
	
}

double UsageData::distance (UsageData *otherUsageData, resource r, int startIndex, int endIndex) {
	double distance = 0.0f;
	int i;
	
	/**
	 * EUCLIDEAN DISTANCE
	 * */
	
	for (i = startIndex; i < endIndex; i++) {
		//distance += pow (data[i].getResource(r) - otherUsageData->getData()[i].getResource(r), 2);	
	   distance += (data[i].getResource(r) - otherUsageData->getData()[i].getResource(r)) * (data[i].getResource(r) - otherUsageData->getData()[i].getResource(r));
	}
	
	return sqrt(distance);
}

// returns true if more than a given percentage of system datas are not estimates or invalid 
bool UsageData::isValid() {
	int invalid = 0;
	for (unsigned int i = 0; i < data.size(); i++)
		if (data[i].isEstimate() || !data[i].isValid()) invalid++;
	return (static_cast<float>(invalid)/data.size()) <= 1.0f - VALID_RESOURCE_DATA_THRESHOLD;
}

Cluster *UsageData::getCluster() {
	return cluster;
}

void UsageData::setCluster (Cluster *c) {
	cluster = c;
}


UsageData* UsageData::derivative() {
	UsageData *ud = new UsageData(this->getDate());
	vector<ResourceData>& d = ud->getData();

	for (unsigned int i = 1; i < d.size(); i++) {
		d[i] = data[i] - data[i-1];
		d[i].setEstimate(data[i].isEstimate() || data[i-1].isEstimate());
	}
	
	return ud;
}

UsageData* UsageData::meanCopy() {
	UsageData *ud = new UsageData(this->getDate());
	vector<ResourceData>& d = ud->getData();
	vector<resource>::iterator resourcesIterator;
	vector<resource> resources = getAvailableResources();
	vector<double> resourceAverages(resources.size());

	for (resourcesIterator = resources.begin(); resourcesIterator != resources.end(); resourcesIterator++) {
		resourceAverages[*resourcesIterator] = this->resourceAverage(*resourcesIterator);
	}
	
	for (unsigned int i = 0; i < d.size(); i++) {
		d[i] = ResourceData();
		for (resourcesIterator = resources.begin(); resourcesIterator != resources.end(); resourcesIterator++) {
			d[i].setResource(*resourcesIterator, data[i].getResource(*resourcesIterator) - resourceAverages[*resourcesIterator]);
		}
		d[i].setEstimate(data[i].isEstimate());
	}
	
	return ud;
}

double UsageData::resourceAverage (resource r) {
	return resourceAverage(r, 0, data.size());
}

double UsageData::resourceAverage (resource r, int startIndex, int endIndex) {
//	assert (startIndex < endIndex);
   double average = 0.0f;
   int count = 0;
   for (int i = startIndex; i < endIndex; i++)
      if (data[i].isValid()) {
         average += data[i].getResource(r);
	 count++;
      }
   
   if (count == 0) return 0.0f;
   return average / count;
}

void UsageData::goBackOneDay() {
   int i;
   
   vector<ResourceData> oldData = this->getData();
   initialize(getDate().addDays(-1));
   
   for (i = 0; i < SAMPLES_PER_DAY; i++) {
      data[i + SAMPLES_PER_DAY] = oldData[i];	
   } 
}
