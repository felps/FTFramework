#include "FileUtils.hpp"

#include <sys/statvfs.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>

FileUtils::FileUtils()
{
}

FileUtils::~FileUtils()
{
}

// Determines the available space on disk
int FileUtils::getAvailableDiskSpace (const char *path) {    
    struct statvfs *buf = new struct statvfs;
    int fsstatus = statvfs(path, buf);
    if (fsstatus == 0)
        return (buf->f_bavail * buf->f_bsize / 1024);   
    else
        return -1;        
}


long FileUtils::getFileSize (const char *path) {

	int inFile = open(path, O_RDONLY);
	struct stat *fileStat = new struct stat;
	fstat(inFile, fileStat);
	long fragmentDataSize = (long)fileStat->st_size;
	delete fileStat;
	close (inFile);

	return fragmentDataSize;
}
