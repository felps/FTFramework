package testIntegrade;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import clusterManagement.ApplicationRepository;
import dataTypes.ApplicationType;
import dataTypes.BinaryDescription;
import dataTypes.FileStruct;

class TestSuiteAlignBsp extends TestSuite {
		
	int nExperiments;
	int nTasks[];
	String parameters[];

	TestSuiteAlignBsp (int firstExecutionId, ApplicationRepository appRepos, String logFile) {
		super(firstExecutionId, appRepos, logFile);
		
		// Registers an application in the repository
		binaryDescription = new BinaryDescription();
		binaryDescription.applicationName = "align";
		binaryDescription.basePath = "alignBase";
		binaryDescription.binaryName = "Linux_i686";
		binaryDescription.description = "";

		nExperiments = 3;		
		nTasks = new int[nExperiments];
		nTasks[0] =  4;
		nTasks[1] =  8;
		nTasks[2] = 16;
		parameters = new String[nExperiments]; 
		parameters[0] = " 4 seq1.txt seq2.txt 16";
		parameters[1] = " 8 seq1.txt seq2.txt 16";
		parameters[2] = "16 seq1.txt seq2.txt 16";    	
		
		registerAtApplicationRepository(appRepos, "../../../examples/bsp/align/align");
		
	}
	
	public RemoteExecutionParameters getNextRemoteExecution() {
						
		RemoteExecutionParameters executionParameters = new RemoteExecutionParameters();        
		executionParameters.executionId = nextExecutionId;
		executionParameters.appType     = ApplicationType.bsp;
		executionParameters.nTasks      = nTasks[nextExecutionId - firstExecutionId];;
		executionParameters.appName     = binaryDescription.applicationName;
		executionParameters.appArgs     = parameters[nextExecutionId - firstExecutionId];;
		executionParameters.inputFiles  = new String[]{"../../../examples/bsp/align/seq1.txt", "../../../examples/bsp/align/seq2.txt"};
        executionParameters.outputFiles = new String[]{"stdout","stderr"};
        
        this.lastExecutionParameters = executionParameters;
        this.nextExecutionId += 1;
        
        return executionParameters;
	}

	public void analyseRemoteExecutionResults(Vector<FileStruct[]> applicationReturnedFiles) {
		
		for (FileStruct[] processOutputFiles : applicationReturnedFiles) {
			
			boolean outputFilesCorrect = false;
			
			if (processOutputFiles[0].fileName.compareTo("stdout") == 0) {
				BufferedReader fileStream = 
					new BufferedReader( new InputStreamReader( new ByteArrayInputStream(processOutputFiles[0].file)));
			
				String line = null;				
				try {
					while ( (line = fileStream.readLine()) != null) 
						if (line.contains("processor") && line.contains("time:"))
							outputFilesCorrect = true;				
				}
				catch (IOException e) { e.printStackTrace(); }								
			}
			
			if (outputFilesCorrect == false) {
				System.out.println("[ERROR] Application results are incorrect! applicationId:" + lastExecutionParameters.executionId);
				return;
			}
		}				
	}

}
