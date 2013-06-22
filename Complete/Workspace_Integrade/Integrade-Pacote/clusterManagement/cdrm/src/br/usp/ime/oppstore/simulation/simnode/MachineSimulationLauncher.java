package br.usp.ime.oppstore.simulation.simnode;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import br.usp.ime.oppstore.simulation.CdrmBootstrapInformation;
import br.usp.ime.oppstore.simulation.OppStoreSimulatorLauncher;
import br.usp.ime.oppstore.simulation.OppStoreSimulatorRemote;

import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.standard.RandomNodeIdFactory;

public class MachineSimulationLauncher {

    OppStoreSimulatorLauncher opStoreSimulatorLauncher;
    OppStoreSimulatorRemote opStoreSimulator;    
    NodeIdFactory nodeIdFactory;
    Environment env;
    
    CdrmBootstrapInformation bootInfo;

    MachineCdrmAdrManager cdrmAdrManager;
    MachineAccessBrokerManager accessBrokerManager;
    
	ORB orb;
	POA poa;
    
    public MachineSimulationLauncher(OppStoreSimulatorRemote opStoreSimulatorRemote) {        
        
        env = new Environment();
        env.getParameters().setInt("pastry_lSetSize", 8);
        nodeIdFactory = new RandomNodeIdFactory(env);        
        
        if (opStoreSimulatorRemote == null)
            this.opStoreSimulator = getOpstoreSimulatorRemote();
        else
            this.opStoreSimulator = opStoreSimulatorRemote;
        
        try {
            bootInfo = opStoreSimulator.getBootstrapNodeAddress();
        } catch (RemoteException e1) {
            System.out.println("Could not get BootstrapNodeAddress from OpStoreSimulator. Exiting...");
            System.exit(-1);
        }                
        
		this.orb = ORB.init(new String[] {}, null);
		try {
			this.poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			this.poa.the_POAManager().activate();
		} catch (InvalidName e1) {
			e1.printStackTrace();
		} catch (AdapterInactive e) {
			e.printStackTrace();
		}
    }
    
    public ORB getOrb() {
		return orb;
	}
    
    private OppStoreSimulatorRemote getOpstoreSimulatorRemote() {
        
        Registry registry;
        OppStoreSimulatorRemote simulatorRemote = null;
        try {
            registry = LocateRegistry.getRegistry();
            simulatorRemote = (OppStoreSimulatorRemote) registry.lookup("OpStoreSimulator");
        } catch (RemoteException e) {
            System.out.println(e.getCause());
        } catch (NotBoundException e) {
            System.out.println("Could not find OpStoreSimulator.");
        }
        return simulatorRemote;
    }

    public void printStatistics() {
        cdrmAdrManager.printStatistics();
    }
    
    public void launchCdrmsAndBrokersFromFile(File inputFile, int localBindPort) {
        SimulationFileReader simulationFileReader = new SimulationFileReader(env, nodeIdFactory, opStoreSimulator, orb, poa);
        simulationFileReader.createCdrmAdrFromFile(inputFile, localBindPort, bootInfo);
        cdrmAdrManager = simulationFileReader.cdrmAdrManager;
        accessBrokerManager = simulationFileReader.accessBrokerManager;    
    }
    
    public void launchCdrmsAndBrokers(int localBindPort) {
    	
        System.err.println("Simulation without files is not implemented yet!");
        System.exit(-1);
        
        int numberOfCdrms = 20;
        int numberOfAdrs = 10;
        int numberOfAccessBrokers = 1;
        
        cdrmAdrManager = new MachineCdrmAdrManager(env, localBindPort, nodeIdFactory, orb);
        cdrmAdrManager.createLocalCdrms(bootInfo.bootstrapNodeAddress, numberOfCdrms, numberOfAdrs);

        accessBrokerManager = new MachineAccessBrokerManager(env, opStoreSimulator, cdrmAdrManager, nodeIdFactory, orb, poa);
        accessBrokerManager.createAndRegisterAccessBrokers(numberOfAccessBrokers);
        
        //printStatistics();
        
    }
            
    /**
     * Main method.
     */
    public static void main(String[] args) {
        
        int localBindPort = 9001;
        if (args.length > 0)
            localBindPort = Integer.parseInt(args[0]);
                
        MachineSimulationLauncher launcher = new MachineSimulationLauncher(null);
        
        File inputFile = null;
        if (args.length > 1) {
            inputFile = new File(args[1]);
            if (inputFile.exists() == false)
                inputFile = null;
            else
                System.out.println("Reading data from file " + args[1]);
        }

        if (inputFile == null)
            launcher.launchCdrmsAndBrokers(localBindPort);
        else
            launcher.launchCdrmsAndBrokersFromFile(inputFile, localBindPort);

        while (true) {
            try { Thread.sleep(60 * 1000); } 
            catch (InterruptedException e) { }
        }
    }

}
