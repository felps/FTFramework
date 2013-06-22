package br.usp.ime.oppstore.simulation;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import br.usp.ime.oppstore.simulation.broker.AccessBrokerSimulatorRemote;

public class OppStoreSimulator implements OppStoreSimulatorRemote {

    /**
     * Contains the list of accessBrokers to which the simulator can send storage and 
     * retrieval requests
     */
    private Vector<AccessBrokerSimulatorRemote> accessBrokerList;
    
    private InetSocketAddress bootstrapNodeAddress;
     
    private AtomicInteger nStorageResponses;
    private AtomicInteger nRetrievalResponses;
        
    private Logger logger;
    
    public OppStoreSimulator () {
            	
    	this.accessBrokerList = new Vector<AccessBrokerSimulatorRemote>();        
        this.logger = Logger.getLogger("oppstoreSim.OppStoreSimulator");
        
        try { 
            int bootstrapPort = 9001;        
            bootstrapNodeAddress = new InetSocketAddress(InetAddress.getLocalHost(), bootstrapPort);
        } catch (UnknownHostException e1) { e1.printStackTrace(); }
    }
    
    /** 
     * Sends storage request list to remote clients
     */
    public void simulateFileStorage(int nFiles, int nFragments, int nNeededFragments) {
                
        nStorageResponses = new AtomicInteger(0);        
        int baseFileSize = 1; // 100k
        
        logger.info("simulateFileStorage -> Starting file storage. nAccessBrokers=" + accessBrokerList.size() +
        		" nFiles=" + nFiles + " nFragments=" + nFragments + " nNeededFragments=" + nNeededFragments + " baseFileSize=" + baseFileSize);
        System.out.println("simulateFileStorage -> Starting file storage.");
        
        for (AccessBrokerSimulatorRemote accessBroker : accessBrokerList) {
            try {
                //accessBroker.addFiles();
                accessBroker.storeFiles(nFiles, nFragments, nNeededFragments, baseFileSize);
                
            } catch (RemoteException e) {
                logger.error("simulateFileStorage -> Remote Exception: could not reach AccessBrokerSimulator.");
                logger.error("Cause: " + e.getMessage());
            }            
        }
        
        while ( this.nStorageResponses.intValue() < accessBrokerList.size() ) {
            try { Thread.sleep(1000); } 
            catch (InterruptedException e) { }
        }

    }

    /** 
     * Sends data retrieval request list to remote clients
     */
    public void simulateFileRetrieval() {

        logger.info("simulateFileRetrieval -> Starting file retrieval.");
        System.out.println("simulateFileRetrieval -> Starting file retrieval.");
        
        nRetrievalResponses = new AtomicInteger(0);
        
        for (AccessBrokerSimulatorRemote accessBroker : accessBrokerList) {
            try {
                accessBroker.retrieveStoredFiles();
            } catch (RemoteException e) {
                logger.error("simulateDataRetrieval -> could not reach AccessBrokerSimulator.");
                logger.error("Cause: " + e.getMessage());
            }
        }
        
        while ( this.nRetrievalResponses.intValue() < accessBrokerList.size() ) {
            try { Thread.sleep(1000); } 
            catch (InterruptedException e) { }
        }

    }

    // ---------------------------------------------------------------------------------------------
    /**
     * Wait for all remote nodes to finish registering and starts the simulation. 
     */
    public void addAccessBrokerRemoteList(AccessBrokerSimulatorRemote[] accessBrokerRemoteList) {

        for ( AccessBrokerSimulatorRemote accessBroker : accessBrokerRemoteList )
            this.accessBrokerList.add(accessBroker);        
                
    }
    
    public void setStorageRequestCompleted(AccessBrokerSimulatorRemote accessBrokerRemote) {
        
        int nResponses = nStorageResponses.incrementAndGet();
        System.out.println("number of storage responses: " + nResponses);
        
        if (nResponses > accessBrokerList.size()) {
            System.out.println("PANIC: OpportuneStoreSimulator -> Too many storage responses!");
            System.exit(-1);
        }        
    }

    public void setRetrievalRequestCompleted(AccessBrokerSimulatorRemote accessBrokerRemote) {

        int nResponses = nRetrievalResponses.incrementAndGet();
        System.out.println("number of retrieval responses: " + nResponses);
        
        if (nResponses > accessBrokerList.size()) {
            System.out.println("PANIC: OpportuneStoreSimulator -> Too many retrieval responses!");
            System.exit(-1);
        }

    }

    public CdrmBootstrapInformation getBootstrapNodeAddress() {
        
        CdrmBootstrapInformation bootInfo = new CdrmBootstrapInformation();
        //bootInfo.nextCdrmNumber = nextCdrmNumber.getAndAdd(numberOfCdrms);
        bootInfo.bootstrapNodeAddress = bootstrapNodeAddress;
        
        return bootInfo;
    }
    
}
