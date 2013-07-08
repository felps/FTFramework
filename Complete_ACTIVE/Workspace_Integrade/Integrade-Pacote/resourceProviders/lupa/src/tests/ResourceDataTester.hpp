#ifndef RESOURCEDATATESTER_H_
#define RESOURCEDATATESTER_H_

#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>

#include <string>

using namespace std;

class ResourceDataTester : public CppUnit::TestFixture
{
public:
	ResourceDataTester() {};
	
	void setUp();
	void testReadFromInput(); 
   	void testWriteToOutput();
   	void testAdditionOperator();
   	void testDivisionOperator();
   	void testIsValid();
	//void testReadFromInputMemoryDouble();
	void testWriteToOutputMemoryDouble();
	
	static CppUnit::TestSuite *suite () {
      CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
      suiteOfTests->addTest(new CppUnit::TestCaller<ResourceDataTester>("testReadFromInput", &ResourceDataTester::testReadFromInput));
      suiteOfTests->addTest(new CppUnit::TestCaller<ResourceDataTester>("testWriteToOutput", &ResourceDataTester::testWriteToOutput));
      suiteOfTests->addTest(new CppUnit::TestCaller<ResourceDataTester>("testIsValid", &ResourceDataTester::testIsValid));
      suiteOfTests->addTest(new CppUnit::TestCaller<ResourceDataTester>("testAdditionOperator", &ResourceDataTester::testAdditionOperator));
      suiteOfTests->addTest(new CppUnit::TestCaller<ResourceDataTester>("testDivisionOperator", &ResourceDataTester::testDivisionOperator));
      suiteOfTests->addTest(new CppUnit::TestCaller<ResourceDataTester>("testWriteToOutputMemoryDouble", &ResourceDataTester::testWriteToOutputMemoryDouble));
      //suiteOfTests->addTest(new CppUnit::TestCaller<ResourceDataTester>("testReadFromInputMemoryDouble", &ResourceDataTester::testReadFromInputMemoryDouble));
      return suiteOfTests;
  	}
private:
	bool testReadFromInputParameters(string s, double cpu, double memory);
};

#endif /*RESOURCEDATATESTER_H_*/
