package br.usp.ime.virtualId.message;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;

public class VirtualIdMessage implements Message {

    private static final long serialVersionUID = -2318493818712027843L;
    public static final int initialTimeToLive = 10;
    
    /**
     * The virtual space used to route the message.
     * Several virtual spaces can coexists, and virtual space 0 always refer to the original id space.
     */
    public int routingVirtualSpaceNumber;
    
    /**
     * 
     */
    public NodeHandle sourceHandle;
    
    public Id messageVirtualId;
    
    /**
     * A message identifier used to guarantee a node does not receives the same message multiple times. 
     */
    public int messageNumber;
    
    /**
     * Indicates wheter this is a response to a message, sent directly to the source node.
     * When this value is 'true', the message is not routed in the virtualId space.
     */
    public boolean isResponse = false;
    
    /**
     * Indicates the maximum number of hops a message may take.
     * This values is decrease at each hop and the message is dropped when this value reaches 0 (zero).
     */
    public int timeToLive = initialTimeToLive;

    public VirtualIdMessage(NodeHandle messageSourceHandle, int routingVirtualSpaceNumber, int messageNumber) {
    	this.sourceHandle = messageSourceHandle;
    	this.routingVirtualSpaceNumber = routingVirtualSpaceNumber;
    	this.messageNumber = messageNumber;
    	this.messageVirtualId = null;
    }
    
    public byte getPriority() { return LOW_PRIORITY; }

}
