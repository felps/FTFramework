#include <fstream>
#include <string>
#include <sstream>
#include <iostream>

#include <time.h>
#include <stdlib.h>
#include <stdio.h>

#include "TimestampTester.hpp"

#include <cppunit/extensions/HelperMacros.h>

using namespace std;

void TimestampTester::setUp () {
	t1 = Timestamp(1155408125); 					// Sat 2006-08-12 15:42:05
	t2 = Timestamp(1155408125 + 29880); 			// Sun 2006-08-13 00:00:05
	t3 = Timestamp(1155408125 + 29870); 			// Sat 2006-08-12 23:59:55
	t4 = Timestamp(1155408125 + 29870 - 86400); 	// Fri 2006-08-11 23:59:55

   	timezone = 10800; /** set the timezone to GMT -0300  */
}
   
   
void TimestampTester::testGetSecondInDay() {
	CPPUNIT_ASSERT( t1.getSecondInDay() == 56525 );
	CPPUNIT_ASSERT( t2.getSecondInDay() == 5 );
	CPPUNIT_ASSERT( t3.getSecondInDay() == 86400 - 5  );
	CPPUNIT_ASSERT( t4.getSecondInDay() == 86400 - 5 );
}

void TimestampTester::testIsSameDay() {
	CPPUNIT_ASSERT( !t1.isSameDay(t2) ); CPPUNIT_ASSERT( !t2.isSameDay(t1) );
	CPPUNIT_ASSERT( !t2.isSameDay(t3) ); CPPUNIT_ASSERT( !t3.isSameDay(t2) );
	CPPUNIT_ASSERT( !t3.isSameDay(t4) ); CPPUNIT_ASSERT( !t4.isSameDay(t3) );
	CPPUNIT_ASSERT( t1.isSameDay(t3) );  CPPUNIT_ASSERT( t3.isSameDay(t1) );
	CPPUNIT_ASSERT( t1.isSameDay(t1) );  
}
   
   
void TimestampTester::testDaysApart() {

	CPPUNIT_ASSERT( t2.daysApart(t1) == 1 ); CPPUNIT_ASSERT( t1.daysApart(t2) == -1 );  
	CPPUNIT_ASSERT( t2.daysApart(t3) == 1 ); CPPUNIT_ASSERT( t3.daysApart(t2) == -1 );
	CPPUNIT_ASSERT( t3.daysApart(t4) == 1 ); CPPUNIT_ASSERT( t4.daysApart(t3) == -1 );  
	CPPUNIT_ASSERT( t2.daysApart(t4) == 2 ); CPPUNIT_ASSERT( t4.daysApart(t2) == -2 );
	CPPUNIT_ASSERT( t1.daysApart(t3) == 0 ); CPPUNIT_ASSERT( t3.daysApart(t1) ==  0 );
	CPPUNIT_ASSERT( t1.daysApart(t1) == 0 );
}
	
void TimestampTester::testSecondsApart() {
	CPPUNIT_ASSERT( t2.secondsApart(t1) == 29880 ); CPPUNIT_ASSERT( t1.secondsApart(t2) == -29880 );  
	CPPUNIT_ASSERT( t2.secondsApart(t3) == 10 ); CPPUNIT_ASSERT( t3.secondsApart(t2) == -10 );
	CPPUNIT_ASSERT( t3.secondsApart(t4) == 86400 ); CPPUNIT_ASSERT( t4.secondsApart(t3) == -86400 );  
	CPPUNIT_ASSERT( t2.secondsApart(t4) == 86410 ); CPPUNIT_ASSERT( t4.secondsApart(t2) == -86410 );
	CPPUNIT_ASSERT( t1.secondsApart(t3) == -29870 ); CPPUNIT_ASSERT( t3.secondsApart(t1) ==  29870 );
	CPPUNIT_ASSERT( t1.secondsApart(t1) == 0 );
}
   
void TimestampTester::testReadFromInput() {
	Timestamp t;
	istringstream iss;
	string s = "1155408125"; // Sat 2006-08-12 15:42:05
	iss.str(s);
	
	iss >> t;
	
	CPPUNIT_ASSERT(t.getYear() == 2006);
	CPPUNIT_ASSERT(t.getMonth() == 8);
	CPPUNIT_ASSERT(t.getMonthDay() == 12);
	CPPUNIT_ASSERT(t.getHour() == 15);
	CPPUNIT_ASSERT(t.getMinutes() == 42);
	CPPUNIT_ASSERT(t.getSeconds() == 5);
	CPPUNIT_ASSERT(t.getWeekDay() == 6);
} 
   
   
void TimestampTester::testWriteToOutput() {
	ostringstream oss;
	string s; 

	CPPUNIT_ASSERT(t1.getYear() == 2006);
	CPPUNIT_ASSERT(t1.getMonth() == 8);
	CPPUNIT_ASSERT(t1.getMonthDay() == 12);
	CPPUNIT_ASSERT(t1.getHour() == 15);
	CPPUNIT_ASSERT(t1.getMinutes() == 42);
	CPPUNIT_ASSERT(t1.getSeconds() == 5);
	CPPUNIT_ASSERT(t1.getWeekDay() == 6);

	oss << t1;

	s = oss.str();

	CPPUNIT_ASSERT(s.compare("1155408125") == 0);
	
}
   
void TimestampTester::testGreaterThanOperator() {
	CPPUNIT_ASSERT( t2 > t1 );
	CPPUNIT_ASSERT( t2 > t3 );
	CPPUNIT_ASSERT( t3 > t1 );
	CPPUNIT_ASSERT( !(t1 > t1) );

}
   
void TimestampTester::testBegginingOfSameDay() {

	Timestamp tmp = t1.beginningOfSameDay();
	CPPUNIT_ASSERT(tmp.getYear() == 2006);
	CPPUNIT_ASSERT(tmp.getMonth() == 8);
	CPPUNIT_ASSERT(tmp.getMonthDay() == 12);
	CPPUNIT_ASSERT(tmp.getHour() == 0);
	CPPUNIT_ASSERT(tmp.getMinutes() == 0);
	CPPUNIT_ASSERT(tmp.getSeconds() == 0);
	CPPUNIT_ASSERT(tmp.getWeekDay() == 6);

	tmp = t3.beginningOfSameDay();
	CPPUNIT_ASSERT(tmp.getYear() == 2006);
	CPPUNIT_ASSERT(tmp.getMonth() == 8);
	CPPUNIT_ASSERT(tmp.getMonthDay() == 12);
	CPPUNIT_ASSERT(tmp.getHour() == 0);
	CPPUNIT_ASSERT(tmp.getMinutes() == 0);
	CPPUNIT_ASSERT(tmp.getSeconds() == 0);
	CPPUNIT_ASSERT(tmp.getWeekDay() == 6);
	
	tmp = t2.beginningOfSameDay();
	CPPUNIT_ASSERT(tmp.getYear() == 2006);
	CPPUNIT_ASSERT(tmp.getMonth() == 8);
	CPPUNIT_ASSERT(tmp.getMonthDay() == 13);
	CPPUNIT_ASSERT(tmp.getHour() == 0);
	CPPUNIT_ASSERT(tmp.getMinutes() == 0);
	CPPUNIT_ASSERT(tmp.getSeconds() == 0);
	CPPUNIT_ASSERT(tmp.getWeekDay() == 0);
	
}


void TimestampTester::testAddDays() {

   Timestamp tmp = t1.addDays(1);
   CPPUNIT_ASSERT(tmp.getYear() == 2006);
   CPPUNIT_ASSERT(tmp.getMonth() == 8);
   CPPUNIT_ASSERT(tmp.getMonthDay() == 13);
   CPPUNIT_ASSERT(tmp.getHour() == 15);
   CPPUNIT_ASSERT(tmp.getMinutes() == 42);
   CPPUNIT_ASSERT(tmp.getSeconds() == 5);
   CPPUNIT_ASSERT(tmp.getWeekDay() == 0);

   tmp = t3.addDays(-2);
   CPPUNIT_ASSERT(tmp.getYear() == 2006);
   CPPUNIT_ASSERT(tmp.getMonth() == 8);
   CPPUNIT_ASSERT(tmp.getMonthDay() == 10);
   CPPUNIT_ASSERT(tmp.getHour() == 23);
   CPPUNIT_ASSERT(tmp.getMinutes() == 59);
   CPPUNIT_ASSERT(tmp.getSeconds() == 55);
   CPPUNIT_ASSERT(tmp.getWeekDay() == 4);
	
   tmp = t2.addDays(-14);
   CPPUNIT_ASSERT(tmp.getYear() == 2006);
   CPPUNIT_ASSERT(tmp.getMonth() == 7);
   CPPUNIT_ASSERT(tmp.getMonthDay() == 30);
   CPPUNIT_ASSERT(tmp.getHour() == 0);
   CPPUNIT_ASSERT(tmp.getMinutes() == 0);
   CPPUNIT_ASSERT(tmp.getSeconds() == 5);
   CPPUNIT_ASSERT(tmp.getWeekDay() == 0);
	
}
