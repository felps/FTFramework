#ifndef DIRECTORY_HPP
#define DIRECTORY_HPP

#include <string>

class DirectoryEntry{

	private:

		std::string _name;
		//std::string _path; 

	public:

		DirectoryEntry(){}
		//DirectoryEntry(const std::string & name, const std::string & path):_name(name),_path(path){}
		DirectoryEntry(const std::string & name):_name(name){}
		const std::string & name() const { return _name; } 
		//const std::string & path() const { return _path; }
		void name(const std::string & aName) { _name.assign(aName); } 
		//void path(const std::string & aPath) { _path.assign(aPath); }

};

class Directory{

	

	public:

		virtual bool hasNext() = 0;
		virtual const DirectoryEntry & next() = 0;

		virtual ~Directory(){}

		static Directory * openDirectory(const std::string & directoryPath);
		static void createDirectory(const std::string & directoryPath);
		static void removeDirectory(const std::string & directoryPath);
};
#endif//DIRECTORY_HPP
