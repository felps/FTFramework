#include "DataParser.hpp"
#include "LupaConstants.hpp"
#include "Timestamp.hpp"
#include "ResourceData.hpp"
#include "UsageData.hpp"
#include <fstream>
#include <sstream>
#include <vector>
#include <map>
#include <cstring>

using namespace std;

#define MAX_LINE_LENGTH 100

vector<UsageData*> *DataParser::parseFile (const char* fileName) {
   return parseFile (fileName, Timestamp(0));
}

vector<UsageData*> *DataParser::parseFile (const char* fileName, Timestamp initial) {
   ResourceData s;
   vector<Timestamp> timestamps;
   vector<ResourceData> resourceDatas;
   vector<UsageData*> *usageDatas = new vector<UsageData*>;
   UsageData *ud;//, *previousUd = NULL;
   int previousUd = -1;
   Timestamp t, currentDay;
   char line[MAX_LINE_LENGTH];
   unsigned int i;
   ifstream ifs(fileName);
   ResourceData currentEstimate;
   
   if (!ifs) {
      //cerr << "Lupa database was not found." << endl;
      return NULL;
   } else {
      do {
         ifs.getline (line, MAX_LINE_LENGTH);
         if (strlen(line) > 2) { // non empty line
            istringstream ist (line);
            ist >> t >> s;
            //cout << "Leu: (" << t << ") " << t.formattedPrint() << "  " << s << endl;
            if (t > initial) {
               //cout << "Leu: (" << t << ") " << t.formattedPrint() << "  " << s << endl;
               timestamps.push_back(t);
               resourceDatas.push_back(s);
            }
         }
      } while (!ifs.eof());

      //cout << "Leu " << timestamps.size() << " timestamps e " << resourceDatas.size() << " resourceDatas" << endl;

      if (timestamps.size() > 0) {
         currentDay = timestamps[0];
         ud = new UsageData(currentDay);
         for (i = 0; i < timestamps.size(); i++) {
            if (timestamps[i].daysApart(currentDay) < 0) continue; // ignores any data that is past than last day parsed
            if (!timestamps[i].isSameDay(ud->getDate())) {
               if (timestamps[i].daysApart(ud->getDate()) == 1) 
                  previousUd = usageDatas->size(); // isn't there a better way to do it ?
               else 
                  previousUd = -1;

               usageDatas->push_back(ud);
               currentDay = timestamps[i];

               // estimate...
               ud = new UsageData(currentDay);
               Timestamp t(timestamps[i].getRawTime() - COLLECT_INTERVAL);
               while (t.isSameDay(timestamps[i])) { 
                  ud->getData()[t.getSecondInDay()/COLLECT_INTERVAL] = currentEstimate;
                  t = Timestamp(t.getRawTime() - COLLECT_INTERVAL);
               }

            }
            ud->getData()[timestamps[i].getSecondInDay()/COLLECT_INTERVAL] = resourceDatas[i];
            if (previousUd != -1) // fills the second day of the previous usageData with current usageData
               usageDatas->at(previousUd)->getData()[(timestamps[i].getSecondInDay()/COLLECT_INTERVAL) + SAMPLES_PER_DAY] = resourceDatas[i];

            // estimate usage of a period that wasn't collected
            Timestamp tmp(timestamps[i].getRawTime() + COLLECT_INTERVAL);
            currentEstimate = resourceDatas[i];
            currentEstimate.setEstimate(true);

            if (((i + 1 < timestamps.size()) && ((timestamps[i+1].getRawTime() > tmp.getRawTime()) ))
                  || i + 1 == timestamps.size()){

               while (tmp.daysApart(timestamps[i]) <= 1) {
                  // the estimate is the last observed value !
                  if (tmp.isSameDay(timestamps[i])) {
                     ud->getData()[tmp.getSecondInDay()/COLLECT_INTERVAL] = currentEstimate;
                     if (previousUd != -1)
                        usageDatas->at(previousUd)->getData()[(tmp.getSecondInDay()/COLLECT_INTERVAL) + SAMPLES_PER_DAY] = currentEstimate;
                  } else {
                     ud->getData()[(tmp.getSecondInDay()/COLLECT_INTERVAL) + SAMPLES_PER_DAY] = currentEstimate;
                  }
                  tmp = Timestamp(tmp.getRawTime() + COLLECT_INTERVAL);
               }
            }	
         }
         usageDatas->push_back(ud);
      }

   }
   return usageDatas;
}
