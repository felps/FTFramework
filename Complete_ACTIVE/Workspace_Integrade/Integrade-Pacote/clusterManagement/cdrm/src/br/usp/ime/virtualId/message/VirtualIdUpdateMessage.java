package br.usp.ime.virtualId.message;

import java.util.HashSet;
import java.util.Set;

import br.usp.ime.virtualId.VirtualIdUpdates;
import br.usp.ime.virtualId.protocol.VirtualIdProtocol.ProtocolType;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

public class VirtualIdUpdateMessage extends VirtualIdProtocolMessage {

	private static final long serialVersionUID = 145379620247837673L;

    public enum UpdateType {NEIGHBORSET, LEAFSET};
    public UpdateType updateType = null;
    
    public ProtocolType protocolType = null;

    public enum LeafSetSide {LEFT, CENTER, RIGHT};
    public LeafSetSide leafSetSide = null;   

	public VirtualIdUpdates virtualIdUpdates;
	public boolean toForward = false;
	public boolean updatesOverlap = false;
	public int leftUpdateIndex = -1;
	public int rightUpdateIndex = -1;
	
	public Id updatingNodeId = null;

	/**
	 * Use to determine to which neighbor nodes updates will be sent
	 * Used only in neighborSet updating
	 */
	public Set<NodeHandle> nodeToForwardSet;
	
    public VirtualIdUpdateMessage( VirtualIdUpdateMessage message ) {
        super(message.sourceHandle, message.protocolSpaceNumber, message.messageNumber);
                
        this.updateType   = message.updateType;
        this.leafSetSide  = message.leafSetSide;
        this.protocolType = message.protocolType;
        
    	this.updatesOverlap   = message.updatesOverlap;
    	this.leftUpdateIndex  = message.leftUpdateIndex;
    	this.rightUpdateIndex = message.rightUpdateIndex;

        this.virtualIdUpdates = message.virtualIdUpdates;
        this.toForward = message.toForward;
        this.updatingNodeId = message.updatingNodeId;
        this.dispatcherMessageIdentifier = message.dispatcherMessageIdentifier;
        this.needResponse = message.needResponse;
        
        this.nodeToForwardSet = new HashSet<NodeHandle>();
        this.nodeToForwardSet.addAll( message.nodeToForwardSet );
    }
	
	public VirtualIdUpdateMessage(NodeHandle messageSourceHandle, int protocolSpaceNumber, int messageNumber) {
		super(messageSourceHandle, protocolSpaceNumber, messageNumber);
		
		this.nodeToForwardSet = new HashSet<NodeHandle>();
	}

}
