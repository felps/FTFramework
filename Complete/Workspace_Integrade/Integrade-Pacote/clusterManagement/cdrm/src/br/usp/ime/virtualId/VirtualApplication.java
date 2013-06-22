package br.usp.ime.virtualId;

import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import br.usp.ime.virtualId.message.VirtualIdCapacityQuery;
import br.usp.ime.virtualId.message.VirtualIdLeafSetQuery;
import br.usp.ime.virtualId.message.VirtualIdMessage;
import br.usp.ime.virtualId.message.VirtualIdProtocolMessage;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage;
import br.usp.ime.virtualId.message.VirtualIdLeafSetQuery.LeafSetQuerySide;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage.LeafSetSide;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage.UpdateType;
import br.usp.ime.virtualId.protocol.VirtualIdJoiningProtocol;
import br.usp.ime.virtualId.protocol.VirtualIdProtocol;
import br.usp.ime.virtualId.protocol.VirtualMessageDispatcher;
import br.usp.ime.virtualId.protocol.VirtualMessageReceiver;
import br.usp.ime.virtualId.protocol.VirtualIdProtocol.ProtocolType;
import br.usp.ime.virtualId.test.TestNodeManager;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.commonapi.rawserialization.MessageDeserializer;

public class VirtualApplication implements Application {

    List<VirtualSpace> virtualSpaceList;
    VirtualNode virtualNode;
    
    VirtualIdRouting virtualIdRouting;
    
    Endpoint endpoint;
    MessageDeserializer messageDeserializer;
    
    Id cwNodeId;
    Id ccwNodeId;
    
    VirtualMessageReceiver messageReceiver;
    
    private Logger logger;
    
    public VirtualApplication(VirtualNode virtualNode, List<VirtualSpace> virtualSpaceList) {
    	this.virtualNode = virtualNode;
    	this.virtualSpaceList = virtualSpaceList;
    	this.messageReceiver = new VirtualMessageReceiver( virtualNode );
    	this.logger = virtualNode.getLogger();
    }
        
    /**
     * Must be called after the endpoint was initialized
     */
    public void setEndpoint(Endpoint endpoint) {
    	this.endpoint = endpoint;
        this.messageDeserializer = endpoint.getDeserializer();
    	this.virtualIdRouting = new VirtualIdRouting( virtualNode, messageDeserializer, virtualSpaceList );
    	if ( virtualNode.getNode().getLeafSet().get(-1) != null )
    		this.ccwNodeId = virtualNode.getNode().getLeafSet().get(-1).getId();
    	if ( virtualNode.getNode().getLeafSet().get(1) != null )
    		this.cwNodeId = virtualNode.getNode().getLeafSet().get(1).getId();
    	
    }
        
    public Id getCwNodeId() {return cwNodeId;}
    public Id getCcwNodeId() {return ccwNodeId;}
    
    public void deliver(Id id, Message message) {
    	
        if (message instanceof VirtualIdMessage) {
            VirtualIdMessage virtualIdMessage = (VirtualIdMessage) message;

        	//System.out.println("Delivering message " + virtualIdMessage.messageNumber + " at " + virtualNode.getNode().getId() + " from " + virtualIdMessage.sourceHandle.getId() );

            if (virtualIdMessage instanceof VirtualIdProtocolMessage) {
				VirtualIdProtocolMessage protocolMessage = (VirtualIdProtocolMessage) virtualIdMessage;				
				processVirtualIdProtocolMessage(protocolMessage);        
            }
            else {
            	VirtualSpace virtualSpace = virtualSpaceList.get(virtualIdMessage.routingVirtualSpaceNumber);
            	virtualSpace.getApplication().deliver(id, message);
            }
        }
        else
        	logger.error("VirtualApplication -> ERROR: Message is not of type VirtualIdMessage!");

    }

	private void processVirtualIdProtocolMessage(VirtualIdProtocolMessage protocolMessage) {

		//logger.debug("A from " + protocolMessage.sourceHandle.getId() + " at " + virtualNode.getNode().getId() + ".");
				
        VirtualSpace protocolVirtualSpace = null;
        if (virtualSpaceList.size() > protocolMessage.protocolSpaceNumber )
        	protocolVirtualSpace = virtualSpaceList.get(protocolMessage.protocolSpaceNumber);
        
		/**
		 * Check if message was already received
		 */
		if ( protocolMessage.isResponse == true ) {
			VirtualMessageDispatcher messageDispatcher = protocolVirtualSpace.getCurrentProtocol().getMessageDispatcher();
			if ( messageDispatcher.setMessageReceived(protocolMessage) == false )
				return;
		}
		else if ( messageReceiver.setMessageReceived(protocolMessage) == false)
			return;		
		
		if (protocolMessage instanceof VirtualIdLeafSetQuery) {
			VirtualIdLeafSetQuery leafSetQuery = (VirtualIdLeafSetQuery) protocolMessage;
			
			if (leafSetQuery.isResponse == false) {

				logger.debug("Received LeafSetQuery message from " + leafSetQuery.sourceHandle.getId() + "." );

				NodeHandle joiningHandle = leafSetQuery.sourceHandle;                    
				leafSetQuery.isResponse = true;				
				leafSetQuery.sourceHandle = endpoint.getLocalNodeHandle();
				if (protocolVirtualSpace != null)
					leafSetQuery.nodeInfoArray = protocolVirtualSpace.getVirtualLeafSet().getNodeInformationArray();

				messageReceiver.sendResponse(joiningHandle, leafSetQuery);
			}
			else {
				if (leafSetQuery.nodeInfoArray != null)
					protocolVirtualSpace.getVirtualLeafSet().setLeafSet(leafSetQuery);
				else {
					if ( leafSetQuery.protocolType == ProtocolType.JOINING ) {
						VirtualIdJoiningProtocol joiningProtocol = (VirtualIdJoiningProtocol) protocolVirtualSpace.getCurrentProtocol();
						joiningProtocol.resendLeafsetQuery( leafSetQuery );
					}
				}
			}
		}
		else if (protocolMessage instanceof VirtualIdCapacityQuery) {
			VirtualIdCapacityQuery capacityQuery = (VirtualIdCapacityQuery) protocolMessage;

			if (capacityQuery.isResponse == false) {

				logger.debug("Received CapacityQuery message from " + capacityQuery.sourceHandle.getId() + "." );

				NodeHandle joiningHandle = capacityQuery.sourceHandle;
				capacityQuery.isResponse = true;
				capacityQuery.sourceHandle = endpoint.getLocalNodeHandle();
				if (protocolVirtualSpace != null) {
					capacityQuery.virtualId = protocolVirtualSpace.getVirtualId();
					capacityQuery.capacity = protocolVirtualSpace.getCapacity();
					capacityQuery.nodeInformationArray = protocolVirtualSpace.getVirtualNeighborSet().getNodeInformationArray();
				}
				messageReceiver.sendResponse(joiningHandle, capacityQuery);
			}
			else
				protocolVirtualSpace.getCurrentProtocol().setNeighborNodeInformation(capacityQuery);
		}

		else if (protocolMessage instanceof VirtualIdUpdateMessage) {
			VirtualIdUpdateMessage updateMessage = (VirtualIdUpdateMessage) protocolMessage;
			
			// The update was already received  
			if (protocolVirtualSpace == null) {
				NodeHandle joiningHandle = updateMessage.sourceHandle;                    
				updateMessage.isResponse = true;
				updateMessage.sourceHandle = endpoint.getLocalNodeHandle();
				messageReceiver.sendResponse(joiningHandle, updateMessage);
				return;
			}
			
			if (updateMessage.isResponse == false) {

				if ( updateMessage.updateType == UpdateType.NEIGHBORSET )
					processNeighborsetUpdateMessage(protocolVirtualSpace, updateMessage);
				
				else if (updateMessage.updateType == UpdateType.LEAFSET )
					processLeafsetUpdateMessage(protocolMessage, protocolVirtualSpace, updateMessage);									
				
			}
			else { // isResponse == true

				protocolVirtualSpace.getCurrentProtocol().setUpdateResponseReceived(updateMessage);
			}

		}
	}

	private void processLeafsetUpdateMessage(VirtualIdProtocolMessage protocolMessage, VirtualSpace protocolVirtualSpace, VirtualIdUpdateMessage updateMessage) {
		logger.debug("Received Leafset Update message from " + updateMessage.sourceHandle.getId() + "." );
		
		/**
		 * If the node is not the node that started the protocol, update the leafset.
		 */
		boolean setUpdated = false;				
		if( protocolMessage.sourceHandle.getId().equals( virtualNode.getNode().getId() ))
			setUpdated = true;					
		else
			setUpdated = protocolVirtualSpace.getVirtualLeafSet().updateLeafSet( updateMessage );					

		/**
		 * If the leafset was updated, forward the message to neighbor nodes.
		 */
		if (updateMessage.updateType == UpdateType.LEAFSET && setUpdated == true) {
			
			HashSet<NodeHandle> forwardHandleSet = new HashSet<NodeHandle>();
			
			if (updateMessage.leafSetSide == LeafSetSide.LEFT) {
				forwardHandleSet.add( virtualNode.getNode().getLeafSet().get(-1) );
				forwardHandleSet.add( virtualNode.getNode().getLeafSet().get(-2) );
			}
			else if (updateMessage.leafSetSide == LeafSetSide.RIGHT) {
				forwardHandleSet.add( virtualNode.getNode().getLeafSet().get( 1) );
				forwardHandleSet.add( virtualNode.getNode().getLeafSet().get( 2) );						
			}
			else if (updateMessage.leafSetSide == LeafSetSide.CENTER) {
				forwardHandleSet.add( virtualNode.getNode().getLeafSet().get(-1) );
				forwardHandleSet.add( virtualNode.getNode().getLeafSet().get(-2) );						
				forwardHandleSet.add( virtualNode.getNode().getLeafSet().get( 1) );
				forwardHandleSet.add( virtualNode.getNode().getLeafSet().get( 2) );						
			}

			for ( NodeHandle forwardHandle : forwardHandleSet ) {							

				if (forwardHandle != null) {
					VirtualIdUpdateMessage newMessage = new VirtualIdUpdateMessage(updateMessage);
					virtualNode.sendDirectMessage(forwardHandle, newMessage);
				}
			}        			        
		}
	}

	private void processNeighborsetUpdateMessage(VirtualSpace protocolVirtualSpace, VirtualIdUpdateMessage updateMessage) {
		logger.debug("Received Neighborset Update message from " + updateMessage.sourceHandle.getId() + "." );

		int index = updateMessage.virtualIdUpdates.getIndexHandle( virtualNode.getNode().getLocalHandle() );
		if (index >= 0)
			protocolVirtualSpace.setVirtualId( updateMessage.virtualIdUpdates.getNewId(index) );					

		/**
		 * Updates the node neighborset.
		 */
		protocolVirtualSpace.getVirtualNeighborSet().updateNeighborsSet(updateMessage);
		protocolVirtualSpace.getVirtualLeafSet().removeNeighborExtraLeafs(updateMessage);
							
		/**
		 * Marks message for forwarding in the protocol manager node
		 */
		if (updateMessage.toForward == true) { // NEIGHBORSET

			if (updateMessage.leafSetSide == LeafSetSide.LEFT) {
				int leafIndex = -virtualNode.getNode().getLeafSet().ccwSize();
				while (leafIndex < 0) {
					NodeHandle nodeHandle = virtualNode.getNode().getLeafSet().get(leafIndex);
					updateMessage.nodeToForwardSet.add( nodeHandle );
					leafIndex++;
				}
			}
			else if (updateMessage.leafSetSide == LeafSetSide.RIGHT) {
				int leafIndex = virtualNode.getNode().getLeafSet().cwSize();
				while (leafIndex > 0) {
					NodeHandle nodeHandle = virtualNode.getNode().getLeafSet().get(leafIndex);
					updateMessage.nodeToForwardSet.add( nodeHandle );
					leafIndex--;
				}
			}

		}

		/**
		 * Responds to the protocol manager that the neighbor node received the message
		 */
		if ( updateMessage.updateType == UpdateType.NEIGHBORSET ) {
			NodeHandle joiningHandle = updateMessage.sourceHandle;                    
			updateMessage.isResponse = true;
			updateMessage.sourceHandle = endpoint.getLocalNodeHandle();
			messageReceiver.sendResponse(joiningHandle, updateMessage);

			//System.out.println("Response sent at " + virtualNode.getNode().getId() + ".");					
		}
	}

    public boolean forward(RouteMessage routeMessage) {

    	/**
    	 * TODO: Should be at deliver!!!!!
    	 */
    	return virtualIdRouting.forward(routeMessage);
    }

    public void update(NodeHandle handle, boolean joined) {

    	logger.debug("Pastry leafset update received for node " + handle.getId() + " joined=" + joined );

    	/**
    	 * Calls the registered virtual space applications.
    	 */
    	for (VirtualSpace virtualSpace : virtualSpaceList )
    		virtualSpace.getApplication().update(handle, joined);    	

    	/**
    	 * If the immediate ccwNode departed, starts the departure protocol.
    	 */
    	if ( joined == false && ccwNodeId.equals( handle.getId() ) ) {
    	
    		logger.info("Node departure detected at " + virtualNode.getNode().getId() + " for node " + handle.getId() + ". Starting departure protocol...");
    		
    		for (int virtualSpaceNumber=1; virtualSpaceNumber < virtualSpaceList.size(); virtualSpaceNumber ++) {
    			VirtualIdProtocol departureProtocol = 
    				virtualNode.getProtocolManager().createDepartureProtocol( 
    					virtualNode, virtualSpaceList.get(virtualSpaceNumber), ccwNodeId );
    			departureProtocol.addProtocolCompletionObserver( TestNodeManager.getInstance() );
    			departureProtocol.startProtocol( virtualNode.getNode() );
    		}
    	}

    	if (virtualNode.getNode().getLeafSet().get(-1) != null)
    		this.ccwNodeId = virtualNode.getNode().getLeafSet().get(-1).getId();
    	else
    		this.ccwNodeId = virtualNode.getNode().getId();
    	
    	if (virtualNode.getNode().getLeafSet().get(1) != null)
    		this.cwNodeId = virtualNode.getNode().getLeafSet().get(1).getId();
    	else
    		this.cwNodeId = virtualNode.getNode().getId();
    	
    }
}
