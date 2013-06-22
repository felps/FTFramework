package br.usp.ime.virtualId.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.NodeSet;
import rice.pastry.PastryNode;
import rice.pastry.Id.Distance;
import br.usp.ime.virtualId.NodeInformation;
import br.usp.ime.virtualId.VirtualIdUpdates;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;
import br.usp.ime.virtualId.message.VirtualIdCapacityQuery;
import br.usp.ime.virtualId.message.VirtualIdMessage;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage.LeafSetSide;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage.UpdateType;
import br.usp.ime.virtualId.util.DistanceManipulator;
import br.usp.ime.virtualId.util.IdComparator;

public abstract class VirtualIdProtocol implements Observer {

	public enum ProtocolType {JOINING, DEPARTURE, UPDATE};
	ProtocolType protocolType;
	
	/**
	 * Reference to the VirtualNode manager;
	 */
	protected VirtualNode virtualNode;

	/**
	 * Reference to the virtual space corresponding to this protocol;
	 */
	protected VirtualSpace virtualSpace;
	
	/**
	 * 
	 */
    protected VirtualMessageDispatcher messageDispatcher;

    /**
     * Contains the adaptiveId of neighbor CDRMs.
     */
    protected TreeMap <Id, NodeInformation> neighborNodeInfoMap;

    /**
     * Reference to the cdrmLogger
     */
    protected Logger logger;        

    /**
     * Used in the departure protocol.
     */
    protected Id leavingNodeId = null;
    
    protected Id leftmostLeafId = null;
    protected Id rightmostLeafId = null;
    
    protected Id localNodeId = null;

    //----------------------------------------------------------------
    
    protected DistanceManipulator distanceManipulator;
    
    protected HashSet<ProtocolObserver> protocolCompletionObserverSet; 
    
    protected TreeMap<Id, NodeInformation> leftNeighborMap  = null; 
    protected TreeMap<Id, NodeInformation> rightNeighborMap = null;
    
    /**
     * 
     */
    protected boolean leafSetContainsFullRing = true;
    
    protected boolean leafRightNeighborMapsIntersect = false;
        
    /**
     * The number of remaining capacity queries responses
     */
    protected Map<Id, VirtualIdCapacityQuery> remainingCapacityResponseSet;

    /**
     * The set of nodes whose responses did not arrived
     */
    protected Set<Id> remainingUpdateResponseSet;

    /**
     * A set of nodes the protocol knows are not part of the virtual space
     */
    protected Set<Id> nodesNotInVirtualSpace;

    /**
     * The set of for which messages were already sent.
     * Used so that messages are not sent twice to neighbor nodes
     */
    protected Set<Id> updateForwardNodeSet;
    
    private boolean hasFinished = false;

    // ====================================================================
    // Main methods
    // ====================================================================

    public VirtualIdProtocol(VirtualNode virtualNode, VirtualSpace virtualSpace) {
        
    	this.nodesNotInVirtualSpace = new HashSet<Id>();
        this.virtualNode = virtualNode;
        this.virtualSpace = virtualSpace;
        this.localNodeId = virtualNode.getNode().getId();
        this.logger = Logger.getLogger("protocol." + virtualNode.getNode().getId().toString().substring(0, 9) + ">");
        
        //this.adaptiveIdManager = adaptiveIdManager;
        this.distanceManipulator = new DistanceManipulator();    
        this.messageDispatcher = virtualNode.getMessageDispatcher();
        this.protocolCompletionObserverSet = new HashSet<ProtocolObserver>();
    }

    public VirtualMessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}
    
    /** 
     * Starts the CDRM protocol
     */
    public void startProtocol ( PastryNode pastryNode ) {
    	
    	logger.debug("VirtualIdProtocol.startProtocol -> starting virtualId protocol.");
    	
    	while ( pastryNode.isReady() == false ) {
        	try { Thread.sleep( 100 ); }
        	catch (InterruptedException e) {}
    	}
        
    	logger.debug("Node is ready.");
    	
        /**
         * Initializes some class fields 
         */        
        leftmostLeafId  = virtualSpace.getVirtualLeafSet().getLeftmostLeaf().getId();        
        rightmostLeafId = virtualSpace.getVirtualLeafSet().getRightmostLeaf().getId();        
        neighborNodeInfoMap = new TreeMap <Id, NodeInformation>(new IdComparator(leftmostLeafId));
        
        /**
         * Creates the left and right neighbors map.
         * These are used to construct the updates class.
         */
        {        
        	NodeHandle localNodeHandle = virtualNode.getNode().getLocalNodeHandle();
        	//NodeInformation nodeInfo = new NodeInformation(localNodeHandle.getId(), virtualSpace.getVirtualId(), localNodeHandle, virtualSpace.getCapacity() );
        	leftNeighborMap  = new TreeMap<Id, NodeInformation>( new IdComparator( localNodeHandle.getId() ) );        	
        	rightNeighborMap = new TreeMap<Id, NodeInformation>( new IdComparator( localNodeHandle.getId() ) );
        	//leftNeighborMap.put( localNodeHandle.getId(), nodeInfo);
        	//rightNeighborMap.put(localNodeHandle.getId(), nodeInfo);
        }


        /**
         * Allows the derivative protocols to perform initialization actions
         */
        this.protocolStarted();

        /**
         * Checks if the leafset contains the full ring
         */
        int nNeighborsToUse = virtualNode.getNode().getLeafSet().maxSize();        
        NodeSet nodeSet = virtualNode.getNode().getLeafSet().neighborSet(nNeighborsToUse+1);
        leafSetContainsFullRing = false;
        if ( nodeSet.size() < nNeighborsToUse+1 && protocolType != ProtocolType.DEPARTURE ) 
        	leafSetContainsFullRing = true;

        /**
         * Gets the current capacity of nodes in the leafSet
         */
        remainingCapacityResponseSet = new HashMap<Id, VirtualIdCapacityQuery>();        
        for ( int i=1; i <  nodeSet.size(); i++) {
                    	
            VirtualIdCapacityQuery capacityQueryMessage = 
            	new VirtualIdCapacityQuery(virtualNode.getNode().getLocalNodeHandle(), 
            			virtualSpace.getVirtualSpaceNumber(), virtualNode.getNextMessageNumber());

            if (IdComparator.isBetween(leftmostLeafId, nodeSet.get(i).getId(), localNodeId))
            	capacityQueryMessage.leafSetSide = LeafSetSide.LEFT;
            else 
            	capacityQueryMessage.leafSetSide = LeafSetSide.RIGHT;
            
            remainingCapacityResponseSet.put( nodeSet.get(i).getId(), capacityQueryMessage );
            messageDispatcher.sendDirectMessage(nodeSet.get(i), capacityQueryMessage, true, this);            
        }       
                
                
        /**
         * The first node to join the network does not query neighbors for their capacity (*)
         */
        if (remainingCapacityResponseSet.size() == 0) {

        	this.protocolFinished(null);

           // Adds the nodeId to neighbor table
           Node node = virtualNode.getNode();
           VirtualIdUpdates updates = new VirtualIdUpdates();
           VirtualIdUpdateMessage updateMessage = new VirtualIdUpdateMessage(virtualNode.getNode().getLocalHandle(), virtualSpace.getVirtualSpaceNumber(), -1);
           updateMessage.leftUpdateIndex  = 0;
           updateMessage.rightUpdateIndex = 0;
           updateMessage.virtualIdUpdates = updates;
           updates.addUpdate(node.getId(), node.getId(), node.getLocalNodeHandle());
           virtualSpace.getVirtualNeighborSet().setNeighborSet(updateMessage);
           virtualSpace.getVirtualLeafSet().setUpdated();
           
           notifyProtocolCompleted( );

        }
        
    }

    /**
     * Adds information about the received capcity query response.
     * After all neighbor Cdrms responded, start the distribution of the adaptiveId range
     * and sends updates messages to affected nodes. 
     * 
     * @param capacityQuery the received message
     */
    public synchronized void setNeighborNodeInformation( VirtualIdCapacityQuery capacityQuery ) {

        Id messageSourceId = capacityQuery.sourceHandle.getId();
        if ( remainingCapacityResponseSet.containsKey( messageSourceId ) == false)
            return;
        
        if (capacityQuery.nodeInformationArray != null) {
        	       
        	/**
        	 * Add nodes to left and right neighbor maps
        	 */
        	int nNodeInfo = capacityQuery.nodeInformationArray.length;        
        	int sourceNodeIndex = -1;
        	for (sourceNodeIndex=0; sourceNodeIndex < nNodeInfo; sourceNodeIndex++)
        		if ( capacityQuery.sourceHandle.getId().equals( capacityQuery.nodeInformationArray[sourceNodeIndex].getNodeId() ))
        			break;    	

        	if (capacityQuery.leafSetSide == LeafSetSide.LEFT && nNodeInfo > 0)
        		for (int i=0; i<=sourceNodeIndex; i++) {
        			NodeInformation nodeInfo = capacityQuery.nodeInformationArray[i];
        			leftNeighborMap.put(nodeInfo.getNodeId(), nodeInfo);
        		}        
        	else if (capacityQuery.leafSetSide == LeafSetSide.RIGHT && nNodeInfo > 0)
        		for (int i=sourceNodeIndex; i<nNodeInfo; i++) {
        			NodeInformation nodeInfo = capacityQuery.nodeInformationArray[i];
        			rightNeighborMap.put(nodeInfo.getNodeId(), nodeInfo);
        		}        	        

        	/**
        	 * Puts the received Cdrm information into the neighbors map
        	 */
        	NodeInformation cdrmInfo = new NodeInformation ( 
        			capacityQuery.sourceHandle.getId(), capacityQuery.virtualId, 
        			capacityQuery.sourceHandle, capacityQuery.capacity );
        	neighborNodeInfoMap.put(capacityQuery.sourceHandle.getId(), cdrmInfo);
        }
        else
        	nodesNotInVirtualSpace.add(messageSourceId);
    
        remainingCapacityResponseSet.remove(messageSourceId);

        //System.out.println("update from" + capacityQuery.sourceHandle.getId()  + " remainining:" + remainingCapacityResponseSet.size());
        if (capacityQuery.nodeInformationArray != null)
        	logger.debug( "Received capacity info from " + capacityQuery.sourceHandle.getId() + ". " + (remainingCapacityResponseSet.size()) + " updates remaining." );
        else
        	logger.debug( "Received response from " + capacityQuery.sourceHandle.getId() + " not in the virtual space. " + (remainingCapacityResponseSet.size()) + " updates remaining." );
        
        if (remainingCapacityResponseSet.size() == 0) {

            /**
             * The first node to join the network that is part of the virtual space (*)
             */
        	if ( leftNeighborMap.size() == 0 ) {
            	this.protocolFinished(null);

               // Adds the nodeId to neighbor table
               Node node = virtualNode.getNode();
               VirtualIdUpdates updates = new VirtualIdUpdates();
               VirtualIdUpdateMessage updateMessage = new VirtualIdUpdateMessage(virtualNode.getNode().getLocalHandle(), virtualSpace.getVirtualSpaceNumber(), -1);
               updateMessage.leftUpdateIndex  = 0;
               updateMessage.rightUpdateIndex = 0;
               updateMessage.virtualIdUpdates = updates;
               updates.addUpdate(node.getId(), node.getId(), node.getLocalNodeHandle());
               virtualSpace.getVirtualNeighborSet().setNeighborSet(updateMessage);
               virtualSpace.getVirtualLeafSet().setUpdated();
               
               notifyProtocolCompleted( );
               return;
            }

        	if ( rightNeighborMap.containsKey( leftNeighborMap.firstKey() ) || leafSetContainsFullRing )
        		leafRightNeighborMapsIntersect = true;
        	
        	/**
        	 * Allows protocols to perform protocol specific actions
        	 */
            this.collectedNeighborInformation();
            
            this.distributeVirtualIdRange();            
        }

    }

    /**
     * Called when a response from a adaptiveId update message is received
     */
    synchronized public void setUpdateResponseReceived ( VirtualIdUpdateMessage updateMessage ) {       

        Id messageSourceId = updateMessage.sourceHandle.getId();
       
        if ( remainingUpdateResponseSet.contains( messageSourceId ) == false)
            return;
        
    	remainingUpdateResponseSet.remove(messageSourceId);

        logger.debug("Received neighborset update response from " + updateMessage.sourceHandle.getId()  + " remainining:" + remainingUpdateResponseSet.size());        

    	if (updateMessage.toForward == true) {

    		for (Iterator<NodeHandle> forwardSetIterator = updateMessage.nodeToForwardSet.iterator(); forwardSetIterator.hasNext();) {
    			
    			NodeHandle nodeHandle = (NodeHandle)forwardSetIterator.next();
    			forwardSetIterator.remove();
    		        		
    			if ( nodeHandle != null && nodeHandle.getId().equals( leavingNodeId ) == false && 
    				updateForwardNodeSet.contains( nodeHandle.getId() ) == false &&
    				nodesNotInVirtualSpace.contains( nodeHandle.getId() ) == false ) {
    		
    				updateForwardNodeSet.add(nodeHandle.getId());
    				remainingUpdateResponseSet.add(nodeHandle.getId());
    				VirtualIdUpdateMessage newUpdateMessage = new VirtualIdUpdateMessage(updateMessage);
    				newUpdateMessage.toForward = false;
    				newUpdateMessage.isResponse = false;
    				newUpdateMessage.sourceHandle = virtualNode.getNode().getLocalHandle();
    				newUpdateMessage.timeToLive = VirtualIdMessage.initialTimeToLive;
    				messageDispatcher.sendDirectMessage(nodeHandle, newUpdateMessage, true, this);

    				logger.debug("Added extra node " + nodeHandle.getId()  + " to update list. Sending message. Remainining:" + remainingUpdateResponseSet.size());        
    			}    	    	
    		}    		
    	}
        
        /**
         * Received responses from all virtual neighbor nodes.
         * Sends update message to node with virtual id = this.nodeId.
         * Message is routed in the original id space. 
         */
        if (remainingUpdateResponseSet.size() == 0) {    		
                            	
        	VirtualIdUpdateMessage leafsetMessage = new VirtualIdUpdateMessage(
        			virtualNode.getNode().getLocalNodeHandle(), updateMessage.protocolSpaceNumber,
        			virtualNode.getNextMessageNumber());        
        	leafsetMessage.updateType = UpdateType.LEAFSET;
            leafsetMessage.leafSetSide = LeafSetSide.CENTER;
            leafsetMessage.virtualIdUpdates = updateMessage.virtualIdUpdates;
            leafsetMessage.toForward = true;
            leafsetMessage.routingVirtualSpaceNumber = 0;
            leafsetMessage.needResponse = false;
            leafsetMessage.updatesOverlap = updateMessage.updatesOverlap;
            leafsetMessage.leftUpdateIndex  = updateMessage.leftUpdateIndex;
            leafsetMessage.rightUpdateIndex = updateMessage.rightUpdateIndex; 
            leafsetMessage.protocolType = updateMessage.protocolType;
            
            this.protocolFinished(leafsetMessage);            
            
            logger.debug("Sending leafSet update messages to " + virtualSpace.getVirtualId() + ".\n");
            
            /**
             * Sending message for updating of remote node leafSet
             */
            messageDispatcher.routeMessage(virtualSpace.getVirtualId(), leafsetMessage);
        
            /**
             * Updates the current node LeafSet
             */
            virtualSpace.getVirtualLeafSet().updateLeafSet(leafsetMessage);

    		notifyProtocolCompleted( );
        }
    }

    // ====================================================================
    // Private methods
    // ====================================================================

    /**
     * Distributes the virtualId range between the node neighbors
     */
    private void distributeVirtualIdRange() {

    	//messageDispatcher.clear();

    	NodeInformation[] nodeInfoArray =  new NodeInformation[neighborNodeInfoMap.size()];
    	neighborNodeInfoMap.values().toArray(nodeInfoArray);

    	leftNeighborMap.remove( localNodeId );
    	NodeInformation[] leftNeighborArray =  new NodeInformation[leftNeighborMap.size()];
    	leftNeighborMap.values().toArray(leftNeighborArray);

    	rightNeighborMap.remove( localNodeId );
    	NodeInformation[] rightNeighborArray =  new NodeInformation[rightNeighborMap.size()];
    	rightNeighborMap.values().toArray(rightNeighborArray);

    	VirtualIdUpdates updates = new VirtualIdUpdates();
    	updates.setWrappingUpdates( leafRightNeighborMapsIntersect );
    	for (int i=0; i<leftNeighborArray.length; i++)
    		if ( neighborNodeInfoMap.containsKey( leftNeighborArray[i].getNodeId() ) == false )
    			updates.addUpdate(leftNeighborArray[i].getVirtualId(), leftNeighborArray[i].getVirtualId(), leftNeighborArray[i].getNodeHandle());    		
    	int leftUpdateIndex = updates.numberOfUpdates();

    	/**
    	 * The number of nodes is smaller than the leafSetSize
    	 * In this case, divides redistributes the entire idSpace among the nodes 
    	 */
    	if ( leafSetContainsFullRing ) {

    		logger.debug ("LeafSet contains all the nodes in the ring.");

    		double meanCapacity = 0;
    		for (int i=0; i<nodeInfoArray.length; i++) {
    			NodeInformation nodeInfo = nodeInfoArray[i];           
    			meanCapacity += nodeInfo.getCapacity();
    		}
    		meanCapacity /= nodeInfoArray.length;           

    		long totalAdaptiveRangeId = DistanceManipulator.MAX_DISTANCE;
    		long meanAdaptiveRangeId  = totalAdaptiveRangeId/nodeInfoArray.length;

    		/**
    		 * Evaluates the new adaptiveIds and puts into the message.
    		 * The first element in the neighbors map maintains its adaptiveId.
    		 */
    		long longOffset = 0;
    		NodeInformation nodeInfo = nodeInfoArray[0];
    		updates.addUpdate(nodeInfoArray[0].getVirtualId(), nodeInfoArray[0].getVirtualId(), nodeInfoArray[0].getNodeHandle());
    		Id baseId = nodeInfoArray[0].getVirtualId();
    		for (int i=1; i<nodeInfoArray.length; i++) {
    			nodeInfo = nodeInfoArray[i];
    			longOffset += (long)( (nodeInfo.getCapacity()/meanCapacity) * meanAdaptiveRangeId );
    			Id newId = baseId.addToId(DistanceManipulator.convertLongtoDistance(longOffset));                   
    			updates.addUpdate(nodeInfo.getVirtualId(), newId, nodeInfo.getNodeHandle());
    		}

    	}
    	else {

    		logger.debug ("LeafSet contains part of the ring.");

    		Id leftId  = neighborNodeInfoMap.get(neighborNodeInfoMap.firstKey()).getVirtualId();
    		Id rightId = neighborNodeInfoMap.get(neighborNodeInfoMap.lastKey()).getVirtualId();

    		Distance rangeIdDistance;
    		if (leftId.clockwise(rightId)) rangeIdDistance = (Distance)leftId.distanceFromId(rightId);
    		else rangeIdDistance = (Distance)leftId.longDistanceFromId(rightId);               
    		long totalAdaptiveRangeId = DistanceManipulator.convertDistanceToLong(rangeIdDistance);
    		long meanAdaptiveRangeId = totalAdaptiveRangeId / (nodeInfoArray.length-1);

    		double meanCapacity = 0;
    		for (int i=1; i<nodeInfoArray.length; i++) {
    			NodeInformation nodeInfo = nodeInfoArray[i];           
    			meanCapacity += nodeInfo.getCapacity();
    		}
    		meanCapacity /= (nodeInfoArray.length-1);

    		/**
    		 * Evaluates the new adaptiveIds and puts into the message.
    		 * The first and last elements in the neighbors map maintains its adaptiveId.
    		 */
    		long longOffset = 0;
    		updates.addUpdate(nodeInfoArray[0].getVirtualId(), nodeInfoArray[0].getVirtualId(), nodeInfoArray[0].getNodeHandle());
    		for (int i=1; i<nodeInfoArray.length-1; i++) {
    			NodeInformation nodeInfo = nodeInfoArray[i];
    			longOffset += (long)( (nodeInfo.getCapacity()/meanCapacity) * meanAdaptiveRangeId );
    			Id newVirtualId = leftId.addToId(DistanceManipulator.convertLongtoDistance(longOffset));                   
    			updates.addUpdate(nodeInfo.getVirtualId(), newVirtualId, nodeInfo.getNodeHandle());
    		}
    		updates.addUpdate(nodeInfoArray[nodeInfoArray.length-1].getVirtualId(), nodeInfoArray[nodeInfoArray.length-1].getVirtualId(), nodeInfoArray[nodeInfoArray.length-1].getNodeHandle());

    	}
    	
    	int rightUpdateIndex = updates.numberOfUpdates()-1;

    	for (int i=0; i<rightNeighborArray.length; i++)
    		if ( neighborNodeInfoMap.containsKey( rightNeighborArray[i].getNodeId() ) == false )
    			if ( leftNeighborMap.containsKey( rightNeighborArray[i].getNodeId() ) == false )
    				updates.addUpdate(rightNeighborArray[i].getVirtualId(), rightNeighborArray[i].getVirtualId(), rightNeighborArray[i].getNodeHandle());    		

        logger.debug(updates);

    	/**
    	 * Creates the update message
    	 */
    	VirtualIdUpdateMessage message =
    		new VirtualIdUpdateMessage(virtualNode.getNode().getLocalNodeHandle(), 
    				virtualSpace.getVirtualSpaceNumber(), virtualNode.getNextMessageNumber() );
    	message.virtualIdUpdates = updates;
    	message.leftUpdateIndex = leftUpdateIndex;
    	message.rightUpdateIndex = rightUpdateIndex;
    	if (leafRightNeighborMapsIntersect) message.updatesOverlap = true;
        
    	/**
    	 * 
    	 */
    	this.updateMessageReady( message );

    	/**
    	 * Notifies the nodes about the nodeIdChanges
    	 */
    	remainingUpdateResponseSet = new HashSet<Id>();            
    	updateForwardNodeSet = new HashSet<Id>();
    	updateForwardNodeSet.add(virtualNode.getNode().getId());
        LeafSetSide leafSetSide = LeafSetSide.LEFT;

    	for (NodeInformation nodeInfo : neighborNodeInfoMap.values()) {
    		NodeHandle targetHandle = nodeInfo.getNodeHandle();
    		VirtualIdUpdateMessage tempMessage = new VirtualIdUpdateMessage(message);
            tempMessage.leafSetSide = leafSetSide;
            if (leafSetContainsFullRing == false)
            	tempMessage.toForward = true;
                        
            //System.out.println( "Message marked to forward:" + tempMessage.toForward);
            
    		if (targetHandle != virtualNode.getNode().getLocalNodeHandle()) {
    	    	updateForwardNodeSet.add(targetHandle.getId());
    			remainingUpdateResponseSet.add(targetHandle.getId());
    			messageDispatcher.sendDirectMessage(targetHandle, tempMessage, true, this);
    		}
            else {
                leafSetSide = LeafSetSide.RIGHT;
            }
    	}

    }  

    // ====================================================================
    // Abstract methods
    // ====================================================================

    /**
     * A virtualId protocol has started.
     * Called after setting the CdrmProtocol main attributes.
     */    
    protected abstract void protocolStarted();

    /**
     * Received response from all neighbor nodes capacity queries
     */    
    protected abstract void collectedNeighborInformation();

    /**
     * Called before sending update messages to other nodes.
     */    
    protected abstract void updateMessageReady(VirtualIdUpdateMessage message);

    /**
     * Called after receiving all responses from the update messages
     */    
    protected abstract void protocolFinished(VirtualIdUpdateMessage leafsetMessage);

    
    // ====================================================================
    // Observer interface
    // ====================================================================
    
    /**
     * From java.util.Observer Interface.
     * Is called when a pastry node is ready and triggers the protocol start.
     */
    public void update(Observable observable, Object arg) {        
       		
    		if (observable instanceof PastryNode) {
    			PastryNode pastryNode = (PastryNode) observable;
    			if (pastryNode.isReady()) {        				
    	    		System.out.println("Node is ready. Starting Protocol at node " + pastryNode.getId() + ".");
    				this.startProtocol( pastryNode );
    			}
    		}    
    }    

    /**
     * Treats error cases where CDRMs could not be reached
     */
    public void setMessageNotDelivered(NodeHandle targetHandle) {

//    	System.out.println("VirtualIdProtcol.setMessage not delivered to " + targetHandle.getId() + ".");
//    	remainingCapacityResponseSet.remove( targetHandle.getId() );
//    	
//        if (remainingCapacityResponseSet.size() == 0) {        
//        	if ( rightNeighborMap.containsKey( leftNeighborMap.firstKey() ) || leafSetContainsFullRing )
//        		leafRightNeighborMapsIntersect = true;
//        
//        	/**
//        	 * Allows protocols to perform protocol specific actions
//        	 */
//            this.collectedNeighborInformation();
//            
//            this.distributeVirtualIdRange();            
//        }
        
    }

    // ====================================================================
    // Protocol finishing notifying
    // ====================================================================

    /**
     * Classes can register to be notified when a protocol finishes.
     * 
     * @param observer
     */
    public void addProtocolCompletionObserver( ProtocolObserver observer ) {
    	
    	synchronized ( protocolCompletionObserverSet ) {
    		if (hasFinished == false)
    			protocolCompletionObserverSet.add(observer);
    		else
    			observer.notifyProtocolFinished( virtualSpace.getVirtualSpaceNumber() );
		}
    }
    
    /**
     * 
     */
    protected void notifyProtocolCompleted() {
    	    	
    	synchronized ( protocolCompletionObserverSet ) {
    		hasFinished = true;
    		for (ProtocolObserver observer : protocolCompletionObserverSet )
    			if (observer != null)
    				observer.notifyProtocolFinished( virtualSpace.getVirtualSpaceNumber() );
		}
    }
    
}
