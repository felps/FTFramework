package br.usp.ime.virtualId.test;

import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.pastry.leafset.LeafSet;

import br.usp.ime.virtualId.NodeInformation;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;
import br.usp.ime.virtualId.util.DistanceManipulator;
import br.usp.ime.virtualId.util.IdComparator;

public class TestVirtualIdRing {
    
    private TreeMap<Id, VirtualSpace> virtualSpaceMap;
    private TreeMap<Id, VirtualNode> virtualNodeMap;
    
    private DistanceManipulator distanceManipulator;
    
    private Logger logger;        
   
    private static TestVirtualIdRing instance;
    
    private TestVirtualIdRing(Environment environment) {        
        distanceManipulator = new DistanceManipulator();
        
        /**
         * Configures Logging
         */        
        this.logger = Logger.getLogger("virtualIdTest"); 
        logger.info("Starting VirtualIdTest logger.");
    }
    
    public static TestVirtualIdRing createInstance(Environment environment) {
    	instance = new TestVirtualIdRing(environment);
    	return instance;
    }
    
    public static TestVirtualIdRing getInstance() {
    	return instance;
    }
    
    public void performTests( Vector<VirtualNode> virtualNodeList, int virtualSpaceNumber, boolean testLeafset ) {
        
        if (virtualNodeList.size() < 3) return;
        
        virtualSpaceMap = new TreeMap<Id, VirtualSpace>();
        virtualNodeMap = new TreeMap<Id, VirtualNode>();
        
        for (VirtualNode virtualNode : virtualNodeList) {
        	//System.out.println("virtualNode:" + virtualNode.getNode().getId());
            VirtualSpace virtualSpace = virtualNode.getVirtualSpace(virtualSpaceNumber);
            virtualSpaceMap.put(virtualSpace.getVirtualId(), virtualSpace);
            virtualNodeMap.put(virtualSpace.getVirtualId(), virtualNode);
        }
        
        assert (virtualSpaceMap.size() == virtualNodeList.size());
        assert (virtualNodeMap.size() == virtualNodeList.size());
             
        VirtualSpace[] virtualSpaceArray = new VirtualSpace[virtualSpaceMap.size()];
        virtualSpaceMap.values().toArray(virtualSpaceArray);

        VirtualNode[] virtualNodeArray = new VirtualNode[virtualNodeMap.size()];
        virtualNodeMap.values().toArray(virtualNodeArray);

        /**
         * Verifies if the adaptiveId value order is the same as the originalIds
         */
        double meanCapacity = testVirtualIdRing(virtualSpaceArray, virtualNodeArray);
        
		/**
         * Prints the CDRM properties, such as capacities, id ranges, etc.  
         */
        printNodeProperties(virtualSpaceArray, virtualNodeArray, meanCapacity);

        /**
         * Test the neighborSet from nodes
         */        
        testNeighborSet(virtualSpaceArray, virtualNodeArray);

        /**
         * Test the neighborSet from nodes
         */                
        if (testLeafset)
        	testLeafSet(virtualSpaceArray, virtualNodeArray);

    }

	private void testLeafSet(VirtualSpace[] virtualSpaceArray, VirtualNode[] virtualNodeArray) {
		int nNodes = virtualSpaceArray.length;
        
        for (int nodeIndex=0; nodeIndex < virtualSpaceArray.length; nodeIndex++) {

        	Id leftmostId, rightmostId;
        	{
        		LeafSet leafSet = virtualNodeArray[nodeIndex].getNode().getLeafSet();
        		int maxLeafSetSize = leafSet.maxSize();
        		int leftLeafOffset  = maxLeafSetSize/2;
        		int rightLeafOffset = maxLeafSetSize - maxLeafSetSize/2;
        	
        		leftLeafOffset = ( nodeIndex - leftLeafOffset + virtualNodeArray.length ) % virtualNodeArray.length;        	        	        
        		rightLeafOffset = ( nodeIndex+rightLeafOffset ) % virtualNodeArray.length;        	
        		leftmostId  = virtualNodeArray[leftLeafOffset].getNode().getId();
        		rightmostId = virtualNodeArray[rightLeafOffset].getNode().getId();
        		
        	}
        	
            NodeInformation[] leafSetArray = virtualSpaceArray[nodeIndex].getVirtualLeafSet().getNodeInformationArray();
            int vLeafSetSize = leafSetArray.length;
            if (vLeafSetSize == 0) continue;

            /**
             * Check leafSet sequence and if contains all elements in the inside range
             */ 
            int leftOffset  = -1;
            int rightOffset = -1;
            for (int node=0; node<nNodes; node++) {
            	
            	if (leafSetArray[0].getNodeId().equals( virtualNodeArray[node].getNode().getId() ))
            		leftOffset = node;
            	if (leafSetArray[vLeafSetSize-1].getNodeId().equals( virtualNodeArray[node].getNode().getId() ))
            		rightOffset = node;
            }
        
            for (int leaf=0; leaf<leafSetArray.length; leaf++) {
            	
            	Id leafId = leafSetArray[leaf].getNodeId();
            	Id nodeId = virtualNodeArray[(leaf+leftOffset)%nNodes].getNode().getId();

            	/**
            	 * Virtual leafSet sequence of nodes is not contiguous
            	 */
            	if (leafId.equals( nodeId ) == false ) {
            		System.err.println( virtualSpaceArray[nodeIndex].getVirtualLeafSet());
            		System.err.println( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            		System.err.println( "Nodes in the leaf set are not contiguous " + leafId);
            		assert (false);	
            	}

            	Id leafVirtualId = leafSetArray[leaf].getVirtualId();            	
            	Id nodeVirtualId = virtualSpaceArray[(leaf+leftOffset)%nNodes].getVirtualId();

            	/**
            	 * Checks if nodes are inside leafset range
            	 */
            	if ( 0 < leaf && leaf < leafSetArray.length-1 && IdComparator.isBetween(leftmostId, leafVirtualId, rightmostId) == false ) {
                	System.err.println( virtualSpaceArray[nodeIndex].getVirtualLeafSet());
                	System.err.println( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                	System.err.println( " The virtualLeafset is outside the expected Virtual leafset range.");
                	assert (false);
            	}
            	
            	/**
            	 * Check if the virtual ids are correct
            	 */
            	if (leafVirtualId.equals( nodeVirtualId ) == false) {
                	System.err.println( virtualSpaceArray[nodeIndex].getVirtualLeafSet());
                	System.err.println( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                	System.err.println("Error in virtual id sequence " + leafVirtualId);
                	assert (false);
            	}

            }           
        
            if (false) {
            	Id previousVirtualId = virtualSpaceArray[(leftOffset-1+nNodes)%nNodes].getVirtualId();
            	if ( IdComparator.isBetween(previousVirtualId, leftmostId, leafSetArray[0].getVirtualId()) == false) {
            		System.out.println("Prev:" + previousVirtualId + " Left:" + leftmostId + " First:" + leafSetArray[0].getVirtualId() + " Right:" + rightmostId);
            		System.out.println( virtualSpaceArray[nodeIndex].getVirtualLeafSet());
            		assert (false);
            	}

            	Id nextVirtualId = virtualSpaceArray[(rightOffset+1)%nNodes].getVirtualId();
            	if ( IdComparator.isBetween(leafSetArray[vLeafSetSize-1].getVirtualId(), rightmostId, nextVirtualId) == false) {
            		System.out.println("Left:" + leftmostId + " Last:" + leafSetArray[vLeafSetSize-1].getVirtualId() + " Right:" + rightmostId + " Next:" + nextVirtualId);
            		System.out.println( virtualSpaceArray[nodeIndex].getVirtualLeafSet());
            		assert (false);            	
            	}
            }
        }
	}
                
	private void testNeighborSet(VirtualSpace[] virtualSpaceArray, VirtualNode[] virtualNodeArray) {
		int nElements = virtualSpaceArray.length;
        
        for (int nodeIndex=0; nodeIndex < virtualSpaceArray.length; nodeIndex++) {

            NodeInformation[] neighborSetArray = virtualSpaceArray[nodeIndex].getVirtualNeighborSet().getNodeInformationArray();
        	int nNeighbors = neighborSetArray.length;
        	
            /**
        	 * Check if the neighborSet is centered
        	 */
            if (false) {
            	assert (neighborSetArray.length > 0);
            	Id currentNodeId = virtualNodeArray[nodeIndex].getNode().getId();
            	for (int i=0; i<nNeighbors; i++) {
            		if ( nElements > 2 && neighborSetArray[i].getNodeId().equals( currentNodeId ) ) {
            			if (nNeighbors % 2 == 1 && nNeighbors / 2 != i && nNeighbors / 2 != i-1 && nNeighbors / 2 != i+1 ) {
            				System.out.println( virtualSpaceArray[nodeIndex].getVirtualNeighborSet());
            				assert (false);            			
            			}
            			if ( nNeighbors % 2 == 0 && nNeighbors / 2 != i && nNeighbors / 2 != i-1 && nNeighbors / 2 != i+1 ) {
            				System.out.println( virtualSpaceArray[nodeIndex].getVirtualNeighborSet());
            				assert (false);
            			}
            		}
            	}
            }
            
            /**
             * Compare neighbor sequence
             */ 
            int offset = -1;
            for (int i=0; i<nElements; i++)
            	if (neighborSetArray[0].getNodeId().equals( virtualNodeArray[i].getNode().getId() )) {
            		offset = i;	break;
            	}

            for (int neighbor=0; neighbor < nNeighbors; neighbor++) {
            	
            	Id neighborSetId = neighborSetArray[neighbor].getNodeId();
            	Id nodeId = virtualNodeArray[(neighbor+offset)%nElements].getNode().getId();
            	
            	if (neighborSetId.equals( nodeId ) == false ) {
            		System.out.println( virtualSpaceArray[nodeIndex].getVirtualNeighborSet());
            		assert (false);
            	}

            	Id neighborSetVirtualId = neighborSetArray[neighbor].getVirtualId();            	
            	Id nodeVirtualId = virtualSpaceArray[(neighbor+offset)%nElements].getVirtualId();

            	if (neighbor != 0 && neighbor != (nNeighbors - 1) && neighborSetVirtualId.equals( nodeVirtualId ) == false) {
            		System.out.println( virtualSpaceArray[nodeIndex].getVirtualNeighborSet());
            		assert (false);
            	}
            	
            }

        }
	}

	private void printNodeProperties(VirtualSpace[] virtualSpaceArray, VirtualNode[] virtualNodeArray, double meanCapacity) {

        long d = 10000000000000L;
        //System.out.println("max = " + DistanceManipulator.MAX_DISTANCE/d);
        long meanDistance = DistanceManipulator.MAX_DISTANCE / virtualSpaceArray.length;
        
        int nElements = virtualSpaceArray.length;
        StringBuffer nodeProperties = new StringBuffer();
        for (int cdrmIndex=0; cdrmIndex < virtualSpaceArray.length; cdrmIndex++) {
            Id currentVirtualId = virtualSpaceArray[cdrmIndex].getVirtualId();
            Id previousVirtualId = virtualSpaceArray[(cdrmIndex-1+nElements)%nElements].getVirtualId();
            
        	rice.pastry.Id.Distance distance = (rice.pastry.Id.Distance) currentVirtualId.distanceFromId(previousVirtualId);
        	long longDistance = DistanceManipulator.convertDistanceToLong(distance);
        	long expectedDistance = (long)(virtualSpaceArray[cdrmIndex].getCapacity() / meanCapacity * meanDistance);            

        	System.out.print(virtualNodeArray[cdrmIndex].getNode().getId() + " -> " + currentVirtualId + " distance: " + longDistance/d + " \texpected: " + expectedDistance/d );
        	System.out.printf(" \tdifference: %4.2f \n", 100.0*(longDistance-expectedDistance)/expectedDistance);
        	
        	nodeProperties.append(virtualNodeArray[cdrmIndex].getNode().getId() + " -> " + currentVirtualId + " distance: " + longDistance/d + " \texpected: " + expectedDistance/d );
        	nodeProperties.append(" \tdifference: " + 100.0*(longDistance-expectedDistance)/expectedDistance + "\n");
        }
        
        logger.debug("Number of nodes: " + virtualNodeArray.length + "\n" + nodeProperties + "\n");
	}

	private double testVirtualIdRing(VirtualSpace[] virtualSpaceArray, VirtualNode[] virtualNodeArray) {

		double meanCapacity = 0;        

        for (int cdrmIndex=0; cdrmIndex < virtualSpaceArray.length; cdrmIndex++) {
            Id currentVirtualId = virtualSpaceArray[cdrmIndex].getVirtualId();
            Id nextVirtualId = virtualSpaceArray[(cdrmIndex+1)%virtualSpaceArray.length].getVirtualId();
            
            Id currentNodeId = virtualNodeArray[cdrmIndex].getNode().getId();
            Id nextNodeId = virtualNodeArray[(cdrmIndex+1)%virtualNodeArray.length].getNode().getId();
                
            if (currentVirtualId.clockwise(nextVirtualId) == false)
                for (int i=0; i < virtualSpaceArray.length; i++)
                    if ( i != cdrmIndex )
                        if ( currentVirtualId.clockwise(nextVirtualId) )
                            assert(false);
            
            if(currentNodeId.clockwise(nextNodeId) == false )
                for (int i=0; i < virtualSpaceArray.length; i++)
                    if ( i != cdrmIndex )
                        if ( currentNodeId.clockwise(nextNodeId) )
                            assert(false);
            
            meanCapacity += virtualSpaceArray[cdrmIndex].getCapacity(); 
        }
        meanCapacity /= virtualSpaceArray.length;
		return meanCapacity;
	}
	
}
