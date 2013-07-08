package br.usp.ime.virtualId;

import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import br.usp.ime.virtualId.message.VirtualIdLeafSetQuery;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage;
import br.usp.ime.virtualId.message.VirtualIdLeafSetQuery.LeafSetQuerySide;
import br.usp.ime.virtualId.protocol.VirtualIdProtocol.ProtocolType;
import br.usp.ime.virtualId.util.IdComparator;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

public class VirtualLeafSet extends VirtualIdSet {

    private boolean isReady = false;
    private boolean isUpdated = false;
    
    public VirtualLeafSet( VirtualNode virtualNode ) {
    	super(virtualNode);
        this.logger = Logger.getLogger("leafset." + virtualNode.getNode().getId().toString().substring(0, 9) + ">");
    }
    
	/**
	 * Used to indicated that the VirtualLeafSet was set by the joining protocol.
	 */
    public boolean isReady() { return isReady; }
    
	/**
	 * Used to indicated that the protocol finished and updated the node leafset.
	 * But the virtualLeafSets from other nodes are not yet updated.
	 */
    public boolean isUpdated() { return isUpdated; }
    public void setUpdated() { isUpdated = true; };
        
    /**
     * Called in the beginning of the joining protocol to set the initial leafset values.
     * The entries are obtained from a node immediate neighbors. 
     * After both neighbors send their entries and the table is later updated, the leafset is marked as ready.
     * @param leafSetQuery
     */
    synchronized public void setLeafSet (VirtualIdLeafSetQuery leafSetQuery) {

        this.lastUpdateTime = System.currentTimeMillis();
        
    	NodeInformation[] nodeInfoArray = leafSetQuery.nodeInfoArray;

    	boolean setIsReady = false;
    	if (virtualIdSet.size() > 0 && isReady == false)
    		setIsReady = true;
    	
    	if (leafSetQuery.leafSetSide == LeafSetQuerySide.LEFT) {
        
    		Id newBaseId = null;
    		if (nodeInfoArray.length > 0) newBaseId = nodeInfoArray[0].getVirtualId();
    		else assert (false);

    		TreeMap <Id, NodeInformation> newLeafMap =
    			new TreeMap <Id, NodeInformation> ( new IdComparator( newBaseId ) );
    		newLeafMap.putAll(virtualIdSet);
    		virtualIdSet = newLeafMap;
    	}

    	//Id leftId  = getLeftmostLeaf().getId();
        //Id rightId = getRightmostLeaf().getId();
        
        /**
         * Adds member from neighbor's VirtualLeafSet to joining node VirtualLeafSet
         */
        for (int i = 0; i < nodeInfoArray.length; i++) {
        	//if (IdComparator.isBetween(leftId, nodeInfoArray[i].getVirtualId(), rightId))
        	virtualIdSet.put(nodeInfoArray[i].getVirtualId(), nodeInfoArray[i]);
        }        
    
        if (setIsReady) {
        	isReady = true;
        	logger.info("Finished setting leafSet table:");
        }
        
    }
      
    synchronized public boolean updateLeafSet (VirtualIdUpdateMessage updateMessage) {
    	
    	VirtualIdUpdates updates = updateMessage.virtualIdUpdates;
    	Id updatingNodeId = updateMessage.updatingNodeId;    				 
    	
    	logger.info("Updating leafSet using updates received from " + updatingNodeId + " and protocol " + updateMessage.protocolType + ".");
    	logger.debug("BEFORE: " + this );

    	boolean setUpdated = false;
    	
        Id newBaseId   = null;
        Id newBottomId = null;
        if (virtualIdSet.size() > 0) {
        	SortedMap<Id, NodeInformation> partialMap = null;
        	
        	partialMap = virtualIdSet.headMap(getLeftmostLeaf().getId());
        	if (partialMap.size() > 0) newBaseId   = partialMap.lastKey();
        	else newBaseId   = virtualIdSet.firstKey();	

        	partialMap = virtualIdSet.tailMap(getRightmostLeaf().getId());
        	if (partialMap.size() > 0) newBottomId   = partialMap.firstKey();
        	else newBottomId = virtualIdSet.lastKey();	        	
        }
         

        int leftmostIndex  = updates.getLeftLeafSetIndex( getLeftmostLeaf().getId()  );
        int rightmostIndex = updates.getRightLeafSetIndex( getRightmostLeaf().getId() );
        
        /**
         * This VirtualLeafset is outside the range ou updates.
         */
//        if (leftmostIndex == -1 && rightmostIndex == -1) {
//        	removeExtraLeafs(updateMessage, updatingNodeId, newBaseId, newBottomId);
//        	logger.debug("leafIndexes: " + leftmostIndex + " " + rightmostIndex + " Skipping node updating because updates are out of range.\n");
//        	return false;
//        }

                
        int tmpLeftmostIndex = leftmostIndex;
        if (tmpLeftmostIndex == -1) tmpLeftmostIndex = 0;
        
        int tmpRightmostIndex = rightmostIndex;
        if (tmpRightmostIndex == -1) 
        	tmpRightmostIndex = updates.numberOfUpdates()-1;
        else if (rightmostIndex < leftmostIndex ) // updates.getWrappingUpdates()
        	tmpRightmostIndex += updates.numberOfUpdates();
        else if (updates.getWrappingUpdates() && rightmostIndex == leftmostIndex )
        	tmpRightmostIndex += updates.numberOfUpdates()-1;
        else if (updates.getWrappingUpdates() && rightmostIndex == leftmostIndex+1 )
        	tmpRightmostIndex += updates.numberOfUpdates()-2;
        
        logger.debug("leafIndexes: " + leftmostIndex + " " + rightmostIndex + " | tmpLeafIndexes: " + tmpLeftmostIndex + " " + tmpRightmostIndex + " wraps:" + updates.getWrappingUpdates());
        
        for (int i = tmpLeftmostIndex; i < updates.numberOfUpdates()+tmpLeftmostIndex; i++) {
            
            NodeHandle nodeHandle = updates.getNodeHandle(i);
            Id oldVirtualId = updates.getOldId(i);
            Id newVirtualId = updates.getNewId(i);              

            /**
             * If oldId is found, update the node's virutalId value.
             */
            NodeInformation nodeInfo = virtualIdSet.get(oldVirtualId);
            if (nodeInfo == null)
            	for ( NodeInformation nodeInfoTmp : virtualIdSet.values() )
            		if ( nodeInfoTmp.getNodeId().equals( nodeHandle.getId() ) ) {
            			nodeInfo = nodeInfoTmp;
            			oldVirtualId = nodeInfo.getVirtualId();
            		}

            if (nodeInfo != null) {

                assert ( nodeInfo.getNodeId().equals( nodeHandle.getId() ) );            	            
            	virtualIdSet.remove(oldVirtualId);            	        		
            	nodeInfo.setVirtualId( newVirtualId );
            	virtualIdSet.put(newVirtualId, nodeInfo);
            	setUpdated = true;
            	
            	logger.debug("Updating node " + nodeHandle.getId() + " to leafset.");
            }

            /**
             * Otherwise, if the missing node is inside the node leafSet range, add to virtual leafSet.
             */
            else if (tmpLeftmostIndex <= i && i <= tmpRightmostIndex ) {
            	
            	if (updates.getWrappingUpdates() || ( i > 0 && virtualIdSet.containsKey( updates.getNewId( i-1 ) ) ) || virtualIdSet.containsKey( updates.getOldId( i+1 ) ) )
            	logger.debug("Adding node " + nodeHandle.getId() + " to leafset.");            		
            	nodeInfo = new NodeInformation (nodeHandle.getId(), newVirtualId, nodeHandle, 0);            	
            	virtualIdSet.put(newVirtualId, nodeInfo);
            	setUpdated = true;
            	//if ( newBaseId != null && IdComparator.isBetweenExcludeRight(getLeftmostLeaf().getId(), newVirtualId, newBaseId) )
            	//	newBaseId = newVirtualId;
            }
        }
        
        if (leftmostIndex >= 0)
        	newBaseId = updates.getNewId( leftmostIndex );        
        if (rightmostIndex >= 0) 
        	newBottomId = updates.getNewId( rightmostIndex );
        
        /**
         * Changes the map baseId.
         */
        if (leftmostIndex >= 0) {
            TreeMap <Id, NodeInformation> newLeafMap =
                new TreeMap <Id, NodeInformation> ( new IdComparator( newBaseId ) );
            newLeafMap.putAll(virtualIdSet);
            virtualIdSet = newLeafMap;
            setUpdated = true;
        }
        	
        /**
         * Remove ids outside the leafset range and the departing node, if is the case.
         */
        logger.debug("newBaseId: " + newBaseId + " newBottomId:" + newBottomId);
        removeExtraLeafs(updateMessage, updatingNodeId, newBaseId, newBottomId);

        logger.debug(updates);
        
        logger.debug(" AFTER: " + this + "\n");
    	
    	/**
    	 * Check if the nodes in the leafset are in sequence
    	 */
    	NodeInformation[] nodeInfoArray = this.getNodeInformationArray();
    	for (int i=1; i<nodeInfoArray.length-1; i++) {
    		assert ( IdComparator.isBetween(nodeInfoArray[i-1].getNodeId(), nodeInfoArray[i].getNodeId(), nodeInfoArray[i+1].getNodeId()));
    		assert ( nodeInfoArray[i-1].getNodeId().equals( nodeInfoArray[i].getNodeId() ) == false );
    	}
    	
    	if (setUpdated)
    		this.lastUpdateTime = System.currentTimeMillis();

    	isUpdated = true;
    	
    	return setUpdated;
    }

    /**
     * Remove ids outside the leafset range and the departing node, if is the case.
     */
	private void removeExtraLeafs(VirtualIdUpdateMessage updateMessage, Id updatingNodeId, Id newBaseId, Id newBottomId) {
		HashSet<Id> removeSet = new HashSet<Id>();
        for (NodeInformation nodeInfo: virtualIdSet.values()) {
        	if ( IdComparator.isBetweenExcludeLeftRight(newBottomId, nodeInfo.getVirtualId(), newBaseId) )
        		removeSet.add( nodeInfo.getVirtualId() );
        	if ( updateMessage.protocolType == ProtocolType.DEPARTURE && nodeInfo.getNodeId().equals (updatingNodeId) )
        		removeSet.add( nodeInfo.getVirtualId() );
        }
        for(Id removeId : removeSet) {
        	logger.debug("Removing node " + removeId + " from leafset.");    
        	virtualIdSet.remove(removeId);
        	//setUpdated = true;
        }
	}
	
    /**
     * Remove ids outside the leafset range and the departing node, if is the case.
     */
	public synchronized void removeNeighborExtraLeafs(VirtualIdUpdateMessage updateMessage) {
		
		if (updateMessage.virtualIdUpdates.getWrappingUpdates() == true)
			return;
		
		HashSet<Id> removeSet = new HashSet<Id>();

		Id newBaseId = null;
		SortedMap<Id, NodeInformation> headMap = virtualIdSet.headMap( getLeftmostLeaf().getId() );
		if (headMap.size() > 1) {
			newBaseId = headMap.lastKey();
			for (Id removeId : headMap.keySet())
				if (removeId.equals( newBaseId ) == false)
					removeSet.add(removeId);
		}
	
		SortedMap<Id, NodeInformation> tailMap = virtualIdSet.tailMap( getRightmostLeaf().getId() );
		if (tailMap.size() > 1) {
			Id tailKeepId = tailMap.firstKey();
			for (Id removeId : tailMap.keySet())
				if (removeId.equals( tailKeepId ) == false)
					removeSet.add(removeId);
		}

				
        for(Id removeId : removeSet) {
        	logger.debug("Removing node " + removeId + " from leafset.");    
        	virtualIdSet.remove(removeId);
        }
        
        if (newBaseId != null) {
    		TreeMap <Id, NodeInformation> newLeafMap = new TreeMap <Id, NodeInformation> ( new IdComparator( newBaseId ) );
    		newLeafMap.putAll(virtualIdSet);
    		virtualIdSet = newLeafMap;
        }
	}

}

