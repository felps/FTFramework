//
// C++ Implementation: LogFileAnalyzer
//
// Description: 
//
//
// Author: Danilo Conde <danconde@danconde-laptop>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//

#include <sys/types.h>
#include <dirent.h>
#include <errno.h>
#include <vector>
#include <string>
#include <iostream>

#include "../UsagePredictor.hpp"
#include "../KMeansClusteringAlgorithm.hpp"

using namespace std;

int main(void) {
  char logFileName[300] = "/home/danconde/coleta/europa.log";
  ResourceData parameters(0.5f, 100000);
  bool answer;
  DataCollector *dtCollector = new DataCollector(logFileName);
  unsigned int i, count;
  UsageData *testUsageData;
  
  setValidResourceDataThreshold(0.9f);
  setPredictionHours(6);
  
  cout << "timestamp\tp_cpu\tp_mem\tanswer\tpcs\tvcs\tcoff" << endl;
  // pcs = predicted cpu satisfability
  // vcs = verified cpu satisfability
  
  UsagePredictor *up = new UsagePredictor(new KMeansClusteringAlgorithm(), dtCollector);
  vector<UsageData*> *usageDatas = dtCollector->getUnclassifiedData();
  
  for (unsigned int k = 0; k < usageDatas->size(); k += 3) {
     testUsageData = usageDatas->at(k);
     if (testUsageData->isValid()) {
	 for (unsigned int j = 6; j < 18; j++) {
	       Timestamp timestamp = Timestamp(testUsageData->getDate().beginningOfSameDay().getRawTime() + j*3600);
	       
	       answer = up->canRunGridApplication(timestamp, parameters, testUsageData);
	       cout << timestamp.formattedPrint() << "\t" << parameters << "\t" << answer << "\t";
	       
	       vector<double> prediction = up->getPrediction(timestamp, CPU_USAGE, getPredictionHours(), testUsageData);
	       count = 0;
	       for (i = 0; i < prediction.size(); i++)
		  if (prediction[i] + parameters.getCpuUsage() <= 1.0f)
		  count++;
	       cout << (static_cast<double>(count) / prediction.size()) * 100 << "%\t";
	       
	       count = 0;
	       for (i = 0; i < prediction.size(); i++) {
		  ResourceData rd = testUsageData->getData()[(timestamp.getSecondInDay()/COLLECT_INTERVAL) + SAMPLES_PER_DAY + i];
		  if (rd.isValid() && (rd.getCpuUsage()  + parameters.getCpuUsage() <= 1.0f))
		     count++;
	       }
	       cout << (static_cast<double>(count) / prediction.size()) * 100 << "%\t";
	       
	       cout << testUsageData->resourceAverage(CPU_USAGE, timestamp.getSecondInDay()/COLLECT_INTERVAL, timestamp.getSecondInDay()/COLLECT_INTERVAL + SAMPLES_PER_DAY) << endl;
	 }
     }
  }
  /* informar qual cluster foi encontrado */
  /* comparar resultado da predicao com o realizado */
  /* pegar o resultado do getPrediction e ver a porcentagem de elementos do vetor de previsao condisseram
     com o realizado */
  
  return 0;
}

