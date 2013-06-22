#ifndef USAGEDATA_HPP_
#define USAGEDATA_HPP_

#include <string>
#include <vector>
#include "Timestamp.hpp"
#include "ResourceData.hpp"
#include "LupaConstants.hpp"

class Cluster;

using namespace std;

class UsageData {
public:
	UsageData();
	UsageData(Timestamp t);
	UsageData(Timestamp t, UsageData ud);
	//~UsageData();
	string description(); // useful when dealing with prototypic objects
	Timestamp getDate();  
	vector<ResourceData>& getData();
	double distance (UsageData *otherUsageData, resource r, int startIndex, int endIndex);
	bool isValid();
	void simplePrint();
	Cluster *getCluster();
	void setCluster(Cluster *c);
	UsageData* meanCopy();
	UsageData* derivative();
	double resourceAverage (resource r);
	double resourceAverage (resource r, int startIndex, int endIndex);
	void goBackOneDay();
	
private:
	vector<ResourceData> data;
	Timestamp date;
	Cluster *cluster;
	void initialize();
	void initialize(Timestamp t);
};

#endif /*USAGEDATA_HPP_*/
