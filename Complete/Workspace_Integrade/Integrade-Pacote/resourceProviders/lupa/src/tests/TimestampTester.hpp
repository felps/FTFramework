#ifndef TIMESTAMPTESTER_HPP_
#define TIMESTAMPTESTER_HPP_

#include "../Timestamp.hpp"
#include <cppunit/TestSuite.h>
#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>


class TimestampTester : public CppUnit::TestFixture {
 protected:

 public:
	Timestamp t1, t2, t3, t4;
   	TimestampTester() {};

   void setUp ();
   
   // tests
   void testDaysApart();
   void testReadFromInput(); 
   void testWriteToOutput();
   void testGetSecondInDay();
   void testIsSameDay();
   void testSecondsApart();
   void testGreaterThanOperator();
   void testBegginingOfSameDay();
   void testAddDays();
   
   static CppUnit::TestSuite *suite () {
      CppUnit::TestSuite *suiteOfTests = new CppUnit::TestSuite;
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testReadFromInput", &TimestampTester::testReadFromInput));
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testWriteToOutput", &TimestampTester::testWriteToOutput));
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testDaysApart", &TimestampTester::testDaysApart));
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testGetSecondInDay", &TimestampTester::testGetSecondInDay));
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testIsSameDay", &TimestampTester::testIsSameDay));
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testSecondsApart", &TimestampTester::testSecondsApart));
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testGreaterThanOperator", &TimestampTester::testGreaterThanOperator));
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testBegginingOfSameDay", &TimestampTester::testBegginingOfSameDay));
      suiteOfTests->addTest(new CppUnit::TestCaller<TimestampTester>("testAddDays", &TimestampTester::testAddDays));
      return suiteOfTests;
  }
 };


#endif /*TIMESTAMPTESTER_HPP_*/
