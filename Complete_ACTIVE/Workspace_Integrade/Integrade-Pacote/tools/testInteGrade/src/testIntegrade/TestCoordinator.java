package testIntegrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import org.omg.CORBA.ORB;

import br.usp.ime.oppstore.broker.OppStoreBroker;

import resourceProviders.Lrm;
import resourceProviders.LrmHelper;

import clusterManagement.ApplicationRepository;
import clusterManagement.Grm;
import dataTypes.ExecutionRequestId;
import dataTypes.FileStruct;
import dataTypes.RequestAcceptanceInformation;

/**
 * @author rcamargo
 */

public class TestCoordinator{

    private TestSuite currentTestSuite;
    private RemoteExecutionInformation remoteExecutionInformation;
    private FailureExperimentController failureController = null;
	
    static final String outputDirectory = "../executionOutput";
    static final boolean useFailures = false;
    
    private ORB orb;
    private Grm grm;
    private ApplicationRepository appRepos;
    
    private String appReposIor;
    private String grmIor;
    private String asctIor;
    
    private OppStoreBroker broker;
        
    private HashMap<String, AcceptedExecutionInformation> acceptedExecutionInformationMap;
        
    TestCoordinator(ORB orb, Grm grm, ApplicationRepository appRepos) {
        this.orb = orb;
        this.grm = grm;
        this.appRepos = appRepos;
        this.broker = new OppStoreBroker();
    
        this.grmIor = orb.object_to_string(grm);        
        this.appReposIor = orb.object_to_string(appRepos);
        this.acceptedExecutionInformationMap = new HashMap<String, AcceptedExecutionInformation>();
        
        // Deletes old output files
        File outputDirectoryFile = new File(outputDirectory);        
        deleteExecutionOutput(outputDirectoryFile);
        outputDirectoryFile.mkdir();
        
        try {
        	(new File(outputDirectory)).mkdir();
			Runtime.getRuntime().exec("rm " + outputDirectory + "/* -rf");			
		} catch (IOException e) { e.printStackTrace(); }
    }
    
    private boolean deleteExecutionOutput(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteExecutionOutput(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }     
        return dir.delete();
    }
    
    public void setAsctIor(String asctIor) {
        this.asctIor = asctIor;        
        this.remoteExecutionInformation = new RemoteExecutionInformation(this.asctIor, grmIor, appReposIor, broker);
    }
        
	public void configureFailureExperiments () {
	    	   
	}

    public void performTest(TestSuite testSuite) {
    	    	
    	RemoteExecutionParameters executionParameters;
    	this.currentTestSuite = testSuite;
    	this.currentTestSuite.setBroker(broker);
    	   		
    	while  ( (executionParameters = testSuite.getNextRemoteExecution()) != null ) {
    		
    		//System.out.println(executionParameters.inputFiles);
    		
    		executionParameters.executionLock.lock();

    		System.out.println("Submitting request " + executionParameters.executionId + ".");
    		
    		remoteExecutionInformation.prepareRemoteExecution(executionParameters);
    		
            // Initializes the execSpecs map entry for this application
    		String requestId = remoteExecutionInformation.processExecutionInformationList[0].executionRequestId.requestId; 
            synchronized(acceptedExecutionInformationMap){
                acceptedExecutionInformationMap.put( requestId, remoteExecutionInformation.acceptedExecutionInformation );
            }

            if (useFailures) {
            	String lambda = "20";
            	int nProcesses = remoteExecutionInformation.processExecutionInformationList.length;            	
            	
            	if (failureController != null)
            		failureController.setExperimentFinished();                	    
            	failureController = new FailureExperimentController();
            	failureController.prepareExperiment(Integer.parseInt(requestId), nProcesses, lambda, 1);
            }
            
            //System.out.println(remoteExecutionInformation.processExecutionInformationList[0].inputFileNames);
            
            grm.requestRemoteExecution(remoteExecutionInformation.applicationExecutionInformation, 
            						   remoteExecutionInformation.processExecutionInformationList);

            if (useFailures)
            	failureController.start();

    		try { executionParameters.executionCondition.await(); }
    		catch (InterruptedException e) {}
			
    		executionParameters.executionLock.unlock();
    	}    		
    	
    	System.out.println("Experiments finished.");
    }

    /**
     * @param executionRequestId
     * @return
     */
    public String[] getAppInputFiles(ExecutionRequestId executionRequestId) {

        AcceptedExecutionInformation execSpecs;
        execSpecs = (AcceptedExecutionInformation)acceptedExecutionInformationMap.get(executionRequestId.requestId);
        if (execSpecs == null) return null;
     
        String[] inputFiles = execSpecs.inputFiles[Integer.parseInt(executionRequestId.processId)];
        return inputFiles;
    }

    /**
     * @param executionRequestId
     */
    public void appFinished(ExecutionRequestId executionRequestId, String[] outputFiles) {
       
		boolean useOppStore = false;
		if (outputFiles != null && outputFiles.length > 0 && outputFiles[0].contains("[key]") )
			useOppStore = true;

						
        AcceptedExecutionInformation execSpecs = null;
        synchronized(acceptedExecutionInformationMap){
            execSpecs = (AcceptedExecutionInformation)acceptedExecutionInformationMap.get(executionRequestId.requestId);
        }
        
        execSpecs.finishedNodes++;
        if (execSpecs.finishedNodes == execSpecs.lrmIorList.length) {
            System.out.println("Our request id: " + executionRequestId.requestId + " has FINISHED succesfully.");
            RemoteExecutionParameters executionParameters = currentTestSuite.getLastExecutionParameters();
            
            if (useOppStore) {
            	int status = collectResultsToFile(executionRequestId, outputFiles);
            }
            else {
            	Vector<FileStruct[]> applicationReturnedFiles = collectResults(executionRequestId);                         
            	try {
            		executionParameters.executionLock.lock();
            		currentTestSuite.analyseRemoteExecutionResults(applicationReturnedFiles);
            		executionParameters.executionCondition.signal(); 
            		executionParameters.executionLock.unlock();
            	}
            	catch (Exception e) {
            		e.printStackTrace();
            	}
            }
        }
       
    }

    private int collectResultsToFile(ExecutionRequestId executionRequestId, String[] outputFiles){

    	String message = executionRequestId.requestId + "-> downloadstart:" + System.currentTimeMillis();
    	currentTestSuite.writeToLog(message);

    	String executionOutputDirectory = outputDirectory + "/" + executionRequestId.requestId;
        if(! (new File(executionOutputDirectory)).mkdir()){
          System.out.println("Directory creation failed: " + executionOutputDirectory);
        }
        
        AcceptedExecutionInformation execSpecs;
        synchronized(acceptedExecutionInformationMap){
          execSpecs = (AcceptedExecutionInformation) acceptedExecutionInformationMap.get(executionRequestId.requestId);
        }    
        System.out.println("Requesting results from " + execSpecs.lrmIorList.length + " nodes");
        
        int nProcesses = execSpecs.lrmIorList.length;
                
        for(int i = 0; i < nProcesses; i++){

          String subpath = outputDirectory + "/" + (executionRequestId.requestId) + "/" + i;
          if(! (new File(subpath)).mkdir()){
            System.out.println("Directory creation failed: " + subpath);
          }
          
          for(int j = 0; j < outputFiles.length; j++){

              String[] splitFileName = outputFiles[j].split(".key.");
              String filePath  = subpath + "/" + splitFileName[0]; 
              String outputKey = splitFileName[1];								

              System.out.println("Downloading file " + outputKey + " to path " + filePath + ".");
              int status = broker.retrieveFileW(outputKey, filePath);
              System.out.println( "status = " + status );

          }
          
        }//for
 
        System.out.println("Results collected sucesfully.");

    	message = executionRequestId.requestId + "-> downloadfinish:" + System.currentTimeMillis();
    	currentTestSuite.writeToLog(message);

        return 0;
        	
    }//method

    private Vector<FileStruct[]> collectResults(ExecutionRequestId executionRequestId){

    	String executionOutputDirectory = outputDirectory + "/" + executionRequestId.requestId;
        if(! (new File(executionOutputDirectory)).mkdir()){
          System.out.println("Directory creation failed: " + executionOutputDirectory);
        }
        
        AcceptedExecutionInformation execSpecs;
        synchronized(acceptedExecutionInformationMap){
          execSpecs = (AcceptedExecutionInformation) acceptedExecutionInformationMap.get(executionRequestId.requestId);
        }    
        System.out.println("Requesting results from " + execSpecs.lrmIorList.length + " nodes");
        
        int nProcesses = execSpecs.lrmIorList.length;
        
        Vector<FileStruct[]> applicationReturnedFiles = new Vector<FileStruct[]>(nProcesses); 
        
        for(int i = 0; i < nProcesses; i++){

          String subpath = outputDirectory + "/" + (executionRequestId.requestId) + "/" + i;
          if(! (new File(subpath)).mkdir()){
            System.out.println("Directory creation failed: " + subpath);
          }
          Lrm lrm = LrmHelper.narrow( orb.string_to_object(execSpecs.lrmIorList[i]) );
          
          FileStruct [] returnFiles = lrm.requestOutputFiles(execSpecs.nodeExecutionIdList[i]);
          applicationReturnedFiles.add(returnFiles);
          for(int j = 0; j < returnFiles.length; j++){
            try{
              FileOutputStream fos =
               new FileOutputStream(subpath + "/" + returnFiles[j].fileName);
              fos.write(returnFiles[j].file);
              fos.close();
            }
            catch(IOException ioe){
              System.err.println("collectResults-->> Write failed");
            }
          }//for          
          
        }//for
 
        System.out.println("Results collected sucesfully.");
        
        return applicationReturnedFiles;
        	
    }//method

    /**
     * @param offerSpecs
     */
    public void acceptedExecutionRequest(RequestAcceptanceInformation offerSpecs) {
        
    	String hostName = null;
    	
        //failure
        Runtime runTime = Runtime.getRuntime();
        //System.out.println(offerSpecs.lrmIor);
        try {
            Process dior = runTime.exec("/home/pub/JacORB/bin/dior " + offerSpecs.lrmIor);
            BufferedReader in = new BufferedReader(new InputStreamReader(dior.getInputStream()));
            
            dior.waitFor();

            String line = in.readLine();
            int index = line.indexOf("Host"); 
            while ( index < 0 ) {
                line = in.readLine();
                index = line.indexOf("Host");             
            }
            String[] hosts = line.split(":");
            hostName = hosts[hosts.length-1];            
            //hostName = "orlandia";
            
            //System.out.println(Integer.parseInt(offerSpecs.executionRequestId.processId) + ": " + hostName + " appId:" + offerSpecs.executionId);
            if (failureController != null)
                failureController.addHost(Integer.parseInt(offerSpecs.executionRequestId.processId), hostName, offerSpecs.executionId);                                   
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }                   
        
        synchronized(acceptedExecutionInformationMap){
            AcceptedExecutionInformation executionInformation = 
            	acceptedExecutionInformationMap.get(offerSpecs.executionRequestId.requestId);
            int nodeId = Integer.parseInt(offerSpecs.executionRequestId.processId);
            executionInformation.nodeExecutionIdList[nodeId] = offerSpecs.executionId;
            executionInformation.lrmIorList[nodeId] = offerSpecs.lrmIor;
            executionInformation.processHostList[nodeId] = hostName;
            executionInformation.acceptedExecutions++;
            if (executionInformation.acceptedExecutions == executionInformation.nodeExecutionIdList.length) {            	
                System.out.println("Our request id: " + offerSpecs.executionRequestId.requestId + " was ACCEPTED.");
                currentTestSuite.setExecutionStarted(executionInformation);
            }
        }
    }

    /**
     * @param executionRequestId
     */
    public void refusedExecutionRequest(ExecutionRequestId executionRequestId) {
        System.out.println("The esecution request was REFUSED :-(");
        
    }

}
