package br.usp.ime.virtualId;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import br.usp.ime.virtualId.util.IdComparator;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.leafset.LeafSet;

public class VirtualIdSet {

    /**
     * Contains the Map from virtualIds to CDRM information.
     * Used during message routing.
     */
	protected TreeMap<Id, NodeInformation> virtualIdSet; 
	
    protected VirtualNode virtualNode;
    
    protected Logger logger;

    protected long lastUpdateTime = 0;
    
    public VirtualIdSet( VirtualNode virtualNode ) {
    	this.virtualIdSet = new TreeMap<Id, NodeInformation>();
    	this.virtualNode = virtualNode;
    	this.logger = virtualNode.getLogger();
    	this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public long getLastUpdateTime() { return lastUpdateTime; }

    public int size() { return virtualIdSet.size(); }
    
    synchronized public Id getTargetVirtualId ( Id targetId ) {
    	SortedMap<Id, NodeInformation> tailMap = virtualIdSet.tailMap(targetId);
    	if (tailMap.size() > 0)
    		return tailMap.firstKey();
    	else
    		return null;
    }

    synchronized public NodeHandle getTargetHandle ( Id targetId ) {
    	SortedMap<Id, NodeInformation> tailMap = virtualIdSet.tailMap(targetId);
    	if (tailMap.size() > 0)
    		return tailMap.get( tailMap.firstKey() ).getNodeHandle();
    	else
    		return null;
    }
    
    synchronized public Id getCcwVirtualId( Id targetId ) {
    	SortedMap<Id, NodeInformation> headMap = virtualIdSet.headMap( targetId );
    	if (headMap.size() > 0)
    		return headMap.get( headMap.lastKey() ).getVirtualId();
    	else
    		return null;
    }

    synchronized public NodeInformation[] getNodeInformationArray() {
    	
    	NodeInformation[] nodeInformationArray = new NodeInformation[virtualIdSet.size()];
    	virtualIdSet.values().toArray(nodeInformationArray);
    	return nodeInformationArray;
    }

    public NodeHandle getLeftmostLeaf () {
    
    	LeafSet leafSet = virtualNode.getNode().getLeafSet();
    	int uniqueCount = leafSet.getUniqueCount();
    	int leftNodeIndex =  -uniqueCount/2;
    	NodeHandle leftNodeHandle = leafSet.get( leftNodeIndex );    	

    	if (leftNodeHandle != null)
    		return leftNodeHandle;
    	else
    		return leafSet.get( leftNodeIndex + 1);
    }

    public NodeHandle getLeftmostLeafWithOffset ( int offset ) {

    	assert (offset >= 0);
    	LeafSet leafSet = virtualNode.getNode().getLeafSet();
    	int uniqueCount = leafSet.getUniqueCount();
    	int leftNodeIndex =  -uniqueCount/2;
    	NodeHandle leftNodeHandle = leafSet.get( leftNodeIndex + offset );    	

    	if (leftNodeHandle != null)
    		return leftNodeHandle;
    	else
    		return leafSet.get( leftNodeIndex + offset + 1);
    }

    public NodeHandle getRightmostLeaf () {
        
    	LeafSet leafSet = virtualNode.getNode().getLeafSet();
    	int uniqueCount = leafSet.getUniqueCount();
    	int rightLeafIndex = uniqueCount/2;
    	if (uniqueCount%2 == 0) rightLeafIndex--;
    	NodeHandle rightNodeHandle = leafSet.get(rightLeafIndex);

    	if (rightNodeHandle != null)
    		return rightNodeHandle;
    	else
    		return leafSet.get( rightLeafIndex - 1);

    }

    public String toString () {
        
        StringBuffer printTableBuffer = new StringBuffer();
        printTableBuffer.append("VirtualIdSet -> nodeId:" + virtualNode.getNode().getId()); 
        printTableBuffer.append(" leftId:" + getLeftmostLeaf().getId() + " rightId:" + getRightmostLeaf().getId());
        if (virtualIdSet.comparator() != null)
        	printTableBuffer.append(" baseId:" + ((IdComparator)virtualIdSet.comparator()).getBaseId());
        printTableBuffer.append("\n");
               
        int k1 = 0;        
        printTableBuffer.append("VirtualId -> "); 
        for ( Id virtualId : virtualIdSet.keySet() )
            printTableBuffer.append(k1++ + ":" + virtualId + " ");
        printTableBuffer.append("\n");
        
        k1=0;
        printTableBuffer.append("NodeId    -> "); 
        for ( NodeInformation nodeInfo : virtualIdSet.values() )
            printTableBuffer.append(k1++ + ":" + nodeInfo.getNodeId() + " ");
        //printTableBuffer.append("\n");        

        return printTableBuffer.toString();

    }	
}
