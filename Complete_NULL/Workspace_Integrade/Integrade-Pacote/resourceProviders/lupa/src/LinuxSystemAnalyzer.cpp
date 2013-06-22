#include <fstream>
#include <string>
#include <stdio.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <stdlib.h>

#include "LinuxSystemAnalyzer.hpp"

using namespace std;

LinuxSystemAnalyzer::LinuxSystemAnalyzer() {
	setValues (&cpuUsage, &uptime);
}

void LinuxSystemAnalyzer::setValues (long *currentUsage, long *currentUptime) {
   string trash;
   long user, nice, system, idle;
   ifstream ifs("/proc/stat", ios::in);

   ifs >> trash;
   ifs >> user;
   ifs >> nice;
   ifs >> system;
   ifs >> idle;

   ifs.close();

   *currentUsage = user + nice + system;
   *currentUptime = *currentUsage + idle;
}

long LinuxSystemAnalyzer::getFreeMemory() {
   string type, trash;
   long free = 0, cached = 0, buffers = 0;
   /*ifstream ifs("/proc/meminfo", ios::in);
	//" TotalFreeMemory = MemFree + Buffers + Cached (buffers and cache will be unallocated as the system needs more memory)"
   while (!ifs.eof() || !ifs.fail()) { // && (free == -1 || cached == -1 || buffers == -1)) {
	   // NOTE: ifs.eof() doesn't work properly on some system, such as fedora core 7, that's why there's also a fail()
	   ifs >> type;
	   ifs >> amount;
	   ifs >> trash;
	   // it's not assumed that /proc/meminfo information comes always in the same order 
	   if (type.compare("MemFree:") == 0) {
	   	free = amount;
	   }
	   if (type.compare("Buffers:") == 0) {
	   	buffers = amount;
	   }
	   if (type.compare("Cached:") == 0) {
	   	cached = amount;
	   }
	}
	//cout << free + buffers + cached << endl;
   
   	ifs.close();
	
*/

   char *buffer = getFileAsArray ("/proc/meminfo");
   free = getValueForToken(buffer, "MemFree:");
   buffers = getValueForToken(buffer, "Buffers:");
   cached = getValueForToken(buffer, "Cached:");

   delete [] buffer;
   
   //cout << free << ":" << buffers << ":" << cached << endl;
   
   return free + buffers + cached;
   
}

long LinuxSystemAnalyzer::getValueForToken (char *buffer, const char *token) {
	// return the long value right after the token in the given buffer
	char *p = strstr (buffer, token);
	long value;
	while (isspace(*p)) p++; while (*p && !isspace(*p)) p++; while (isspace(*p)) p++; // skips token
	sscanf(p, "%ld", &value);
	return value;
}

char* LinuxSystemAnalyzer::getFileAsArray(const char *filename) {
	int length;
	int fd;
	char *buffer;
	buffer = (char*) malloc (sizeof(char) * 4096);
	fd = open(filename, O_RDONLY);
	if ( fd < 0 ) {
		cerr << "cannot read: " << filename << endl;
		return 0;
	}
 	length = read(fd, buffer, 4096);
	if (length <= 0) {
		return 0;
  	}
	close(fd);
	
	buffer[length] = '\0';
	return buffer;
}

ResourceData LinuxSystemAnalyzer::getResourceData() {
	long currentCpuUsage, currentUptime;
	float usageAverage;

	setValues (&currentCpuUsage, &currentUptime);
	
	// returns an empty(invalid) system data
	if (currentUptime - uptime == 0) 
		return ResourceData();
	
	usageAverage = (static_cast<float>(currentCpuUsage - cpuUsage)) / (currentUptime - uptime);

	cpuUsage = currentCpuUsage;
	uptime = currentUptime;
	
	ResourceData resourceData(usageAverage, getFreeMemory());
	
	return resourceData;
}
