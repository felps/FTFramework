#include <unistd.h>
#include <fcntl.h>
#include <errno.h>

#include "DataCollector.hpp"
#include "LinuxSystemAnalyzer.hpp"
#include "LupaConstants.hpp"
#include "Timestamp.hpp"
#include "DataParser.hpp"
#include <cassert>
#include <cstdlib>

#define TIMESTAMP_INDEX(tmsmtp) ((tmsmtp.getSecondInDay()/COLLECT_INTERVAL) + SAMPLES_PER_DAY)

using namespace std;

DataCollector::DataCollector(const char* fileName, SystemAnalyzer *stAnal, CurrentTimestampFactory *tmstmpFactory) {
   lastCollectTimestamp = Timestamp(1);
   logFileOutputStream = NULL;
   logFileName = fileName;
   currentTimestampFactory = tmstmpFactory;
   systemAnalyzer = stAnal;
   retrieveRecentUsageFromLogFile();
}

void DataCollector::retrieveRecentUsageFromLogFile() {
   DataParser parser;
   Timestamp now = this->now();
   vector<UsageData*> *data = parser.parseFile (logFileName, now.addDays(-2).beginningOfSameDay());
   recentUsageData = NULL;

   if (data != NULL) {
      for (unsigned int i = 0; i < data->size() && recentUsageData == NULL; i++) {
         if (data->at(i)->getDate().isSameDay(now.addDays(-1))) {
            recentUsageData = data->at(i);
         } else {
            if (data->at(i)->getDate().isSameDay(now)) {
               recentUsageData = data->at(i);
               recentUsageData->goBackOneDay();
            }
         }
      }
#ifdef __DEBUG_OUTPUT
      if (recentUsageData != NULL)  {
         cout << "LUPA: recent usage data retrieved from file (" << now.formattedPrint() << ")" << endl;
      }
#endif
   } 
   if (recentUsageData == NULL) {
      recentUsageData = new UsageData(now.addDays(-1).beginningOfSameDay()); // current data is stored in the second half of this UD
   }

}

void DataCollector::collectData() {
   Timestamp currentTimestamp = now();

   // in the case that now is less than the beginning of recentUsageData -- this can occur
   // when the last data collected filled the last position of the UD and this method was
   // called before that new day started (!) (sic!)
   Timestamp tmp = recentUsageData->getDate().addDays(1);
   if (tmp > currentTimestamp)
      return;

   ResourceData currentResourceData = systemAnalyzer->getResourceData();

   updateRecentUsageData (currentTimestamp, currentResourceData);
   updateLogFile (currentTimestamp, currentResourceData);
   lastCollectTimestamp = currentTimestamp;
}

void DataCollector::createNewRecentUsageData(Timestamp t) {
   UsageData *oldUsageData = recentUsageData;
   recentUsageData = new UsageData(t.addDays(-1), *oldUsageData);
   delete oldUsageData;
}

void DataCollector::updateRecentUsageData (Timestamp currentTimestamp, ResourceData currentResourceData) {
   int daysApart = currentTimestamp.addDays(-1).daysApart(recentUsageData->getDate());
   //	assert((daysApart >> 1) == 0); // => daysApart in [0,1]

   if (daysApart == 1) 
      createNewRecentUsageData(currentTimestamp.beginningOfSameDay());

   recentUsageData->getData()[TIMESTAMP_INDEX(currentTimestamp)] = currentResourceData;

   if (TIMESTAMP_INDEX(currentTimestamp) == ((2*SAMPLES_PER_DAY) - 1)) {
      // moves recent data to the first half of this UD when the last element of this day is collected
      // adds collect_interval to current timestamp to make the new UD start tomorrow
      Timestamp tmp (currentTimestamp.getRawTime() + COLLECT_INTERVAL);
      createNewRecentUsageData (tmp.beginningOfSameDay());
   }
}

   void DataCollector::updateLogFile (Timestamp currentTimestamp, ResourceData currentResourceData) {
      if (logFileOutputStream == NULL) 
         openLogFile();

      //cout << currentTimestamp << "\t" << currentResourceData << endl;
      *logFileOutputStream << currentTimestamp << "\t" << currentResourceData << endl;
   }

bool DataCollector::lockLogFile() {
   int fd;
   struct flock fl;

   fd = open(logFileName, O_RDWR | O_CREAT, 0640);
   if (fd == -1) { 
      cerr << "Handle error" << endl;
      return false;
   }

   fl.l_type = F_WRLCK;
   fl.l_whence = SEEK_SET;
   fl.l_start = 0;
   fl.l_len = 1000000000;

   if (fcntl(fd, F_SETLK, &fl) == -1) {
      if (errno == EACCES || errno == EAGAIN) {
         cout << "Log file locked by another process" << endl;
         cout << "Log file: " << logFileName << endl;
      } else {
         cout << "Unexpected handle error" << endl;
      }
      return false;
   }
   return true;
}

void DataCollector::startCollectingData() {
   pthread_create(&collectingThread, NULL, DataCollector::collectDataThread, (void*) this);
}

void *DataCollector::collectDataThread(void *dataCollector) {
   while (true) {
      sleep(COLLECT_INTERVAL);
      ((DataCollector*) dataCollector)->collectData();
   }

}

void DataCollector::joinCollectingThread() {
   pthread_join(collectingThread, NULL);
}


// as it should not be called very often, it's easier to parse the log file
vector<UsageData*>* DataCollector::getUnclassifiedData() {
   DataParser parser;
   return parser.parseFile (logFileName);
}

void DataCollector::openLogFile() {
   /*if (!lockLogFile()) {
      cout << "Nao lockou" << endl;
      exit(1);
   }*/
   
   logFileOutputStream = new ofstream(logFileName, ios::app); /* opens the file in append mode */

   if (logFileOutputStream->fail()) {
      cout << "Error opening log file" << endl;
      exit(-1);
   }
}

UsageData* DataCollector::getRecentUsageData() {
   //TODO: it's better return a copy
   return recentUsageData;
} 

bool DataCollector::hasEnoughRecentUsageToPredict(Timestamp now, int hours) {
   // test if there are at least 'hours' of recent usage data
   int count = 0;
   int samples = (3600 * hours)/COLLECT_INTERVAL;
   int startIndex = (now.getSecondInDay()/COLLECT_INTERVAL) + (SAMPLES_PER_DAY - samples);

   vector<ResourceData> data = recentUsageData->getData();

   for (int i = 0; i < samples; i++) { // do not consider the system data of the current timestamp that, in fact, is not even collected
      if (data[startIndex + i].isValid() && !data[startIndex + i].isEstimate())
         count++;	
   }

   return (static_cast<double>(count)/samples) >= VALID_RESOURCE_DATA_THRESHOLD;

}

Timestamp DataCollector::now() {
   return currentTimestampFactory->getCurrentTimestamp();
}
