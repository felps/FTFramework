package br.usp.ime.virtualId.protocol;

import rice.p2p.commonapi.Id;
import br.usp.ime.virtualId.NodeInformation;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage.UpdateType;

public class VirtualIdDepartureProtocol extends VirtualIdProtocol {
    
    public VirtualIdDepartureProtocol(VirtualNode virtualNode, VirtualSpace virtualSpace, Id leavingNodeId) {
		super(virtualNode, virtualSpace);
		this.leavingNodeId = leavingNodeId;
	}

    // ====================================================================
    // Supperclass methods
    // ====================================================================

    /**
     * Abstract method from VirtualIdProtocol
     */
	protected void protocolFinished(VirtualIdUpdateMessage leafsetMessage) {
			            
		if (leafsetMessage != null)
			leafsetMessage.updatingNodeId = leavingNodeId;
		logger.info("VirtualId departure protocol finished!");
		
		//((SocketNodeHandle)cdrmApp.getNode().getLocalHandle()).markDeadForever();
        //virtualNode.getNode().destroy();
		//virtualNode.setConnected(false);
	}

    /**
     * Abstract method from VirtualIdProtocol
     */
	protected void updateMessageReady(VirtualIdUpdateMessage message) {

        /**
         * Sets the new virtual Ids in the current node and neighbors set.
         */
		virtualSpace.getVirtualNeighborSet().setNeighborSet( message );

        int index = message.virtualIdUpdates.getIndexHandle( virtualNode.getNode().getLocalHandle() );
        virtualSpace.setVirtualId( message.virtualIdUpdates.getNewId(index) );

        // virtualSpace.getVirtualNeighborSet().removeNode( leavingNodeVirtualId );

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
		
		protocolType = ProtocolType.DEPARTURE;
		
        double capacity = virtualSpace.getCapacity();
        Id nodeId = virtualNode.getNode().getId(); 

        NodeInformation nodeInfo = new NodeInformation(
                nodeId, virtualSpace.getVirtualId(), virtualNode.getNode().getLocalNodeHandle(), capacity );        
        neighborNodeInfoMap.put( nodeId, nodeInfo );

		logger.info("Starting departure protocol at " + virtualNode.getNode().getId() + ".");        
        //virtualNode.adaptiveIdTable.deleteAdaptiveIdTable();
		
	}
    
    
}
