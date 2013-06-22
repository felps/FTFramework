#include "ResourceData.hpp"

ResourceData::ResourceData(double cpu, double memory) 
	: cpuUsage(cpu), freeMemory(memory) {
	estimate = false;	
}

ResourceData::ResourceData(const ResourceData& rd) {
//	cerr << "ResourceData>>copy constructor" << endl;
	cpuUsage = rd.getCpuUsage();
	freeMemory = rd.getFreeMemory();
	estimate = rd.isEstimate();
}

 ResourceData::~ResourceData() {
//	cerr << "~ResourceData (" << this << ")" << endl;
 }

double ResourceData::getCpuUsage() const {
	return cpuUsage;
}

double ResourceData::getFreeMemory() const {
	return freeMemory;
}

double ResourceData::getResource(resource r) const {
	double (ResourceData::*resourceGetter)() const = NULL;
	
	switch (r) {
		case CPU_USAGE:		resourceGetter = &ResourceData::getCpuUsage; break;
		case FREE_MEMORY:	resourceGetter = &ResourceData::getFreeMemory; break;
	}
	
	return (this->*resourceGetter)();
}

ostream& operator<<(ostream& out, ResourceData s) {
	return (out << static_cast<float>(s.cpuUsage) << "\t" << static_cast<float>(s.freeMemory));
}

istream& operator>>(istream& in, ResourceData& s) {
	// using float because it seems to be the type that better converts strings to numbers
	float cpu;
	float memory;
	in >> cpu >> memory;
	s.cpuUsage = cpu;
	s.freeMemory = memory;
	
	return in;
}

// if there's no cpu information, no data was collected 
bool ResourceData::isValid() const {
	//return true; 
	return cpuUsage != INVALID_DATA; // porcooooo !	
}

bool ResourceData::isEstimate() const {
	return estimate;
}

void ResourceData::setEstimate(bool value) {
	estimate = value;
} 

ResourceData ResourceData::operator+(ResourceData otherSd) {
	return ResourceData(this->getCpuUsage() + otherSd.getCpuUsage(), 
					 this->getFreeMemory() + otherSd.getFreeMemory());
}

ResourceData ResourceData::operator-(ResourceData otherSd) {
	return ResourceData(this->getCpuUsage() - otherSd.getCpuUsage(),
			  this->getFreeMemory() - otherSd.getFreeMemory());
}

ResourceData ResourceData::operator/(int d) {
	return ResourceData(this->getCpuUsage() / d, 
			  this->getFreeMemory() / d);
}

vector<resource> getAvailableResources() {
	vector<resource> ret;
	ret.push_back(CPU_USAGE);
	ret.push_back(FREE_MEMORY);
	return ret;
}

void ResourceData::setResource(resource r, double value) {
	
	switch (r) {
		case CPU_USAGE:		cpuUsage = value; break;
		case FREE_MEMORY:	freeMemory = value; break;
	}
}

bool resourceSatisfiesMinimum(resource r, double value, double minimum) {
   if (r == CPU_USAGE) {
      if ((1.0f - value) < minimum)
         return false;
   } else {
      if (value < minimum)
         return false;
   }
   return true;  
}
