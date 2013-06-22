package br.usp.ime.oppstore.simulation.adr;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import br.usp.ime.oppstore.AdrAddress;
import br.usp.ime.oppstore.adrmanager.AdrManagerImpl;
import br.usp.ime.oppstore.simulation.ClusterAdrSimulationMap;
import br.usp.ime.oppstore.statistics.ClusterAdrsInformation;

public class ClusterAdrSimulator implements ClusterAdrs {
    
    MachineAdrStateController adrStateController;
    
    public static enum AdrState {IDLE, OCCUPIED, UNAVAILABLE, DESTROY}

    /**
     * Maps AdrNumbers to Adr objects
     */
	protected HashMap <Integer, Adr> adrMap;
    
    protected AdrManagerImpl adrManager;
    
    protected int nextAdrNumber = 0;
    
    long clusterNumber = 0;
    
    public ClusterAdrSimulator (AdrManagerImpl adrManager, long clusterNumber) {
             
        this.adrMap = new HashMap <Integer, Adr>();
        this.adrManager = adrManager;
        this.clusterNumber = clusterNumber;
        this.adrStateController = new MachineAdrStateController();        
    
    }
    
    /**
     * Creates numberOfAdrs ADRs and registers them in the AdrManager 
     * 
     * @param numberOfAdrs The number of ADRs to create 
     */
    public void createAdrs (int numberOfAdrs) {

        ClusterAdrs clusterAdrsStub = null;
        try { clusterAdrsStub = (ClusterAdrs) UnicastRemoteObject.exportObject(this, 0); } 
        catch (RemoteException e) { e.printStackTrace(); }

        ClusterAdrSimulationMap.addClusterAdr( String.valueOf(clusterNumber), this);
        
        for (int adrCount=0; adrCount < numberOfAdrs; adrCount++ ) {
            int adrNumber = nextAdrNumber++;        

            AdrAddress adrAddress = new AdrAddress();
            adrAddress.address = String.valueOf(clusterNumber) + ":" + String.valueOf(adrNumber); 
            adrAddress.clusterAdrStub = clusterAdrsStub;
            
            Adr adr = new Adr(adrAddress);        
            adrMap.put(adrNumber, adr);
        }
    }
    
    /**
     * Called after setting the ADR parameters.     
     */
    public void registerAdrs () {
        
        for (Adr adr : adrMap.values() )  {
            int adrId = adrManager.registerAdr(adr.adrAddress.address, adr.freeStorageSpace, adr.meanUptime, adr.meanIdleness);
            adr.adrLongId = adrId;
        }
    }
    
    public void setAdrParameters(int adrNumber, int freeStorageSpace, int nExperiments, int timeOffSet, 
            double meanDayUptime, double meanNightUptime, double meanDayIdleness, double meanNightIdleness) {
        
        Adr adr = adrMap.get(adrNumber);
        adr.setAdrParameters(freeStorageSpace, nExperiments, timeOffSet, 
            meanDayUptime, meanNightUptime, meanDayIdleness, meanNightIdleness);
    }
    
    public void updateAdrParameter (int adrNumber, int freeStorageSpace, double meanUptime, double meanIdleness) {
    	Adr adr = adrMap.get (adrNumber);    	
    	adrManager.adrStatusChanged(adr.adrLongId, freeStorageSpace - adr.freeStorageSpace, meanUptime - adr.meanUptime, meanIdleness - adr.meanIdleness);
    }

    public ClusterAdrsInformation getStatisticalInformation () {
        
        int numberOfAdrs = adrMap.size(); 
        ClusterAdrsInformation clusterAdrInfo = new ClusterAdrsInformation();
        clusterAdrInfo.capacity = new double[numberOfAdrs];
        clusterAdrInfo.meanIdletime = new double[numberOfAdrs];
        clusterAdrInfo.storedFragments = new int[numberOfAdrs];
        clusterAdrInfo.freeSpace = new long[numberOfAdrs];
        clusterAdrInfo.totalSpace = new long[numberOfAdrs];

        int adrNumber = 0;
        for (Adr adr : adrMap.values()) {

            clusterAdrInfo.storedFragments[adrNumber]= adr.getNumberOfStoredFragments();
            clusterAdrInfo.freeSpace[adrNumber]   = adr.freeStorageSpace;
            clusterAdrInfo.totalSpace[adrNumber]  = adr.totalStorageSpace;
            clusterAdrInfo.meanIdletime[adrNumber]= adr.meanIdleness;
            clusterAdrInfo.capacity[adrNumber]    = adrManager.evaluateAdrCapacity(adr.totalStorageSpace, adr.meanIdleness);
            adrNumber++;
        }
        
        return clusterAdrInfo;
    }
    
    /**
     * From ClusterAdrs interface
     */
    public void storeFragment(int adrNumber, byte[] fragmentKey, byte[] data, int dataSize) 
    throws AdrUnavailableException {
                
        Adr adr = adrMap.get(adrNumber);
        long freeSpace = adr.storeData(fragmentKey, data, dataSize);

        if (freeSpace >=0) { 
            adrManager.setFragmentStored(adr.adrLongId, fragmentKey, dataSize, 1);
        }
        else 
            System.out.println("ClusterAdrSimulator::storeFragment -> ERROR: No available free space.");        
    }

    /**
     * From ClusterAdrs interface
     */
    public byte[] getFragment(int adrNumber, byte[] fragmentKey) 
    throws AdrUnavailableException, FragmentNotStoredException {
        return adrMap.get(adrNumber).getData(fragmentKey);
    }

    /**
     * Changes the state of an ADR from this cluster
     */
    public void updateAdrStates() {
        for (Adr adr : adrMap.values())
            adr.updateAdrStateList();
    }

    public MachineAdrStateController getAdrStateController() {
        return this.adrStateController;
    }
    
	public AdrState[] getStatus(int adrNumber) {
		return adrMap.get(adrNumber).getAdrStateList();
	}
	
	public int numberOfAdrs() {
		return adrMap.size();
	}
	
	public Collection< Entry<Integer, Adr> > getAdrList () {
		return adrMap.entrySet();
	}
    
    public Adr getAdr(int adrNumber) {
        return adrMap.get(adrNumber);        
    }
    
    public void printStorageStatistics () {
        
        //adrManager.printIdList();
        long minFreeStorageSpace=Long.MAX_VALUE;
        long totalStorageSpace=0;
        
        for (Adr adr : adrMap.values()) {
        	if (adr.freeStorageSpace < minFreeStorageSpace) {
        		minFreeStorageSpace = adr.freeStorageSpace;
        		totalStorageSpace = adr.totalStorageSpace;
        	}
            //System.out.print(adr.getNumberOfStoredFragments() + "|" + adr.freeStorageSpace + "of" + adr.totalStorageSpace  + " " );
        }
        System.out.print(minFreeStorageSpace + " of " + totalStorageSpace  + " | ");
    }
}
