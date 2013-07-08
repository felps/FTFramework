#include "UsagePredictor.hpp"

//#define __DEBUG_OUTPUT

UsagePredictor::UsagePredictor (ClusteringAlgorithm *clAlgorithm, DataCollector *dtCollector) {
   clusterAnalyzer = new ClusterAnalyzer(clAlgorithm, dtCollector);
   dataCollector = dtCollector;	
}

// mem in kB (default = 10 mB)
// this call means: can run grid application with
//	- at least rd.cpuUsage free CPU available
//	- at least rd.freeMemory free memory available
bool UsagePredictor::canRunGridApplication(Timestamp timestamp, ResourceData rd, int hours) {
   return canRunGridApplication(timestamp, rd, NULL, hours);
}

// same as above, including the recentDataSample representing the recent usage data (if left null, the
// recent usage data will be obtained via dataCollector)
bool UsagePredictor::canRunGridApplication(Timestamp timestamp, ResourceData rd, UsageData *recentDataSample, int hours) {
   // TODO: this method should be synchronized (Java-like)

   Cluster *predicted;
   double resourceOffset;
   UsageData *ud;
   vector<resource> resources = getAvailableResources();
   vector<resource>::iterator resourcesIterator;

   int startIndex = timestamp.getSecondInDay()/COLLECT_INTERVAL;

   clusterAnalyzer->updateClustersIfNeeded(timestamp);

   if (((recentDataSample != NULL) || dataCollector->hasEnoughRecentUsageToPredict(timestamp)) 
         && clusterAnalyzer->hasClusters()) {
#ifdef __DEBUG_OUTPUT
      cout << "LUPA: using clusters to predict usage (" << timestamp.formattedPrint() << ")" << endl;
#endif
      for (resourcesIterator = resources.begin(); resourcesIterator != resources.end(); resourcesIterator++) {
         predicted = clusterAnalyzer->findUsagePattern(*resourcesIterator, startIndex, startIndex + SAMPLES_PER_DAY, recentDataSample);
         //cout << "==> predicted cluster: " << predicted->getNumberOfElements() << " elements" << endl;
         if (recentDataSample == NULL) ud = dataCollector->getRecentUsageData();
         else ud = recentDataSample;
         resourceOffset = ud->resourceAverage(*resourcesIterator, startIndex, startIndex + SAMPLES_PER_DAY);
         if (!predicted->satisfiesResource(*resourcesIterator,
                  rd.getResource(*resourcesIterator),
                  resourceOffset,
                  startIndex + SAMPLES_PER_DAY,
                  //startIndex + SAMPLES_PER_DAY + PREDICTION_INTERVAL))
                  startIndex + SAMPLES_PER_DAY + ((hours * 60 * 60) / COLLECT_INTERVAL)))
               return false;
      }
   } else {
      //TODO: predict based on the current usage -- predicted usage is the average of the last 4 hours
      if (dataCollector->hasEnoughRecentUsageToPredict(timestamp, getHoursToConsiderInAverage())) {
#ifdef __DEBUG_OUTPUT
         cout << "LUPA: using last " << getHoursToConsiderInAverage() << " hours to predict usage (" << timestamp.formattedPrint() << ")" << endl;
#endif
         ud = dataCollector->getRecentUsageData();
         for (resourcesIterator = resources.begin(); resourcesIterator != resources.end(); resourcesIterator++) {
            resourceOffset = ud->resourceAverage(*resourcesIterator, startIndex + ((24 - getHoursToConsiderInAverage())*(3600/COLLECT_INTERVAL)), startIndex + SAMPLES_PER_DAY);
            if (!resourceSatisfiesMinimum(*resourcesIterator, resourceOffset, rd.getResource(*resourcesIterator)))
               return false;
         }
      } else {
#ifdef __DEBUG_OUTPUT
         cout << "LUPA: unable to predict usage (" << timestamp.formattedPrint() << ")" << endl;
#endif
      }
   }
   return true;
}


vector<double> UsagePredictor::getPrediction(Timestamp timestamp, resource r, int hours) {
   return getPrediction(timestamp, r, hours, dataCollector->getRecentUsageData());
}

vector<double> UsagePredictor::getPrediction(Timestamp timestamp, resource r, int hours, UsageData *recentDataSample) {
   Cluster *predicted;
   double resourceOffset;
   UsageData *ud;
   unsigned int retSize = (hours * 3600)/COLLECT_INTERVAL;
   vector<double> ret(retSize);

   int startIndex = timestamp.getSecondInDay()/COLLECT_INTERVAL;

   clusterAnalyzer->updateClustersIfNeeded(timestamp);
   if (((recentDataSample != NULL) || dataCollector->hasEnoughRecentUsageToPredict(timestamp)) && clusterAnalyzer->hasClusters()) {
      cout << "Prevendo no getPrediction()" << endl;
      predicted = clusterAnalyzer->findUsagePattern(r, startIndex, startIndex + SAMPLES_PER_DAY, recentDataSample);
      if (recentDataSample == NULL) ud = dataCollector->getRecentUsageData();
      else ud = recentDataSample;
      resourceOffset = ud->resourceAverage(r, startIndex, startIndex + SAMPLES_PER_DAY);

      /*
       * FIXME: URGENTE !!! essa lógica e a do canRunGridApplication não podem ficar separadas ! O canRun.. tem que usar o getPrediction
       *        pra ver se satisfaz a condição
       */
      for (unsigned int i = 0; i < retSize; i++) {
         resourceOffset += predicted->getRepresentativeElement().getData()[startIndex + SAMPLES_PER_DAY + i].getResource(r);
         ret[i] = resourceOffset;
      }

   } else {
      //TODO: predict based on the current usage
      return ret;
   }
   return ret;

}
