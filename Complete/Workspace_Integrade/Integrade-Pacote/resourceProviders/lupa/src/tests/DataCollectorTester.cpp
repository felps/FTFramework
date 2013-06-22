#include "DataCollectorTester.hpp"
#include "TestCurrentTimestampFactory.hpp"

#include "../DataCollector.hpp"
#include "../UsageData.hpp"
#include "../LupaConstants.hpp"

#include <vector>
#include <fstream>
#include <sstream>
#include <iostream>
#include <string>

#include <cppunit/extensions/HelperMacros.h>

#define INDEX_OF_SECOND_IN_USAGE_DATA(X) (((X-timezone) % 86400)/COLLECT_INTERVAL)
#define TEST_USAGE_DATA(IX, C, M, E) rd = ud->getData().at(IX); CPPUNIT_ASSERT(rd.getCpuUsage() == C); CPPUNIT_ASSERT(rd.getFreeMemory() == M); CPPUNIT_ASSERT(rd.isEstimate() == E)

using namespace std;

void DataCollectorTester::tearDown() {
	setCollectInterval (DEFAULT_COLLECT_INTERVAL);
	setValidResourceDataThreshold(DEFAULT_VALID_RESOURCE_DATA_THRESHOLD);
}

void DataCollectorTester::setUp() {

	setCollectInterval (3600 * 6);

	testTimestampFactory = new TestCurrentTimestampFactory();
	vector<Timestamp> v;
	v.push_back(Timestamp(1160767678)); //Fri 2006-10-13 16:27:58
	v.push_back(Timestamp(1160767978));
	v.push_back(Timestamp(1160768278));
	
	testTimestampFactory->setTimestampsToReturn(v);

	vector<ResourceData> list;
	list.push_back(ResourceData(0.50f, 10000));
	list.push_back(ResourceData(0.51f, 20000));
	list.push_back(ResourceData(0.52f, 30000));
	list.push_back(ResourceData(0.53f, 40000));
	list.push_back(ResourceData(0.54f, 50000));
	list.push_back(ResourceData(0.55f, 60000));
	list.push_back(ResourceData(0.56f, 70000));
	list.push_back(ResourceData(0.57f, 80000));
	list.push_back(ResourceData(0.58f, 90000));
	
	testSAnal = new TestSystemAnalyzer();
	testSAnal->setResourceDatasToReturn(list);

	dataCollector = DataCollector("src/tests/lupa.persisted.test", testSAnal, testTimestampFactory);
	ofs = new ofstream("src/tests/lupa.persisted.test");

	*ofs << "1155354300      0.1       222" << endl /*Sat 2006-08-12 00:45:00*/
//		<< "1155375900      0.2       333" << endl
		<< "1155397500      0.3       111" << endl
		<< "1155440700      0.4       999" << endl /*Sun 2006-08-13 00:45:00*/
		<< "1155462300      0.5       888" << endl
		<< "1155483900      0.6       777" << endl
		<< "1155548700      0.41       123" << endl /*Mon 2006-08-14 06:45:00*/
		<< "1155570300      0.51       234" << endl
		<< "1155743100      0.22       1888" << endl; /*wed 2006-08-16 18:45:00*/

	ofs->close();
	timezone = 10800;
}

void DataCollectorTester::testGetUnclassifiedData() {
	// testar que os usageData lidos est�o corretos 
	ResourceData rd;
	UsageData* ud;
	 
	vector<UsageData*>* usageDatas = dataCollector.getUnclassifiedData();
	
	CPPUNIT_ASSERT(usageDatas->size() == 4);
	CPPUNIT_ASSERT(usageDatas->at(0)->getDate().getRawTime() == 1155354300);
	CPPUNIT_ASSERT(usageDatas->at(1)->getDate().getRawTime() == 1155440700);
	CPPUNIT_ASSERT(usageDatas->at(2)->getDate().getRawTime() == 1155548700);
	CPPUNIT_ASSERT(usageDatas->at(3)->getDate().getRawTime() == 1155743100);

	ud = usageDatas->at(0);
	TEST_USAGE_DATA(0, 0.1f, 222, false);
	TEST_USAGE_DATA(1, 0.1f, 222, true);
	TEST_USAGE_DATA(2, 0.3f, 111, false);
	TEST_USAGE_DATA(3, 0.3f, 111, true);
	TEST_USAGE_DATA(4, 0.4f, 999, false);
	TEST_USAGE_DATA(5, 0.5f, 888, false);
	TEST_USAGE_DATA(6, 0.6f, 777, false);
	TEST_USAGE_DATA(7, 0.6f, 777, true);
	
	ud = usageDatas->at(1);
	TEST_USAGE_DATA(0, 0.4f, 999, false);
	TEST_USAGE_DATA(1, 0.5f, 888, false);
	TEST_USAGE_DATA(2, 0.6f, 777, false);
	TEST_USAGE_DATA(3, 0.6f, 777, true);
	TEST_USAGE_DATA(4, 0.6f, 777, true);
	TEST_USAGE_DATA(5, 0.41f, 123, false);
	TEST_USAGE_DATA(6, 0.51f, 234, false);
	TEST_USAGE_DATA(7, 0.51f, 234, true);
		
	ud = usageDatas->at(2);
	TEST_USAGE_DATA(0, 0.6f, 777, true);
	TEST_USAGE_DATA(1, 0.41f, 123, false);
	TEST_USAGE_DATA(2, 0.51f, 234, false);
	TEST_USAGE_DATA(3, 0.51f, 234, true);
	TEST_USAGE_DATA(4, 0.51f, 234, true);
	TEST_USAGE_DATA(5, 0.51f, 234, true);
	TEST_USAGE_DATA(6, 0.51f, 234, true);
	TEST_USAGE_DATA(7, 0.51f, 234, true);

	ud = usageDatas->at(3);
	TEST_USAGE_DATA(0, 0.51f, 234, true);
	TEST_USAGE_DATA(1, 0.51f, 234, true);
	TEST_USAGE_DATA(2, 0.22f, 1888, false);
	TEST_USAGE_DATA(3, 0.22f, 1888, true);
	TEST_USAGE_DATA(4, 0.22f, 1888, true);
	TEST_USAGE_DATA(5, 0.22f, 1888, true);
	TEST_USAGE_DATA(6, 0.22f, 1888, true);
	TEST_USAGE_DATA(7, 0.22f, 1888, true);
} 

void DataCollectorTester::testCollectData() {
	vector<UsageData*>* usageDatas = dataCollector.getUnclassifiedData();
	CPPUNIT_ASSERT(usageDatas->size() == 4);

	dataCollector.collectData();
	
	usageDatas = dataCollector.getUnclassifiedData();
	CPPUNIT_ASSERT(usageDatas->size() == 5);
	
	CPPUNIT_ASSERT(dataCollector.getRecentUsageData()->getDate().isSameDay(Timestamp(1160767978).addDays(-1)));
	
	//TODO: fazer um getRecentUsageData pro dataCollector
	
}

void DataCollectorTester::testCollectDataOvernight() {
	vector<Timestamp> v;
	v.push_back(Timestamp(1160794406)); //13-10-2006 23:53:34
	v.push_back(Timestamp(1160794706));
	v.push_back(Timestamp(1160795006));
	v.push_back(Timestamp(1160795306));
	testTimestampFactory->setTimestampsToReturn(v);
	
	dataCollector.collectData();
	dataCollector.collectData();
	
	UsageData *ud = dataCollector.getRecentUsageData();
	
	CPPUNIT_ASSERT(ud->getData().at(INDEX_OF_SECOND_IN_USAGE_DATA(1160794406)).isValid());
	CPPUNIT_ASSERT(ud->getData().at(INDEX_OF_SECOND_IN_USAGE_DATA(1160794706)).isValid());
	
	dataCollector.collectData();
	dataCollector.collectData();

	ud = dataCollector.getRecentUsageData();

	CPPUNIT_ASSERT(ud->getData().at(INDEX_OF_SECOND_IN_USAGE_DATA(1160794406)).isValid());
	CPPUNIT_ASSERT(ud->getData().at(INDEX_OF_SECOND_IN_USAGE_DATA(1160794706)).isValid());
	CPPUNIT_ASSERT(ud->getData().at(INDEX_OF_SECOND_IN_USAGE_DATA(1160795006) + SAMPLES_PER_DAY).isValid());
	CPPUNIT_ASSERT(ud->getData().at(INDEX_OF_SECOND_IN_USAGE_DATA(1160795306) + SAMPLES_PER_DAY).isValid());

	//TODO: maybe it would be good test if all the other system datas are invalid..
}

void DataCollectorTester::testGetUnclassifiedDataLogFileGoindBackwards() {
	ofs = new ofstream("src/tests/lupa.persisted.test");
	*ofs << "1155354300      0.1       222" << endl /*Sat 2006-08-12 00:45:00*/
//		<< "1155375900      0.2       333" << endl
		<< "1155397500      0.3       111" << endl
		<< "1155419100      0.31       211" << endl/*Sat 2006-08-12 18:45:00*/
		<< "1155440700      0.4       999" << endl /*Sun 2006-08-13 00:45:00*/
		<< "1155419100      0.13       111" << endl  /* <--- back to Sat 2006-08-12 18:45:00  ==> must be ignored */
		<< "1155462300      0.5       888" << endl
		<< "1155483900      0.6       777" << endl;

	ofs->close();

	ResourceData rd;
	UsageData* ud;
	 
	vector<UsageData*>* usageDatas = dataCollector.getUnclassifiedData();
	
	CPPUNIT_ASSERT(usageDatas->size() == 2);
	CPPUNIT_ASSERT(usageDatas->at(0)->getDate().getRawTime() == 1155354300);
	CPPUNIT_ASSERT(usageDatas->at(1)->getDate().getRawTime() == 1155440700);

	ud = usageDatas->at(0);
	TEST_USAGE_DATA(0, 0.1f, 222, false);
	TEST_USAGE_DATA(1, 0.1f, 222, true);
	TEST_USAGE_DATA(2, 0.3f, 111, false);
	TEST_USAGE_DATA(3, 0.31f, 211, false);
	TEST_USAGE_DATA(4, 0.4f, 999, false);
	TEST_USAGE_DATA(5, 0.5f, 888, false);
	TEST_USAGE_DATA(6, 0.6f, 777, false);
	TEST_USAGE_DATA(7, 0.6f, 777, true);
	
}


void DataCollectorTester::testHasEnoughDataToPredict() {
	
	setCollectInterval (3600 * 6);

	testTimestampFactory = new TestCurrentTimestampFactory();
	vector<Timestamp> v;
	
	v.push_back(Timestamp(1160724478));
	v.push_back(Timestamp(1160746078)); 
	v.push_back(Timestamp(1160767678)); //Fri 2006-10-13 16:27:58
	v.push_back(Timestamp(1160789278));
	v.push_back(Timestamp(1160810878));
	v.push_back(Timestamp(1160832478));
	v.push_back(Timestamp(1160854078));
	v.push_back(Timestamp(1160875678));
	v.push_back(Timestamp(1160897278));
	v.push_back(Timestamp(1160918878));
	
	testTimestampFactory->setTimestampsToReturn(v);
	dataCollector = DataCollector("src/tests/lupa.persisted.test", testSAnal, testTimestampFactory);

	Timestamp t(1160724478); // this timestamp corresponds to the first valid system data in recent usage data
	testTimestampFactory->setTimestampsToReturn(v);
	dataCollector.collectData();
	dataCollector.collectData();

	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 2*getCollectInterval())));

	dataCollector.collectData();
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 2*getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 3*getCollectInterval())));

	dataCollector.collectData();
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 2*getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 3*getCollectInterval())));
	CPPUNIT_ASSERT( dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 4*getCollectInterval())));
	
	setValidResourceDataThreshold(0.75f);
	CPPUNIT_ASSERT( dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 2*getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 3*getCollectInterval())));
	CPPUNIT_ASSERT( dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 4*getCollectInterval())));
	
	setValidResourceDataThreshold(DEFAULT_VALID_RESOURCE_DATA_THRESHOLD);

	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 5*getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 6*getCollectInterval())));

	dataCollector.collectData();
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 2*getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 3*getCollectInterval())));
	CPPUNIT_ASSERT( dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 4*getCollectInterval())));
	CPPUNIT_ASSERT( dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 5*getCollectInterval())));
	CPPUNIT_ASSERT(!dataCollector.hasEnoughRecentUsageToPredict(Timestamp(t.getRawTime() + 6*getCollectInterval())));

}
