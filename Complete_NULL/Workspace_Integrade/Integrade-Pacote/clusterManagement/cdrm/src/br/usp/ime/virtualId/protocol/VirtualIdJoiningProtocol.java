package br.usp.ime.virtualId.protocol;

import rice.p2p.commonapi.Id;
import rice.pastry.Id.Distance;
import rice.pastry.leafset.LeafSet;
import br.usp.ime.virtualId.NodeInformation;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;
import br.usp.ime.virtualId.message.VirtualIdLeafSetQuery;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage;
import br.usp.ime.virtualId.message.VirtualIdLeafSetQuery.LeafSetQuerySide;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage.UpdateType;

public class VirtualIdJoiningProtocol extends VirtualIdProtocol {

	int leftLeafQueryIndex  = -1;
	int rightLeafQueryIndex =  1;
	
    public VirtualIdJoiningProtocol(VirtualNode virtualNode, VirtualSpace virtualSpace) {
		super(virtualNode, virtualSpace);
	}

    public void resendLeafsetQuery(VirtualIdLeafSetQuery leafSetQuery) {

    	leafSetQuery.isResponse = false;
    	leafSetQuery.sourceHandle = virtualNode.getNode().getLocalNodeHandle();

        /**
         * Request the leafSet from the next neighbors
         */
        LeafSet leafSet = virtualNode.getNode().getLeafSet();
        if ( leafSetQuery.leafSetSide == LeafSetQuerySide.LEFT ) {
        	leftLeafQueryIndex--;
        	messageDispatcher.sendDirectMessage(leafSet.get(leftLeafQueryIndex), leafSetQuery, true, this);
        }
        else if ( leafSetQuery.leafSetSide == LeafSetQuerySide.RIGHT ) {
        	rightLeafQueryIndex++;
        	messageDispatcher.sendDirectMessage(leafSet.get(rightLeafQueryIndex), leafSetQuery, true, this);
        }
    }

    // ====================================================================
    // Superclass methods
    // ====================================================================

    /**
     * Abstract method from VirtualIdProtocol.
     * 
     * Start the creation of the adaptiveId table of this node
     */
    protected void protocolFinished (VirtualIdUpdateMessage leafsetMessage) {
    	
    	if (leafsetMessage != null)
    		leafsetMessage.updatingNodeId = virtualNode.getNode().getId();
    	logger.info("VirtualId joining protocol finished!");
    }

    /**
     * Abstract method from VirtualIdProtocol.
     * 
     * Updates the adaptiveIds of the current node
     */
    protected void updateMessageReady ( VirtualIdUpdateMessage message ) {

        /**
         * Sets the new virtual Ids in the current node and neighbors set.
         */    	
        virtualSpace.getVirtualNeighborSet().setNeighborSet(message);                
        int index = message.virtualIdUpdates.getIndexHandle( virtualNode.getNode().getLocalHandle() );
        virtualSpace.setVirtualId( message.virtualIdUpdates.getNewId(index) );
        
        message.updateType = UpdateType.NEIGHBORSET;
        message.protocolType = protocolType;
        message.updatingNodeId = virtualNode.getNode().getId();
    }

    /**
     * Abstract method from VirtualIdProtocol.
     * 
     * Sets the current CDRM adaptiveId just at right of its left node
     */
    protected void collectedNeighborInformation() {
    
    		Id leftLeafId = virtualNode.getNode().getLeafSet().get(-1).getId(); 
    		for (NodeInformation nodeInfo : neighborNodeInfoMap.values())
    			if ( nodeInfo.getNodeId().equals( leftLeafId ) ) {
    				Id virtualId = nodeInfo.getVirtualId().addToId( new Distance(new int[] {1,0,0,0,0}) );
    				neighborNodeInfoMap.get(virtualNode.getNode().getId()).setVirtualId( virtualId );
    				break;
    			}            	
    }

    /**
     * Abstract method from VirtualIdProtocol.
     * 
     * Adds the current node information to the neighbor map.
     */    
    protected void protocolStarted() {
    	
    	protocolType = ProtocolType.JOINING;
    
        double capacity = virtualSpace.getCapacity();
        Id nodeId = virtualNode.getNode().getId(); 

        NodeInformation nodeInfo = new NodeInformation(
                nodeId, nodeId, virtualNode.getNode().getLocalNodeHandle(), capacity );        
        neighborNodeInfoMap.put( nodeId, nodeInfo );
        
        logger.info("Starting joining protocol at " + nodeId + " with capacity:" + capacity );        
        
        /**
         * Obtains the leafSet from the immediate neighbors
         */
        LeafSet leafSet = virtualNode.getNode().getLeafSet();
        if ( leafSet.getUniqueCount() > leafSet.maxSize() ) {
        	int nextMessageNumber = virtualNode.getNextMessageNumber();
        	
        	VirtualIdLeafSetQuery leafSetQueryL = new VirtualIdLeafSetQuery(
        			virtualNode.getNode().getLocalHandle(), virtualSpace.getVirtualSpaceNumber() ,nextMessageNumber);
        	leafSetQueryL.leafSetSide = LeafSetQuerySide.LEFT;
        	messageDispatcher.sendDirectMessage(leafSet.get(leftLeafQueryIndex), leafSetQueryL, true, this);

        	VirtualIdLeafSetQuery leafSetQueryR = new VirtualIdLeafSetQuery(
        			virtualNode.getNode().getLocalHandle(), virtualSpace.getVirtualSpaceNumber() ,nextMessageNumber);
        	leafSetQueryR.leafSetSide = LeafSetQuerySide.RIGHT;
        	messageDispatcher.sendDirectMessage(leafSet.get(rightLeafQueryIndex), leafSetQueryR, true, this);
        }
        else {
        	virtualSpace.getVirtualLeafSet();
        }
    }
        
}
