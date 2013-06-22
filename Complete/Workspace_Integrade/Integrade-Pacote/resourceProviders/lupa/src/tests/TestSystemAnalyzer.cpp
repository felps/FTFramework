#include "TestSystemAnalyzer.hpp"
#include "../LinuxSystemAnalyzer.hpp"

TestSystemAnalyzer::TestSystemAnalyzer() {
	offset = 0;
	realSystemAnalyzer = new LinuxSystemAnalyzer();
}

TestSystemAnalyzer::~TestSystemAnalyzer() {
	delete realSystemAnalyzer;
}

ResourceData TestSystemAnalyzer::getResourceData() {
	if (offset < resourceDataList.size()) {
		return resourceDataList[offset++];
	}

	return realSystemAnalyzer->getResourceData();
}

void TestSystemAnalyzer::setResourceDatasToReturn(vector<ResourceData> list) {
	resourceDataList = list;
	offset = 0;
}

