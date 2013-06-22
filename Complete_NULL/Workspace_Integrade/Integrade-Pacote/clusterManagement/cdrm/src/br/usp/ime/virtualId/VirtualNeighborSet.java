package br.usp.ime.virtualId;

import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.leafset.LeafSet;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage;
import br.usp.ime.virtualId.protocol.VirtualIdProtocol.ProtocolType;
import br.usp.ime.virtualId.util.IdComparator;

public class VirtualNeighborSet extends VirtualIdSet {
    
    private int maxNeighbors;
    private int virtualSpaceNumber;
    private boolean isReady = false;
    
    private NodeInformation extraLeftNode = null;
    private NodeInformation extraRightNode = null;
           
    public VirtualNeighborSet(int maxNeighbors, int virtualSpaceNumber, VirtualNode virtualNode) {
    	super(virtualNode);
        this.maxNeighbors = maxNeighbors;
        this.virtualSpaceNumber = virtualSpaceNumber;
        this.logger = Logger.getLogger("neighborset." + virtualNode.getNode().getId().toString().substring(0, 9) + ">");
    }

    public boolean isReady() { return isReady; }
    
    synchronized public boolean removeNode( Id virtualId ) {
    	
    	if ( virtualIdSet.remove( virtualId ) != null ) {
    		this.lastUpdateTime = System.currentTimeMillis();
    		return true;
    	}
    	else
    		return false;
    }
    
    synchronized public void setNeighborSet (VirtualIdUpdateMessage updateMessage) {
    	
    	VirtualIdUpdates updates = updateMessage.virtualIdUpdates;    	
    	this.lastUpdateTime = System.currentTimeMillis();
    	
        Id newBaseId = updates.getNewId( 0 );
        TreeMap <Id, NodeInformation> newNeighborMap =
            new TreeMap <Id, NodeInformation> ( new IdComparator( newBaseId ) );
        virtualIdSet = newNeighborMap;

        /**
         * Updates the adaptiveId of neighbor set.
         */
        for (int i = updateMessage.leftUpdateIndex; i <= updateMessage.rightUpdateIndex; i++) {
            
            NodeHandle nodeHandle = updates.getNodeHandle(i);
            Id virtualId = updates.getNewId(i);            
            NodeInformation nodeInfo = new NodeInformation (nodeHandle.getId(), virtualId, nodeHandle, 0);
            virtualIdSet.put(virtualId, nodeInfo);            
        }        
    
        /**
         * Check if every element in leafset is in updates 
         */
        LeafSet leafset = virtualNode.getNode().getLeafSet();
        assert ( updates.getIndexHandle( leafset.get(0) ) >= 0 );
//        for (int i=1; i<=leafset.cwSize(); i++) 
//        	assert ( updates.getIndexHandle( leafset.get(i) ) >= 0 );
//        for (int i=1; i<=leafset.ccwSize(); i++)
//        	assert ( updates.getIndexHandle( leafset.get(-i) ) >= 0 );
        
        logger.info("Set virtual neighborset completed.");
        logger.debug(this);
        
        isReady = true;
    }
        
    /**
     * Updates the neighborSet and returns the address of the node to forward the message or null
     * if the message should not be forwarded.
     * @param message
     */
    synchronized public void updateNeighborsSet(VirtualIdUpdateMessage message) {
    	
    	this.lastUpdateTime = System.currentTimeMillis();    	
    	
    	logger.info("Updating neighborset using updates received for " + message.protocolType + " of " + message.updatingNodeId + " at " + message.leafSetSide + " and protocol.");
    	logger.debug("LEAFSET\n" + virtualNode.getNode().getLeafSet().toString());
    	logger.debug("BEFORE: " + this );
    	
    	findNewBaseId(message);    	        
    	TreeSet<Id> tempLeafSet = createTreeLeafIdSet();   	
    	pruneNeighborSet(message, tempLeafSet);
    	
    	logger.debug("PRUNED: " + this );
    	
        performUpdates(message, tempLeafSet);                       
        
        logger.debug("UPDATED: " + this );
        
        /**
         * Set the extra nodes
         */
        NodeInformation leftNodeInfo = virtualIdSet.get( virtualIdSet.firstKey() );
        int leftNodeIndex = message.virtualIdUpdates.getIndexHandle( leftNodeInfo.getNodeHandle() );
        //this.extraLeftNode = null;
        if (message.leftUpdateIndex < leftNodeIndex) {
        	NodeHandle nodeHandle = message.virtualIdUpdates.getNodeHandle( leftNodeIndex-1 );
        	Id leftVirtualId = message.virtualIdUpdates.getNewId(leftNodeIndex-1);
        	this.extraLeftNode = new NodeInformation(nodeHandle.getId(), leftVirtualId, nodeHandle, 1.0);
        }
        
        NodeInformation rightNodeInfo = virtualIdSet.get( virtualIdSet.lastKey() );
        int rightNodeIndex = message.virtualIdUpdates.getIndexHandle( rightNodeInfo.getNodeHandle() );
        //this.extraRightNode = null;
        if ( rightNodeIndex >= 0 && rightNodeIndex < message.rightUpdateIndex ) {
        	NodeHandle nodeHandle = message.virtualIdUpdates.getNodeHandle( rightNodeIndex+1 );
        	Id rightVirtualId = message.virtualIdUpdates.getNewId( rightNodeIndex+1 );
        	this.extraRightNode = new NodeInformation(nodeHandle.getId(), rightVirtualId, nodeHandle, 1.0);
        }        	      

        int uniqueCount = virtualNode.getNode().getLeafSet().getUniqueCount();
        if ( virtualIdSet.size() < uniqueCount  && extraRightNode != null ) {
        	logger.debug( "Adding extraRightNode: " + this.extraRightNode.getNodeId() + " to neighborSet.");
        	virtualIdSet.put(extraRightNode.getVirtualId(), extraRightNode);
        }

        logger.debug( "AFTER: " + this );
    	if (this.extraLeftNode != null)
    		logger.debug( "extraLeftNode: " + this.extraLeftNode.getNodeId());
    	if (this.extraRightNode != null)
    		logger.debug( "extraRightNode: " + this.extraRightNode.getNodeId());
    	
    	if (uniqueCount != virtualIdSet.size()) {
        	logger.debug("Leafset uniqueCount: " + uniqueCount);
        	System.out.println("WARNING!!!!!!!!!!!! The neighborSet contains less elements than the leafset (" + uniqueCount + " of " + virtualIdSet.size() + ").");
    		//assert (false);
    	}
    	
    	logger.debug("\n");	
    }

	/**
	 * Creates a set containing the nodeId of all nodes in the leafset 
	 */
	private TreeSet<Id> createTreeLeafIdSet() {

		TreeSet<Id> tempLeafSet = new TreeSet <Id> ( );

    	LeafSet leafset = virtualNode.getNode().getLeafSet();
    	tempLeafSet.add( leafset.get(0).getId() );
        for (int i=1; i<=leafset.cwSize(); i++) 
        	tempLeafSet.add( leafset.get(i).getId() );
        for (int i=1; i<=leafset.ccwSize(); i++)
        	tempLeafSet.add( leafset.get(-i).getId() );
        
        assert (tempLeafSet.size() == leafset.getUniqueCount());
        
		return tempLeafSet;
	}

    /**
     * Removes the elements from the virtualNeighborSet which are not in the node leafset
     */
    private void pruneNeighborSet(VirtualIdUpdateMessage message, TreeSet<Id> tempLeafSet) {

    	NodeInformation[] nodeInfos = this.getNodeInformationArray();
    	for (NodeInformation nodeInfo : nodeInfos) {
    		if ( tempLeafSet.contains( nodeInfo.getNodeId() ) == false )
    			virtualIdSet.remove( nodeInfo.getVirtualId() );
    		if ( message.protocolType == ProtocolType.DEPARTURE && message.updatingNodeId.equals( nodeInfo.getNodeId() ) )
    			virtualIdSet.remove( nodeInfo.getVirtualId() );
    	}
   
    }
    
	/**
	 * Finds the if the updates will change the baseId of the set.
	 * If it the baseId changes, create a new treeSet with the new baseId
	 */
    private Id findNewBaseId(VirtualIdUpdateMessage message) {
    	   	
    	Id oldBaseId = null;     	
    	if (virtualIdSet.comparator() != null)
    		oldBaseId = ((IdComparator)virtualIdSet.comparator()).getBaseId();
    	
    	//logger.debug("findNewBaseId -> leftmostHandle: " + leftmostHandle );
    	
    	Id newBaseId = null;
    	
    	NodeHandle leftmostHandle = this.getLeftmostLeaf();
    	if (message.protocolType != ProtocolType.DEPARTURE || leftmostHandle.getId().equals( message.updatingNodeId ) == false)
    		newBaseId = getBaseIdVirtualValue(message, leftmostHandle);
    	    	
    	for (int offset = 1; newBaseId == null; offset++)
    		newBaseId = getBaseIdVirtualValue(message, this.getLeftmostLeafWithOffset( offset ));
    		
    	assert (newBaseId != null);
    	
    	/**
    	 * Changes the baseId of the neighborSet
    	 */
    	if ( oldBaseId == null || oldBaseId.equals( newBaseId ) == false ) {
    		TreeMap <Id, NodeInformation> newNeighborMap =
    			new TreeMap <Id, NodeInformation> ( new IdComparator( newBaseId ) );
    		newNeighborMap.putAll(this.virtualIdSet);
    		this.virtualIdSet = newNeighborMap;
    	}
        
    	
    	logger.debug("findNewBaseId -> newBaseId: " + newBaseId );
    	
    	return newBaseId;
    }

	private Id getBaseIdVirtualValue(VirtualIdUpdateMessage message, NodeHandle leftmostHandle) {

		Id newBaseId = null;
		int updateIndex = message.virtualIdUpdates.getIndexHandle(leftmostHandle);

    	if (message.leftUpdateIndex <= updateIndex && updateIndex <= message.rightUpdateIndex)
    		newBaseId = message.virtualIdUpdates.getNewId( updateIndex );

    	/**
    	 * Checks if the value is in the virtualIdSet
    	 */
		if (newBaseId == null) {
    		for (NodeInformation nodeInfo : this.virtualIdSet.values())
    			if ( nodeInfo.getNodeId().equals( leftmostHandle.getId() ) == true ) {
    				newBaseId = nodeInfo.getVirtualId();
    				break;
    			}
    	}

    	/**
    	 * Checks if the value is in the stored extra leaf
    	 */
		if (newBaseId == null && this.extraLeftNode != null) {
			if ( leftmostHandle.getId().equals( this.extraLeftNode.getNodeId() ) ) {
				newBaseId = extraLeftNode.getVirtualId();
				virtualIdSet.put(extraLeftNode.getVirtualId(), extraLeftNode);
			}
		}
		return newBaseId;
	}
    

    /**
     * Update the adaptiveIds of other CDRMs and adds the new CDRM to the list
     * 
     * @param message
     * @return
     */
    private void performUpdates(VirtualIdUpdateMessage message, TreeSet<Id> tempLeafSet) {
        
        //System.out.println("Updating neighbors at " + virtualNode.getNode().getId() + ", " + message.leafSetSide);
                
        /**
         * Updates the adaptiveId of neighbor set.
         */
        VirtualIdUpdates updates = message.virtualIdUpdates;
        
        if (message.leftUpdateIndex == -1 || message.rightUpdateIndex == -1) return;
        
        for (int i = message.leftUpdateIndex; i <= message.rightUpdateIndex; i++) {
            
            NodeHandle nodeHandle = updates.getNodeHandle(i);
            Id oldVirtualId = updates.getOldId(i);
            Id newVirtualId = updates.getNewId(i);

            /**
             * If the old virtualId is found, updates the node's virtualId value.
             */
            NodeInformation nodeInfo = virtualIdSet.get(oldVirtualId);
            if (nodeInfo != null) {
                
            	assert ( nodeInfo.getNodeId().equals( nodeHandle.getId() ) );
            	virtualIdSet.remove(oldVirtualId);
                nodeInfo.setVirtualId( newVirtualId );
                virtualIdSet.put(newVirtualId, nodeInfo);
            }
            
            /**
             * Adds a new node to the neighborSet
             */
            else if (tempLeafSet.contains( nodeHandle.getId() )) {
            	
            	nodeInfo = new NodeInformation(nodeHandle.getId(), newVirtualId, nodeHandle, 1.0);
            	virtualIdSet.put(newVirtualId, nodeInfo);
            }
            
        }
        
    }
    
}
