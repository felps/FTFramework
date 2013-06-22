#include "ResourceDataTester.hpp"
#include "../ResourceData.hpp"
#include <cppunit/extensions/HelperMacros.h>

#include <sstream>
#include <iostream>

#include <math.h>

#define EPSILON 0.0001f
#define FLOAT_EQUALITY(A, B) (fabsf(A - B) < EPSILON)

using namespace std;

void ResourceDataTester::setUp() {
	
}

bool ResourceDataTester::testReadFromInputParameters(string s, double cpu, double memory) {
	ResourceData rd;
	istringstream iss;
	iss.str(s);
	
	iss >> rd;
	
	return 	FLOAT_EQUALITY(rd.getCpuUsage(), cpu) &&
		FLOAT_EQUALITY(rd.getFreeMemory(), memory) &&
		!rd.isEstimate();

}

void ResourceDataTester::testReadFromInput() {

	CPPUNIT_ASSERT(testReadFromInputParameters(string("0.25\t476896"), 0.25f, 476896));
	CPPUNIT_ASSERT(testReadFromInputParameters(string("5.00325e-05\t1.78624e+06"), 0.00005f, 1786240));
	CPPUNIT_ASSERT(testReadFromInputParameters(string("0.000316883\t1.78624e+06"), 0.000316883f, 1786240));
	
} 

/*void ResourceDataTester::testReadFromInputMemoryDouble() {
	ResourceData rd;
	istringstream iss;
	string s = "0.25\t476896.4\n0.22\t555"; 
	iss.str(s);
	
	iss >> rd;
	
	CPPUNIT_ASSERT(FLOAT_EQUALITY(rd.getCpuUsage(), 0.25f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(rd.getFreeMemory(), 476896));
	CPPUNIT_ASSERT(!rd.isEstimate());
	
	iss >> rd;
	cout << rd << endl;
	CPPUNIT_ASSERT(FLOAT_EQUALITY(rd.getCpuUsage(), 0.22f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(rd.getFreeMemory(), 555));
	CPPUNIT_ASSERT(!rd.isEstimate());
	
} */

void ResourceDataTester::testWriteToOutputMemoryDouble() {
	ResourceData rd (0.25f, 476896.5f);
	ostringstream oss;
	
	oss << rd;
	string s = oss.str();
	
	CPPUNIT_ASSERT(FLOAT_EQUALITY(rd.getFreeMemory(), 476896.5f));
	CPPUNIT_ASSERT(s.compare("0.25\t476896") == 0);
}

void ResourceDataTester::testWriteToOutput() {
	ResourceData rd (0.25f, 476896);
	ostringstream oss;
	
	oss << rd;
	string s = oss.str();
	
	CPPUNIT_ASSERT(s.compare("0.25\t476896") == 0);
}


void ResourceDataTester::testIsValid() {
	ResourceData rd (0.25f, 476896), rd2;

	CPPUNIT_ASSERT(rd.isValid());
	CPPUNIT_ASSERT(!rd2.isValid());
	
	CPPUNIT_ASSERT(!rd.isEstimate());
	CPPUNIT_ASSERT(!rd2.isEstimate());
}

void ResourceDataTester::testAdditionOperator() {
	ResourceData a (0.3f, 500), b(0.1f, 100), c;
	
	c = a + b;

	CPPUNIT_ASSERT(FLOAT_EQUALITY(c.getCpuUsage(), 0.4f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(c.getFreeMemory(), 600));
}
 
void ResourceDataTester::testDivisionOperator() {
	ResourceData a (0.25f, 500), c;
	
	c = a / 2;

	CPPUNIT_ASSERT(c.getCpuUsage() == 0.125f);
	CPPUNIT_ASSERT(c.getFreeMemory() == 250);
}
