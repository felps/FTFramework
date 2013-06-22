#include "Mutex.hpp" 
#include "ConditionVariable.hpp"
#include "Thread.hpp"
#include "Process.hpp"
#include "Directory.hpp"

#include "MutexFactory.hpp"
#include "ConditionVariableFactory.hpp"
#include "ThreadFactory.hpp"
#include "ProcessFactory.hpp"

#include "ProcessDataTypes.hpp"
#include "SystemOperationException.hpp"

#include <iostream>
#include <fstream>


static ConditionVariable * aDone = ConditionVariableFactory::createConditionVariable();
static ConditionVariable * bDone = ConditionVariableFactory::createConditionVariable();
static ConditionVariable * aAllDone = ConditionVariableFactory::createConditionVariable();
static ConditionVariable * bAllDone = ConditionVariableFactory::createConditionVariable();

void * printA(void * unused){

	for(int i = 0; i < 5; i++){
		bDone->wait();
		std::cout << "A";
		aDone->signal();

	}
	aAllDone->signal();
	return NULL;
}

void * printB(void * unused){

	for(int j = 0; j < 5; j++){
		aDone->wait();
		std::cout << "B";
		bDone->signal();
	}
	bAllDone->signal();
	return NULL;
}

void copyFile(const std::string & srcPath, const std::string & dstPath){

	std::ifstream ifs(srcPath.c_str());
	std::ofstream ofs(dstPath.c_str());
	while(ifs.good()){
		char c;
		ifs.get(c);
		ofs << c;
	}
	ifs.close();
	ofs.close();
}


std::string processStatusToString(ProcessStatus status){

	if(status == NORMAL_EXIT)
		return "NORMAL_EXIT";
	if(status == RUNNING)
		return "RUNNING";
	if(status == KILLED)
		return "KILLED";
	if(status == ABNORMAL_EXIT)
		return "ABNORMAL_EXIT";
	if(status == UNKNOWN)
		return "UNKNOWN";
	else
		return "UNKNOWN UNKNOWN";
}

	void testMutex(){

		//Testing mutex methods
		Mutex * mutex = MutexFactory::createMutex();
		std::cout << "MutexFactory::createMutex OK" << std::endl;
		mutex->lock();
		std::cout << "Mutex::lock OK" << std::endl;
		mutex->trylock();
		std::cout << "Mutex::trylock OK" << std::endl;
		mutex->unlock();
		std::cout << "Mutex::unlock OK" << std::endl;
		std::cout << "Mutex::all methods OK" << std::endl;
	}

	void testConditionVariable(){

		//Testing condition valiable
		ConditionVariable * conditionVariable = ConditionVariableFactory::createConditionVariable();
		std::cout << "ConditionVariableFactory::createConditionVariable OK" << std::endl;
		conditionVariable->broadcast();
		std::cout << "ConditionVariable::broadcast OK" << std::endl;
		conditionVariable->signal();
		std::cout << "ConditionVariable::signal OK" << std::endl;
		std::cout << "ConditionVariable::all methods OK" << std::endl;
	}

	void testDirectory(){

		//Testing directory methods
		try{
			Directory::createDirectory("testDirectory");
			Directory::createDirectory("testDirectory/a");
			Directory::createDirectory("testDirectory/a/b");
			Directory::createDirectory("testDirectory/a/b/c");
		}
		catch(SystemOperationException & soe){
			std::cerr << soe.toString() << std::endl;
		}

		std::cout << "Directory::createDirectory OK" << std::endl;
		std::ofstream aFile("testDirectory/a/a");
		aFile.close();
		std::ofstream bFile("testDirectory/b");
		bFile.close();
		std::ofstream cFile("testDirectory/c");
		cFile.close();

		std::cout << "Listing testDirectory. This should read: a b c" << std::endl;

		Directory * directory = Directory::openDirectory("testDirectory");

		while(directory->hasNext()){
			const DirectoryEntry & entry = directory->next();
			std::cout << entry.name() << " ";
		}

		std::cout << std::endl;

		delete directory;

		Directory::removeDirectory("testDirectory");
		std::cout << "Directory::removeDirectory OK" << std::endl;
	}

	void testCorrectApp(){

		try{
			Directory::createDirectory("SampleCorrectAppDir");
		}
		catch(SystemOperationException & soe){
			std::cerr << soe.toString() << std::endl;
		}

		copyFile("SampleCorrectApp", "SampleCorrectAppDir/SampleCorrectApp.exe");

		Process * sampleCorrectProcess =
			ProcessFactory::createProcess("SampleCorrectAppDir", "SampleCorrectApp.exe", "");

		std::cout << "ProcessFactory::createProcess (SampleCorrectApp) OK" << std::endl;

		ProcessStatus correctStatus = sampleCorrectProcess->getProcessStatus();

		std::cout << "Process status (SampleCorrectApp): " << processStatusToString(correctStatus) << std::endl;
	}

	void testLongRunningApp(){

		try{
			Directory::createDirectory("SampleLongRunningAppDir");
		}
		catch(SystemOperationException & soe){
			std::cerr << soe.toString() << std::endl;
		}


		copyFile("SampleLongRunningApp", "SampleLongRunningAppDir/SampleLongRunningApp.exe");

		Process * sampleLongRunningProcess =
			ProcessFactory::createProcess("SampleLongRunningAppDir", "SampleLongRunningApp.exe", "");
		std::cout << "ProcessFactory::createProcess (SampleLongRunningApp) OK" << std::endl;

		ProcessStatus longRunningStatus = sampleLongRunningProcess->getProcessStatus();

		std::cout << "Process status (SampleLongRunningApp): " << processStatusToString(longRunningStatus) << std::endl;

		sampleLongRunningProcess->killProcess();
		std::cout << "Process::killProcess (SampleLongRunningApp) OK" << std::endl;

		Thread::sleep(6);

		longRunningStatus = sampleLongRunningProcess->getProcessStatus();
		std::cout << "Process status (SampleLongRunningApp): " << processStatusToString(longRunningStatus) << std::endl;
	}

	void testCrashingApp(){

		try{
			Directory::createDirectory("SampleCrashingApplicationDir");
		}
		catch(SystemOperationException & soe){
			std::cerr << soe.toString() << std::endl;
		}

		copyFile("SampleCrashingApplication", "SampleCrashingApplicationDir/SampleCrashingApplication.exe");
		
		Process * sampleCrashingApplicationProcessID =
			ProcessFactory::createProcess("SampleCrashingApplicationDir", "SampleCrashingApplication.exe", "");
		std::cout << "ProcessFactory::createProcess (SampleCrashingApplication) OK" << std::endl;
		Thread::sleep(6);

		ProcessStatus sampleCrashingApplicationStatus = sampleCrashingApplicationProcessID->getProcessStatus();
		std::cout << "Process status (SampleCrashingApplication): " << processStatusToString(sampleCrashingApplicationStatus) << std::endl;
	}

	void testThreads(){

		std::cout << "Testing Threads and Condition variables. This should read: ABABABABAB" << std::endl;

		Thread * a = ThreadFactory::createThread(&printA, NULL);
		Thread * b = ThreadFactory::createThread(&printB, NULL);
		a->start();
		b->start();

		aAllDone->wait();
		bAllDone->wait();
		std::cout << std::endl;
		std::cout << "***** All tests passed *****" << std::endl;
	}



int main(int argc, char ** argv[])
{

	try{

		bDone->signal();

		testMutex();

		testConditionVariable();

		testDirectory();

		testCorrectApp();

		testLongRunningApp();

		testCrashingApp();

		testThreads();

	}
	catch(SystemOperationException & soe){
		std::cerr << "Code: " << soe.errorCode() << " Description: " << soe.errorDescription() << std::endl;
	}
}
