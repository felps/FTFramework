#include "WindowsDirectory.hpp"
#include "WindowsUtils.hpp"
#include "SystemOperationException.hpp"
#include <Windows.h>

	WindowsDirectory::WindowsDirectory(const std::string & directoryPath){
	
		_searchHandle = NULL;
		_currentEntry = new DirectoryEntry();
		done = false;

		WIN32_FIND_DATA findData;
			

		_searchHandle = FindFirstFile((directoryPath + "/*").c_str(), &findData);

		if(_searchHandle == INVALID_HANDLE_VALUE){
			done = true;
			return;
		}

		
		_currentEntry->name(findData.cFileName);
		
	
	}

	WindowsDirectory::~WindowsDirectory(){

		FindClose(_searchHandle);
		delete _currentEntry;
	}

	bool WindowsDirectory::hasNext(){

		if(done)
			return false;

		WIN32_FIND_DATA findData;

		int result = FindNextFile(_searchHandle, &findData);
		if(result != 0){

			//Eliminate '.' and '..'
			if((std::string(findData.cFileName) == ".") || (std::string(findData.cFileName) == ".."))
				return hasNext();
			_currentEntry->name(findData.cFileName);
			
		}
		else
			done = true;
		return !done;
	}

	const DirectoryEntry & WindowsDirectory::next(){

		return *_currentEntry;		
	}

		void WindowsDirectory::createDirectory(const std::string & directoryPath){

		//TODO: This will only create the last directory if a directory chain is provided. 
		//This may not be enough
			int code = CreateDirectory(directoryPath.c_str(), NULL);
		if(code == 0)
			throw SystemOperationException(GetLastError(), 
			"WindowsDirectory::createDirectory: could not create directory: " +
			directoryPath + ". Error description: " + WindowsUtils::getErrorDescription(GetLastError()));
	}

	void WindowsDirectory::removeDirectory(const std::string & directoryPath){

		WindowsDirectory * directory = new  WindowsDirectory(directoryPath);

		while(directory->hasNext()){

			const DirectoryEntry & entry = directory->next();

			std::string entryName(directoryPath + "/" + entry.name());

			int fileAttributes = 
				GetFileAttributes(entryName.c_str());
			if(fileAttributes & FILE_ATTRIBUTE_DIRECTORY)
				removeDirectory(entryName.c_str());
			else
				DeleteFile(entryName.c_str());
		}

		delete directory;

		int code = RemoveDirectory(directoryPath.c_str());

		if(code == 0)
			throw SystemOperationException(GetLastError(), 
			"WindowsDirectory::createDirectory: could not remove directory: " +
			directoryPath + ". Error description: " + WindowsUtils::getErrorDescription(GetLastError()));
	}
