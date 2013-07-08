package br.usp.ime.virtualId.protocol;

import rice.p2p.commonapi.Id;
import br.usp.ime.virtualId.NodeInformation;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage.UpdateType;

public class VirtualIdUpdateProtocol extends VirtualIdProtocol {
    
    public VirtualIdUpdateProtocol(VirtualNode virtualNode, VirtualSpace virtualSpace) {
		super(virtualNode, virtualSpace);
	}
    
    // ====================================================================
    // Superclass methods
    // ====================================================================

    /**
     * Abstract method from VirtualIdProtocol
     */
	protected void protocolFinished(VirtualIdUpdateMessage leafsetMessage) {
			            
		if (leafsetMessage != null)
			leafsetMessage.updatingNodeId = virtualNode.getNode().getId();
		logger.info("VirtualId update protocol finished!");		
	}

    /**
     * Abstract method from VirtualIdProtocol
     */
	protected void updateMessageReady(VirtualIdUpdateMessage message) {

        /**
         * Sets the new virtual Ids in the current node and neighbors set.
         */
		virtualSpace.getVirtualNeighborSet().updateNeighborsSet(message);

        int index = message.virtualIdUpdates.getIndexHandle( virtualNode.getNode().getLocalHandle() );
        virtualSpace.setVirtualId( message.virtualIdUpdates.getNewId(index) );

        message.updateType = UpdateType.NEIGHBORSET;
        message.protocolType = protocolType;
        message.updatingNodeId = leavingNodeId; 
	}

    /**
     * Abstract method from VirtualIdProtocol
     */
	protected void collectedNeighborInformation() { 
			
	}

    /**
     * Abstract method from VirtualIdProtocol
     */
	protected void protocolStarted() {
		
		protocolType = ProtocolType.UPDATE;
		
        double capacity = virtualSpace.getCapacity();
        Id nodeId = virtualNode.getNode().getId(); 

        NodeInformation nodeInfo = new NodeInformation(
                nodeId, virtualSpace.getVirtualId(), virtualNode.getNode().getLocalNodeHandle(), capacity );        
        neighborNodeInfoMap.put( nodeId, nodeInfo );

		logger.info("Starting update protocol at " + virtualNode.getNode().getId() + " with capacity " + capacity + ".");        
        //virtualNode.adaptiveIdTable.deleteAdaptiveIdTable();
		
	}
    
    
}
