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

class TestSuiteMean extends TestSuite {
		
	int nExperiments;
	int nTasks[];
	String parameters[];

	TestSuiteMean (int firstExecutionId, ApplicationRepository appRepos, String logFile) {
		super(firstExecutionId, appRepos, logFile);
		
		// Registers an application in the repository
		binaryDescription = new BinaryDescription();
		binaryDescription.applicationName = "mean";
		binaryDescription.basePath = binaryDescription.applicationName + "Base";
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
		
		registerAtApplicationRepository(appRepos, "/home/rcamargo/tmp/mean.py");
		
	}
	
	public RemoteExecutionParameters getNextRemoteExecution() {
						
		RemoteExecutionParameters executionParameters = new RemoteExecutionParameters();        
		executionParameters.executionId = nextExecutionId;
		//executionParameters.appType     = ApplicationType.bsp;
		executionParameters.appType     = ApplicationType.regular;
		executionParameters.nTasks      = nTasks[nextExecutionId - firstExecutionId];;
		executionParameters.appName     = binaryDescription.applicationName;
		executionParameters.appArgs     = parameters[nextExecutionId - firstExecutionId];
		executionParameters.inputFiles  = new String[]{"/home/rcamargo/tmp/input.dat"};
        executionParameters.outputFiles = new String[]{"output.dat"};//new String[]{"stdout","stderr","output.dat"};

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
