#ifndef CLUSTERINGALGORITHMSTESTER_
#define CLUSTERINGALGORITHMSTESTER_

#include "../Cluster.hpp"

#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>

class ClusteringAlgorithmsTester : public CppUnit::TestFixture {
public:
	ClusteringAlgorithmsTester() {};

	void setUp();
	void tearDown();
	void testKMeans(); 
	void testGetClosestCluster();
	void testGetDistanceElementToCluster();
	
	static CppUnit::TestSuite *suite () {
      CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
//      suiteOfTests->addTest(new CppUnit::TestCaller<ClusteringAlgorithmsTester>("testKMeans", &ClusteringAlgorithmsTester::testKMeans));
      suiteOfTests->addTest(new CppUnit::TestCaller<ClusteringAlgorithmsTester>("testGetDistanceElementToCluster", &ClusteringAlgorithmsTester::testGetDistanceElementToCluster));
      suiteOfTests->addTest(new CppUnit::TestCaller<ClusteringAlgorithmsTester>("testGetClosestCluster", &ClusteringAlgorithmsTester::testGetClosestCluster));
      return suiteOfTests;
	}

//private:
	//Cluster *cluster;
};

#endif /*CLUSTERINGALGORITHMSTESTER_*/
