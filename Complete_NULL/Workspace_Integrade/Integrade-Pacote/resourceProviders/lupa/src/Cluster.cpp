#include "Cluster.hpp"
#include "LupaConstants.hpp"

Cluster::Cluster() {
}

void Cluster::addUsageData(UsageData* ud) {
	// adds a new element and update the representative object
	
	elements.push_back(ud);
	ud->setCluster(this);
	//updateRepresentativeElement();

	// updates the representativeElement
	int numberOfElements = this->getNumberOfElements();
	if (numberOfElements == 1)
		representativeElement = *elements[0];
	else {
		vector<ResourceData>& data = representativeElement.getData();
	
		for (int j = 0; j < 2*SAMPLES_PER_DAY; j++)
			data[j] = data[j] + (ud->getData()[j] / numberOfElements) - (data[j]/ numberOfElements);
	}
	
}

UsageData* Cluster::removeUsageData(UsageData* ud) {
	// removes a given element and update the representative object
	unsigned int i = 0;
	while (elements[i] != ud && i < elements.size()) i++;
	if (elements[i] == ud) {  
		elements[i] = elements.back();
		elements.pop_back();
		//updateRepresentativeElement();
		// updates the representativeElement
		int numberOfElements = this->getNumberOfElements();
		if (numberOfElements == 0)
			representativeElement = UsageData();
		else {
			vector<ResourceData>& data = representativeElement.getData();
	
			for (int j = 0; j < 2*SAMPLES_PER_DAY; j++)
				data[j] = data[j] - (ud->getData()[j] / numberOfElements) + (data[j]/ numberOfElements);
		}

		return ud;
	}
	
	return NULL;
}


int Cluster::getNumberOfElements() {
	return elements.size();
}

UsageData& Cluster::getRepresentativeElement() {
	return representativeElement;
}


void Cluster::updateRepresentativeElement() {
	// deprecated !!!!
	int i, j, numberOfElements;
	numberOfElements = getNumberOfElements(); 
	
	if (numberOfElements == 0) {
		representativeElement = UsageData();
	} else {
		representativeElement = *elements[0]; 
		vector<ResourceData>& data = representativeElement.getData();
	
		for (j = 0; j < 2*SAMPLES_PER_DAY; j++) { 
			for (i = 1; i < numberOfElements; i++)
				data[j] = data[j] + elements[i]->getData()[j];
			data[j] = data[j] / numberOfElements;
		}
	}
}

bool Cluster::satisfiesResource (resource r, double minimum, double resourceOffset, int startIndex, int endIndex) {
	int j;
	vector<ResourceData>& data = representativeElement.getData();
	double resourceValue = resourceOffset;
	
   for (j = startIndex; j < endIndex; j++) {
		resourceValue += data[j].getResource(r);
		if (!resourceSatisfiesMinimum(r, resourceValue, minimum))
		   return false;
	}
	
	return true;	
}

double Cluster::getResourceOffset (resource r, UsageData *recentUsageData, int startIndex, int endIndex) {
	// start + 1 because the first element of the the predicted is invalid
	int correctedStartIndex = startIndex + ((startIndex == 0) ? 1 : 0);

	double recentUDAverage = recentUsageData->resourceAverage(r, correctedStartIndex, endIndex);
	double predictedAverage = this->getRepresentativeElement().resourceAverage(r, correctedStartIndex, endIndex);

	return recentUDAverage - predictedAverage;
}

