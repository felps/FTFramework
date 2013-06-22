#ifndef REQUESTLOG_HPP
#define REQUESTLOG_HPP

#include "Timestamp.hpp"
#include "ResourceData.hpp"

class RequestLog {
public:
	RequestLog();
	RequestLog(Timestamp t, ResourceData rd, bool a);
	~RequestLog();

	bool getAnswer();
	Timestamp getTimestamp();
	ResourceData getResourceRequest();
	
	friend ostream& operator<<(ostream &out, RequestLog rl);
	friend istream& operator>>(istream &in, RequestLog& rl);

private:
	Timestamp timestamp;
	ResourceData resourceRequest;
	bool answer;
};

#endif
