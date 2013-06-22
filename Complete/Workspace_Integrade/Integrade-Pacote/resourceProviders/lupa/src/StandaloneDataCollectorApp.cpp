#include "DataCollector.hpp"

int main(int nargs, char *args[]) {
	const char *filename;
	
   filename = (nargs > 1) ? args[1] : LOG_FILE; 
	DataCollector *d = new DataCollector(filename);
	d->startCollectingData();
	d->joinCollectingThread();	

	return 0;
}
