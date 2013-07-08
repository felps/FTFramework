#ifndef DATACOLLECTORTESTER_H_
#define DATACOLLECTORTESTER_H_

#include "TestCurrentTimestampFactory.hpp"
#include "TestSystemAnalyzer.hpp"
#include "../DataCollector.hpp"

#include <fstream>

#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>

class DataCollectorTester : public CppUnit::TestFixture
{
public:
	DataCollectorTester() {};
	
	DataCollector dataCollector;
	ofstream *ofs;
	TestCurrentTimestampFactory* testTimestampFactory;
	
	void setUp();
	void tearDown();
	void testGetUnclassifiedData(); 
	void testGetUnclassifiedDataLogFileGoindBackwards();
	void testCollectData(); 
	void testCollectDataOvernight();
	void testHasEnoughDataToPredict();
	
	static CppUnit::TestSuite *suite () {
      CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
      suiteOfTests->addTest(new CppUnit::TestCaller<DataCollectorTester>("testGetUnclassifiedData", &DataCollectorTester::testGetUnclassifiedData));
      suiteOfTests->addTest(new CppUnit::TestCaller<DataCollectorTester>("testGetUnclassifiedDataLogFileGoindBackwards", &DataCollectorTester::testGetUnclassifiedDataLogFileGoindBackwards));
      suiteOfTests->addTest(new CppUnit::TestCaller<DataCollectorTester>("testCollectData", &DataCollectorTester::testCollectData));
      suiteOfTests->addTest(new CppUnit::TestCaller<DataCollectorTester>("testCollectDataOvernight", &DataCollectorTester::testCollectDataOvernight));
      suiteOfTests->addTest(new CppUnit::TestCaller<DataCollectorTester>("testHasEnoughDataToPredict", &DataCollectorTester::testHasEnoughDataToPredict));
      return suiteOfTests;
  	}

private:
	TestSystemAnalyzer *testSAnal;
};

#endif /*DATACOLLECTORTESTER_H_*/
