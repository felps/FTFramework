package grm.ckpReposManager;

import java.util.HashMap;

/**
 * Contains information about a global checkpoint
 */
public class GlobalCkpInformation {

	/**
     * The total number of saved checkpoint fragments
     */
    private HashMap<Integer, String> processKeyMap;
    
    /**
     * The identifier of this checkpoint
     */
    private int checkpointNumber;

    /**
     * The number of processes the application contains
     */
    private int numberOfProcesses;
        
    public GlobalCkpInformation(int numberOfProcesses, int checkpointNumber) {
    	this.processKeyMap     = new HashMap<Integer, String>();
    	this.numberOfProcesses = numberOfProcesses;
        this.checkpointNumber  = checkpointNumber;
    }
               
    public int setCheckpointStored(int processId, String checkpointKey) {

    	processKeyMap.put(processId, checkpointKey);
        
        return processKeyMap.size();
    }
    
    public int getNumberCkps() {return processKeyMap.size();}
    
    public boolean isComplete() {
        if (numberOfProcesses == processKeyMap.size()) 
        	return true;
        else 
        	return false;
    }
    
    public String getKeyList( int processId ) {
    	return processKeyMap.get(processId);
    }
    
    public int getCkpNumber() {return checkpointNumber;}

}
