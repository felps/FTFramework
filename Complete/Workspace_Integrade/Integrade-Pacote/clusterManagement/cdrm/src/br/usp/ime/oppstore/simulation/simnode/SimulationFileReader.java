package br.usp.ime.oppstore.simulation.simnode;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.Map.Entry;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import rice.environment.Environment;
import rice.pastry.Id;
import rice.pastry.NodeIdFactory;
import br.usp.ime.oppstore.cdrm.CdrmApp;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreIdProtocol;
import br.usp.ime.oppstore.simulation.CdrmBootstrapInformation;
import br.usp.ime.oppstore.simulation.OppStoreSimulatorRemote;
import br.usp.ime.oppstore.simulation.adr.Adr;
import br.usp.ime.oppstore.simulation.adr.ClusterAdrSimulator;
import br.usp.ime.virtualId.VirtualSpace;

// Is likely to replace most of MachineCdrmAndBrokerLauncher
public class SimulationFileReader {

    private PrintStream cdrmStartLog;
    
    enum InputPosition {adr, cdrm, simulation, beginnning};
    
    MachineCdrmAdrManager cdrmAdrManager;
    MachineAccessBrokerManager accessBrokerManager;    
    OppStoreSimulatorRemote oppStoreSimulator;
    
    CdrmBootstrapInformation bootInfo;
    NodeIdFactory nodeIdFactory;
    Environment env;    
    
    ORB orb;
    POA poa;
    
    //int numberOfBrokers     = -1;
    //int numberOfAdrs        = -1;
    int numberOfCdrms       = -1;
    //int numberOfExperiments = -1;
        
    public SimulationFileReader(
            Environment env, NodeIdFactory nodeIdFactory, OppStoreSimulatorRemote opStoreSimulator, ORB orb, POA poa) {
        
    	this.orb = orb;
    	this.poa = poa;
        this.env = env;
        this.nodeIdFactory = nodeIdFactory;
        this.oppStoreSimulator = opStoreSimulator;
        
        try {
            this.cdrmStartLog = 
                new PrintStream( new BufferedOutputStream( new FileOutputStream("cdrmStartup.log") ));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
        
    void createCdrmAdrFromFile (File inputFile, int localBindPort, CdrmBootstrapInformation bootInfo) {
        
        this.cdrmAdrManager = new MachineCdrmAdrManager(env, localBindPort, nodeIdFactory, orb);
        this.accessBrokerManager = new MachineAccessBrokerManager(env, oppStoreSimulator, cdrmAdrManager, nodeIdFactory, orb, poa);
        this.bootInfo = bootInfo;        
        int cdrmNumber = 0;
        
        /**
         * Parse all file contents
         */
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            String line = null;
            while ( (line = bufferedReader.readLine()) != null ) {

            	if (line.length() == 0 || line.charAt(0) == ' ' || line.charAt(0) == '#')
                    continue; 

                else if (line.startsWith("@SIMULATION")) {
                    processSimulationInfo(line, localBindPort, bootInfo);
                }

                else if (line.startsWith("@CDRM")) {

                	processCdrmInfo(line, cdrmNumber);                	                	

                	/**
                	 * Tests Virtual Id protocol and writes statistics to file 
                	 */
                	if (cdrmNumber > 8 && CdrmApp.idProtocol == OpStoreIdProtocol.ADAPTIVE && cdrmNumber%1 == 0)
                		cdrmAdrManager.testVirtualIdRing();
                	if (cdrmNumber > 3 && CdrmApp.idProtocol == OpStoreIdProtocol.ADAPTIVE)
                		cdrmAdrManager.printVirtualIdStatistics();                    
                	                	
                    cdrmNumber++;
                }
                 
                else if (line.startsWith("@KILL_CDRM")) {
                	
                	processKillCdrmInfo(line);                    
                }
                
                else if (line.startsWith("@UPDATE_CDRM_CAPACITY")) {
                    
                	processUpdateCdrmInfo(line);                    
                }
            	
                else if (line.startsWith("@STORE_FILES")) {
                	processFileStorageInfo(line);
                }
            	
                else if (line.startsWith("@RECOVER_FILES")) {
                    try { oppStoreSimulator.simulateFileRetrieval(); } 
                    catch (RemoteException e) { e.printStackTrace(); }
                }
                
            }            
        } catch (IOException e) {
            e.printStackTrace();
        }                      	                             
		      
        cdrmAdrManager.checkNumberOfReplicas();
    }

    /**
     * Waits until the previous CDRM is ready.
     */
	private void waitUntilCdrmIsReady(int cdrmNumber) {
		
		if (CdrmApp.idProtocol == CdrmApp.OpStoreIdProtocol.ADAPTIVE && cdrmAdrManager.getNumberOfAdrsInCdrm(cdrmNumber) > 0) {
			VirtualSpace virtualSpace = cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getVirtualSpace(1);
			while ( virtualSpace == null || virtualSpace.getVirtualLeafSet().isUpdated() == false ) {
				try { Thread.sleep(100); }
				catch (InterruptedException e) {}
				virtualSpace = cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getVirtualSpace(1);
			}
            try { Thread.sleep( (cdrmNumber/50 + 1) * 2000 ); }
            catch (InterruptedException e) {}            
		}
		else if (CdrmApp.idProtocol == CdrmApp.OpStoreIdProtocol.PASTRY) {
			while ( cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getNode().isReady() == false ) {
				try { Thread.sleep(100); }
				catch (InterruptedException e) {}
			}                        	
		}
	}

	private void processUpdateCdrmInfo(String line) {
		
        String[] cdrmInfo = line.split("[= ]");
        assert(cdrmInfo.length == 3);

        assert (cdrmInfo[1].compareTo("cdrmNumber") == 0);
        int cdrmNumber = Integer.parseInt(cdrmInfo[2]);
            	    	
    	System.out.println("Updating ADR values at node " + cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getNode().getId() + ".");
    	
    	ClusterAdrSimulator clusterAdrSimulator = cdrmAdrManager.getClusterAdrSimulator(cdrmNumber); 
    	for (Entry< Integer, Adr> entry: clusterAdrSimulator.getAdrList() ) {
    		Adr adr = entry.getValue();
    		clusterAdrSimulator.updateAdrParameter(entry.getKey(), (int)adr.getFreeStorageSpace(), adr.getMeanUptime() * 0.5, adr.getMeanIdleness() * 0.5);
    	}
    	try { Thread.sleep( 5 * 1000 ); }
    	catch (InterruptedException e) {}

	}

	private void processKillCdrmInfo(String line) {
		
        String[] cdrmInfo = line.split("[= ]");
        assert(cdrmInfo.length == 3);

        assert (cdrmInfo[1].compareTo("cdrmNumber") == 0);
        int cdrmNumber = Integer.parseInt(cdrmInfo[2]);
        
    	cdrmAdrManager.destroyLocalCdrm(cdrmNumber);                        
    	try { Thread.sleep(60 * 1000); }
    	catch (InterruptedException e) {}
	}
	
	private void processFileStorageInfo(String line) {
		
        String[] cdrmInfo = line.split("[= ]");
        assert(cdrmInfo.length == 7);
        int pos=1;
       
        assert (cdrmInfo[pos++].compareTo("numberOfFiles") == 0);        
        int nFiles = Integer.parseInt(cdrmInfo[pos++]);
        assert (cdrmInfo[pos++].compareTo("numberOfFragments") == 0);        
        int nFragments= Integer.parseInt(cdrmInfo[pos++]);        
        assert (cdrmInfo[pos++].compareTo("numberOfNeeded") == 0);                   
        int nNeededFragments = Integer.parseInt(cdrmInfo[pos++]);

        try { oppStoreSimulator.simulateFileStorage(nFiles, nFragments, nNeededFragments); } 
        catch (RemoteException e) { e.printStackTrace(); }
	}
	
    private void processSimulationInfo(String line, int localBindPort, CdrmBootstrapInformation bootInfo) {

        String[] simulationStringInfo = line.split("[= ]");
        assert(simulationStringInfo.length == 3);

        assert (simulationStringInfo[1].compareTo("numberOfCdrms") == 0);
        numberOfCdrms = Integer.parseInt(simulationStringInfo[2]);
    }

    /**
     * Creates a CDRM and the ClusterAdrSimulator from the information contained in the file.
     * 
     * @CDRM
     * numberOfAdrs=10 freeSpace=3523 timeOffset=0 meanDayUptime=0.1 meanNightUptime=0.1 meanDayIdleness=0.1 meanNightIdleness=0.1
     * 
     * @param line
     * @param cdrmNumber
     */
    private void processCdrmInfo(String line, int cdrmNumber) {

    	/**
    	 * Reads CDRM info from file
    	 */
        String[] cdrmInfo = line.split("[= ]");
        assert(cdrmInfo.length == 17);
        int pos=1;
       
        assert (cdrmInfo[pos++].compareTo("numberOfAdrs") == 0);        
        int numberOfAdrs = Integer.parseInt(cdrmInfo[pos++]);
        assert (cdrmInfo[pos++].compareTo("hasBroker") == 0);        
        int hasBroker = Integer.parseInt(cdrmInfo[pos++]);
        
        assert (cdrmInfo[pos++].compareTo("freeSpace") == 0);                   
        int freeSpace = Integer.parseInt(cdrmInfo[pos++]);
        assert (cdrmInfo[pos++].compareTo("timeOffset") == 0);                   
        int timeOffset = Integer.parseInt(cdrmInfo[pos++]);            
        
        assert (cdrmInfo[pos++].compareTo("meanDayUptime") == 0);        
        double meanDayUptime = Double.parseDouble(cdrmInfo[pos++]);
        assert (cdrmInfo[pos++].compareTo("meanNightUptime") == 0);        
        double meanNightUptime = Double.parseDouble(cdrmInfo[pos++]);
        
        assert (cdrmInfo[pos++].compareTo("meanDayIdleness") == 0);        
        double meanDayIdleness = Double.parseDouble(cdrmInfo[pos++]);
        assert (cdrmInfo[pos++].compareTo("meanNightIdleness") == 0);        
        double meanNightIdleness = Double.parseDouble(cdrmInfo[pos++]);       
        
        /**
         * Creates the cluster CDRMs, ADRs, and access brokers
         */
        Id nodeId = null; // Id.build (new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,-127});
        cdrmAdrManager.createLocalCdrm(cdrmNumber, nodeId, bootInfo.bootstrapNodeAddress, 0);
        cdrmAdrManager.createClusterAdrSimulator(cdrmNumber, cdrmAdrManager.getCdrmApp(cdrmNumber), numberOfAdrs);
        
		ClusterAdrSimulator clusterAdrs = cdrmAdrManager.getClusterAdrSimulator(cdrmNumber);
		int numberOfExperiments=1;
		for (int adrNumber=0; adrNumber<numberOfAdrs; adrNumber++)
			clusterAdrs.setAdrParameters(adrNumber, freeSpace, numberOfExperiments, timeOffset,
					meanDayUptime, meanNightUptime, meanDayIdleness, meanNightIdleness);
	
		clusterAdrs.registerAdrs();
        
		if (hasBroker == 1)
			accessBrokerManager.createSingleAccessBroker(cdrmNumber);       
        
		/**
		 * Prints debug information
		 */
        System.out.println(cdrmNumber + ": " + cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getNode() + " [" + 
                cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getNode().getId().toByteArray()[19] + "," +
                cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getNode().getId().toByteArray()[18] + "] ");

        cdrmStartLog.println(cdrmNumber + ": " + cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getNode() + " [" + 
                cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getNode().getId().toByteArray()[19] + "," +
                cdrmAdrManager.getCdrmApp(cdrmNumber).getVirtualNode().getNode().getId().toByteArray()[18] + "] ");
        cdrmStartLog.flush();

        /**
         * Waits until the CDRM is ready
         */
        waitUntilCdrmIsReady( cdrmNumber );
        
    }    
 
}