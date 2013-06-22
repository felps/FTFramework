package br.usp.ime.oppstore.simulation.simnode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CORBA.ORB;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import br.usp.ime.oppstore.FileFragmentIndex;
import br.usp.ime.oppstore.cdrm.CdrmApp;
import br.usp.ime.oppstore.cdrm.CdrmRequestsImpl;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage;
import br.usp.ime.oppstore.simulation.adr.ClusterAdrSimulator;
import br.usp.ime.oppstore.simulation.adr.MachineAdrStateController;
import br.usp.ime.oppstore.simulation.cdrm.MachineCdrmStateController;
import br.usp.ime.oppstore.statistics.ClusterAdrsInformation;
import br.usp.ime.oppstore.statistics.StatisticsCollector;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.test.TestVirtualIdRing;

public class MachineCdrmAdrManager {

    private Environment env;
    private SocketPastryNodeFactory nodeFactory;
    private ORB orb;
    
    StatisticsCollector statisticsCollector;

    MachineEventController localCdrmEventController;
    
    private TreeMap<Integer, CdrmApp> cdrmAppMap = new TreeMap<Integer, CdrmApp>();
    private TreeMap<Integer, CdrmRequestsImpl> cdrmImplMap = new TreeMap<Integer, CdrmRequestsImpl>();
    private TreeMap<Integer, ClusterAdrSimulator> clusterAdrSimulatorMap = new TreeMap<Integer, ClusterAdrSimulator>();
    
    Random rand;

    public MachineCdrmAdrManager(Environment env, int localBindPort, NodeIdFactory nodeIdFactory, ORB orb) {
            	
    	this.orb = orb;
        this.rand = new Random(256);
        this.env = env;
        this.statisticsCollector = new StatisticsCollector();        
        this.localCdrmEventController = new MachineEventController(new MachineAdrStateController(), new MachineCdrmStateController());
        try { this.nodeFactory = new SocketPastryNodeFactory(nodeIdFactory, localBindPort, env); } 
        catch (IOException e) { e.printStackTrace(); }        
    }

    public int getNumberOfCdrms() {
        return cdrmImplMap.size();
    }

    public void checkNumberOfReplicas () {
    	
    	System.out.println("Checking the number of replicas of each FFI.");
    	
    	HashMap<Id, AtomicInteger> replicaCount = new HashMap<Id, AtomicInteger>();
    	
    	for (CdrmApp cdrmApp : cdrmAppMap.values()) {
    		Map<Id, FileFragmentIndex> ffiMap = cdrmApp.getStoredFfiMap();
    		for (Id ffiId : ffiMap.keySet()) {
    			AtomicInteger count = replicaCount.get(ffiId);    			
    			if ( count == null )
    				replicaCount.put(ffiId, new AtomicInteger(1));
    			else
    				count.incrementAndGet();
    		}
    	}
    	
    	for ( Entry<Id, AtomicInteger> nReplicas : replicaCount.entrySet() ) {
    		//System.out.print(nReplicas + " ");
    		//assert (nReplicas.intValue() >= 3);
    		if (nReplicas.getValue().intValue() != 3)
    			System.err.println( nReplicas.getKey() + " -> " + nReplicas.getValue() );
    	}
    	//System.out.println();
    }
    
    public ClusterAdrSimulator getClusterAdrSimulator(int index) {    
    		return clusterAdrSimulatorMap.get(index);
    }
    
    public void testVirtualIdRing () { 
        Vector<VirtualNode> virtualNodeList = new Vector<VirtualNode>();
        for (CdrmApp cdrmApp : cdrmAppMap.values())
        	if (cdrmApp.getVirtualNode().getVirtualSpace(1) != null)
        		virtualNodeList.add( cdrmApp.getVirtualNode() );
        
        TestVirtualIdRing testVirtualIdRing = TestVirtualIdRing.createInstance(env);
        testVirtualIdRing.performTests(virtualNodeList, 1, false);
    }

    public int getNumberOfAdrsInCdrm(int index) {
        if (clusterAdrSimulatorMap.size() > 0)
            return clusterAdrSimulatorMap.get(index).numberOfAdrs();
        else
            return -1;
    }

    public CdrmApp getCdrmApp(int index) {
        if (cdrmAppMap.size() > 0)
            return cdrmAppMap.get(index);
        else
            return null;
    }

    public CdrmRequestsImpl getCdrmImpl(int index) {
        return cdrmImplMap.get(index);
    }

    public void printVirtualIdStatistics() {
    	HashSet<CdrmApp> virtualCdrmSet = new HashSet<CdrmApp>();
    	for (CdrmApp cdrmApp : cdrmAppMap.values())
    		if (cdrmApp.getVirtualNode().getVirtualSpace(1) != null)
    			virtualCdrmSet.add( cdrmApp );
    	statisticsCollector.printVirtualIdStatistics(virtualCdrmSet);
    }
    
    public void printStatistics() {
        
        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        Vector<ClusterAdrsInformation> clusterAdrsInfoVector = 
            new Vector<ClusterAdrsInformation>(clusterAdrSimulatorMap.size());
        for (ClusterAdrSimulator clusterAdrSimulator : clusterAdrSimulatorMap.values())
            clusterAdrsInfoVector.add( clusterAdrSimulator.getStatisticalInformation() );
                
        statisticsCollector.printCdrmStorageCount();        
        statisticsCollector.printVirtualIdStatistics(cdrmAppMap.values());        
        statisticsCollector.printMeanHopCount();        
        statisticsCollector.printClusterAdrsInfo(clusterAdrsInfoVector);        
        statisticsCollector.flushOutput();
        
        //for (ClusterAdrSimulator adrSimulator : clusterAdrSimulators)
        //    adrSimulator.printStorageStatistics();
        //System.out.println();
        
        //fisLocationTester.performTests(cdrmApps);
     
    }

    /**
     * @param bootstrapNodeAddress
     * @param numberOfCdrms
     */
    public void createLocalCdrms (InetSocketAddress bootstrapNodeAddress, int numberOfCdrms, int numberOfAdrs) {
                
        // Constructs numNodes CdrmApps and inserts in the cdrmApps vector
        for (int cdrmNumber = 0; cdrmNumber < numberOfCdrms; cdrmNumber++) {
            createLocalCdrm(cdrmNumber, null, bootstrapNodeAddress, numberOfAdrs);      
            System.out.println(cdrmNumber + ": " + cdrmAppMap.get(cdrmNumber).getVirtualNode().getNode());
            
            //while (!cdrmApps.lastElement().getVirtualNode().getNode().isReady()) {
            while (cdrmAppMap.get(cdrmNumber).getVirtualNode().getVirtualSpace(1).getVirtualNeighborSet().isReady() == false) {
            	try { Thread.sleep( 100 * (cdrmNumber+1) ); }
            	catch (InterruptedException e) {}
            }
        }
        
        Vector<VirtualNode> virtualNodeList = new Vector<VirtualNode>();
        for (CdrmApp cdrmApp : cdrmAppMap.values())
        	virtualNodeList.add( cdrmApp.getVirtualNode() );
               
        TestVirtualIdRing testVirtualIdRing = TestVirtualIdRing.createInstance(env);
        testVirtualIdRing.performTests(virtualNodeList, 1, false);

    }
    
    /**
     * @param bootstrapNodeAddress
     * @param numberOfAdrs
     */
    public void createLocalCdrm(int cdrmNumber, rice.pastry.Id nodeId, InetSocketAddress bootstrapNodeAddress, int numberOfAdrs) {

        NodeHandle bootstrapNodeHandle = nodeFactory.getNodeHandle(bootstrapNodeAddress);

        CdrmApp cdrmApp = new CdrmApp(nodeFactory, nodeId, bootstrapNodeHandle, statisticsCollector);
        cdrmAppMap.put(cdrmNumber, cdrmApp);
        
        CdrmRequestsImpl cdrmImpl = new CdrmRequestsImpl(cdrmApp.getFileStorageRetrievalManager(), env, orb);
        cdrmImplMap.put(cdrmNumber, cdrmImpl);
        
        if (numberOfAdrs > 0) {
            createClusterAdrSimulator(cdrmNumber, cdrmApp, numberOfAdrs);
            ClusterAdrSimulator adrSimulator = clusterAdrSimulatorMap.get(cdrmNumber);
            for (int adrNumber=0, adrMult = 1; adrNumber < numberOfAdrs; adrNumber++, adrMult *= 2)
                adrSimulator.setAdrParameters(adrNumber, 1000, 1, 0, 1, 1, 1, 1);
            adrSimulator.registerAdrs();
        }        
    }
    
    public void createClusterAdrSimulator(int cdrmNumber, CdrmApp cdrmApp, int numberOfAdrs) {
        ClusterAdrSimulator clusterAdrSimulator = 
            new ClusterAdrSimulator(cdrmApp.getAdrManager(), rand.nextLong());
        clusterAdrSimulator.createAdrs(numberOfAdrs);        
        clusterAdrSimulatorMap.put(cdrmNumber, clusterAdrSimulator);
        
        //System.out.println("numbrOfAdrs:" +  numberOfAdrs + " " + clusterAdrSimulators.size());
    }
       
    public void destroyLocalCdrm (int cdrmIndex) {

    	CdrmApp cdrmApp = cdrmAppMap.get(cdrmIndex);  

    	System.out.println("Killing node " + cdrmApp.getVirtualNode().getNode().getId() + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    	cdrmApp.getVirtualNode().getNode().destroy();

    	cdrmAppMap.remove(cdrmIndex);    		
    	cdrmImplMap.remove(cdrmIndex);
    	clusterAdrSimulatorMap.remove(cdrmIndex);
            
    	//cdrmApp.leaveNetwork();
    }
}
