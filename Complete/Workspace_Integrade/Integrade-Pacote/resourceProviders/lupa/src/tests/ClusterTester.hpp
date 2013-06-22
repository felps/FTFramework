#ifndef CLUSTERTESTER_H_
#define CLUSTERTESTER_H_

#include "../Cluster.hpp"

#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>

class ClusterTester : public CppUnit::TestFixture {
public:
	ClusterTester() {};

	void setUp();
	void tearDown();
	void testAddUsageData(); 
	void testSatisfiesResource(); 
	void testRemoveUsageData();
			
	static CppUnit::TestSuite *suite () {
      CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
      suiteOfTests->addTest(new CppUnit::TestCaller<ClusterTester>("testAddUsageData", &ClusterTester::testAddUsageData));
      suiteOfTests->addTest(new CppUnit::TestCaller<ClusterTester>("testSatisfiesResource", &ClusterTester::testSatisfiesResource));
      suiteOfTests->addTest(new CppUnit::TestCaller<ClusterTester>("testRemoveUsageData", &ClusterTester::testRemoveUsageData));
      return suiteOfTests;
	}

private:
	Cluster *cluster;
};

#endif /*CLUSTERTESTER_H_*/
