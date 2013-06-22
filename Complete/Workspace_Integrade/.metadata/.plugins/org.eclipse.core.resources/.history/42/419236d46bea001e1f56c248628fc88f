package grm.executionManager;

import grm.ckpReposManager.ExecutionCheckpoints;
import grm.executionManager.dataBase.ExecutionDatabaseManager;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.omg.CORBA.ORB;

import tools.Asct;
import tools.AsctHelper;
import clusterManagement.ExecutionManagerPOA;
import clusterManagement.Grm;
import dataTypes.ApplicationExecutionInformation;
import dataTypes.ApplicationType;
import dataTypes.BspProcessZeroInformation;
//{IMPI
import dataTypes.MpiConnectInformation;
//}IMPI
import dataTypes.ExecutionRequestId;
import dataTypes.ProcessExecutionInformation;
import dataTypes.RequestAcceptanceInformation;
import dataTypes.ApplicationExecutionStateInformation;
import grm.executionManager.dataTypes.ApplicationExecutionStateTypes;
import dataTypes.ProcessExecutionStateInformation;
import grm.executionManager.dataTypes.ProcessExecutionStateTypes;

/** Flag to display debugging info. */
interface ExecutionManagerDebugFlag {
    public static final boolean debug = true;
}

public class ExecutionManagerImpl extends ExecutionManagerPOA {  
    
    private ConcurrentHashMap<String, ExecutionInformation> currentExecutionsMap;
    private Grm grm; // Reference to the GRM corba object
    private ORB orb; // Reference to the ORB
  /*  private BspFinishedExecutionManager bspFinishedExecutionManager; 
//{IMPI
    private MpiFinishedExecutionManager mpiFinishedExecutionManager; 
//}IMPI
  */
    private ParallelFinishedExecutionManager parallelFinishedExecutionManager;
    private StandardFinishedExecutionManager standardFinishedExecutionManager;
	ExecutionDatabaseManager executionDatabaseManager;    
    
    public ExecutionManagerImpl(ORB orb, Grm grm) {
        this.orb = orb;
        this.grm = grm;
        this.executionDatabaseManager = new ExecutionDatabaseManager();
        this.currentExecutionsMap = new ConcurrentHashMap<String, ExecutionInformation>();
       // this.bspFinishedExecutionManager       = new BspFinishedExecutionManager(currentExecutionsMap, orb, grm, executionDatabaseManager);
//{IMPI
        //this.mpiFinishedExecutionManager       = new MpiFinishedExecutionManager(currentExecutionsMap, orb, grm, executionDatabaseManager);
//}IMPI
        this.parallelFinishedExecutionManager       = new ParallelFinishedExecutionManager(currentExecutionsMap, orb, grm, executionDatabaseManager);
        this.standardFinishedExecutionManager  = new StandardFinishedExecutionManager(currentExecutionsMap, orb, grm, executionDatabaseManager);
    }

    public ConcurrentHashMap<String, ExecutionInformation> getCurrentExecutionsMap() {
		return currentExecutionsMap;
	}
    
    /** ---------------------------------------------------------------------------
     *  ExecutionManager Methods
     *  ---------------------------------------------------------------------------
     **/

    
    // TODO: Check if only part of the processes from an application
    // Insert a timer that periodically checks each scheduled application application 
    
    public void setExecutionScheduled(ApplicationExecutionInformation applicationExecutionInformation,
            ProcessExecutionInformation[] processExecutionInformationList) {
        
        if (ExecutionManagerDebugFlag.debug)
            System.err.println(">>>>> ExecutionManagerImpl.reportExecutionScheduled--> applicationId:" + 
            		processExecutionInformationList[0].executionRequestId.requestId);        
                
        String requestId = processExecutionInformationList[0].executionRequestId.requestId;        
        
		executionDatabaseManager.registerApplicationExecution(
				applicationExecutionInformation, processExecutionInformationList);

		executionDatabaseManager.changeApplicationExecutionState(
				requestId, ApplicationExecutionStateTypes.SCHEDULED);
		
        ExecutionInformation appInfo = new ExecutionInformation();
        
        if (currentExecutionsMap.putIfAbsent(requestId, appInfo) == null) {                        
            appInfo.applicationInformation   = applicationExecutionInformation;
            appInfo.processInformationList = processExecutionInformationList;
            appInfo.processLocationMap = new HashMap<Integer, String[]>(processExecutionInformationList.length);
            appInfo.checkpoints = new ExecutionCheckpoints(requestId, processExecutionInformationList.length);
            
            if (applicationExecutionInformation.applicationType == ApplicationType.bsp) {
                appInfo.restartCoordinator = new BspRestartCoordinator(orb, grm, processExecutionInformationList.length);      
                appInfo.isActiveLock = new ReentrantLock();
                appInfo.nextRestartCondition = appInfo.isActiveLock.newCondition();
                appInfo.waitingResponsesLock = new ReentrantLock();
                appInfo.finishedResponsesCondition = appInfo.waitingResponsesLock.newCondition();                
            }
//{IMPI
		else if (applicationExecutionInformation.applicationType == ApplicationType.mpi) 
		{
			appInfo.restartCoordinator = new MpiRestartCoordinator(orb, grm, processExecutionInformationList.length);      
			appInfo.isActiveLock = new ReentrantLock();
			appInfo.nextRestartCondition = appInfo.isActiveLock.newCondition();
			appInfo.waitingResponsesLock = new ReentrantLock();
			appInfo.finishedResponsesCondition = appInfo.waitingResponsesLock.newCondition();                
		}
//}IMPI
        }

    }

	public void setExecutionRefused(
			ApplicationExecutionInformation applicationExecutionInformation,
			ProcessExecutionInformation[] processExecutionInformationList) {

		String requestId = processExecutionInformationList[0].executionRequestId.requestId;

		executionDatabaseManager.registerApplicationExecution(
				applicationExecutionInformation, processExecutionInformationList);

		executionDatabaseManager.changeApplicationExecutionState(requestId,
				ApplicationExecutionStateTypes.REFUSED);

	}

    //--------------------------------------------------------------------------------------
    
    public void setProcessExecutionStarted(String lrmIor, String executionId,
            int restartId, ExecutionRequestId executionRequestId) {

		executionDatabaseManager.changeProcessExecutionStateToExecutingOrFailure(
				executionRequestId.requestId,
				executionRequestId.processId, lrmIor,
				ProcessExecutionStateTypes.EXECUTING);

		// sends messages to Asct only if the process has no replicas
		if(!executionRequestId.requestId.contains(":")){
			ApplicationExecutionInformation execInfo = 		executionDatabaseManager.getApplicationExecutionInformation(executionRequestId.requestId);
			Asct asct = AsctHelper.narrow(orb
					.string_to_object(execInfo.requestingAsctIor));
			RequestAcceptanceInformation requestInfo = new RequestAcceptanceInformation();
			requestInfo.executionId = executionId;
			requestInfo.executionRequestId = executionRequestId;
			requestInfo.lrmIor = lrmIor;			
			asct.setExecutionAccepted(requestInfo);			
		}

        ExecutionInformation appInfo = currentExecutionsMap.get(executionRequestId.requestId);
        if (appInfo == null) {
            /** TODO: We should treat this error better. It would be a good idea to kill the process in the LRM. */
            System.err.println("ExecutionManagerImpl.reportExecutionStarted --> ERROR: Couldn't find execution info.");
            System.err.println("appId:" + executionId + " asctRequestId:" + executionRequestId.requestId+ "|" + executionRequestId.processId);
        }

        int processId = Integer.parseInt(executionRequestId.processId);
        appInfo.processLocationMap.put(processId, new String[] {lrmIor, executionId});
        int nRestarts = appInfo.nRestarts.incrementAndGet();

        if (ExecutionManagerDebugFlag.debug)
            System.err.println(">>>>> ExecutionManagerImpl.reportExecutionStarted-->asctRequestId:" + executionRequestId.requestId + "|" + executionRequestId.processId +  ", nRestarts=" + nRestarts);       
        
        // Waits for all responses from appInfo 
        while (appInfo.isRestarting == true && appInfo.nResponses.get() < appInfo.processInformationList.length) {
            try {Thread.sleep(100);} 
            catch (InterruptedException e) {}
        }
            
        if (nRestarts == appInfo.processInformationList.length) {
            System.out.println("ExecutionManagerImpl.reportExecutionStarted--> Execution Started [" + executionRequestId.requestId + "," + restartId + "]");                
            appInfo.currentRestart++;
            if (appInfo.isActiveLock != null) {
                appInfo.isActiveLock.lock();
                try{
                    if (ExecutionManagerDebugFlag.debug)
                        System.out.println("Finished Starting");
                    appInfo.isRestarting = false;
                    appInfo.nextRestartCondition.signalAll();
                }
                finally{ appInfo.isActiveLock.unlock(); }
            }
        }
 
    }

    //--------------------------------------------------------------------------------------
    
    public int setProcessExecutionFinished(int restartId, ExecutionRequestId executionRequestId, String[] outputFileNames, int executionState, int executionCode) {
                
        if (ExecutionManagerDebugFlag.debug)
            System.err.println(">>>>> ExecutionManagerImpl.reportExecutionFinished-->asctRequestId: " + executionRequestId.requestId + "|" + executionRequestId.processId + " executionState:" + executionState + " executionCode:" + executionCode + " restartId: " + restartId + ".");

        /** Get the appInfo for the finished process */
        ExecutionInformation appInfo = currentExecutionsMap.get(executionRequestId.requestId);
        if (appInfo == null) {
            System.err.println("ExecutionManagerImpl.reportExecutionFinished --> ERROR: No existing execution." + " asctRequestId:" + executionRequestId.requestId + "|" + executionRequestId.processId);
            return -1;
        }
        
        if (appInfo.applicationInformation.applicationType == ApplicationType.bsp)
            parallelFinishedExecutionManager.treatFinishedExecution(appInfo, executionRequestId, outputFileNames, restartId, executionState, executionCode);
//{IMPI
        else if (appInfo.applicationInformation.applicationType == ApplicationType.mpi)
        	parallelFinishedExecutionManager.treatFinishedExecution(appInfo, executionRequestId, outputFileNames,restartId, executionState, executionCode);
//}IMPI
        else
            standardFinishedExecutionManager.treatFinishedExecution(appInfo, executionRequestId, outputFileNames, restartId, executionState, executionCode);
        
        return 0;
    }

	// --------------------------------------------------------------------------------------

	public ApplicationExecutionInformation getApplicationExecutionInformation(String requestId){
		return executionDatabaseManager.getApplicationExecutionInformation(requestId);	
	}

        
	public ApplicationExecutionStateInformation getApplicationExecutionStateInformation(String requestId){
		return executionDatabaseManager.getApplicationExecutionStateInformation(requestId);
	}
	        

	public ProcessExecutionInformation getProcessExecutionInformation(ExecutionRequestId executionRequestId){
		return executionDatabaseManager.
			getProcessExecutionInformation(executionRequestId.requestId,executionRequestId.processId);

	}

      
	public ProcessExecutionStateInformation getProcessExecutionStateInformation(ExecutionRequestId executionRequestId){
		return executionDatabaseManager.
			getProcessExecutionStateInformation(executionRequestId.requestId,executionRequestId.processId);
	}

    //--------------------------------------------------------------------------------------
    
    public BspProcessZeroInformation registerBspProcess(ExecutionRequestId executionRequestId,
            String bspProxyIor) {
        
        /** Get the appInfo for the finished process */
        ExecutionInformation appInfo = currentExecutionsMap.get(executionRequestId.requestId);
        if (appInfo == null) {
            System.err.println("ExecutionManagerImpl.registerBspNode --> ERROR: No existing execution." + " asctRequestId:" + executionRequestId.requestId + "|" + executionRequestId.processId);
            return new BspProcessZeroInformation();
        }
        
        return ((BspRestartCoordinator)appInfo.restartCoordinator).registerBspNode(executionRequestId, bspProxyIor);
    }
    
    //--------------------------------------------------------------------------------------
//{IMPI
	public MpiConnectInformation[] registerMpiProcess(ExecutionRequestId executionRequestId,
						String kvs, int rank) 
	{
		/** Get the appInfo for the finished process */
		ExecutionInformation appInfo = currentExecutionsMap.get(executionRequestId.requestId);
		if (appInfo == null) {
			System.err.println("ExecutionManagerImpl.registerMpiProcess --> ERROR: No existing execution." + " asctRequestId:" + executionRequestId.requestId + "|" + executionRequestId.processId);
			return new MpiConnectInformation[]{};
		}

		return ((MpiRestartCoordinator)appInfo.restartCoordinator).registerMpiNode(executionRequestId, kvs, rank);
	}
//}IMPI
    //--------------------------------------------------------------------------------------
}
