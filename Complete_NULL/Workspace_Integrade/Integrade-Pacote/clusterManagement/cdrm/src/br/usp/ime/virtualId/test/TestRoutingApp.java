package br.usp.ime.virtualId.test;

import java.net.InetSocketAddress;

import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.message.VirtualIdMessage;
import br.usp.ime.virtualId.protocol.ProtocolObserver;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.pastry.PastryNode;
import rice.pastry.socket.SocketPastryNodeFactory;

public class TestRoutingApp implements Application {

	private VirtualNode virtualNode;
	
	private VirtualSpaceTester virtualSpaceTester;
	
	public TestRoutingApp (VirtualSpaceTester virtualSpaceTester, int numberOfVirtualSpaces,
			InetSocketAddress bootstrapAddress, SocketPastryNodeFactory nodeFactory, ProtocolObserver protocolObserver) {
		    	
		this.virtualSpaceTester = virtualSpaceTester;
		
        rice.pastry.NodeHandle bootstrapHandle = nodeFactory.getNodeHandle(bootstrapAddress);

        PastryNode node = nodeFactory.newNode( bootstrapHandle );
        //if (nodeId == null) node = nodeFactory.newNode( bootstrapHandle );
        //else node = nodeFactory.newNode(bootstrapHandle, (rice.pastry.Id)nodeId);        

        this.virtualNode = new VirtualNode(this, node);

        for (int spaceNumber=1; spaceNumber <= numberOfVirtualSpaces; spaceNumber++)
        	virtualNode.joinVirtualSpace(spaceNumber, this, Math.random() * 100, protocolObserver);

	}
		
	public VirtualNode getVirtualNode() {
		return virtualNode;
	}
    
    public void deliver(Id messageId, Message message) {

        VirtualIdMessage virtualIdMessage = (VirtualIdMessage)message;
        int virtualSpaceNumber = virtualIdMessage.routingVirtualSpaceNumber;
        Id targetNodeId = virtualNode.getVirtualSpace(virtualSpaceNumber).getVirtualId();
        
        virtualSpaceTester.setMessageDelivered(targetNodeId, virtualIdMessage.messageVirtualId, virtualSpaceNumber);

    }

    public boolean forward(RouteMessage message) {
    	return true;
    }

    public void update(NodeHandle handle, boolean joined) { }

}
