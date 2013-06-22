package br.usp.ime.oppstore.adrmanager;

import java.util.HashMap;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;

/**
 *  Perform management of data stored in the CDRM.
 *  It contains the locations of fragments contents and manages the liveness of fragments.
 *  Is initialized by the AdrManager
 */
public class StoredFragmentManager {
    
    /**
     * Maintains the addresses of the ADR where fragments are stored 
     */
    HashMap <Id, AdrInformationStructure> fragmentLocationMap;
    
    FragmentLivenessMonitor livenessMonitor;
    
    Logger logger;
        
    public StoredFragmentManager (Logger logger) {
    	this.logger = logger;
        fragmentLocationMap = new HashMap <Id, AdrInformationStructure>();
        livenessMonitor = new FragmentLivenessMonitor( this );
        livenessMonitor.start();
    }
    
    public void setFragmentLocation (Id fragmentId, AdrInformationStructure adrInfo, int timeoutMinutes) {        
        fragmentLocationMap.put(fragmentId, adrInfo);
        livenessMonitor.addLivenessTimeout(timeoutMinutes, fragmentId);
    }
    
    public void setFragmentLeaseRenewed (Id fragmentId, int timeoutMinutes) {
    	
        livenessMonitor.renewLivenessTimeout(timeoutMinutes, fragmentId);        
    }
    
    public AdrInformationStructure setFragmentRemoved (Id fragmentId) {
    	
    	livenessMonitor.removeLivenessTimeout(fragmentId);
        return fragmentLocationMap.remove(fragmentId);        
    }
    
    public AdrInformationStructure getAdrInfo(Id fragmentId) {
        return fragmentLocationMap.get(fragmentId);
    }    
        
}
