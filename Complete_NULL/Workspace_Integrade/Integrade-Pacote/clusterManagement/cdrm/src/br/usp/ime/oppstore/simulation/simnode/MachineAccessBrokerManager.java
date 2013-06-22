package br.usp.ime.oppstore.simulation.simnode;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POA;

import rice.environment.Environment;
import rice.pastry.NodeIdFactory;

import br.usp.ime.oppstore.corba.AccessBroker;
import br.usp.ime.oppstore.corba.AccessBrokerHelper;
import br.usp.ime.oppstore.simulation.OppStoreSimulatorRemote;
import br.usp.ime.oppstore.simulation.broker.AccessBrokerSimulator;
import br.usp.ime.oppstore.simulation.broker.AccessBrokerSimulatorRemote;
import br.usp.ime.oppstore.statistics.BrokerStatisticsCollector;

public class MachineAccessBrokerManager {

    private Environment env;
    
    private Vector<AccessBrokerSimulator> accessBrokerSimulatorList;    
    
    private OppStoreSimulatorRemote opStoreSimulatorRemote;
    private BrokerStatisticsCollector brokerStatisticsCollector;    
    private MachineCdrmAdrManager cdrmAdrManager;
    private NodeIdFactory nodeIdFactory;
    
    POA poa;
    ORB orb;
    
    MachineAccessBrokerManager(Environment env, 
            OppStoreSimulatorRemote opStoreSimulatorRemote, 
            MachineCdrmAdrManager cdrmAdrManager,
            NodeIdFactory nodeIdFactory, ORB orb, POA poa) {
        
        this.env = env;
        this.opStoreSimulatorRemote = opStoreSimulatorRemote;
        this.brokerStatisticsCollector = new BrokerStatisticsCollector();
        this.cdrmAdrManager = cdrmAdrManager;
        this.nodeIdFactory = nodeIdFactory;
        this.poa = poa;
        this.orb = orb;
        this.accessBrokerSimulatorList = new Vector<AccessBrokerSimulator>();
    }
    	       
    void createSingleAccessBroker(int cdrmNumber) {
    	                        	
    	AccessBrokerSimulator accessBrokerSimulator = 
    		new AccessBrokerSimulator( opStoreSimulatorRemote, cdrmAdrManager.getCdrmImpl(cdrmNumber), nodeIdFactory, brokerStatisticsCollector, cdrmNumber );
    	accessBrokerSimulatorList.add( accessBrokerSimulator );

    	try {
    		Object simulatorObj = this.poa.servant_to_reference( accessBrokerSimulator );
    		AccessBroker accessBroker = AccessBrokerHelper.narrow( simulatorObj );
    		String accessBrokerIor = orb.object_to_string(accessBroker);
    		accessBrokerSimulator.setAccessBrokerIor( accessBrokerIor );

    	} catch (Exception e) {	e.printStackTrace(); }

    	brokerStatisticsCollector.incrementNumberOfBrokers();
    	
    	try {
    		AccessBrokerSimulatorRemote simulatorRemote = (AccessBrokerSimulatorRemote) UnicastRemoteObject.exportObject(accessBrokerSimulator, 0); 
    		opStoreSimulatorRemote.addAccessBrokerRemoteList( new AccessBrokerSimulatorRemote[]{simulatorRemote} );                        
    	}	 
    	catch (RemoteException e) {
            System.out.println("Could not register local ADRs. Aborting...");
            System.out.println(e.getMessage());
            env.destroy();
            System.exit(-1);
        }
    	

    }

    void createAndRegisterAccessBrokers(int numberOfAccessBrokers) {        
        
        Vector<AccessBrokerSimulatorRemote> accessBrokerRemoteList = new Vector<AccessBrokerSimulatorRemote>();

    	try {
    		
    		for (int i=0; i<numberOfAccessBrokers; i++) {

    			AccessBrokerSimulator simulator = 
    				new AccessBrokerSimulator( opStoreSimulatorRemote, cdrmAdrManager.getCdrmImpl(i), nodeIdFactory, brokerStatisticsCollector, i );
    			accessBrokerSimulatorList.add( simulator );

    			try {
    				Object simulatorObj = this.poa.servant_to_reference( simulator );
    				AccessBroker accessBroker = AccessBrokerHelper.narrow( simulatorObj );
    				String accessBrokerIor = orb.object_to_string(accessBroker);
    				simulator.setAccessBrokerIor( accessBrokerIor );

    			} catch (Exception e) {	e.printStackTrace(); }

    			AccessBrokerSimulatorRemote simulatorRemote = (AccessBrokerSimulatorRemote) UnicastRemoteObject.exportObject(simulator, 0); 
    			accessBrokerRemoteList.add( simulatorRemote );                        

    		}
    		opStoreSimulatorRemote.addAccessBrokerRemoteList( accessBrokerRemoteList.toArray(new AccessBrokerSimulatorRemote[0]) );
    		
    	}	 
    	catch (RemoteException e) {
    		System.out.println("Could not register local ADRs. Aborting...");
    		System.out.println(e.getMessage());
    		env.destroy();
    		System.exit(-1);
    	}

        brokerStatisticsCollector.setNumberOfBrokers(accessBrokerSimulatorList.size());
    }

}
