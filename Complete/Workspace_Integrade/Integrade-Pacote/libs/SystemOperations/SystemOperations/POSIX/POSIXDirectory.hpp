#ifdef POSIX
#ifndef POSIXDIRECTORY_HPP
#define POSIXDIRECTORY_HPP

#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include "Directory.hpp"



class POSIXDirectory: public Directory{

	public:

	POSIXDirectory(const std::string & directoryPath);
	~POSIXDirectory();

	bool hasNext();
	const DirectoryEntry & next();

	static void createDirectory(const std::string & directoryPath);
	static void removeDirectory(const std::string & directoryPath);

	private:

	DirectoryEntry * _currentEntry;
	DIR * _directory;

};

#endif// POSIXDIRECTORY_HPP
#endif//POSIX
