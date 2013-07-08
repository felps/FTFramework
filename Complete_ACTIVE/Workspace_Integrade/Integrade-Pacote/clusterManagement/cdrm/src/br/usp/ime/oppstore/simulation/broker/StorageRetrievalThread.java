package br.usp.ime.oppstore.simulation.broker;

import java.util.Collection;
import java.util.Vector;

import br.usp.ime.oppstore.simulation.broker.BrokerRequestManager.BrokerFileInformation;

public class StorageRetrievalThread extends Thread {
 
    enum ExperimentType {storage, retrieval};
    
    private ExperimentType type;
    private AccessBrokerSimulator simulator;
    private byte[][] keyListToStore;
    
    static int maxSimultaneousStorageRequests = 2; 
    static int maxSimultaneousRetrievalRequests = 2;
    
    public StorageRetrievalThread(ExperimentType type, AccessBrokerSimulator simulator, byte[][] keyListToStore) {
        
        this.type = type;
        this.simulator = simulator;
        this.keyListToStore = keyListToStore;
    }
    
    public void run() {
        
        long lastPrintTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        
        int storageNumber = 1;
        if (type == ExperimentType.storage) {
            long maxTime = 0;
            simulator.getLogger().info("Starting file storage experiment.");
            
            for (byte[] fileKey : this.keyListToStore) {                
            	
            	BrokerFileInformation fileInfo = simulator.brokerRequestManager.getBrokerFileInformation(fileKey);
                long beginTime = System.currentTimeMillis();                
                
                int requestNumber = simulator.cdrmImpl.requestFileStorage(
                        fileInfo.fileKey, fileInfo.fragmentKeyList, fileInfo.fileSize, fileInfo.fragmentSizeList, fileInfo.neededFragments, simulator.getAccessBrokerIor(), 60, true );
                
                simulator.brokerRequestManager.setRequestNumber(requestNumber, fileInfo.fileKey, true);
                simulator.nActiveStorageRequests.incrementAndGet();
                
                storageNumber++;                
                
                while (simulator.nActiveStorageRequests.get() >= maxSimultaneousStorageRequests) {
                    try { Thread.sleep(100); } 
                    catch (InterruptedException e) { e.printStackTrace(); }
                }                                
                
                long endTime = System.currentTimeMillis();
                if (endTime - beginTime > maxTime)
                    maxTime = endTime - beginTime; 
                
                if (simulator.brokerNumber == 0) {
                	simulator.getLogger().debug("Storage number " + (storageNumber-1) + " completed in " +  maxTime + " ms.");
                	if (endTime - lastPrintTime > 10 * 1000) {
                		System.out.println("Storage number " + (storageNumber-1) + " of " + this.keyListToStore.length + ".");
                		lastPrintTime = endTime;
                	}                		
                }
                
                maxTime = 0;
            }
            
            System.out.println("Finished file storage of " + (storageNumber-1) + " files.");
            simulator.getLogger().info("Finished file storage of " + (storageNumber-1) + " files.");
        }

        else if (type == ExperimentType.retrieval) {
            
            simulator.nFragmentsTotalVector  = new Vector< Integer >();
            simulator.nFragmentsUptimeVector = new Vector< int[] >(); 
            simulator.nFragmentsIdleVector   = new Vector< int[] >();    

            simulator.getLogger().info("Starting file retrieval experiment.");

            Collection<BrokerFileInformation> fileInformationCollection = 
                simulator.brokerRequestManager.getFileInformationCollection();

            for (BrokerFileInformation fileInfo : fileInformationCollection) {
            	            	
            	int requestNumber = simulator.cdrmImpl.requestFileRetrieval( fileInfo.fileKey, simulator.getAccessBrokerIor() );
            	simulator.getLogger().debug("Requested file retrieval for requestNumber " + requestNumber + ".");
            	
            	simulator.brokerRequestManager.setRequestNumber(requestNumber, fileInfo.fileKey, false);
                simulator.nActiveRetrievalRequests.incrementAndGet();

                //if (simulator.brokerNumber == 0)
                	//simulator.getLogger().debug("Finished file retrieval for requestNumber " + requestNumber + ".");

                storageNumber++;

                if (simulator.brokerNumber == 0) {
                	long endTime = System.currentTimeMillis();                	
                	if (endTime - lastPrintTime > 10 * 1000) {
                		simulator.getLogger().debug("Retrieval number " + (storageNumber-1) + " completed in " +  (endTime-startTime) + " ms.");
                		System.out.println("Retrieval number " + (storageNumber-1) + " of " + fileInformationCollection.size() + ".");
                		lastPrintTime = endTime;
                	}                		
                }

                while ( simulator.nActiveRetrievalRequests.get() >= maxSimultaneousRetrievalRequests ) {
                    try { Thread.sleep(100); }              
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
            
            System.out.println("Finished file retrieval of " + (storageNumber-1) + " files.");
            simulator.getLogger().info("Finished file retrieval of " + (storageNumber-1) + " files.");
        }

        try { Thread.sleep(2000); } 
        catch (InterruptedException e) { e.printStackTrace(); }                                     
    }
}
