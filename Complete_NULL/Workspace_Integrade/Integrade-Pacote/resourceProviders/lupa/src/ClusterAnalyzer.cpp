#include "ClusterAnalyzer.hpp"
#include <sstream>

ClusterAnalyzer::ClusterAnalyzer(ClusteringAlgorithm *clAlgorithm, DataCollector *dtCollector) {
   clusteringAlgorithm = clAlgorithm;
   dataCollector = dtCollector;
   lastClusterUpdate = 0;
   //TODO: loadClustersFromFile();
   //TODO: start update thread	
}

vector< vector<Cluster*> > ClusterAnalyzer::updateClusters(vector<UsageData*>* uds) {
   vector<UsageData*>* usageDatas = uds;

   if (uds == NULL) 
      usageDatas = dataCollector->getUnclassifiedData();

   if (usageDatas != NULL) {
      //	assert (usageDatas->size() >= K);

      // collect the derivates
      vector<UsageData*>* derivatives = new vector<UsageData*>();
      for (unsigned int i = 0; i < usageDatas->size(); i++)
         derivatives->push_back(usageDatas->at(i)->derivative());

      usagePatterns = vector< vector<Cluster*> >(2);
      usagePatterns[CPU_USAGE] = clusteringAlgorithm->analyzeData(derivatives, CPU_USAGE);
      usagePatterns[FREE_MEMORY] = clusteringAlgorithm->analyzeData(derivatives, FREE_MEMORY);

      //FIXME
      //TODO: fazer esses deletes em algum outro lugar (sugest�o no mind map de criar uma classe 
      // esperta que encapsula essa lista e o destrutor faz esses deletes)
      //for (i = 0; usageDatas->size(); i++)
      //	delete usageDatas->at(i);
      delete usageDatas;
      delete derivatives;
   }

   return usagePatterns;
}

bool ClusterAnalyzer::needsClusterUpdate(Timestamp timestamp) {
   // updates clusters once a day
   return timestamp.daysApart(Timestamp(lastClusterUpdate)) > 0;

}

vector< vector<Cluster*> > ClusterAnalyzer::updateClustersIfNeeded(Timestamp timestamp) {

   if (needsClusterUpdate(timestamp)) {
      vector<UsageData*>* unclassified = dataCollector->getUnclassifiedData();
      if (unclassified != NULL) {
         vector<UsageData*>* usageDatas = new vector<UsageData*>();

         // filter only valid UsageDatas
         for (unsigned int i = 0; i < unclassified->size(); i++)
            if (unclassified->at(i)->isValid())
               usageDatas->push_back(unclassified->at(i));

         if (usageDatas->size() >= K) {
            updateClusters(usageDatas);
            lastClusterUpdate = timestamp.getRawTime();
#ifdef __DEBUG_OUTPUT
            cout << "LUPA: clusters updated (" << timestamp.formattedPrint() << ")" << endl;
#endif
         }
      }
   }

   return usagePatterns;
}

Cluster* ClusterAnalyzer::findUsagePattern (resource r, int startIndex, int endIndex, UsageData* ud) {
   UsageData *udSample = (ud == NULL) ? dataCollector->getRecentUsageData()->derivative() : ud;
   return clusteringAlgorithm->getClosestCluster(udSample, &usagePatterns[r], r, startIndex, endIndex);
}

bool ClusterAnalyzer::hasClusters() {
//   if (usagePatterns.size() >= 2)
//      cout << "ClusterAnalyzer::hasClusters() - Number of Clusters: " << usagePatterns[0].size() << " - " << usagePatterns[1].size() << endl;
   return usagePatterns.size() > 0;
}
