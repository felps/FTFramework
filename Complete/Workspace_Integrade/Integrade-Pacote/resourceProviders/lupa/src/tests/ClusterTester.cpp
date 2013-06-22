#include "ClusterTester.hpp"
#include "../DataCollector.hpp"
#include "../UsageData.hpp"

#include <fstream>
#include <vector>
#include <math.h>

using namespace std;


/**
 * DTMMIBCP = Don't tell my mother i've been copying & pasting (source: DataCollectorTester) 
 */
#define EPSILON 0.00001f
#define TEST_USAGE_DATA(IX, C, M) rd = ud->getData().at(IX); CPPUNIT_ASSERT((fabsf(rd.getCpuUsage() - C)) < EPSILON); CPPUNIT_ASSERT(fabsf(rd.getFreeMemory()- M) < EPSILON);
#define FLOAT_EQUALITY(A, B) (fabsf(A - B) < EPSILON)


void ClusterTester::tearDown() {
	setCollectInterval(DEFAULT_COLLECT_INTERVAL);
}

void ClusterTester::setUp() {
	cluster = NULL;

	setCollectInterval(3600 * 4); // 6 resourcedata per day
	timezone = 10800;

	//DTMMIBCP
	ofstream *ofs = new ofstream("src/tests/lupa.persisted.test");
	*ofs << "1155351601      0.1       222" << endl /*Sat 2006-08-12 00:00:01*/
		 << "1155366001      0.2       333" << endl
		 << "1155380401      0.3       111" << endl
		 << "1155394801      0.4       999" << endl 
		 << "1155409201      0.5       888" << endl
		 << "1155423601      0.6       777" << endl
		 << "1155438001      0.15       2222" << endl /*Sun 2006-08-13 00:00:01*/
		 << "1155452401      0.25      3333" << endl
		 << "1155466801      0.35       1111" << endl
		 << "1155481201      0.45       9999" << endl 
		 << "1155495601      0.55       8888" << endl
		 << "1155510001      0.65       7777" << endl;
	ofs->close();
}

void ClusterTester::testAddUsageData() {
	
	cluster = new Cluster();
	DataCollector dataCollector = DataCollector("src/tests/lupa.persisted.test");
	
	vector<UsageData*>* usageDatas = dataCollector.getUnclassifiedData();
	
	cluster->addUsageData(usageDatas->at(0));
	CPPUNIT_ASSERT(usageDatas->at(0)->getCluster() == cluster);
	
	CPPUNIT_ASSERT(cluster->getNumberOfElements() == 1);
	
	//DTMMIBCP
	ResourceData rd;
	UsageData re = cluster->getRepresentativeElement(), *ud;
	ud = &re;
	//ud->simplePrint();
	TEST_USAGE_DATA(0, 0.1f, 222);
	TEST_USAGE_DATA(1, 0.2f, 333);
	TEST_USAGE_DATA(2, 0.3f, 111);
	TEST_USAGE_DATA(3, 0.4f, 999);
	TEST_USAGE_DATA(4, 0.5f, 888);
	TEST_USAGE_DATA(5, 0.6f, 777);
	TEST_USAGE_DATA(6, 0.15f, 2222);
	TEST_USAGE_DATA(7, 0.25f, 3333);
	TEST_USAGE_DATA(8, 0.35f, 1111);
	TEST_USAGE_DATA(9, 0.45f, 9999);
	TEST_USAGE_DATA(10, 0.55f, 8888);
	TEST_USAGE_DATA(11, 0.65f, 7777);
	
	cluster->addUsageData(usageDatas->at(1));
	CPPUNIT_ASSERT(usageDatas->at(1)->getCluster() == cluster);

	CPPUNIT_ASSERT(cluster->getNumberOfElements() == 2);
	
	re = cluster->getRepresentativeElement();
	ud = &re;
	//ud->simplePrint();
	TEST_USAGE_DATA(0, 0.125f, 1222);
	TEST_USAGE_DATA(1, 0.225f, 1833);
	TEST_USAGE_DATA(2, 0.325f, 611);
	TEST_USAGE_DATA(3, 0.425f, 5499);
	TEST_USAGE_DATA(4, 0.525f, 4888);
	TEST_USAGE_DATA(5, 0.625f, 4277);
	TEST_USAGE_DATA(6, 0.4f, 4999.5);
	TEST_USAGE_DATA(7, 0.45f, 5555);
	TEST_USAGE_DATA(8, 0.5f, 4444);
	TEST_USAGE_DATA(9, 0.55f, 8888);
	TEST_USAGE_DATA(10, 0.6f, 8332.5);
	TEST_USAGE_DATA(11, 0.65f, 7777);
	
} 

void ClusterTester::testSatisfiesResource() {
	setCollectInterval (3600 * 8);
	
	UsageData ud1 = UsageData(Timestamp());
	
	ud1.getData()[0] = ResourceData(0.1f, 1000); 
	ud1.getData()[1] = ResourceData(0.2f, 2000); 
	ud1.getData()[2] = ResourceData(0.1f, 3000);
	ud1.getData()[3] = ResourceData(-0.3f, 5000);
	ud1.getData()[4] = ResourceData(-0.1f, 6000);
	ud1.getData()[5] = ResourceData(0.0f, 7000);
	
	cluster = new Cluster();
	cluster->addUsageData (&ud1);
	
	CPPUNIT_ASSERT(cluster->satisfiesResource(CPU_USAGE, 0.09f, 0.5f, 0, 6));
	CPPUNIT_ASSERT(cluster->satisfiesResource(FREE_MEMORY, 900, 4000.0f, 0, 6));

	CPPUNIT_ASSERT(!cluster->satisfiesResource(CPU_USAGE, 0.75f, 0.2f, 0, 6));
	CPPUNIT_ASSERT(!cluster->satisfiesResource(CPU_USAGE, 0.75f, 0.0f, 0, 6));
	CPPUNIT_ASSERT(!cluster->satisfiesResource(FREE_MEMORY, 9000, 1000.0f, 0, 6));
	
	CPPUNIT_ASSERT(!cluster->satisfiesResource(CPU_USAGE, 0.45f, 0.4f, 0, 3));
	CPPUNIT_ASSERT( cluster->satisfiesResource(CPU_USAGE, 0.29f, 0.4f, 1, 3));
	CPPUNIT_ASSERT(!cluster->satisfiesResource(CPU_USAGE, 0.31f, 0.4f, 1, 4));
	
	CPPUNIT_ASSERT( cluster->satisfiesResource(CPU_USAGE, 0.29f, 0.31f, 0, 6));
	CPPUNIT_ASSERT(!cluster->satisfiesResource(CPU_USAGE, 0.61f, 0.7f, 3, 6));
	CPPUNIT_ASSERT(!cluster->satisfiesResource(FREE_MEMORY, 110000, 0, 0, 6));
	CPPUNIT_ASSERT( cluster->satisfiesResource(FREE_MEMORY, 14000, 12001, 2, 5));
	
}


void ClusterTester::testRemoveUsageData() {
	
	cluster = new Cluster();
	DataCollector dataCollector = DataCollector("src/tests/lupa.persisted.test");
	
	vector<UsageData*>* usageDatas = dataCollector.getUnclassifiedData();
	
	cluster->addUsageData(usageDatas->at(0));
	cluster->addUsageData(usageDatas->at(1));
	
	UsageData *ud = cluster->removeUsageData(usageDatas->at(0));
	
	CPPUNIT_ASSERT(cluster->getNumberOfElements() == 1);
	CPPUNIT_ASSERT(ud == usageDatas->at(0));
	
	ResourceData rd;
	UsageData re = cluster->getRepresentativeElement();
	ud = &re;
	//ud->simplePrint();
	TEST_USAGE_DATA(0, 0.15f, 2222);
	TEST_USAGE_DATA(1, 0.25f, 3333);
	TEST_USAGE_DATA(2, 0.35f, 1111);
	TEST_USAGE_DATA(3, 0.45f, 9999);
	TEST_USAGE_DATA(4, 0.55f, 8888);
	TEST_USAGE_DATA(5, 0.65f, 7777);
	TEST_USAGE_DATA(6, 0.65f, 7777);
	TEST_USAGE_DATA(7, 0.65f, 7777);
	TEST_USAGE_DATA(8, 0.65f, 7777);
	TEST_USAGE_DATA(9, 0.65f, 7777);
	TEST_USAGE_DATA(10, 0.65f, 7777);
	TEST_USAGE_DATA(11, 0.65f, 7777);

	ud = cluster->removeUsageData(usageDatas->at(0));
	CPPUNIT_ASSERT(cluster->getNumberOfElements() == 1);
	CPPUNIT_ASSERT(ud == NULL);
	
	re = cluster->getRepresentativeElement();
	ud = &re;
	
	CPPUNIT_ASSERT(ud->distance(usageDatas->at(1), CPU_USAGE, 0, 2 * SAMPLES_PER_DAY) == 0.0f);
	
	// removes the last ud in the cluster
	ud = cluster->removeUsageData(usageDatas->at(1));
	CPPUNIT_ASSERT(cluster->getNumberOfElements() == 0);
	CPPUNIT_ASSERT(ud == usageDatas->at(1));
	
	re = cluster->getRepresentativeElement();
	ud = &re;
	
	for (int i = 0; i < 12; i++)
		CPPUNIT_ASSERT(!ud->getData()[i].isValid());
	
	
} 


