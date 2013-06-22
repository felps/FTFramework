#ifndef USAGEPREDICTORTESTER_HPP_
#define USAGEPREDICTORTESTER_HPP_

#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>

#include "TestSystemAnalyzer.hpp"
#include "TestCurrentTimestampFactory.hpp"
#include "../UsagePredictor.hpp"

class UsagePredictorTester : public CppUnit::TestFixture {
public:
	UsagePredictorTester();
	virtual ~UsagePredictorTester();

	void setUp();
	void tearDown();
	void testCanRunGridApplication();
	void testCanRunGridApplicationWithoutClusters();
	void testCanRunGridApplicationWithoutRecentData();
	void testGetPrediction();
	void setUpWithData(DataCollector **dtCollector, UsagePredictor **up);
	
	static CppUnit::TestSuite *suite () {
		CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
		suiteOfTests->addTest(new CppUnit::TestCaller<UsagePredictorTester>("testCanRunGridApplication", &UsagePredictorTester::testCanRunGridApplication));
		suiteOfTests->addTest(new CppUnit::TestCaller<UsagePredictorTester>("testCanRunGridApplicationWithoutClusters", &UsagePredictorTester::testCanRunGridApplicationWithoutClusters));
		suiteOfTests->addTest(new CppUnit::TestCaller<UsagePredictorTester>("testCanRunGridApplicationWithoutRecentData", &UsagePredictorTester::testCanRunGridApplicationWithoutRecentData));
		suiteOfTests->addTest(new CppUnit::TestCaller<UsagePredictorTester>("testGetPrediction", &UsagePredictorTester::testGetPrediction));
		return suiteOfTests;
	}
private:
	TestSystemAnalyzer *testSAnal;
	TestCurrentTimestampFactory *testTimestampFactory;
};

#endif /*USAGEPREDICTORTESTER_HPP_*/
