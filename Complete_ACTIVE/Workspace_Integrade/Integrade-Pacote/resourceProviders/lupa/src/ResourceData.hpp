#ifndef RESOURCEDATA_HPP_
#define RESOURCEDATA_HPP_

#include "LupaConstants.hpp"
#include <iostream>
#include <vector>


using namespace std;

class ResourceData {
public:	
	ResourceData(double cpu = INVALID_DATA, double memory = INVALID_DATA);
	ResourceData(const ResourceData& rd);
	~ResourceData();
	
	double getCpuUsage() const;
	double getFreeMemory() const; // free memory in kB
	double getResource(resource r) const;
	void setResource(resource r, double value);
	bool isValid() const; 
	bool isEstimate() const;
	void setEstimate(bool value);
	
	friend ostream& operator<<(ostream &out, ResourceData s);
	friend istream& operator>>(istream &in, ResourceData& s);
	
	ResourceData operator+(ResourceData otherSd);
	ResourceData operator-(ResourceData otherSd);
	ResourceData operator/(int d);
	
private:
	double cpuUsage;
	double freeMemory;
	bool estimate;
};

vector<resource> getAvailableResources();
bool resourceSatisfiesMinimum(resource r, double value, double minimum);

#endif /*RESOURCEDATA_HPP_*/
