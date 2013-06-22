package br.usp.ime.virtualId.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import rice.p2p.commonapi.Id;
import rice.pastry.NodeIdFactory;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.message.VirtualIdMessage;

public class VirtualSpaceTester {

    private NodeIdFactory nodeIdFactory;
    private int nMessages = 0;
    private int nDelievered = 0;    
    
    private int nVirtualSpaces = 0; 
    
    List<VirtualNode> virtualNodeList;
    
    private List < TreeSet<Id> > idSetList;

    public VirtualSpaceTester( NodeIdFactory nodeIdFactory ) {
        this.nodeIdFactory = nodeIdFactory; 
    }
    
    public void setVirtualNodeList ( List<VirtualNode> virtualNodeList, int nVirtualSpaces ) {
    
    	this.virtualNodeList = virtualNodeList;
    	this.nVirtualSpaces = nVirtualSpaces;
        this.idSetList = new ArrayList<TreeSet<Id>>(nVirtualSpaces+1);
        
        for (int virtualSpaceNumber = 0; virtualSpaceNumber <= nVirtualSpaces; virtualSpaceNumber++) {
        	TreeSet <Id> idSet = new TreeSet<Id>();
        	for (VirtualNode virtualNode : virtualNodeList)
        		idSet.add( virtualNode.getVirtualSpace(virtualSpaceNumber).getVirtualId() );
        	this.idSetList.add(idSet);
        }

    }
    
    public void launchTests(int nMessages) {

    	Random random = new Random( System.currentTimeMillis() );

        System.out.println("Launching tests! Sending " + nMessages + " messages.");
        
        // sends several messages
        this.nMessages = nMessages;
        int numberOfNodes = virtualNodeList.size();
        
        for (int messageNumber=0; messageNumber<nMessages; messageNumber++) {
        	VirtualNode virtualNode = virtualNodeList.get(messageNumber % numberOfNodes);
            
        	VirtualIdMessage testMessage = new VirtualIdMessage(virtualNode.getNode().getLocalHandle(), random.nextInt(nVirtualSpaces+1), messageNumber);
        	//VirtualIdMessage testMessage = new VirtualIdMessage(virtualNode.getNode().getLocalHandle(), 1, messageNumber);

        	virtualNode.routeMessage(nodeIdFactory.generateNodeId(), testMessage);
        	
        	try { Thread.sleep(100); } 
        	catch (InterruptedException e) { e.printStackTrace(); }
        }        

    }
    
    public void setMessageDelivered (Id targetNodeId, Id messageId, int virtualSpaceNumber) {
    	
    	System.out.println("Message received received (" + nDelievered + " of " + nMessages + ") at virtual space " + virtualSpaceNumber + ".");
        
    	SortedSet <Id> headSet = idSetList.get(virtualSpaceNumber).headSet(messageId);
    	SortedSet <Id> tailSet = idSetList.get(virtualSpaceNumber).tailSet(messageId);
    	Id correctNodeId = null;
    	
    	if (virtualSpaceNumber == 0) { // TODO: There is an error in the algorithm
    		Id leftId = null;
    		if (headSet.size() > 0 ) leftId = headSet.last();
    		else leftId = idSetList.get(virtualSpaceNumber).last();    		

    		Id rightId = null;
    		if (tailSet.size() > 0 ) rightId = tailSet.first();
    		else rightId = idSetList.get(virtualSpaceNumber).first();    		
    		
    		if ( leftId.distanceFromId( messageId ).compareTo( rightId.distanceFromId( messageId ) ) < 0 )
    			correctNodeId = leftId;
    		else
    			correctNodeId = rightId;
    	}
    	// TODO: In pastry, messages are delivered to the nearest node, and not to the clockwise one.    	
    	else if ( virtualSpaceNumber > 0 ) {
    		if (tailSet.size() > 0)
    			correctNodeId = tailSet.first();
    		else
    			correctNodeId = idSetList.get(virtualSpaceNumber).first();    		
    	}
    	
    	if( targetNodeId.equals( correctNodeId ) == false) {
    		System.out.println("*vspace:" + virtualSpaceNumber + ": messageId:" + messageId + " targetId:" + targetNodeId + " correctId:" + correctNodeId + " " + tailSet.size());
    		assert ( false );
    		//targetNodeId.
    	}
    	
    	nDelievered++;
        if (nDelievered == nMessages) {
            System.out.println("All messages received!");
            System.exit(0);
        }
    }

}
