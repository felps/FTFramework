#ifndef LUPACONSTANTS_HPP_
#define LUPACONSTANTS_HPP_

#include <ostream>
using namespace std;

#define __DEBUG_OUTPUT 1

#define DEFAULT_COLLECT_INTERVAL 300
#define COLLECT_INTERVAL (getCollectInterval())
#define SAMPLES_PER_DAY ((24*60*60)/COLLECT_INTERVAL)

#define DEFAULT_PREDICTION_HOURS 6
#define PREDICTION_HOURS (getPredictionHours())
#define PREDICTION_INTERVAL (SAMPLES_PER_DAY/(24/PREDICTION_HOURS))

#define DEFAULT_K 4
#define K (getK())

#define DEFAULT_HOURS_TO_CONSIDER_IN_AVERAGE 4

#define VALID_RESOURCE_DATA_THRESHOLD (getValidResourceDataThreshold())
#define DEFAULT_VALID_RESOURCE_DATA_THRESHOLD 0.9f
#define INVALID_DATA -10.0f

#define DEFAULT_LOG_FILENAME "lupa.log"
#define DEFAULT_REQUEST_LOG_FILENAME "lupaRequests.log"

#ifndef LOG_FILE
//#define LOG_FILE "lupa.log"

#define LOG_FILE (getLupaLogFileName())

#endif /*LOG_FILE*/

//#define REQUEST_LOG_FILE_NAME "lupaRequests.log"
#define REQUEST_LOG_FILE_NAME (getLupaRequestLogFileName())

enum resource { CPU_USAGE, FREE_MEMORY };

int getCollectInterval();
void setCollectInterval(int v);
void setK(unsigned int v);
unsigned int getK();
double getValidResourceDataThreshold();
void setValidResourceDataThreshold(double v);
int getPredictionHours();
void setPredictionHours(int v);
int getHoursToConsiderInAverage();
void setHoursToConsiderInAverage(int v);

ostream& operator<<(ostream &out, resource r);

void initLupaLogFileName();
char *getLupaLogFileName();
char *getLupaRequestLogFileName();

#endif /*LUPACONSTANTS_HPP_*/
