#ifndef TIMESTAMP_HPP_
#define TIMESTAMP_HPP_

#include <iostream>
#include <string>
#include <time.h>

#define TIMESTAMP_PARSING_ERROR "ERROR READING Timestamp FROM INPUT STREAM"

using namespace std;

class Timestamp {
public:
	Timestamp();
	Timestamp(const Timestamp& t);
	Timestamp(time_t raw);
	~Timestamp();
	
	int getWeekDay() const;
	int getMonthDay() const;
	int getMonth() const;
	int getYear() const;
	int getHour() const;
	int getMinutes() const;
	int getSeconds() const;
	time_t getRawTime() const;
	
	bool isSameDay(const Timestamp t);
	int daysApart(const Timestamp t);
	int getSecondInDay() const;
	int secondsApart (Timestamp formerTime);
	Timestamp addDays(int days);
		
	string getWeekDayName() const;
	string formattedPrint();
	
	friend ostream& operator<<(ostream& out, Timestamp t);
	friend istream& operator>>(istream& out, Timestamp &t);

	friend bool operator>(Timestamp &t1, Timestamp &t2);

	Timestamp beginningOfSameDay();

private:
	void setRawTime(time_t r);
	time_t rawTime;

};

#endif /*TIMESTAMP_HPP_*/
