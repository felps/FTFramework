package br.usp.ime.virtualId.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Vector;

import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;

import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.protocol.ProtocolObserver;

public class TestNodeManager implements ProtocolObserver {
   
    private SocketPastryNodeFactory nodeFactory;
    
    private Vector<VirtualNode> virtualNodeList;
    private Vector<TestRoutingApp> testRoutingAppList;
    
    private TestVirtualIdRing testVirtualIdRing;
    
    private VirtualSpaceTester virtualSpaceTester;
    
    private int numberOfVirtualSpaces = 0;
    
    private int numberOfProtocolNotifications = 0;  
    
    private static TestNodeManager testNodeManager;
    
    private TestNodeManager(Environment env, int localBindPort, NodeIdFactory nodeIdFactory) {
                
        virtualNodeList    = new Vector<VirtualNode>();
        testRoutingAppList = new Vector<TestRoutingApp>();
        
        testVirtualIdRing  = TestVirtualIdRing.createInstance(env);
        virtualSpaceTester = new VirtualSpaceTester(nodeIdFactory);
        
        try { this.nodeFactory = new SocketPastryNodeFactory(nodeIdFactory, localBindPort, env); } 
        catch (IOException e) { e.printStackTrace(); }        
    }

    public static TestNodeManager getInstance() { return testNodeManager; }
    
    public static TestNodeManager createInstance(Environment env, int localBindPort, NodeIdFactory nodeIdFactory) {
    	TestNodeManager.testNodeManager = new TestNodeManager(env, localBindPort, nodeIdFactory);
    	return TestNodeManager.testNodeManager;
    }
    
    public List<VirtualNode> getVirtualNodeList () { return virtualNodeList; }
    
    public VirtualNode getVirtualNode (int index) {
        return virtualNodeList.get(index);
    }
    
    public int getNumberOfNodes() {        
        return virtualNodeList.size();
    }
    
    public void launchRoutingTests(int numberOfMessages) {
        virtualSpaceTester.setVirtualNodeList( virtualNodeList, numberOfVirtualSpaces );
        virtualSpaceTester.launchTests(numberOfMessages);
    }
    
    public void createTestNodes (InetSocketAddress bootstrapAddress, int numberOfNodes, int numberOfVirtualSpaces) {
                
    	this.numberOfVirtualSpaces = numberOfVirtualSpaces;
    	
        for (int curNode = 0; curNode < numberOfNodes; curNode++) {

        	this.numberOfProtocolNotifications = 0;
        	
        	TestRoutingApp testRoutingApp = 
        		new TestRoutingApp(virtualSpaceTester, numberOfVirtualSpaces, bootstrapAddress, nodeFactory, this);
        	testRoutingAppList.add( testRoutingApp );
        	VirtualNode virtualNode = testRoutingApp.getVirtualNode();
            virtualNodeList.add( virtualNode );
            
            System.out.println(curNode + ": " + virtualNode.getNode());                        
            
            while ( numberOfVirtualSpaces > 0 && numberOfProtocolNotifications < numberOfVirtualSpaces) {
            	try { Thread.sleep( 100 ); }
            	catch (InterruptedException e) {}
            }
            
        }

        System.out.println ("Preparing to test the virtualLeafset.");
        try { Thread.sleep( 5000 ); }
        catch (InterruptedException e) {}               

        for (int virtualSpaceNumber=1; virtualSpaceNumber<=numberOfVirtualSpaces; virtualSpaceNumber++)
        	testVirtualIdRing.performTests(virtualNodeList, virtualSpaceNumber, true);

        System.out.println ("Updating leafSet capacity for node " + virtualNodeList.lastElement().getNode().getId());
        virtualNodeList.lastElement().updateNodeCapacity(1, 128.0, null);
        
        System.out.println ("Preparing to test the virtualLeafset.");
        try { Thread.sleep( 5000 ); }
        catch (InterruptedException e) {}               
        
        for (int virtualSpaceNumber=1; virtualSpaceNumber<=numberOfVirtualSpaces; virtualSpaceNumber++)
        	testVirtualIdRing.performTests(virtualNodeList, virtualSpaceNumber, true);

        //for (int curNode = 0; curNode < numberOfNodes; curNode++) {
        for (int curNode = 0; curNode < numberOfNodes/4; curNode++) {

        	this.numberOfProtocolNotifications = 0;
        	System.out.println("Killing node " + virtualNodeList.lastElement().getNode().getId() + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        	virtualNodeList.lastElement().getNode().destroy();
        	virtualNodeList.remove( virtualNodeList.lastElement() );
        	testRoutingAppList.remove( testRoutingAppList.lastElement() );

        	while ( numberOfVirtualSpaces > 0 && numberOfProtocolNotifications < numberOfVirtualSpaces) {
        		try { Thread.sleep( 100 ); }
        		catch (InterruptedException e) {}
        	}
        }
        
        System.out.println ("Preparing to test the virtualLeafset.");
        try { Thread.sleep( 5000 ); }
        catch (InterruptedException e) {}

        for (int virtualSpaceNumber=1; virtualSpaceNumber<=numberOfVirtualSpaces; virtualSpaceNumber++)
        	testVirtualIdRing.performTests(virtualNodeList, virtualSpaceNumber, true);

        	
    }

	public void notifyProtocolFinished(int virtualSpaceNumber) {

//    	try { Thread.sleep( 1000 ); }
//    	catch (InterruptedException e) {}

		testVirtualIdRing.performTests(virtualNodeList, virtualSpaceNumber, false);
		numberOfProtocolNotifications++;
		
		System.out.println("Protocol finished: " + numberOfProtocolNotifications + " of " + numberOfVirtualSpaces );
	}        
}
