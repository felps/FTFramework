#ifndef CLUSTERANALYZERTESTER_HPP_
#define CLUSTERANALYZERTESTER_HPP_

#include "../ClusterAnalyzer.hpp"

#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>

class ClusterAnalyzerTester : public CppUnit::TestFixture {
public:
	ClusterAnalyzerTester();
	virtual ~ClusterAnalyzerTester();

	void setUp();
	void tearDown();
	void testFindUsagePattern();
	void testHasClusters();

	static CppUnit::TestSuite *suite () {
      CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
      suiteOfTests->addTest(new CppUnit::TestCaller<ClusterAnalyzerTester>("testFindUsagePattern", &ClusterAnalyzerTester::testFindUsagePattern));
      suiteOfTests->addTest(new CppUnit::TestCaller<ClusterAnalyzerTester>("testHasClusters", &ClusterAnalyzerTester::testHasClusters));
      return suiteOfTests;
	}

private:
	vector <Cluster*> clusters;
};

#endif /*CLUSTERANALYZERTESTER_HPP_*/
