#include "UsageDataTester.hpp"
#include "../LupaConstants.hpp"

#include <cppunit/extensions/HelperMacros.h>
#include <math.h>

#define EPSILON 0.0001f
#define FLOAT_EQUALITY(A, B) (fabsf(A - B) < EPSILON)
#define TEST_USAGE_DATA(I, C, M, E) (CPPUNIT_ASSERT(FLOAT_EQUALITY(ud->getData()[I].getCpuUsage(), C) && (FLOAT_EQUALITY(ud->getData()[I].getFreeMemory(), M)) && (ud->getData()[I].isEstimate() == E)))

void UsageDataTester::setUp() { 
	setCollectInterval (3600 * 8);
	timezone = 10800;
	
	 ud1 = UsageData(Timestamp());
	 ud2 = UsageData(Timestamp());

	 ud1.getData()[0] = ResourceData(0.1f, 1000);
	 ud1.getData()[1] = ResourceData(0.2f, 2000);
	 ud1.getData()[2] = ResourceData(0.3f, 3000);
	 ud1.getData()[3] = ResourceData(0.5f, 5000);
	 ud1.getData()[4] = ResourceData(0.6f, 6000);
	 ud1.getData()[5] = ResourceData(0.7f, 7000);
	
	 ud2.getData()[0] = ResourceData(0.15f, 100);
	 ud2.getData()[1] = ResourceData(0.25f, 200);
	 ud2.getData()[2] = ResourceData(0.35f, 300);
	 ud2.getData()[3] = ResourceData(0.55f, 500);
	 ud2.getData()[4] = ResourceData(0.65f, 600);
	 ud2.getData()[5] = ResourceData(0.75f, 700);

}

void UsageDataTester::tearDown() { 
	setCollectInterval (DEFAULT_COLLECT_INTERVAL);
	setValidResourceDataThreshold(DEFAULT_VALID_RESOURCE_DATA_THRESHOLD);
}

void UsageDataTester::testDistance() { 
	
	
	//printf ("%f\t%f\n", ud1.distance(&ud2, CPU_USAGE),ud1.distance(&ud2, FREE_MEMORY));
	
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.distance(&ud2, CPU_USAGE, 0, 2 * SAMPLES_PER_DAY), 0.12247f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(floor(ud1.distance(&ud2, FREE_MEMORY, 0, 2 * SAMPLES_PER_DAY)), 10021.0f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.distance(&ud2, CPU_USAGE, 0, 3), 0.0866025f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.distance(&ud2, CPU_USAGE, 2, 5), 0.0866025f));
	
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud2.distance(&ud1, CPU_USAGE, 0, 2 * SAMPLES_PER_DAY), 0.12247f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(floor(ud2.distance(&ud1, FREE_MEMORY, 0, 2 * SAMPLES_PER_DAY)), 10021.0f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud2.distance(&ud1, CPU_USAGE, 0, 3), 0.0866025f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud2.distance(&ud1, CPU_USAGE, 2, 5), 0.0866025f));
	
}

void UsageDataTester::testResourceAverage() {
	
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.resourceAverage(CPU_USAGE), 0.4f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.resourceAverage(FREE_MEMORY), 4000));

	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud2.resourceAverage(CPU_USAGE), 0.45f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud2.resourceAverage(FREE_MEMORY), 400));

	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.resourceAverage(CPU_USAGE, 0, 3), 0.2f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.resourceAverage(FREE_MEMORY, 0, 3), 2000));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.resourceAverage(CPU_USAGE, 3, 6), 0.6f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.resourceAverage(FREE_MEMORY, 3, 6), 6000));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.resourceAverage(CPU_USAGE, 2, 4), 0.4f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud1.resourceAverage(FREE_MEMORY, 2, 4), 4000));

}

void UsageDataTester::testMeanCopy() { 
	
	UsageData *ud = ud1.meanCopy();
	//ud->simplePrint();
	CPPUNIT_ASSERT(ud->isValid());
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud->resourceAverage(CPU_USAGE), 0.0f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud->resourceAverage(FREE_MEMORY), 0.0f));
	TEST_USAGE_DATA(0, -0.3f, -3000, false);
	TEST_USAGE_DATA(1, -0.2f, -2000, false);
	TEST_USAGE_DATA(2, -0.1f, -1000, false);
	TEST_USAGE_DATA(3,  0.1f,  1000, false);
	TEST_USAGE_DATA(4,  0.2f,  2000, false);
	TEST_USAGE_DATA(5,  0.3f,  3000, false);

	ud2.getData()[2].setEstimate(true);
	
	setValidResourceDataThreshold(0.8f);
	ud = ud2.meanCopy();
	//ud->simplePrint();

	CPPUNIT_ASSERT(ud->isValid());
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud->resourceAverage(CPU_USAGE), 0.0f));
	CPPUNIT_ASSERT(FLOAT_EQUALITY(ud->resourceAverage(FREE_MEMORY), 0.0f));
	TEST_USAGE_DATA(0, -0.3f, -300, false);
	TEST_USAGE_DATA(1, -0.2f, -200, false);
	TEST_USAGE_DATA(2, -0.1f, -100, true);
	TEST_USAGE_DATA(3,  0.1f,  100, false);
	TEST_USAGE_DATA(4,  0.2f,  200, false);
	TEST_USAGE_DATA(5,  0.3f,  300, false);

}

void UsageDataTester::testDerivative() {
	
	setValidResourceDataThreshold(0.2f);

	UsageData *ud = ud1.derivative();
	//ud->simplePrint();
	CPPUNIT_ASSERT(ud->isValid());
	
	TEST_USAGE_DATA(0, INVALID_DATA, INVALID_DATA, false);
	TEST_USAGE_DATA(1, 0.1f, 1000, false);
	TEST_USAGE_DATA(2, 0.1f, 1000, false);
	TEST_USAGE_DATA(3, 0.2f, 2000, false);
	TEST_USAGE_DATA(4, 0.1f, 1000, false);
	TEST_USAGE_DATA(5, 0.1f, 1000, false);

	ud2.getData()[2].setEstimate(true);
	
	ud = ud2.derivative();
	//ud->simplePrint();

	CPPUNIT_ASSERT(ud->isValid());
	TEST_USAGE_DATA(0, INVALID_DATA, INVALID_DATA, false);
	TEST_USAGE_DATA(1, 0.1f, 100, false);
	TEST_USAGE_DATA(2, 0.1f, 100, true);
	TEST_USAGE_DATA(3, 0.2f, 200, true);
	TEST_USAGE_DATA(4, 0.1f, 100, false);
	TEST_USAGE_DATA(5, 0.1f, 100, false);

	UsageData ud3 = UsageData(Timestamp());

	ud3.getData()[0] = ResourceData(0.1f,  1000);
	ud3.getData()[1] = ResourceData(0.0f,  1000);
	ud3.getData()[2] = ResourceData(0.3f,  1000);
	ud3.getData()[3] = ResourceData(0.5f,  5000);
	ud3.getData()[4] = ResourceData(0.2f,  4990);
	ud3.getData()[5] = ResourceData(0.05f, 4000);

	ud = ud3.derivative();
	//ud->simplePrint();

	CPPUNIT_ASSERT(ud->isValid());
	TEST_USAGE_DATA(0, INVALID_DATA, INVALID_DATA, false);
	TEST_USAGE_DATA(1, -0.1f,    0, false);
	TEST_USAGE_DATA(2,  0.3f,    0, false);
	TEST_USAGE_DATA(3,  0.2f, 4000, false);
	TEST_USAGE_DATA(4, -0.3f,  -10, false);
	TEST_USAGE_DATA(5, -0.15f,-990, false);

}

void UsageDataTester::testIsValid() {

	setValidResourceDataThreshold(0.67f);
	
	CPPUNIT_ASSERT(ud1.isValid());

	ud1.getData()[0] = ResourceData();
	CPPUNIT_ASSERT(ud1.isValid());
	
	ud1.getData()[4].setEstimate(true);
	CPPUNIT_ASSERT(!ud1.isValid());
	
	ud1.getData()[4] = ResourceData();
	CPPUNIT_ASSERT(!ud1.isValid());
}

void UsageDataTester::testGoBackOneDay() {

   Timestamp t = ud1.getDate();
   ud1.goBackOneDay();
   
   UsageData *ud = &ud1;
   //ud->simplePrint();
   
   CPPUNIT_ASSERT(t.daysApart(ud1.getDate()) == 1);
   
   CPPUNIT_ASSERT(!ud1.getData()[0].isValid());
   CPPUNIT_ASSERT(!ud1.getData()[1].isValid());
   CPPUNIT_ASSERT(!ud1.getData()[2].isValid());
   
   CPPUNIT_ASSERT(ud1.getData()[3].isValid());
   CPPUNIT_ASSERT(ud1.getData()[4].isValid());
   CPPUNIT_ASSERT(ud1.getData()[5].isValid());
   
   TEST_USAGE_DATA(0, INVALID_DATA, INVALID_DATA, false);
   TEST_USAGE_DATA(1, INVALID_DATA, INVALID_DATA, false);
   TEST_USAGE_DATA(2, INVALID_DATA, INVALID_DATA, false);

   TEST_USAGE_DATA(3, 0.1f, 1000, false);
   TEST_USAGE_DATA(4, 0.2f, 2000, false);
   TEST_USAGE_DATA(5, 0.3f, 3000, false);


}
