#include "Directory.hpp"
#ifdef WIN32
	#include "Windows/WindowsDirectory.hpp"
#endif
#ifdef POSIX
	#include "POSIX/POSIXDirectory.hpp"
#endif

	Directory * Directory::openDirectory(const std::string & directoryPath){

		#ifdef WIN32
			return new WindowsDirectory(directoryPath);
		#endif
		#ifdef POSIX
			return new POSIXDirectory(directoryPath);
		#endif
	}

	void Directory::createDirectory(const std::string & directoryPath){

		#ifdef WIN32
			WindowsDirectory::createDirectory(directoryPath);
		#endif
		#ifdef POSIX
			POSIXDirectory::createDirectory(directoryPath);
		#endif
	}

	void Directory::removeDirectory(const std::string & directoryPath){

		#ifdef WIN32
			WindowsDirectory::removeDirectory(directoryPath);
		#endif
		#ifdef POSIX
			POSIXDirectory::removeDirectory(directoryPath);
		#endif
	}
