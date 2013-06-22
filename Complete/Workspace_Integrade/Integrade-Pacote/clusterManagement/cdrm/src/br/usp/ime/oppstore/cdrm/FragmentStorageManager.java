package br.usp.ime.oppstore.cdrm;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.NodeHandle;
import br.usp.ime.oppstore.adrmanager.AdrManagerImpl;
import br.usp.ime.oppstore.message.StoreFragmentListMessage;
import br.usp.ime.oppstore.message.StoreFragmentMessage;
import br.usp.ime.virtualId.VirtualNode;

public class FragmentStorageManager  {

    private AdrManagerImpl adrManager;
    private VirtualNode virtualNode;
    private Logger logger;
    
    public FragmentStorageManager(VirtualNode virtualNode) {
	
    	this.adrManager  = new AdrManagerImpl( virtualNode );
    	this.virtualNode = virtualNode;
    	this.logger = Logger.getLogger("adrManager." + virtualNode.getNode().getId().toString().substring(0, 9) + ">");
	}
    
    public AdrManagerImpl getAdrManager() { return adrManager; }
    
    boolean processStoreFragmentListMessage(StoreFragmentListMessage message) {

    	logger.debug("Received fragment store request for fragment list from " + message.sourceHandle + ".");
    	
    	String adrAddress = adrManager.getAdrFromIpAddress( message.fragmentSizeList.get(0).intValue(), message.timeoutMinutes, message.brokerAddress );
    	if (adrAddress == null) adrAddress = "";
    	message.addAdrAddress( adrAddress );
    	
    	/**
    	 * Gets the address of a suitable ADRs
    	 */
    	for (int fragment = 1; fragment<message.fragmentIdList.size(); fragment++) {
    		adrAddress = adrManager.getAdrAddress( message.fragmentSizeList.get(fragment).intValue(), message.fragmentIdList.get(fragment), message.timeoutMinutes, message.brokerAddress);
    		if (adrAddress == null) adrAddress = "";
    		message.addAdrAddress( adrAddress );
    	}

    	message.isResponse = true;
    	logger.debug("CDRM " + virtualNode.getNode().getId() + " is returning list of selected ADRs.");
    	virtualNode.sendDirectMessage(message.sourceHandle, message);

    	return true;
    }

    /**
     * @param message
     * @return true if an ADR from the current cluster was selected and false otherwise.
     */
    boolean processStoreFragmentMessage(StoreFragmentMessage message) {
                      
    	logger.debug("Received fragment store request for fragment " + message.fragmentId + ".");

    	if (message.isCacheRequest == true) {
    		
    		String adrAddress = adrManager.getAdrFromIpAddress( message.fragmentSize, message.timeoutMinutes, message.brokerAddress );
    		if (adrAddress == null) adrAddress = "";
    		message.adrAddress = adrAddress ;
    		
    		message.isResponse = true;
    		virtualNode.sendDirectMessage(message.sourceHandle, message);
        	return true;        	
    	}

    	/**
    	 * Gets the address of a suitable ADR
    	 */
    	String adrAddress = adrManager.getAdrAddress(message.fragmentSize, message.fragmentId, message.timeoutMinutes, message.brokerAddress);

    	/**
    	 * Returns the address of the ADR to the source CDRM.
    	 */ 
    	if (adrAddress != null || message.nStorageTrials == 0) {    		
    		logger.debug("Adr selected for fragment storage at " + adrAddress + ".");
    		
    		message.setAdrAddress(adrAddress);
    		message.isResponse = true;
    		virtualNode.sendDirectMessage(message.sourceHandle, message);
    	}
    	
    	/**
    	 * If a suitable ADR is not found in the current cluster, forwards the request to another CDRM.
    	 */
    	else {
    		message.nStorageTrials--;
    		NodeHandle nextHandle = virtualNode.getNode().getLeafSet().get(1);

    		if (nextHandle != null) {
    			logger.debug("Adr for fragment storage could not be found! Redirecting storage request to node " + nextHandle.getId() + ".");
    			virtualNode.sendDirectMessage(nextHandle, message);
    		}
    		else {
    			logger.debug("Adr for fragment storage could not be found! Returning empty address.");
        		message.setAdrAddress("");
        		message.isResponse = true;
        		virtualNode.sendDirectMessage(message.sourceHandle, message);	
    		}
    	}

    	if (adrAddress != null) return true;
    	else return false;
    }

}
