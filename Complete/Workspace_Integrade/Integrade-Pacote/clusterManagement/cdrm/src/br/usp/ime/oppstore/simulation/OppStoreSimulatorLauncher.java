package br.usp.ime.oppstore.simulation;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import br.usp.ime.oppstore.cdrm.CdrmApp;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreCapacityProtocol;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreIdProtocol;
import br.usp.ime.oppstore.simulation.simnode.MachineSimulationLauncher;

/**
 * OppStoreSimulationLauncher launches the simulation. It can run in two modes: 
 * 1) If an RMI Registry is running, this process registers as the manager of a distributed simulation;
 * 2) Otherwise, this process is started as a standalone simulation machine.
 * 
 * The following parameters can be passed to OppStoreSimulationLauncher:
 * 1) Log4j configuration file;
 * 2) Input file containing the cluster descriptions;
 * 3) protocol that will be used: pi (pastry), ai-linear (virtual ids), ai-quad (virtual ids)
 * 4) number of fragments per file
 * Example:
 * > log4j.properties experiments/inputData/exp-30-24.dat pi 24
 * 
 * The following VM arguments are also recommended:
 * > -ea -Xmx500M
 * 
 * @author Raphael Y. de Camargo
 */
public class OppStoreSimulatorLauncher {

    /**
     * Used when CDRMs and OpStoreSimulator are started in the same JVM
     */
    OppStoreSimulator opStoreSimulator;
    OppStoreSimulatorRemote opStoreSimulatorRemote;
    
    /**
     * Used when CDRMs and OpStoreSimulator are started in the same JVM
     */
    public OppStoreSimulatorLauncher() {
        opStoreSimulator = new OppStoreSimulator();
        try { 
            this.opStoreSimulatorRemote = 
                (OppStoreSimulatorRemote) UnicastRemoteObject.exportObject(opStoreSimulator, 0); 
        } 
        catch (RemoteException e) { e.printStackTrace(); }
        
        // Register opStoreLauncher with Registry
    }
        
    /**
     * Used when CDRMs and OpStoreSimulator are started in the same JVM
     */
    public OppStoreSimulatorRemote getOpStoreSimulatorReference() {        
        return opStoreSimulatorRemote;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    	/**
    	 * Configures logging
    	 */
        if (args.length > 0)
        	PropertyConfigurator.configure(args[0]);
    	Logger logger = Logger.getLogger("oppstoreSim.OppStoreSimulatorLauncher"); 

        File inputFile = null;
        if (args.length > 1) {
            inputFile = new File(args[1]);
            if (inputFile.exists() == false)
                inputFile = null;
        }
        
        if (args.length > 2) {
        	if ( args[2].compareTo("pi") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.PASTRY;      		
        	}
        	else if ( args[2].compareTo("ai-linear") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
        		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.LINEAR;
        	}
        	else if ( args[2].compareTo("ai-quad") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
        		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.QUADRATIC;
        	}
        	else if ( args[2].compareTo("ai-hyper") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
        		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.HIPERBOLIC;
        	}
        	else if ( args[2].compareTo("ai-sqrt") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
        		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.SQUARED;
        	}        		
        	else {
        		System.out.println("ERROR!!!!");
        		System.exit(-1);
        	}
        }
        else {
    		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
    		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.LINEAR;
        }

        int nStorages = 100;
        int nFragments = 12;
        if (args.length > 3) {
        	nFragments = Integer.parseInt( args[3] );
        }
        int nNeeded = nFragments/2;
        
        System.out.println("nFragments=" + nFragments + " nNeeded=" + nNeeded + " nStorages=" + nStorages +  ".");
        
        if (inputFile != null)
        	logger.info("Reading data from file " + args[1]);
        logger.info("Using " + CdrmApp.idProtocol + " " + CdrmApp.capacityProtocol);

    	/**
    	 * Creates the OppStore simulator and registers in the registry
    	 */ 
        OppStoreSimulatorLauncher simulatorLauncher = new OppStoreSimulatorLauncher();
        OppStoreSimulatorRemote simulatorRemote = simulatorLauncher.getOpStoreSimulatorReference();
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry();
            registry.rebind("OpStoreSimulator", simulatorRemote);
            logger.info("OpStoreSimulator registered succesfully.");
            
        } catch (RemoteException e1) {             
            logger.info("Registry not found. Running standalone OpStoreSimulator.");
            logger.info("Cause: " + e1.getCause());
        } 
        
        int localBindPort = 9001;
               
        /**
         * Creates the clusters and adrs on this machine.
         * The simulation is automatically launched after all the clusters and ADRs are created. 
         */
        MachineSimulationLauncher machineSimulator = new MachineSimulationLauncher(simulatorRemote);
        if (inputFile == null) machineSimulator.launchCdrmsAndBrokers(localBindPort);
        else machineSimulator.launchCdrmsAndBrokersFromFile(inputFile, localBindPort);
       
        logger.info("Simulation finished successfully!");
        
        try { Thread.sleep(5 * 1000); }              
        catch (InterruptedException e) { e.printStackTrace(); }
        
        logger.info("Exiting...");
        System.exit(0);
    }

}
