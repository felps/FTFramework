package grm.executionManager;

import grm.executionManager.ExecutionInformation;
import grm.executionManager.dataBase.ExecutionDatabaseManager;
import grm.executionManager.dataTypes.ProcessExecutionStateTypes;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.omg.CORBA.ORB;

import resourceProviders.Lrm;
import resourceProviders.LrmHelper;
import tools.Asct;
import tools.AsctHelper;

import clusterManagement.Grm;
import dataTypes.ApplicationExecutionInformation;
import dataTypes.ApplicationType;
import dataTypes.ExecutionRequestId;
import dataTypes.ProcessExecutionInformation;


public class StandardFinishedExecutionManager {

    Map<String, ExecutionInformation> currentExecutionsMap;
    ORB orb;
    Grm grm;
	ExecutionDatabaseManager executionDatabaseManager;
	
    //--------------------------------------------------------------------------------------
    
	StandardFinishedExecutionManager(
			Map<String, ExecutionInformation> currentExecutionsMap, ORB orb,
			Grm grm, ExecutionDatabaseManager executionDatabaseManager) {
		this.currentExecutionsMap = currentExecutionsMap;
		this.orb = orb;
		this.grm = grm;
		this.executionDatabaseManager = executionDatabaseManager;
	}
	
    //--------------------------------------------------------------------------------------
    
    public int treatFinishedExecution(ExecutionInformation appInfo, ExecutionRequestId executionRequestId, 
    		String[] outputFileNames, int restartId, int executionState, int executionCode) {

		if (executionState == ProcessExecutionStateTypes.FINISHED) {
			processNormalExit(executionRequestId, appInfo, executionState, executionCode);
			reportRequestingASCT(executionRequestId, outputFileNames);
		}
		else if (executionState == ProcessExecutionStateTypes.FINISHED_WITH_HANDLED_FAILURE )
			processRestart(executionRequestId, appInfo, executionState, executionCode);
		else {
			processAbnormalExit(executionRequestId, appInfo, executionState, executionCode);
			reportRequestingASCT(executionRequestId, outputFileNames);
		}

        return 0;
    }

    //--------------------------------------------------------------------------------------
    
	void reportRequestingASCT(ExecutionRequestId executionRequestId, String[] outputFileNames){
		
		String [] reqId = executionRequestId.requestId.split(":");
		String asctApplicationId = reqId[2];
		try {		
			ApplicationExecutionInformation execInfo = executionDatabaseManager.getApplicationExecutionInformation(executionRequestId.requestId);
			Asct asct = AsctHelper.narrow(orb
					.string_to_object(execInfo.requestingAsctIor));

                 	executionRequestId.requestId=asctApplicationId;
			asct.setExecutionFinished(executionRequestId, outputFileNames);		
		
		}catch(org.omg.CORBA.TRANSIENT transientException) {
			System.err.println("Unable to reach ASCT. Failure notification is incomplete.");
		}
	
	}

	// --------------------------------------------------------------------------------------

    int processNormalExit(ExecutionRequestId executionRequestId, ExecutionInformation executionInfo, int executionState,
			int executionCode) {

		String requestId = executionRequestId.requestId;
		String processId = executionRequestId.processId;

		this.executionDatabaseManager.changeProcessExecutionStateToFinished(
				requestId, processId, executionState, executionCode);
				
        executionInfo.processLocationMap.remove(Integer.parseInt(executionRequestId.processId));                
        
        /** Application is removed from currentExecutions */
        if (executionInfo.processLocationMap.size()==0) {
            System.out.println("ExecutionManagerImpl.reportExecutionFinished--> Execution Finished [" + executionRequestId.requestId + "]");
            executionInfo.checkpoints.removeExecutionCheckpoints();
            currentExecutionsMap.remove(executionRequestId.requestId);
        }
        
        return 0;
    }

    //--------------------------------------------------------------------------------------
    
    int processAbnormalExit(ExecutionRequestId executionRequestId, ExecutionInformation executionInfo, int executionState,
			int executionCode) {

		String requestId = executionRequestId.requestId;
		String processId = executionRequestId.processId;

		this.executionDatabaseManager.changeProcessExecutionStateToFinished(
				requestId, processId, executionState, executionCode);

        /** Remove the reported node from the list, so that it will not be killed again */
        executionInfo.processLocationMap.remove(executionRequestId.processId);                   
        
        /** Kills all the remaining processes */
        Iterator<String[]> it = executionInfo.processLocationMap.values().iterator();                                     
        while (it.hasNext()) {
            String[] lrmIorApplicationId = it.next();
            Lrm lrm = LrmHelper.narrow( orb.string_to_object( lrmIorApplicationId[0] ));
            String applicationId = lrmIorApplicationId[1];               
            if (ExecutionManagerDebugFlag.debug)
                System.err.println(">>>>> ExecutionManagerImpl.processAbnormalExit--> KILLING !!!!! " + " (" + applicationId + ")");
            lrm.killProcess(applicationId);
        }                               
        
        /** Application is removed from the ExecutionManager */
        System.out.println("ExecutionManagerImpl.reportExecutionFinished--> Execution finished [" + executionRequestId.requestId + "]");
        executionInfo.checkpoints.removeExecutionCheckpoints();
        currentExecutionsMap.remove(executionRequestId.requestId);                
        
        return 0;
    }

    //--------------------------------------------------------------------------------------
    
    int processRestart(ExecutionRequestId executionRequestId, ExecutionInformation executionInfo, int executionState,
			int executionCode) {

		String requestId = executionRequestId.requestId;
		String processId = executionRequestId.processId;

		this.executionDatabaseManager.changeProcessExecutionStateToFinished(
				requestId, processId, executionState, executionCode);

		if (executionInfo.applicationInformation.applicationType == ApplicationType.regular) {

			String[] nodeIorApplicationId = executionInfo.processLocationMap
					.remove(Integer.parseInt(executionRequestId.processId));
			Lrm lrm = LrmHelper.narrow(orb.string_to_object(nodeIorApplicationId[0]));
			int ckpNumber = lrm.getLastCheckpointNumber(nodeIorApplicationId[1]);
			if (ckpNumber >= 0) {
				String ckpName = " -r" + ckpNumber + " -d../"
						+ nodeIorApplicationId[1];
				executionInfo.processInformationList[0].processArguments = ckpName;
			}
			grm.requestRemoteExecution(executionInfo.applicationInformation,
					executionInfo.processInformationList);

		}

		/** Removes the node from appInfo and reschedule the execution of the finished process */
		else if (executionInfo.applicationInformation.applicationType == ApplicationType.parametric) {

			ProcessExecutionInformation processExecutionInformation = executionInfo.processInformationList[Integer
					.parseInt(executionRequestId.processId)];
			String[] nodeIorApplicationId = executionInfo.processLocationMap
					.remove(Integer.parseInt(executionRequestId.processId));
			Lrm lrm = LrmHelper.narrow(orb.string_to_object(nodeIorApplicationId[0]));
			int ckpNumber = lrm.getLastCheckpointNumber(nodeIorApplicationId[1]);
			if (ckpNumber >= 0) {
				String ckpName = " -r" + ckpNumber + " -d../"
						+ nodeIorApplicationId[1];
				processExecutionInformation.processArguments = ckpName;
			}
			grm.requestRemoteExecution( executionInfo.applicationInformation,
					new ProcessExecutionInformation[] { processExecutionInformation });
		}

		return 0;
	}

    //--------------------------------------------------------------------------------------
    
}
