//
// C++ Implementation: RequestLogTester
//
// Description: 
//
//
// Author: Danilo Conde <danconde@danconde-laptop>, (C) 2007
//
// Copyright: See COPYING file that comes with this distribution
//
//

#include "RequestLogTester.hpp"

#include <ostream>
#include <string>
#include <vector>
#include <math.h>

using namespace std;


/**
 * DTMMIBCP = Don't tell my mother i've been copying & pasting (source: DataCollectorTester)
 */
#define EPSILON 0.00001f
#define TEST_USAGE_DATA(IX, C, M) rd = ud->getData().at(IX); CPPUNIT_ASSERT((fabsf(rd.getCpuUsage() - C)) < EPSILON); CPPUNIT_ASSERT(fabsf(rd.getFreeMemory()- M) < EPSILON);
#define FLOAT_EQUALITY(A, B) (fabsf(A - B) < EPSILON)


void RequestLogTester::tearDown() {
	setCollectInterval(DEFAULT_COLLECT_INTERVAL);
}

void RequestLogTester::setUp() {
	timezone = 10800;
}

void RequestLogTester::testOutputOperator() {
	ostringstream oss;
	string s;
	RequestLog rl(Timestamp(1155408125), ResourceData(0.5f, 1000), true);
	oss << rl;

	s = oss.str();

	//cout << s;
	CPPUNIT_ASSERT(s.compare("1155408125\t0.5\t1000\t1") == 0);

}

void RequestLogTester::testInputOperator() {
	RequestLog rl;
	istringstream iss;
	iss.str("1155408125\t0.5\t1000\t0\n");
	
	iss >> rl;

	ResourceData rd = rl.getResourceRequest();
	
	CPPUNIT_ASSERT(rl.getTimestamp().getRawTime() == 1155408125);
	CPPUNIT_ASSERT(rl.getAnswer() == false);
	CPPUNIT_ASSERT(FLOAT_EQUALITY(rd.getCpuUsage(), 0.5f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(rd.getFreeMemory(), 1000));

}



