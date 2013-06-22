#ifndef LinuxSystemAnalyzer_HPP
#define LinuxSystemAnalyzer_HPP

#include "ResourceData.hpp"
#include "SystemAnalyzer.hpp"

class LinuxSystemAnalyzer : public SystemAnalyzer {
public:
	LinuxSystemAnalyzer();
	virtual ~LinuxSystemAnalyzer() {};
	
	virtual ResourceData getResourceData();

private:
	long int cpuUsage;
	long int uptime;

	void setValues (long *currentUsage, long *currentUptime);
	long getFreeMemory();
	char* getFileAsArray(const char *fileName);
	long getValueForToken (char *buffer, const char *token);
};

#endif
