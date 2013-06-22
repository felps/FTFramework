//
// C++ Interface: RequestLogTester
//
// Description: 
//
//
// Author: Danilo Conde <danconde@danconde-laptop>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//

#ifndef REQUESTLOGTESTER_H_
#define REQUESTLOGTESTER_H_

#include "../RequestLog.hpp"

#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>

class RequestLogTester : public CppUnit::TestFixture {
	public:
		RequestLogTester() {};

		void setUp();
		void tearDown();
		void testOutputOperator();
		void testInputOperator();
			
		static CppUnit::TestSuite *suite () {
			CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
			suiteOfTests->addTest(new CppUnit::TestCaller<RequestLogTester>("testOutputOperator", &RequestLogTester::testOutputOperator));
			suiteOfTests->addTest(new CppUnit::TestCaller<RequestLogTester>("testInputOperator", &RequestLogTester::testInputOperator));
			return suiteOfTests;
		}

};

#endif /*REQUESTLOGTESTER_H_*/
