#ifdef POSIX

#include <iostream>
#include <unistd.h>
#include <errno.h>

#include "POSIXDirectory.hpp"
#include "POSIXUtils.hpp"
#include "SystemOperationException.hpp"

	POSIXDirectory::POSIXDirectory(const std::string & directoryPath){
		_directory = opendir(directoryPath.c_str());
		_currentEntry = new DirectoryEntry();
	}

	POSIXDirectory::~POSIXDirectory(){
		delete _currentEntry;
		closedir(_directory);
		//TODO_LONG: is it necessary to delete the pointer?
	}

	bool POSIXDirectory::hasNext(){

		if(_directory == NULL)
			return false;

		struct dirent * directoryEntry = readdir(_directory);

		if(directoryEntry == NULL)
			return false;

		if((std::string(directoryEntry->d_name) == ".") || (std::string(directoryEntry->d_name) == ".."))
			return hasNext();
		_currentEntry->name(directoryEntry->d_name);
		

		return true;
	}

	const DirectoryEntry & POSIXDirectory::next(){

		return *_currentEntry;
	}


	void POSIXDirectory::createDirectory(const std::string & directoryPath){
	    int mkdirStatus = mkdir(directoryPath.c_str(),  S_IRUSR | S_IWUSR | S_IXUSR);
		if (mkdirStatus == -1) {
			POSIXUtils::printError("createDirectory", "mkdir");
			throw SystemOperationException(errno, "POSIXDirectory::createDirectory: could not create directory: " + directoryPath + ". Error description: " + strerror(errno));
        }
	}

	void POSIXDirectory::removeDirectory(const std::string & directoryPath){

		DIR * dir = NULL;
		dir = opendir(directoryPath.c_str());
		if(dir == NULL){
			POSIXUtils::printError("POSIXDirectory", "removeDirectory", directoryPath);
			throw SystemOperationException(errno, "POSIXDirectory::removeDirectory: could not open directory: " + directoryPath + ". Error description: " + strerror(errno));
			return;
		}

		struct dirent * dirEntry;

		while((dirEntry = readdir(dir)) != NULL){

			std::string fullPath = directoryPath + "/" + dirEntry->d_name;
			struct stat statHolder;

			if(stat(fullPath.c_str(), &statHolder) == 0){
				if((std::string(dirEntry->d_name) == ".") || (std::string(dirEntry->d_name) == ".."))
				continue;
				if(S_ISDIR(statHolder.st_mode) == 1){
					std::cout << "Recursing: " << dirEntry->d_name << std::endl;
					POSIXDirectory::removeDirectory(fullPath);
				}
				else if(S_ISREG(statHolder.st_mode) == 1){
					std::cout << "Deleting: " << fullPath << std::endl;
					if(unlink(fullPath.c_str()) != 0){
						POSIXUtils::printError("removeDirectory", "unlink", fullPath);
						throw SystemOperationException(errno, 
							"POSIXDirectory::removeDirectory: could not unlink file: " + fullPath + ". Error description: " + strerror(errno));
					}
				}
			}
			else{
				POSIXUtils::printError("removeDirectory", "stat", fullPath);
				throw SystemOperationException(errno, 
							"POSIXDirectory::removeDirectory: could not stat file: " + fullPath + ". Error description: " + strerror(errno));
			}	
		}

		//20080223: Deleting directory
		if(closedir(dir) !=0){
			POSIXUtils::printError("removeDirectory", "closedir", directoryPath);
				throw SystemOperationException(errno, 
							"POSIXDirectory::removeDirectory: could not close directory: " + directoryPath + ". Error description: " + strerror(errno));
		}
		
		if(rmdir(directoryPath.c_str()) != 0){
			POSIXUtils::printError("removeDirectory", "closedir", directoryPath);
				throw SystemOperationException(errno, 
							"POSIXDirectory::removeDirectory: could not remove directory: " + directoryPath + ". Error description: " + strerror(errno));
		}
	}


#endif//POSIX
