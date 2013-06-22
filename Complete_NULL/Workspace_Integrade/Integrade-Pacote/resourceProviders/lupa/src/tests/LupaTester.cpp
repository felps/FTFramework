#include <time.h>
#include <stdlib.h>
#include <stdio.h>

#include "TimestampTester.hpp"
#include "ResourceDataTester.hpp"
#include "DataCollectorTester.hpp"
#include "ClusterTester.hpp"
#include "UsageDataTester.hpp"
#include "ClusteringAlgorithmsTester.hpp"
#include "ClusterAnalyzerTester.hpp"
#include "UsagePredictorTester.hpp"
#include "RequestLogTester.hpp"


#include <cppunit/TextTestRunner.h>

using namespace std;

int main (void) {
	
	CppUnit::TextTestRunner runner;
 	runner.addTest( TimestampTester::suite() );
 	runner.addTest( ResourceDataTester::suite() );
 	runner.addTest( ClusterTester::suite() );
 	runner.addTest( UsageDataTester::suite() );
 	runner.addTest( ClusteringAlgorithmsTester::suite() );
 	runner.addTest( DataCollectorTester::suite() );
 	runner.addTest( ClusterAnalyzerTester::suite() );
	runner.addTest( UsagePredictorTester::suite() );
	runner.addTest( RequestLogTester::suite() );
	
 	runner.run();    // Run all tests and wait
 
 	return 0;
}
