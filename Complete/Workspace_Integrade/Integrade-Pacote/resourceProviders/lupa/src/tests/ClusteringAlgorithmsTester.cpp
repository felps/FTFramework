#include "ClusteringAlgorithmsTester.hpp"
#include "../DataCollector.hpp"
#include "../KMeansClusteringAlgorithm.hpp"

#include <fstream>
#include <vector>
#include <math.h>

using namespace std;


#define EPSILON 0.0001f
#define FLOAT_EQUALITY(A, B) (fabsf(A - B) < EPSILON)
 

void ClusteringAlgorithmsTester::tearDown() {
	setCollectInterval(DEFAULT_COLLECT_INTERVAL);
	setK(DEFAULT_K);
	setValidResourceDataThreshold(DEFAULT_VALID_RESOURCE_DATA_THRESHOLD);
}

void ClusteringAlgorithmsTester::setUp() {
	//setCollectInterval(3600 * 4); // 6 resourcedata per day
	
	setK(4);
	
	ofstream *ofs = new ofstream("src/tests/lupa.persisted.test");

	*ofs << "1155351601      0.1       250" << endl /*Sat 2006-08-12 00:00:01*/
		 << "1155373201      0.1       250" << endl 
		 << "1155394801      0.1       250" << endl
		 << "1155416401      0.1       250" << endl
		 << "1155438001      0.3       100" << endl/*Sun 2006-08-13 00:00:01*/
		 << "1155459601      0.3       100" << endl 
		 << "1155481201      0.3       100" << endl
		 << "1155502801      0.3       100" << endl
		 << "1155524401      0.6       300" << endl/*Mon 2006-08-14 00:00:01*/
		 << "1155546001      0.6       300" << endl
		 << "1155567601      0.6       300" << endl
		 << "1155589201      0.6       300" << endl;
	ofs->close();
}

void ClusteringAlgorithmsTester::testKMeans() {
	//DataCollector dataCollector = DataCollector("/home/danconde/coleta/lupa.persisted");
	DataCollector dataCollector = DataCollector("/home/danconde/coleta/plutao.log");
	KMeansClusteringAlgorithm kmeans;
	vector<Cluster*> clusters;

	//setCollectInterval(3600);
	//setK(10);
	setValidResourceDataThreshold(0.9f);
	
	vector<UsageData*>* data = dataCollector.getUnclassifiedData();
	vector<UsageData*> derivatives;
	for (unsigned int i = 0; i < data->size(); i++)
		if (data->at(i)->isValid())
			derivatives.push_back(data->at(i)->derivative());
//	cout << "CPU_CLUSTERS: unclassified " << endl;
	clusters = kmeans.analyzeData(data, CPU_USAGE);
	
//	cout << "CPU_CLUSTERS: derivatives" << endl;
	clusters = kmeans.analyzeData(&derivatives, CPU_USAGE);

//	cout << "FREE_MEMORY_CLUSTERS: unclassified" << endl;
	clusters = kmeans.analyzeData(data, FREE_MEMORY);

//	cout << "FREE_MEMORY_CLUSTERS: derivatives" << endl;
	clusters = kmeans.analyzeData(&derivatives, FREE_MEMORY);
	
	//TODO: testar direito o KMeans
	
} 

void ClusteringAlgorithmsTester::testGetClosestCluster() {
	setCollectInterval(3600 * 6); // 4 resourcedata per day
	
	KMeansClusteringAlgorithm ca;
	vector<Cluster*> clusters;
	DataCollector dataCollector = DataCollector("src/tests/lupa.persisted.test");
	vector<UsageData*>* usageDatas = dataCollector.getUnclassifiedData();
	
	Cluster *cluster = new Cluster();
	cluster->addUsageData(usageDatas->at(1));
	clusters.push_back(cluster);
	
	cluster = new Cluster();
	cluster->addUsageData(usageDatas->at(2));
	clusters.push_back(cluster);
	
	CPPUNIT_ASSERT(ca.getClosestCluster (usageDatas->at(0), &clusters, CPU_USAGE, 0, 2 * SAMPLES_PER_DAY) == clusters[0]);
	CPPUNIT_ASSERT(ca.getClosestCluster (usageDatas->at(0), &clusters, FREE_MEMORY, 0, 2 * SAMPLES_PER_DAY) == clusters[1]);
	
	CPPUNIT_ASSERT(ca.getClosestCluster (usageDatas->at(1), &clusters, CPU_USAGE, 0, 2 * SAMPLES_PER_DAY) == clusters[0]);
	CPPUNIT_ASSERT(ca.getClosestCluster (usageDatas->at(1), &clusters, FREE_MEMORY, 0, 2 * SAMPLES_PER_DAY) == clusters[0]);

}

void ClusteringAlgorithmsTester::testGetDistanceElementToCluster() {
	setCollectInterval(3600 * 6); // 4 resourcedata per day
	
	KMeansClusteringAlgorithm ca;
	Cluster *cluster = new Cluster();
	DataCollector dataCollector = DataCollector("src/tests/lupa.persisted.test");
	
	vector<UsageData*>* usageDatas = dataCollector.getUnclassifiedData();
	cluster->addUsageData(usageDatas->at(0));
	
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ca.getDistanceElementToCluster(usageDatas->at(0), cluster, CPU_USAGE, 0, 2 * SAMPLES_PER_DAY), 0.0f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ca.getDistanceElementToCluster(usageDatas->at(1), cluster, CPU_USAGE, 0, 2 * SAMPLES_PER_DAY), 0.72111f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ca.getDistanceElementToCluster(usageDatas->at(2), cluster, CPU_USAGE, 0, 2 * SAMPLES_PER_DAY), 1.16619f));
	
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ca.getDistanceElementToCluster(usageDatas->at(0), cluster, FREE_MEMORY, 0, 2 * SAMPLES_PER_DAY), 0.0f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ca.getDistanceElementToCluster(usageDatas->at(1), cluster, FREE_MEMORY, 0, 2 * SAMPLES_PER_DAY), 500.0f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ca.getDistanceElementToCluster(usageDatas->at(2), cluster, FREE_MEMORY, 0, 2 * SAMPLES_PER_DAY), 412.31056f));
	
}
