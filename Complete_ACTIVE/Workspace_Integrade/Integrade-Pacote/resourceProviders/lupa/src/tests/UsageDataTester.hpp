#ifndef USAGEDATATESTER_H_
#define USAGEDATATESTER_H_

#include "../UsageData.hpp"
#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>


class UsageDataTester : public CppUnit::TestFixture {
public: 
	UsageData ud1, ud2;

	void setUp ();
	void tearDown ();

	void testDistance();
	void testResourceAverage();
	void testMeanCopy();
	void testDerivative();
	void testIsValid();
	void testGoBackOneDay();
				
	static CppUnit::TestSuite *suite () {
		CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
		suiteOfTests->addTest(new CppUnit::TestCaller<UsageDataTester>("testDistance", &UsageDataTester::testDistance));
		suiteOfTests->addTest(new CppUnit::TestCaller<UsageDataTester>("testResourceAverage", &UsageDataTester::testResourceAverage));
		suiteOfTests->addTest(new CppUnit::TestCaller<UsageDataTester>("testMeanCopy", &UsageDataTester::testMeanCopy));
		suiteOfTests->addTest(new CppUnit::TestCaller<UsageDataTester>("testDerivative", &UsageDataTester::testDerivative));
		suiteOfTests->addTest(new CppUnit::TestCaller<UsageDataTester>("testIsValid", &UsageDataTester::testIsValid));
		suiteOfTests->addTest(new CppUnit::TestCaller<UsageDataTester>("testGoBackOneDay", &UsageDataTester::testGoBackOneDay));
		return suiteOfTests;
	}
};

#endif /*USAGEDATATESTER_H_*/
