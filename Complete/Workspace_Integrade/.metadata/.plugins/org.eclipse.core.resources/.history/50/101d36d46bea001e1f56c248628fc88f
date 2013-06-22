package grm.ckpReposManager;

import java.util.concurrent.ConcurrentHashMap;

import grm.executionManager.ExecutionInformation;
import grm.executionManager.ExecutionManagerImpl;
import clusterManagement.CheckpointingInformation;
import clusterManagement.CkpReposManagerPOA;

public class CkpReposManagerImpl extends CkpReposManagerPOA {

	private ConcurrentHashMap<String, ExecutionInformation> currentExecutionsMap;
	
	public CkpReposManagerImpl(ExecutionManagerImpl executionManagerImpl) {
		this.currentExecutionsMap = executionManagerImpl.getCurrentExecutionsMap();
	}
	
    /**
     * Obtains information regarding the last checkpoint stored.
     * @return CkpInfo, which contains the checkpoint number and location.
     * 
     */    
    public CheckpointingInformation getCheckpointingInformation(String executionId) {
        int executionIdDelimiter = executionId.lastIndexOf(':');
        int processId = Integer.parseInt(executionId.substring(executionIdDelimiter+1));        
        ExecutionInformation executionInfo = currentExecutionsMap.get(executionId.substring(0, executionIdDelimiter));                
        
        /**
         * Obtains the checkpoint info from the application executionInfo
         */
        CheckpointingInformation checkpointingInformation = null;
        if (executionInfo != null)
        	checkpointingInformation = executionInfo.checkpoints.getLastCkpInfo(processId, 3);
        
        return checkpointingInformation;
    }
    
    /**
     * notifyCkpStored
     * 
     */    
    public void setCheckpointStored(String executionId, String checkpointId, int checkpointNumber) {
        int executionIdDelimiter = executionId.lastIndexOf(':');
        int processId = Integer.parseInt(executionId.substring(executionIdDelimiter+1));        
        ExecutionInformation executionInfo = currentExecutionsMap.get(executionId.substring(0, executionIdDelimiter));
        
        if (executionInfo != null)
            executionInfo.checkpoints.setCheckpointStored(processId, checkpointId, checkpointNumber);
        
    }
}
