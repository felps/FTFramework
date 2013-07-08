package br.usp.ime.oppstore.adrmanager;

import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;
import rice.pastry.NodeIdFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import br.usp.ime.oppstore.adrmanager.AdrInformationStructure.AdrLiveness;
import br.usp.ime.oppstore.cdrm.CdrmApp;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreCapacityProtocol;
import br.usp.ime.oppstore.corba.AdrManagerPOA;
import br.usp.ime.virtualId.VirtualNode;

/**
 * Manages the ADRs from a storage cluster
 * 
 * @author Raphael Y. de Camargo
 */
public class AdrManagerImpl extends AdrManagerPOA {

    /**
     * Maintains a map from a nodeId to its AdrInformationStructure
     * Also used to determine if node has joined the CDRM in the past 
     */
    private TreeMap <Id, AdrInformationStructure> orderedAdrInfoMap;
    
    private TreeMap <Integer, AdrInformationStructure> initialAdrMap;
        
    private AdrIdRangeManipulator adrIdRangeManipulator;
    
    private Logger logger;
    
    private VirtualNode virtualNode;
    
    private PastryIdFactory idFactory;
    
    protected NodeIdFactory nodeIdFactory;
    
    protected double adrCapacityChangeThreshold = 0.5;
    
    protected long updateThreadWaitingTime = 1 * 1000;
    
    // set by the update thread
    private double lastUpdatedCdrmCapacity = 0.0;
    private double cdrmCapacityThreshold = 0.5;
    CapacityUpdatedThread capacityUpdatedThread = null;
    
    AdrLivenessMonitor adrLivenessMonitor = null;
    
    StoredFragmentManager storedFragmentManager  = null;
    
    public AdrManagerImpl( VirtualNode virtualNode ) {
    	
        this.orderedAdrInfoMap  = new TreeMap <Id, AdrInformationStructure>();        
        this.initialAdrMap = new TreeMap <Integer, AdrInformationStructure>();

        this.virtualNode = virtualNode;
        this.nodeIdFactory = new RandomNodeIdFactory(virtualNode.getEndpoint().getEnvironment());
        this.idFactory = new PastryIdFactory(virtualNode.getEndpoint().getEnvironment());        
        
        this.capacityUpdatedThread = new CapacityUpdatedThread(virtualNode, this, 0, 0, 0);
        
        this.logger = Logger.getLogger("adrManager." + virtualNode.getNode().getId().toString().substring(0, 9) + ">");
        this.adrIdRangeManipulator = new AdrIdRangeManipulator();
        this.storedFragmentManager = new StoredFragmentManager(logger);
                        
        launchCapacityUpdateThread();
                        
        logger.info("AdrManager created successfully!");
    }   
            
    public synchronized double evaluateCdrmCapacity() {
        
        if (initialAdrMap.size() == 0)
        	return 0.000000001;
        
        double capacity=0;
        for (AdrInformationStructure adrInfo : initialAdrMap.values())
            capacity += adrInfo.currentCapacity;
        return capacity;
    }
        
    public Logger getLogger() { return logger; }
    
    public void setLastUpdatedCdrmCapacity(double lastUpdatedCdrmCapacity) {
		this.lastUpdatedCdrmCapacity = lastUpdatedCdrmCapacity;
	}

    public synchronized long getTotalAdrSpace() {
        int totalAdrSpace = 0;
        for (AdrInformationStructure adrInfo : initialAdrMap.values())
            totalAdrSpace += adrInfo.initialAvailableSpace;
        return totalAdrSpace;
    }

    /**
     * Gets the ADR address of an available ADR at a machine with IP 'requiredIpAddress'
     * 
     * @param fragmentId
     * @return An available ADR address
     */
    public synchronized String getAdrFromIpAddress(int fragmentSize, int timeoutMinutes, String requiredIpAddress) {
        
        if (orderedAdrInfoMap.size() == 0) return null;
                       
        AdrInformationStructure adrInfo = null;
        for (AdrInformationStructure tmpAdrInfo : orderedAdrInfoMap.values()) 
        	if (tmpAdrInfo.adrAddress.contains( requiredIpAddress ))
        		adrInfo = tmpAdrInfo;
        
        if (adrInfo == null) {
        	logger.debug( "getAdrFromIpAddress: Could not find adr for IP address " + requiredIpAddress + ".");
        	return null;
        }       	

        logger.debug( "getAdrFromIpAddress: " + adrInfo.adrAddress + " freeSpace " + adrInfo.freeUnreservedSpace + " fragmentSize " + fragmentSize + " requiredIpAddress: " + requiredIpAddress );
        if ( fragmentSize < adrInfo.freeUnreservedSpace ) {
            adrInfo.freeUnreservedSpace -= fragmentSize;
            //storedFragmentManager.setFragmentLocation(fragmentId, adrInfo, timeoutMinutes);
            return adrInfo.adrAddress;
        }       
        
        return null;
    }

    
    /**
     * Gets the ADR address of an available ADR
     * 
     * @param fragmentId
     * @param excludeIpAddress If not null, ADRs located in the machine with IP 'excludeIpAddress' are excluded 
     * @return An available ADR address
     */
    public synchronized String getAdrAddress(int fragmentSize, Id fragmentId, int timeoutMinutes, String excludeIpAddress) {
        
        if (orderedAdrInfoMap.size() == 0) return null;
               
        Id randomStorageId = nodeIdFactory.generateNodeId();
        SortedMap<Id, AdrInformationStructure> tailMap = orderedAdrInfoMap.tailMap(randomStorageId);
        AdrInformationStructure adrInfo = tailMap.get( tailMap.firstKey() );
        
        logger.debug( "getAdrAddress: " + adrInfo.adrAddress + " freeSpace " + adrInfo.freeUnreservedSpace + " fragmentSize " + fragmentSize + " excludeIpAddress: " + excludeIpAddress);
        if ( fragmentSize < adrInfo.freeUnreservedSpace && (excludeIpAddress == null || adrInfo.adrAddress.contains( excludeIpAddress ) == false)) {
            adrInfo.freeUnreservedSpace -= fragmentSize;
            //storedFragmentManager.setFragmentLocation(fragmentId, adrInfo, timeoutMinutes);
            return adrInfo.adrAddress;
        }       
        
        /**
         * In case the first selected ADR does not contains enough free storage space
         */
        HashSet<AdrInformationStructure> removedAdrs = new HashSet<AdrInformationStructure>();
                
        while ( initialAdrMap.size() > 1 ) {
        	adrInfo = initialAdrMap.remove( adrInfo.adrId );
        	removedAdrs.add( adrInfo );
        	redistributeIdRange();
        	
        	tailMap = orderedAdrInfoMap.tailMap(randomStorageId);
        	adrInfo = tailMap.get( tailMap.firstKey() );
        	
        	if ( fragmentSize < adrInfo.freeUnreservedSpace && (excludeIpAddress == null || adrInfo.adrAddress.contains( excludeIpAddress ) == false)) {        		
                for (AdrInformationStructure tempAdr : removedAdrs) 
                	initialAdrMap.put(tempAdr.adrId, tempAdr);        	
                adrInfo.freeUnreservedSpace -= fragmentSize;
                //storedFragmentManager.setFragmentLocation(fragmentId, adrInfo, timeoutMinutes);
                redistributeIdRange();
                return adrInfo.adrAddress;        		
        	}
        }
        
        for (AdrInformationStructure tempAdr : removedAdrs) 
        	initialAdrMap.put(tempAdr.adrId, tempAdr);
        redistributeIdRange();

        return null;
    }

    //---------------------------------------------------------------------------------
    
    public double getLastUpdatedCdrmCapacity() {
		return lastUpdatedCdrmCapacity;
	}
    
    //---------------------------------------------------------------------------------
    
    public synchronized void printIdList() {

        for (Id adrId : orderedAdrInfoMap.keySet())
            System.out.print(adrId + " ");
        System.out.println();
    }

    //---------------------------------------------------------------------------------
    public synchronized int registerAdr( String address, int freeStorageSpaceInt, double meanUptime, double meanIdleness ) {
        
    	long freeStorageSpace = 1024L * freeStorageSpaceInt;
    	
        int adrId = address.hashCode();
        
        AdrInformationStructure adrInfo = initialAdrMap.get(adrId); 
        if (adrInfo == null) {
            adrInfo = new AdrInformationStructure();
            adrInfo.virtualId = null;
            adrInfo.adrId = adrId;
            adrInfo.initialAvailableSpace = freeStorageSpace;
            
            initialAdrMap.put(adrInfo.adrId, adrInfo);
            
            //System.out.println("Registering new ADR at " + address + " with adrId " + adrId + ".");
            logger.info("Registering new ADR at " + address + " with adrId " + adrId + " and free space " + freeStorageSpaceInt + ".");
        	logger.info("Registering new ADR at " + address + " with adrId " + adrId + " and free space " + freeStorageSpace + ".");
        }        
        else {
        	logger.info("ADR at " + address + " with adrId " + adrId + " already registered! Updating its parameters.");
        }
        
        adrInfo.adrAddress          = address;
        adrInfo.freeStorageSpace    = freeStorageSpace;
        adrInfo.freeUnreservedSpace = freeStorageSpace;        
        adrInfo.meanUptime          = meanUptime;
        adrInfo.meanIdleness        = meanIdleness;
        adrInfo.lastUpdatedCapacity = 0.0;
        adrInfo.currentCapacity     = 0.0;
        adrInfo.lastUpdateTime      = System.currentTimeMillis();
        adrInfo.adrLiveness         = AdrLiveness.ALIVE;

        /**
         * Updates the capacity of ADRs.
         */
        updateAdrCapacity( adrInfo );

    	if (adrLivenessMonitor == null) {
        	adrLivenessMonitor = new AdrLivenessMonitor( this, initialAdrMap.values() );
        	adrLivenessMonitor.start();
    	}

        return adrId;
    }
    
    //---------------------------------------------------------------------------------

    synchronized private void updateAdrCapacity(AdrInformationStructure adrInfo) {

    	adrInfo.currentCapacity = evaluateAdrCapacity(adrInfo.freeStorageSpace, adrInfo.meanIdleness);
    	double changeRatio = Math.abs(adrInfo.currentCapacity - adrInfo.lastUpdatedCapacity) / adrInfo.lastUpdatedCapacity;
    	if ( changeRatio > adrCapacityChangeThreshold ) {                
            
            redistributeIdRange();            
            logger.debug( "Redistributing Id ranges." );
    	}
        adrInfo.lastUpdatedCapacity = adrInfo.currentCapacity;
        
        checkCdrmCapacityChange();
                
    }

    synchronized private void checkCdrmCapacityChange() {

		double currentCdrmCapacity = evaluateCdrmCapacity();
        double changeRatio = Math.abs(currentCdrmCapacity - lastUpdatedCdrmCapacity) / lastUpdatedCdrmCapacity;
        if ( changeRatio > cdrmCapacityThreshold && ( capacityUpdatedThread == null || capacityUpdatedThread.isAlive() == false ) ) {
        
        	launchCapacityUpdateThread();
        	logger.info("CDRM capacity changed from " + lastUpdatedCdrmCapacity + " to " + currentCdrmCapacity + ". Starting the update protocol.");        	
        }
	}

	private void launchCapacityUpdateThread() {
		capacityUpdatedThread = new CapacityUpdatedThread(virtualNode, this, updateThreadWaitingTime, lastUpdatedCdrmCapacity*0.5, lastUpdatedCdrmCapacity*1.5 );
		capacityUpdatedThread.start();
	}

    //  ---------------------------------------------------------------------------------
    
	private void redistributeIdRange() {
		orderedAdrInfoMap.clear();
		adrIdRangeManipulator.setAdrVirtualIds( initialAdrMap.values() );                
		for (AdrInformationStructure tempAdrInfo : initialAdrMap.values())
			orderedAdrInfoMap.put(tempAdrInfo.virtualId, tempAdrInfo);
	}

    //  ---------------------------------------------------------------------------------
    
    public double evaluateAdrCapacity (long freeStorageSpace, double meanIdletime) {
    	
    	double capacity = 0;
    	
    	double storageSpaceMultiplier = 1.0;
    	if ( freeStorageSpace < 1000000 )
    		storageSpaceMultiplier *= (freeStorageSpace / 1000000.0); 

        if (CdrmApp.idProtocol == CdrmApp.OpStoreIdProtocol.PASTRY)
        	capacity = 1.0; 
        else if (CdrmApp.capacityProtocol == OpStoreCapacityProtocol.LINEAR)
    		capacity = storageSpaceMultiplier * meanIdletime;            
    	else if (CdrmApp.capacityProtocol == OpStoreCapacityProtocol.QUADRATIC)
    		capacity = storageSpaceMultiplier * meanIdletime * meanIdletime;            
    	else if (CdrmApp.capacityProtocol == OpStoreCapacityProtocol.HIPERBOLIC)
    		capacity = storageSpaceMultiplier * ( Math.tanh( 3 * ( 2 * meanIdletime - 1 ) ) + 1 ) / 2;
    	else if (CdrmApp.capacityProtocol == OpStoreCapacityProtocol.SQUARED)
    		capacity = Math.sqrt(storageSpaceMultiplier * meanIdletime);

    	//System.out.println( "capacity=" + capacity + " storage=" + freeStorageSpace + " meanIdleTime=" + meanIdletime ); 
    	
    	return capacity;
    }

    
    //---------------------------------------------------------------------------------
    
    public synchronized void adrStatusChanged
    (int adrId, int freeStorageSpaceChangeInt, double meanUptimeChange, double meanIdlenessChange) {
    
    	long freeStorageSpaceChange = freeStorageSpaceChangeInt * 1024;
    	
        AdrInformationStructure adrInfo = initialAdrMap.get(adrId);
        adrInfo.freeStorageSpace    += freeStorageSpaceChange;
        adrInfo.freeUnreservedSpace += freeStorageSpaceChange;
        adrInfo.meanUptime          += meanUptimeChange;
        adrInfo.meanIdleness        += meanIdlenessChange;        
        
        updateAdrCapacity( adrInfo );
    }    

    //---------------------------------------------------------------------------------
    
    public synchronized void setFragmentStored (int adrId, byte[] fragmentKey, int fragmentSize, int timeoutMinutes) {

    	logger.debug("setFragmentStored for Adr " + adrId + ".");
    	
        AdrInformationStructure adrInfo = initialAdrMap.get(adrId);
        if (adrInfo != null) {
            adrInfo.freeStorageSpace -= fragmentSize;
            updateAdrCapacity( adrInfo );
            
            Id fragmentId = idFactory.buildId(fragmentKey);
            storedFragmentManager.setFragmentLocation(fragmentId, adrInfo, timeoutMinutes);
            return;
        }
        
        logger.warn("Could not find Adr " + adrId + ".");        
    }

	public void setFragmentLeaseRenewed(int adrId, byte[] fragmentKey, int timeoutMinutes) {

    	logger.debug("setFragmentLeaseRenewed for Adr " + adrId + ".");
    	
        AdrInformationStructure adrInfo = initialAdrMap.get(adrId);
        if (adrInfo != null) {            
            Id fragmentId = idFactory.buildId(fragmentKey);
            storedFragmentManager.setFragmentLeaseRenewed(fragmentId, timeoutMinutes);            
            return;
        }
        
        logger.warn("Could not find Adr " + adrId + ".");
	}

	public void setFragmentRemoved(int adrId, byte[] fragmentKey, int fragmentSize) {

    	logger.debug("setFragmentRemoved for Adr " + adrId + ".");
    	
        AdrInformationStructure adrInfo = initialAdrMap.get(adrId);
        if (adrInfo != null) {
            adrInfo.freeStorageSpace += fragmentSize;
            updateAdrCapacity( adrInfo );
            
            Id fragmentId = idFactory.buildId(fragmentKey);
            AdrInformationStructure sourceAdr = storedFragmentManager.setFragmentRemoved(fragmentId);
            
            if (sourceAdr.adrId != adrId)
            	logger.warn("Fragment location was wrong for ADR " + adrId + ".");
            
            return;
        }
        
        logger.warn("Could not find Adr " + adrId + ".");        		
	}

	public int adrKeepAlive(int adrId) {

		AdrInformationStructure adrInfo = initialAdrMap.get(adrId);
		if (adrInfo != null) {
			adrInfo.lastUpdateTime = System.currentTimeMillis();	
			setAdrLiveness(adrInfo, AdrLiveness.ALIVE);
			
			if (adrInfo.fragmentRemovalList.size() == 0)
				return 0;
			else
				return 1;
		}
		else {
			logger.warn("Received keepAlive message from adrId " + adrId + " which is not in the adrIdMap.");
			return -1;
		}
	}

	public byte[][] getFragmentRemovalList(int adrId) {

		AdrInformationStructure adrInfo = initialAdrMap.get( adrId );
		if (adrInfo == null)
			return new byte[0][0];
		else if (adrInfo.fragmentRemovalList.size() == 0)
			return new byte[0][0];
		else {
			int nFragments = adrInfo.fragmentRemovalList.size();
			byte[][] removalList = new byte[ nFragments ][];
			for (int i=0; i < nFragments; i++) {
				removalList[i] = adrInfo.fragmentRemovalList.get(0).toByteArray();
				adrInfo.fragmentRemovalList.remove(0);
			}
			
			return removalList;
		}
	}    

    //---------------------------------------------------------------------------------
	
	synchronized public void setAdrLiveness(AdrInformationStructure adrInfo, AdrLiveness adrLiveness) {

		if (adrLiveness == AdrLiveness.ALIVE) {

			if (adrInfo.adrLiveness != AdrLiveness.ALIVE) {
				logger.debug( "Setting ADR " + adrInfo.adrId + " liveness as ALIVE." );
				adrInfo.adrLiveness = AdrLiveness.ALIVE;
				redistributeIdRange();
			}
		}

		else if (adrLiveness == AdrLiveness.UNRESPONSIVE) {
			
			if (adrInfo.adrLiveness != AdrLiveness.UNRESPONSIVE) {
				logger.debug( "Setting ADR " + adrInfo.adrId + " liveness as UNRESPONSIVE." );
				adrInfo.adrLiveness = AdrLiveness.UNRESPONSIVE;
				redistributeIdRange();
			}			
		}

		else if (adrLiveness == AdrLiveness.DEPARTED) {
			
			logger.debug( "Removing ADR " + adrInfo.adrId + " from AdrManager." );
			initialAdrMap.remove(adrInfo.adrId);
			orderedAdrInfoMap.remove( adrInfo.virtualId );
			redistributeIdRange();
			checkCdrmCapacityChange();
		}
		
	}
	
    //---------------------------------------------------------------------------------
}
