package testIntegrade;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import br.usp.ime.oppstore.broker.OppStoreBroker;

import clusterManagement.ApplicationRepository;
import dataTypes.ApplicationType;
import dataTypes.BinaryDescription;
import dataTypes.FileStruct;

class RemoteExecutionParameters {
	int executionId;
	ApplicationType appType;
	int nTasks;
	String appName;
	String appArgs;
	String[] inputFiles;
	String[] outputFiles;
	
	Lock executionLock = new ReentrantLock();
	Condition executionCondition = executionLock.newCondition();
}

public class TestSuite {

	
	int firstExecutionId;
	int nextExecutionId;
	OppStoreBroker broker;
	
	RemoteExecutionParameters lastExecutionParameters;
	ApplicationRepository appRepos;
	
	BinaryDescription binaryDescription;
	
	PrintWriter outFile;
	
	TestSuite (int firstExecutionId, ApplicationRepository appRepos, String logFile) {
		this.firstExecutionId   = firstExecutionId;
		this.nextExecutionId    = firstExecutionId;
		this.lastExecutionParameters = null;
		this.appRepos = appRepos;
		
		try { outFile = new PrintWriter(new FileWriter(logFile)); }					
		catch (IOException e) {}
	}
	
	public void writeToLog(String message) {
		outFile.println( message );
		outFile.flush();
	}
	
	public void setBroker(OppStoreBroker broker) {
		this.broker = broker;
	}
	
	public RemoteExecutionParameters getLastExecutionParameters() {
		return lastExecutionParameters;
	}
	
	/** Gets the parameters for the next execution */
	public RemoteExecutionParameters getNextRemoteExecution() {
		return null;
	}
	
	/** Analyze the retuned results */
	public void analyseRemoteExecutionResults (Vector<FileStruct[]> applicationReturnedFiles){		
	}
	
	/** Used to perform operations after the application has started */
	public void setExecutionStarted (AcceptedExecutionInformation executionInformation) {

		if (true) return;

		/**
		 * Kills the application after a given time to test the reinitialization process
		 */
		String hostName = executionInformation.processHostList[0];
		String executionId = executionInformation.nodeExecutionIdList[0];
		                                                              
		try { Thread.sleep(10*1000); } 
		catch (InterruptedException e) {}
		
		try { Runtime.getRuntime().exec("ssh " + hostName + " killall -9 " + executionId); } 
		catch (IOException e) {
			System.out.println("Could not kill remote process " + executionId + " at " + hostName);
		}

	}

	protected void registerAtApplicationRepository(ApplicationRepository appRepos, String binaryPath) {
		
		int fileSize = 1000000;		
		byte[] binaryCode = new byte[fileSize];
		FileInputStream inputFile;
		try {
			inputFile = new FileInputStream(binaryPath);
			int bytesRead = inputFile.read(binaryCode);
			assert (bytesRead > 0);
						
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {appRepos.deleteApplicationBinary(binaryDescription.basePath, binaryDescription.applicationName, binaryDescription.binaryName);} 
		catch (Exception e) {} // {e.printStackTrace();}		
		//try {appRepos.removeDirectory(binaryDescription.basePath);}
		//catch (Exception e) {e.printStackTrace();}
		try {appRepos.unregisterApplication(binaryDescription.basePath, binaryDescription.applicationName);} 
		catch (Exception e) {e.printStackTrace();}		
		
		try {appRepos.registerApplication(binaryDescription.basePath, binaryDescription.applicationName);} 
		catch (Exception e) {e.printStackTrace();}		
		try {appRepos.uploadApplicationBinary(binaryDescription, binaryCode);}
		catch (Exception e) {e.printStackTrace();}
	}
	
}
