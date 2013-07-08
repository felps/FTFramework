package grm.executionManager;

import grm.executionManager.ExecutionInformation;
import grm.executionManager.dataBase.ExecutionDatabaseManager;
import grm.executionManager.dataTypes.ProcessExecutionStateTypes;

import java.util.Map;
import org.omg.CORBA.ORB;

import clusterManagement.Grm;

import dataTypes.ExecutionRequestId;

public class ParallelFinishedExecutionManager extends StandardFinishedExecutionManager {

    //--------------------------------------------------------------------------------------
    
	ParallelFinishedExecutionManager(
			Map<String, ExecutionInformation> currentExecutionsMap, ORB orb,
			Grm grm, ExecutionDatabaseManager executionDatabaseManager) {
		super(currentExecutionsMap, orb, grm, executionDatabaseManager);
	}

    //--------------------------------------------------------------------------------------
    
    int processRestart(ExecutionRequestId executionRequestId, ExecutionInformation appInfo) {
        appInfo.restartCoordinator.restartApplication(executionRequestId, appInfo, true);
        return 0;
    }

    //--------------------------------------------------------------------------------------
    
    public int treatFinishedExecution(ExecutionInformation appInfo, ExecutionRequestId executionRequestId, 
    		String[] outputFileNames, int restartId, int executionState, int executionCode) {

        /** Only one process from an execution shall pass each time */        
        appInfo.isActiveLock.lock();
        try {
            
            /** This is a failure of the next iteration.
             *  Must wait until all processes from the previous iteration start. */        
            if (restartId == appInfo.currentRestart + 1) {
                try {appInfo.nextRestartCondition.await();}
                catch (Exception e) {e.printStackTrace();}
            }
           
            /** Process that finished during a restart process */
            if (appInfo.isRestarting == true) {
                appInfo.restartCoordinator.restartApplication(executionRequestId, appInfo, false);
            }
            /** Process that finished due to a error during the abort process */
            else if (appInfo.isAborting == true) {
                appInfo.processLocationMap.remove(executionRequestId.processId);
            }
            /** Process finished */
			else {
				if (executionState == ProcessExecutionStateTypes.FINISHED){
					processNormalExit(executionRequestId, appInfo,
							executionState, executionCode);

					reportRequestingASCT(executionRequestId, outputFileNames);
				}
				else if (executionState == ProcessExecutionStateTypes.FINISHED_WITH_HANDLED_FAILURE) {
					appInfo.isRestarting = true;
					appInfo.restartCoordinator.restartApplication(
							executionRequestId, appInfo, true);
				} else {
					/** TODO: Insert synchronization here */
					appInfo.isAborting = true;
					processAbnormalExit(executionRequestId, appInfo,
							executionState, executionCode);
					reportRequestingASCT(executionRequestId, outputFileNames);
				}
			}
                        
            return 0;
        }
        finally{ appInfo.isActiveLock.unlock(); }                
    }

}
