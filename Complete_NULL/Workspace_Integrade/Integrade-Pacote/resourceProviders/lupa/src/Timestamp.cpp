#include "Timestamp.hpp"
#include <sstream>
#include <cstdio>

//TODO: move to the class definition
static string weekDayNames[7] = {
	string("Sun"),
	string("Mon"),
	string("Tue"),
	string("Wed"),
	string("Thu"),
	string("Fri"),
	string("Sat")
};

Timestamp::~Timestamp() {
//	cout << "<destruiu Timestamp " << this << ">" << endl;
// 	delete timeInfo;
}

// default constructor: returns current time
Timestamp::Timestamp() {
	time_t r;
	time (&r);
	setRawTime(r);
}

Timestamp::Timestamp(time_t raw) {
	setRawTime(raw);
}

Timestamp::Timestamp(const Timestamp& t) {
	setRawTime (t.getRawTime());
//	cout << "<copiou Timestamp " << this << ">" << endl;
}

void Timestamp::setRawTime(time_t r) {
	rawTime = r;
   /* Sets variables like timezone to correct values */
   tzset();
}

int Timestamp::getYear() 		const { return (localtime (&rawTime))->tm_year + 1900; }
int Timestamp::getMonth() 		const { return (localtime (&rawTime))->tm_mon + 1; }
int Timestamp::getMonthDay() 		const { return (localtime (&rawTime))->tm_mday; }
int Timestamp::getWeekDay() 		const { return (localtime (&rawTime))->tm_wday; }
int Timestamp::getHour() 		const { return (localtime (&rawTime))->tm_hour; }
int Timestamp::getMinutes() 		const { return (localtime (&rawTime))->tm_min; }
int Timestamp::getSeconds() 		const { return (localtime (&rawTime))->tm_sec; }
time_t Timestamp::getRawTime() 		const { return rawTime; }

string Timestamp::getWeekDayName() const {
	return weekDayNames[getWeekDay()];
}

int Timestamp::daysApart(Timestamp formerTime) {
	// ignores the time part => considers only the date part

	// calculates de index of the days since 1970 and subtracts one from the other
	return ((static_cast<int>(getRawTime()) - timezone) / (24*60*60))
			- ((static_cast<int>(formerTime.getRawTime())- timezone) / (24*60*60));
}

string Timestamp::formattedPrint() {
	char str[20];
	//TODO: usar formatted output da ostream
	sprintf(str, "%4d-%02d-%02d %02d:%02d:%02d",
			getYear(), getMonth(), getMonthDay(), getHour(), getMinutes(), getSeconds());
	return string(getWeekDayName()) + " " + str;
}

ostream& operator<<(ostream &out, Timestamp t) {
	out << t.rawTime;
	return out;
}

istream& operator>>(istream &in, Timestamp &t) {
	time_t rt;
	in >> rt;
	t.setRawTime(rt);

	return in;
}

bool operator>(Timestamp &t1, Timestamp &t2) {
	return t1.getRawTime() > t2.getRawTime();
}

bool Timestamp::isSameDay(const Timestamp t) {
	return (this->daysApart(t) == 0);
}

int Timestamp::getSecondInDay() const {
	return (static_cast<int>(this->getRawTime()) - timezone) % (24*60*60);
}

int Timestamp::secondsApart (Timestamp formerTime) {
	return static_cast<int>(getRawTime() - formerTime.getRawTime());
}

Timestamp Timestamp::beginningOfSameDay() {
	time_t time = this->getRawTime();
	time = time - ((time - timezone) % (3600*24));
	return Timestamp(time);

}

Timestamp Timestamp::addDays(int days) {
   return Timestamp(this->getRawTime() + (days * (24*60*60)));
}
