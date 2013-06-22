package grm.ckpReposManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import br.usp.ime.oppstore.broker.OppStoreBroker;
import clusterManagement.CheckpointingInformation;

/**
 * Contains information about all checkpoints from an execution.
 * Is kept as a filed in the class ExecutionInformation from the ExecutionManager.
 */
public class ExecutionCheckpoints {
    
    HashMap<Integer, GlobalCkpInformation> globalCheckpointInformationList;    
    int numberOfProcesses;
    String requestId;
	OppStoreBroker oppStoreBroker;
        
    public ExecutionCheckpoints(String requestId, int numberOfProcesses) {
        globalCheckpointInformationList = new HashMap<Integer, GlobalCkpInformation>();
        this.numberOfProcesses = numberOfProcesses;
        this.requestId = requestId;
        this.oppStoreBroker = new OppStoreBroker();
    }

    //------------------------------------------------------------------------
    
    synchronized public int setCheckpointStored(int processId, String checkpointKey, int checkpointNumber) {

    	System.out.println("Set checkpoint stored. processId=" + processId );
    	
        GlobalCkpInformation globalCkpInfo = globalCheckpointInformationList.get(checkpointNumber);
        if ( globalCkpInfo == null ) {
        	globalCkpInfo = new GlobalCkpInformation(numberOfProcesses, checkpointNumber);
        	globalCheckpointInformationList.put( checkpointNumber, globalCkpInfo );
        }
            
        globalCkpInfo.setCheckpointStored( processId, checkpointKey );
        if ( globalCkpInfo.isComplete() ) {
        	System.out.println("notifyCkpStored --> Finished storing checkpoint " + checkpointNumber + " from request " + requestId + ".");
        	removeOldCheckpoints(checkpointNumber-3);
        }
        
        return 0;
    }

    //------------------------------------------------------------------------------------
    
	/**
	 * Removes from OppStore all checkpoints whose number is smaller than 'lastCheckpointToKeep'
	 */
    synchronized public void removeOldCheckpoints (int lastCheckpointToKeep) {
    	
    	List<Integer> ckpRemovalList = new LinkedList<Integer>();
    	
    	for ( Entry<Integer, GlobalCkpInformation> ckpEntry : globalCheckpointInformationList.entrySet()) {
    		
    		if ( ckpEntry.getKey() < lastCheckpointToKeep ) {
    			
    			ckpRemovalList.add( ckpEntry.getKey() );
    			for (int process=0; process < numberOfProcesses; process++) {
    				String key = ckpEntry.getValue().getKeyList(process);
    				oppStoreBroker.removeDataW(key, null);
    			}
    		}
    	}
    	
    	for (Integer ckpNumber : ckpRemovalList)
    		globalCheckpointInformationList.remove(ckpNumber);    	
    }

    //------------------------------------------------------------------------------------
    
    /**
     * Removes from OppStore all checkpoints from this execution
     */
    synchronized public void removeExecutionCheckpoints () {
    	
    	for (GlobalCkpInformation ckpInfo : globalCheckpointInformationList.values()) {    		
    		for (int process=0; process < numberOfProcesses; process++) {
    			String key = ckpInfo.getKeyList(process);
    			oppStoreBroker.removeDataW(key, null);
    		}
    	}
    	
    	globalCheckpointInformationList.clear();    	
    }
    
    //------------------------------------------------------------------------------------
    
    /**
     * Returns the CheckpointingInformation for the highest numbered stored global checkpoint.
     */
    synchronized public CheckpointingInformation getLastCkpInfo (int processId, int numberOfCheckpoints) {

    	System.out.println("Getting last ckpInfo. numberOfCkps=" + globalCheckpointInformationList.size() );
    	
        CheckpointingInformation checkpointingInfo = new CheckpointingInformation();
        if (globalCheckpointInformationList.size() == 0) {
            checkpointingInfo.checkpointNumber = new int[0];
            checkpointingInfo.checkpointKey   = new String[0];
            return checkpointingInfo;
        }

        GlobalCkpInformation[] globalCkpInfoArray = 
        	globalCheckpointInformationList.values().toArray( new GlobalCkpInformation[globalCheckpointInformationList.size()] );
        
        int remainingCheckpoints = 0;
        for ( GlobalCkpInformation globalCkpInfo : globalCkpInfoArray )
        	if (globalCkpInfo.isComplete())
        		remainingCheckpoints++;
        
        if (remainingCheckpoints > numberOfCheckpoints)
        	remainingCheckpoints = numberOfCheckpoints;
        
        System.out.println("remainingCheckpoints=" + remainingCheckpoints );
        
        checkpointingInfo.checkpointNumber = new int[remainingCheckpoints];
        checkpointingInfo.checkpointKey    = new String[remainingCheckpoints];
        
        for (int i=globalCkpInfoArray.length-1; i >= 0 && remainingCheckpoints > 0; i--)
        	
        	if (globalCkpInfoArray[i].isComplete()) {
        		remainingCheckpoints--;
        		checkpointingInfo.checkpointNumber[remainingCheckpoints] = globalCkpInfoArray[i].getCkpNumber();
        		checkpointingInfo.checkpointKey[remainingCheckpoints]    = globalCkpInfoArray[i].getKeyList(processId);
        	}

        return checkpointingInfo;        
    }
    
} // class
