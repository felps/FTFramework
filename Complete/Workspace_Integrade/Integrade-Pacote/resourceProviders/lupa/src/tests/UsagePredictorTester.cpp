#include "UsagePredictorTester.hpp"

#include "../KMeansClusteringAlgorithm.hpp"
#include "../LupaConstants.hpp"

#include <fstream>

#define ASSERT_MAX_FREE_CPU_USAGE(__CPU_MAX, __BOOLEAN) max = -1000.0f; for (i = 0; i < prediction.size(); i++) if (prediction[i] > max) max = prediction[i]; CPPUNIT_ASSERT(((1.0f - max) > __CPU_MAX) == __BOOLEAN);
#define ASSERT_MAX_FREE_MEMORY(__MEMORY_MAX, __BOOLEAN) max = -1000.0f; for (i = 0; i < prediction.size(); i++) if (prediction[i] > max) max = prediction[i]; CPPUNIT_ASSERT((max > __MEMORY_MAX) == __BOOLEAN);
   

UsagePredictorTester::UsagePredictorTester() {}

UsagePredictorTester::~UsagePredictorTester() {}


void UsagePredictorTester::tearDown() {
	setCollectInterval(DEFAULT_COLLECT_INTERVAL);
	setK(DEFAULT_K);
	setValidResourceDataThreshold(DEFAULT_VALID_RESOURCE_DATA_THRESHOLD);
	setPredictionHours(DEFAULT_PREDICTION_HOURS);
	setHoursToConsiderInAverage(DEFAULT_HOURS_TO_CONSIDER_IN_AVERAGE);
}

void UsagePredictorTester::setUp() {

	setCollectInterval (3600 * 8); // 3 resourcedata per day
	setK(3);
	setValidResourceDataThreshold(0.01f);

}

void UsagePredictorTester::testCanRunGridApplicationWithoutClusters() {

      setHoursToConsiderInAverage(8);
   
      testTimestampFactory = new TestCurrentTimestampFactory();
	vector<Timestamp> v;

	v.push_back(Timestamp(1160710078));
	v.push_back(Timestamp(1160738878));
	v.push_back(Timestamp(1160767678)); //Fri 2006-10-13 16:27:58
	v.push_back(Timestamp(1160796478));
	v.push_back(Timestamp(1160825278));
	v.push_back(Timestamp(1160854078));
	v.push_back(Timestamp(1160882878));
	v.push_back(Timestamp(1160911678));
	v.push_back(Timestamp(1160940478));
	v.push_back(Timestamp(1160969278));
	v.push_back(Timestamp(1160998078));
	v.push_back(Timestamp(1161026878));
	v.push_back(Timestamp(1161055678));
	v.push_back(Timestamp(1161084478));
	v.push_back(Timestamp(1161113278));
	v.push_back(Timestamp(1161142078));
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

	ofstream *ofs = new ofstream("src/tests/lupa.persisted.test");
	*ofs << "" << endl;
	ofs->close();

	DataCollector *collector = new DataCollector("src/tests/lupa.persisted.test",
							testSAnal,
							testTimestampFactory);
	UsagePredictor *up = new UsagePredictor(new KMeansClusteringAlgorithm(), collector);

	setK(5);

	Timestamp now = testTimestampFactory->getCurrentTimestamp();
	// data collector and cluster analyzer with no data
	CPPUNIT_ASSERT(up->canRunGridApplication(now, ResourceData(0.5f, 100000)));
	
	// only cluster analyzer with no data
	// fills recent usage data
	for (unsigned int i = 0; i < 9; i++)
		collector->collectData();

	// predict base only on the recent usage data
	CPPUNIT_ASSERT(collector->hasEnoughRecentUsageToPredict(now, 8));
	CPPUNIT_ASSERT(!up->canRunGridApplication(now, ResourceData(0.9f, 100000)));
	CPPUNIT_ASSERT(!up->canRunGridApplication(now, ResourceData(0.4f, 100000)));
	CPPUNIT_ASSERT( up->canRunGridApplication(now, ResourceData(0.4f, 1000)));
}

void UsagePredictorTester::testCanRunGridApplicationWithoutRecentData() {

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
	
	Timestamp now(1155618001);

	DataCollector *collector = new DataCollector("src/tests/lupa.persisted.test");
	UsagePredictor *up = new UsagePredictor(new KMeansClusteringAlgorithm(), collector);
	
	// only data collector with no data
	CPPUNIT_ASSERT(!collector->hasEnoughRecentUsageToPredict(now));
	CPPUNIT_ASSERT(up->canRunGridApplication(now, ResourceData(-0.99f, 00000)));
	
}


void UsagePredictorTester::setUpWithData(DataCollector **dtCollector, UsagePredictor **up) {
   setCollectInterval(3600 * 6);
   setValidResourceDataThreshold(0.51f);

   ofstream *ofs = new ofstream("src/tests/lupa.persisted.test");

   *ofs << "1155351601	0.2	1000" << endl /*Sat 2006-08-12 00:00:01*/
	 << "1155373201	0.3	5000" << endl
	 << "1155394801	0.4	1000" << endl
	 << "1155416401	0.5	5000" << endl
	 << "1155438001	0.30	1000" << endl /* -- */
	 << "1155459601	0.25	1100" << endl
	 << "1155481201	0.6	1050" << endl
	 << "1155502801	0.10	900" << endl;
// the lines below are going to written by the collector
/*
	 << "1155524401	0.8	10000" << endl 
	 << "1155546001	0.7	12000" << endl
	 << "1155567601	0.6	8000" << endl
	 << "1155589201	0.30	6000" << endl
	 << "1155610801	0.3	6000" << endl
	 << "1155632401	0.4	5000" << endl
	 << "1155654001	0.5	4000" << endl
	 << "1155675601	0.55	3000" << endl;
*/

   ofs->close();
   
   vector<ResourceData> list;
   list.push_back(ResourceData(0.8f, 10000));
   list.push_back(ResourceData(0.7f, 12000));
   list.push_back(ResourceData(0.6f, 8000));
   list.push_back(ResourceData(0.3f, 6000));
   
   list.push_back(ResourceData(0.3f, 6000));
   list.push_back(ResourceData(0.4f, 5000));
   list.push_back(ResourceData(0.5f, 4000));
   list.push_back(ResourceData(0.55f, 3000));
   
   testSAnal = new TestSystemAnalyzer();
   testSAnal->setResourceDatasToReturn(list);
   
   testTimestampFactory = new TestCurrentTimestampFactory();
   vector<Timestamp> v;
   v.push_back(Timestamp(1155524401)); 
   v.push_back(Timestamp(1155546001));
   v.push_back(Timestamp(1155567601));
   v.push_back(Timestamp(1155589201));
   
   v.push_back(Timestamp(1155610801));
   v.push_back(Timestamp(1155632401));
   v.push_back(Timestamp(1155654001));
   v.push_back(Timestamp(1155675601));

   testTimestampFactory->setTimestampsToReturn(v);
   *dtCollector = new DataCollector("src/tests/lupa.persisted.test", testSAnal, testTimestampFactory);

   *up = new UsagePredictor(new KMeansClusteringAlgorithm(), *dtCollector);

   testTimestampFactory->setTimestampsToReturn(v);
   for (int i = 0; i < 8; i++)
      (*dtCollector)->collectData();

}

void UsagePredictorTester::testCanRunGridApplication() {
   DataCollector *dtCollector;
   UsagePredictor *up;
   
   setUpWithData(&dtCollector, &up);
   
   CPPUNIT_ASSERT(dtCollector->hasEnoughRecentUsageToPredict(Timestamp(1155697201)));
   CPPUNIT_ASSERT(up->canRunGridApplication(Timestamp(1155697201), ResourceData(0.76f, 1000)));
   CPPUNIT_ASSERT(!up->canRunGridApplication(Timestamp(1155697201), ResourceData(0.77f, 5000)));

   // test that the prediction hours are taken into account when predicting resource usage
   setPredictionHours(12);
   CPPUNIT_ASSERT(up->canRunGridApplication(Timestamp(1155697201), ResourceData(0.76f, 13599)));
   CPPUNIT_ASSERT(!up->canRunGridApplication(Timestamp(1155697201), ResourceData(0.81f, 1000)));
   CPPUNIT_ASSERT(!up->canRunGridApplication(Timestamp(1155697201), ResourceData(0.70f, 15610)));
   
   
   CPPUNIT_ASSERT(!up->canRunGridApplication(Timestamp(1155697201), ResourceData(0.1f, 5000000)));
   CPPUNIT_ASSERT(!up->canRunGridApplication(Timestamp(1155697201), ResourceData(0.77f, 5000)));
   CPPUNIT_ASSERT(!up->canRunGridApplication(Timestamp(1155697201), ResourceData(0.01f, 9995000)));
}


void UsagePredictorTester::testGetPrediction() {
   DataCollector *dtCollector;
   UsagePredictor *up;
   vector<double> prediction;
   unsigned int i;
   double max;
   
   setUpWithData(&dtCollector, &up);
   
   // the assertions below are equivalent to the assertions of the method testCanRunGridApplication
   
   prediction = up->getPrediction(Timestamp(1155697201), CPU_USAGE, 6);
   ASSERT_MAX_FREE_CPU_USAGE(0.76f, true);
   ASSERT_MAX_FREE_CPU_USAGE(0.77f, false);
   //max = 0.0f; for (i = 0; i < prediction.size(); i++) if (prediction[i] > max) max = prediction[i]; CPPUNIT_ASSERT((1.0f - prediction[i]) > 0.76f);
   //for (i = 0; i < prediction.size(); i++) CPPUNIT_ASSERT(!((1.0f - prediction[i]) > 0.77f));
   
   prediction = up->getPrediction(Timestamp(1155697201), FREE_MEMORY, 6);
   ASSERT_MAX_FREE_MEMORY(1000, true);
   
   prediction = up->getPrediction(Timestamp(1155697201), CPU_USAGE, 12);
   ASSERT_MAX_FREE_CPU_USAGE(0.76f, true);
   ASSERT_MAX_FREE_CPU_USAGE(0.82f, false);
   ASSERT_MAX_FREE_CPU_USAGE(0.70f, true);
   
   prediction = up->getPrediction(Timestamp(1155697201), FREE_MEMORY, 12);
   ASSERT_MAX_FREE_MEMORY(15610, false);
   ASSERT_MAX_FREE_MEMORY(13599, true);
   ASSERT_MAX_FREE_MEMORY(50000000, false);
   
}
