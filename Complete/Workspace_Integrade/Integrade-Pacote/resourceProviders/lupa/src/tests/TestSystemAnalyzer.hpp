#ifndef TestSystemAnalyzer_HPP
#define TestSystemAnalyzer_HPP

#include "../ResourceData.hpp"
#include "../SystemAnalyzer.hpp"

#include <vector>

class TestSystemAnalyzer : public SystemAnalyzer {
public:
	TestSystemAnalyzer();
	virtual ~TestSystemAnalyzer();

	virtual ResourceData getResourceData();
	void setResourceDatasToReturn(vector<ResourceData> list);

private:
	vector<ResourceData> resourceDataList;
	unsigned int offset;
	SystemAnalyzer *realSystemAnalyzer;

};

#endif
