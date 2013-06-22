#ifndef WINDOWSDIRECTORY_HPP
#define WINDOWSDIRECTORY_HPP

#include <string>
#include <Windows.h>
#include "Directory.hpp"

class WindowsDirectory: public Directory{

public:

	WindowsDirectory(const std::string & directoryPath);
	~WindowsDirectory();

	bool hasNext();
	const DirectoryEntry & next();

	static void createDirectory(const std::string & directoryPath);
	static void removeDirectory(const std::string & directoryPath);


private:

	DirectoryEntry * _currentEntry;
	HANDLE _searchHandle;
	bool done;

};
#endif//WINDOWSDIRECTORY_HPP
