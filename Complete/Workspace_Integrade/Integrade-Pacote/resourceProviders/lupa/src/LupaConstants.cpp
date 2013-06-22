#include <sys/utsname.h>
#include <string.h>

#include <stdio.h>

#include "LupaConstants.hpp"

static int __collectInterval = DEFAULT_COLLECT_INTERVAL;
static int __predictionHours = DEFAULT_PREDICTION_HOURS;
static int __hoursToConsiderInAverage = DEFAULT_HOURS_TO_CONSIDER_IN_AVERAGE;
static unsigned int __K_ = DEFAULT_K;
static double __SD_THRESHOLD_ = DEFAULT_VALID_RESOURCE_DATA_THRESHOLD; 
static char lupaLogFileName[60] = DEFAULT_LOG_FILENAME;
static char lupaRequestLogFileName[70] = DEFAULT_REQUEST_LOG_FILENAME;

int getCollectInterval() {
	return __collectInterval;
}

void setCollectInterval(int v) {
	__collectInterval = v;
}

void setK(unsigned int v) { 
	__K_ = v;
}

unsigned int getK() {
	return __K_;	
}

double getValidResourceDataThreshold() {
	return __SD_THRESHOLD_;
}

void setValidResourceDataThreshold(double v) {
	__SD_THRESHOLD_ = v;
}

int getPredictionHours() {
	return __predictionHours;
}

void setPredictionHours(int v) {
	__predictionHours = v;
}

int getHoursToConsiderInAverage() {
   return __hoursToConsiderInAverage;
}

void setHoursToConsiderInAverage(int v) {
   __hoursToConsiderInAverage = v;
}

ostream& operator<<(ostream& out, resource r) {
	switch (r) {
		case CPU_USAGE: 	out << "CPU_USAGE"; break;
		case FREE_MEMORY: 	out << "FREE_MEMORY"; break;
		default:		out << r; break;
	}
	return out;
}

void initLupaLogFileName() {
   struct utsname name;


   if (uname(&name) < 0) {
      strcpy(lupaLogFileName, "lupa.log");
      strcpy(lupaRequestLogFileName, "lupaRequests.log");
      return;
   }
   
   /* Concatenate the host name with 'lupa.' */
   strcpy(lupaLogFileName, "lupa.");
   strcpy(lupaLogFileName + 5, name.nodename);
   
   /* Concatenates the current name with the extension '.log' */
   strcpy(lupaLogFileName + strlen(lupaLogFileName), ".log");

   /* Same thing for the lupaRequests.log file */ 
   strcpy(lupaRequestLogFileName, "lupaRequests.");
   strcpy(lupaRequestLogFileName + 13, name.nodename);
   strcpy(lupaRequestLogFileName + strlen(lupaRequestLogFileName), ".log");
}

char *getLupaLogFileName() {
   return lupaLogFileName;
}

char *getLupaRequestLogFileName() {
   return lupaRequestLogFileName;
}
