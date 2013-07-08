#ifndef CLUSTER_HPP_
#define CLUSTER_HPP_

#include "UsageData.hpp"
#include "LupaConstants.hpp"

class Cluster {
public:
	Cluster();

	void addUsageData(UsageData* ud);
	UsageData *removeUsageData(UsageData *ud);
	UsageData& getRepresentativeElement();
	int getNumberOfElements();
	bool satisfiesResource (resource r, double minimum, double resourceOffset, int startIndex, int endIndex);
	double getResourceOffset (resource r, UsageData *recentUsageData, int startIndex, int endIndex);
	
private:
	vector<UsageData*> elements;
	UsageData representativeElement;
	
	void updateRepresentativeElement();
};

#endif /*CLUSTER_HPP_*/
