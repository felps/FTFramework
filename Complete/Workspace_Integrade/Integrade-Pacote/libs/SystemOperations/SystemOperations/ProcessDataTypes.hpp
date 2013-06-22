#ifndef PROCESSDATATYPES_HPP
#define PROCESSDATATYPES_HPP

#ifdef POSIX
	#include <sys/types.h>
#endif

	typedef enum{
		NORMAL_EXIT,
		RUNNING,
		KILLED,
		ABNORMAL_EXIT,
		UNKNOWN
	} ProcessStatus;

	#ifdef WIN32
		typedef void * PROCESS_ID;
	#endif
	#ifdef POSIX
		typedef pid_t PROCESS_ID;
	#endif

#endif//PROCESSDATATYPES_HPP
