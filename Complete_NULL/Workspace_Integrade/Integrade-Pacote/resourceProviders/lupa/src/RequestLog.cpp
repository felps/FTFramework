#include "RequestLog.hpp"

RequestLog::RequestLog() {}

RequestLog::RequestLog ( Timestamp t, ResourceData rd, bool a )
		: timestamp ( t ), resourceRequest ( rd ), answer ( a ) {}


RequestLog::~RequestLog() {}

Timestamp RequestLog::getTimestamp() {
	return timestamp;
}

ResourceData RequestLog::getResourceRequest() {
	return resourceRequest;
}

bool RequestLog::getAnswer() {
	return answer;
}

ostream& operator<< ( ostream& out, RequestLog rl ) {
	return ( out << rl.getTimestamp() << "\t" << rl.getResourceRequest() << "\t" << rl.getAnswer() );
}

istream& operator>> ( istream& in, RequestLog& rl ) {
	Timestamp t;
	ResourceData rd;
	bool a;
	in >> t >> rd >> a;
	rl.timestamp = t;
	rl.resourceRequest = rd;
	rl.answer = a;

	return in;
}

