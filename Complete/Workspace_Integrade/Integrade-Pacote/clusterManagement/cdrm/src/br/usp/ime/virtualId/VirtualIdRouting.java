package br.usp.ime.virtualId;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.commonapi.rawserialization.MessageDeserializer;
import rice.pastry.leafset.LeafSet;
import br.usp.ime.virtualId.message.VirtualIdMessage;

public class VirtualIdRouting {

	List<VirtualSpace> virtualSpaceList;
	
	MessageDeserializer messageDeserializer;
	
	VirtualNode virtualNode;
	
	private Logger logger;

	public VirtualIdRouting (VirtualNode virtualNode, MessageDeserializer md, List<VirtualSpace> virtualSpaceList) {
		
		this.messageDeserializer = md;
		this.virtualSpaceList = virtualSpaceList;
		this.virtualNode = virtualNode;
		this.logger = Logger.getLogger("routing." + virtualNode.getNode().getId().toString().substring(0, 9) + ">");
	}
	
	public boolean forward (RouteMessage routeMessage) {

        try {
        	 
        	// maybe we should only reroute nonResponse messages
        	
            Message message = routeMessage.getMessage(messageDeserializer);
            if ( message instanceof VirtualIdMessage ) {
                VirtualIdMessage virtualIdMessage = (VirtualIdMessage)message;            
                
                if (virtualIdMessage.timeToLive-- == 0) {
                    virtualNode.getLogger().info("CdrmApp::forward -> Time to live expired, dropping message.");
                    return false;
                }

                /**
                 * This node is not registered in virtual space 'virtualIdMessage.routingVirtualSpaceNumber' 
                 */
                if ( virtualIdMessage.routingVirtualSpaceNumber >= virtualSpaceList.size() ) {
                	if ( routeMessage.getNextHopHandle().getId().equals( virtualNode.getNode().getId() ) )
                		routeMessage.setNextHopHandle( virtualNode.getNode().getLeafSet().get(1) );
                		logger.debug( "Forwarding message from node " + virtualIdMessage.sourceHandle.getId() + " to node " + routeMessage.getNextHopHandle().getId() + ".");
                	return true;
                }

                VirtualSpace virtualSpace = virtualSpaceList.get(virtualIdMessage.routingVirtualSpaceNumber);
                               
                if ( virtualIdMessage.isResponse == false && virtualIdMessage.routingVirtualSpaceNumber > 0) {
                    logger.debug( "Routing message from node " + virtualIdMessage.sourceHandle.getId() + " with messageId " + virtualIdMessage.messageVirtualId + " nextHop:" + routeMessage.getNextHopHandle().getId() + " vSpaceNumber:" + virtualIdMessage.routingVirtualSpaceNumber);
                    updateRouteMessage(routeMessage, virtualIdMessage.messageVirtualId, virtualSpace, virtualIdMessage);                    
                }

                return virtualSpace.getApplication().forward(routeMessage);
            }
            else {
            	
                logger.error("VirtualApplication -> ERROR: Message is not of type VirtualIdMessage!");
                return false;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
	
	}
	
	/**
	 * 
	 */
    private void updateRouteMessage(RouteMessage routeMessage, Id messageVirtualId, VirtualSpace virtualSpace, VirtualIdMessage virtualIdMessage) {
        
    	if (messageVirtualId == null) return;
    	
    	NodeHandle leafTargetHandle = virtualSpace.getVirtualLeafSet().getTargetHandle(messageVirtualId);
    	NodeHandle neighborTargetHandle = virtualSpace.getVirtualNeighborSet().getTargetHandle(messageVirtualId);
    	
    	Id neighborTargetId = virtualSpace.getVirtualNeighborSet().getTargetVirtualId(messageVirtualId);
    	
        /**
         * Check if this is the target node for message delivery and changes routing destination
         */
        if ( neighborTargetHandle != null && neighborTargetId.equals ( virtualSpace.getVirtualId() ) ) {
            
        	logger.debug("Delivering message to target node. vspace:" + virtualSpace.getVirtualSpaceNumber() + ": messageId:" + messageVirtualId + " targetId:" + neighborTargetId + " nodeId:" + virtualNode.getNode().getId() + ".");    		
        	
            routeMessage.setNextHopHandle(virtualNode.getNode().getLocalNodeHandle());                        
            routeMessage.setDestinationId(virtualNode.getNode().getId());
        }
        
        /**
         * This is not the target node. Will try to find the target node in the virtual id sets.
         */
        else {

        	Id leafTargetId = virtualSpace.getVirtualLeafSet().getTargetVirtualId(messageVirtualId);
        	
            /**
             * Found target in the virtual id sets.
             * The message is destination is changed to the node covering the target adaptiveId range. 
             */
            if ( neighborTargetHandle != null ) {

            	logger.debug("Redirecting message to node from virtual Neighborset: vspace:" + virtualSpace.getVirtualSpaceNumber() + ": messageId:" + messageVirtualId + " targetId:" + neighborTargetId + " nextHopId:" + neighborTargetHandle.getId() + ".");    		

                routeMessage.setNextHopHandle(neighborTargetHandle);                        
                routeMessage.setDestinationId(neighborTargetHandle.getId());                            	
            }
            else if ( leafTargetHandle != null ) { 

            	logger.debug("Redirecting message to node from virtual Leafset: vspace:" + virtualSpace.getVirtualSpaceNumber() + ": messageId:" + messageVirtualId + " targetId:" + leafTargetId + " nextHopId:" + leafTargetHandle.getId() + ".");    		

            	routeMessage.setNextHopHandle(leafTargetHandle);                        
                routeMessage.setDestinationId(leafTargetHandle.getId());// TODO: Error might be here
            }
            
            /**
             * The message will be delievered to this node, but the target virtual node is not in the table.
             */
            else if ( virtualNode.getNode().getId().equals( routeMessage.getNextHopHandle().getId() ) ) {
                
                logger.warn("Message with targetId " + messageVirtualId + " not found in table at " + virtualNode.getNode().getId() + " with TTL " + virtualIdMessage.timeToLive + ". Forwarding...");
                logger.debug("NeighborSet: " + virtualSpace.getVirtualNeighborSet());
                logger.debug("LeafSet:     " + virtualSpace.getVirtualLeafSet());
                
                NodeHandle nextNodeHandle;
                LeafSet leafSet = virtualNode.getNode().getLeafSet();
                if ( virtualSpace.getVirtualId().clockwise( messageVirtualId ) )
                    nextNodeHandle = leafSet.get(  leafSet.cwSize() );
                else
                    nextNodeHandle = leafSet.get( -leafSet.ccwSize() );
                
                routeMessage.setNextHopHandle(nextNodeHandle);                        
                routeMessage.setDestinationId(nextNodeHandle.getId());
                
            }
        }
    }

}
