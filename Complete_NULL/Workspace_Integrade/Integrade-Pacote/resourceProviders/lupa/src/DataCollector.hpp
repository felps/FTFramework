#ifndef DATACOLLECTOR_HPP_
#define DATACOLLECTOR_HPP_

#include <fstream>
#include <string>
#include <vector>
#include <pthread.h>

#include "SystemAnalyzer.hpp"
#include "LinuxSystemAnalyzer.hpp"
#include "UsageData.hpp"
#include "LupaConstants.hpp"
#include "CurrentTimestampFactory.hpp"

using namespace std;

class DataCollector {
public:
	DataCollector(const char* fileName = LOG_FILE, SystemAnalyzer *stAnal = new LinuxSystemAnalyzer(), CurrentTimestampFactory *tmstmpFactory = new CurrentTimestampFactory());
	void startCollectingData();
	vector<UsageData*>* getUnclassifiedData();

	void collectData(); //TODO: should be private
	UsageData *getRecentUsageData(); 
	bool hasEnoughRecentUsageToPredict(Timestamp now, int hours = 24);
	Timestamp now();
	static void *collectDataThread(void *dataCollector);
	void joinCollectingThread();
		
private:
	const char *logFileName;
	ofstream *logFileOutputStream;
	SystemAnalyzer *systemAnalyzer;
	UsageData *recentUsageData;
	CurrentTimestampFactory *currentTimestampFactory;
	Timestamp lastCollectTimestamp;
	pthread_t collectingThread;

	bool lockLogFile();
	void updateRecentUsageData (Timestamp currentTimestamp, ResourceData currentResourceData);
	void updateLogFile (Timestamp currentTimestamp, ResourceData currentResourceData);
	void openLogFile(); 	
	void createNewRecentUsageData(Timestamp t);
	void retrieveRecentUsageFromLogFile();
};

#endif /*DATACOLLECTOR_HPP_*/
