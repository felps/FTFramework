package testIntegrade;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import clusterManagement.ApplicationRepository;
import dataTypes.ApplicationType;
import dataTypes.BinaryDescription;
import dataTypes.FileStruct;

class TestSuiteMeanBsp extends TestSuite {

	int nExperiments;
	int nRepetitions;
	int nProcesses[];
	String parameters[];
	String inputFiles[];

	TestSuiteMeanBsp (int firstExecutionId, ApplicationRepository appRepos, String logFile) {
		super(firstExecutionId, appRepos, logFile);

		// Registers an application in the repository
		binaryDescription = new BinaryDescription();
		binaryDescription.applicationName = "mean";
		binaryDescription.basePath = binaryDescription.applicationName + "Base";
		binaryDescription.binaryName = "Linux_i686";
		binaryDescription.description = "";

		nRepetitions = 1;
		nExperiments = 4;
		int nProc = 2;
		nProcesses = new int[nExperiments];
		nProcesses[0] =  nProc;
		nProcesses[1] =  nProc;
		nProcesses[2] =  nProc;
		nProcesses[3] =  nProc;
		inputFiles = new String[nExperiments];
		inputFiles[0] = "input1.dat";
		inputFiles[1] = "input10.dat";
		inputFiles[2] = "input100.dat";
		inputFiles[3] = "input1000.dat";
		parameters = new String[nExperiments]; 
		parameters[0] = inputFiles[0];
		parameters[1] = inputFiles[1];
		parameters[2] = inputFiles[2];
		parameters[3] = inputFiles[3];

		registerAtApplicationRepository(appRepos, "/home/grenoble/agoldman/rcamargo/tmp/meanbsp");
	}

	public RemoteExecutionParameters getNextRemoteExecution() {

		int execIndex = (nextExecutionId - firstExecutionId) % nExperiments;
			
		RemoteExecutionParameters executionParameters = new RemoteExecutionParameters();
		executionParameters.executionId = nextExecutionId;
		executionParameters.appType     = ApplicationType.bsp;
		executionParameters.nTasks      = nProcesses[execIndex];
		executionParameters.appName     = binaryDescription.applicationName;
		executionParameters.appArgs     = parameters[execIndex];
		executionParameters.inputFiles  = new String[]{"/home/grenoble/agoldman/rcamargo/tmp/" + inputFiles[execIndex]};
        executionParameters.outputFiles = new String[]{"output.dat"};

    	String message = executionParameters.executionId + "-> uploadstart:" + System.currentTimeMillis();
    	writeToLog(message);

		/**
		 * Upload InputFiles to OppStore
		 * TODO: Must also work with parametric applications
		 */		
		String[] inputKeys = new String[ executionParameters.inputFiles.length ];
		boolean successfulUpload = true;
		for ( int i=0; i < executionParameters.inputFiles.length && successfulUpload ; i++ ) {
			
			File file = new File(executionParameters.inputFiles[i]);
			String inputKey = null;
			if (file.isFile())
				inputKey = broker.storeFileW(executionParameters.inputFiles[i], null);

			
			if ( inputKey != null ) {				
				String[] splitFileName = executionParameters.inputFiles[i].split("/");
				String fileName = splitFileName[ splitFileName.length - 1]; 
				String integradeKey = fileName + "[key]" + inputKey;
				inputKeys[i] = integradeKey;
				System.out.println("TestSuiteMean -> fileName:" + integradeKey + ".");
			}
			else { 
				successfulUpload = false;
				System.out.println("Input file upload failed.");
			}
			
		}
		if (successfulUpload)
			executionParameters.inputFiles  = inputKeys;

    	message = executionParameters.executionId + "-> uploadfinished:" + System.currentTimeMillis();
    	writeToLog(message);

    	if ( (nextExecutionId - firstExecutionId) == (nExperiments * nRepetitions) )
    		return null;
    	
    	else { 
    		this.lastExecutionParameters = executionParameters;
    		this.nextExecutionId += 1;
        
    		return executionParameters;
    	}
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
