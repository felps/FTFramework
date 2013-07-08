#include "ClusterAnalyzerTester.hpp"

#include "../KMeansClusteringAlgorithm.hpp"
#include "../LupaConstants.hpp"
#include <fstream>
#include <math.h>

#define EPSILON 0.0001f

ClusterAnalyzerTester::ClusterAnalyzerTester() {}

ClusterAnalyzerTester::~ClusterAnalyzerTester() {}


void ClusterAnalyzerTester::tearDown() {
	setCollectInterval(DEFAULT_COLLECT_INTERVAL);
	setK(DEFAULT_K);
	setValidResourceDataThreshold(DEFAULT_VALID_RESOURCE_DATA_THRESHOLD);
}

void ClusterAnalyzerTester::setUp() {

	setCollectInterval (3600 * 8); // 3 resourcedata per day
	setK(3);
	setValidResourceDataThreshold(0.1f);

	ofstream *ofs = new ofstream("src/tests/lupa.persisted.test");

	*ofs << "1155354300	0.1	1000" << endl /*Sat 2006-08-12 00:45:00*/
		<< "1155383100	0.2	2000" << endl
		<< "1155411900	0.3	3000" << endl
		<< "1155440700	0.7	500" << endl
		<< "1155469500	0.6	6000" << endl
		<< "1155498300	0.4	700" << endl
		<< "1155527100	0.1	11000" << endl
		<< "1155555900	0.6	11000" << endl
		<< "1155584700	0.2	11000" << endl;
	ofs->close();
	
}

void ClusterAnalyzerTester::testFindUsagePattern() {

	DataCollector *dtCol = new DataCollector("src/tests/lupa.persisted.test");
	ClusteringAlgorithm *ca = new KMeansClusteringAlgorithm(); 
	ClusterAnalyzer clay(ca, dtCol);
	UsageData ud1 = UsageData(Timestamp());
	Cluster *cl;
	unsigned int i;
	double minDist, dist;
	Timestamp now(1155613500);
	
	ud1.getData()[0] = ResourceData(0.1f, 11000); 
	ud1.getData()[1] = ResourceData(0.21f, 12000); 
	ud1.getData()[2] = ResourceData(0.31f, 13000);
	ud1.getData()[3] = ResourceData(0.5f, 2111);
	ud1.getData()[4] = ResourceData(0.6f, 2222);
	ud1.getData()[5] = ResourceData(0.7f, 1370);
	
	vector< vector<Cluster*> > clusters = clay.updateClusters();
	
	int startIndex = now.getSecondInDay()/COLLECT_INTERVAL;
	
	cl = clay.findUsagePattern(CPU_USAGE, startIndex, startIndex + SAMPLES_PER_DAY, &ud1);
	// test if the usage pattern is the closest cluster
	minDist = ca->getDistanceElementToCluster(&ud1, cl, CPU_USAGE, startIndex, startIndex + SAMPLES_PER_DAY);
	for (i = 0; i < clusters[CPU_USAGE].size(); i++) {
	   dist = ca->getDistanceElementToCluster(&ud1, clusters[CPU_USAGE][i], CPU_USAGE, startIndex, startIndex + SAMPLES_PER_DAY);
	   if(fabs(dist - minDist) < EPSILON) 
		    dist = minDist;
	   CPPUNIT_ASSERT(dist >= minDist);
	}
	
	cl = clay.findUsagePattern(FREE_MEMORY, startIndex, startIndex + SAMPLES_PER_DAY, &ud1);
	minDist = ca->getDistanceElementToCluster(&ud1, cl, FREE_MEMORY, startIndex, startIndex + SAMPLES_PER_DAY);
	
	for (i = 0; i < clusters[FREE_MEMORY].size(); i++) {
		double dist = ca->getDistanceElementToCluster(&ud1, clusters[FREE_MEMORY][i], FREE_MEMORY, startIndex, startIndex + SAMPLES_PER_DAY);
		CPPUNIT_ASSERT(dist >= minDist);
	}

}

void ClusterAnalyzerTester::testHasClusters() {
	DataCollector *dtCol = new DataCollector("src/tests/lupa.persisted.test");
	ClusteringAlgorithm *ca = new KMeansClusteringAlgorithm();
	ClusterAnalyzer clay(ca, dtCol);

	CPPUNIT_ASSERT(!clay.hasClusters());
	
	clay.updateClusters();
	CPPUNIT_ASSERT(clay.hasClusters());

}
