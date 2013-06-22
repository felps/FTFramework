package br.usp.ime.oppstore.cdrm;

import java.util.Map;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.pastry.PastryNode;
import rice.pastry.Id.Distance;
import rice.pastry.commonapi.PastryEndpoint;
import rice.pastry.leafset.LeafSet;
import rice.pastry.socket.SocketPastryNodeFactory;
import br.usp.ime.oppstore.FileFragmentIndex;
import br.usp.ime.oppstore.adrmanager.AdrManagerImpl;
import br.usp.ime.oppstore.message.CdrmReplicaInfoMessage;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage;
import br.usp.ime.oppstore.message.StoreFragmentListMessage;
import br.usp.ime.oppstore.message.StoreFragmentMessage;
import br.usp.ime.oppstore.message.CdrmReplicaInfoMessage.CdrmInfoSide;
import br.usp.ime.oppstore.message.CdrmReplicaInfoMessage.CdrmInfoType;
import br.usp.ime.oppstore.statistics.StatisticsCollector;
import br.usp.ime.oppstore.tests.adaptive.CdrmTestInformation;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;
import br.usp.ime.virtualId.util.DistanceManipulator;

/**
 * Communicates with the Pastry network
 *
 * @version February, 09 of 2006
 * @author Raphael Camargo
 */
public class CdrmApp implements Application {

    public enum OpStoreIdProtocol {ADAPTIVE, PASTRY};    
    public enum OpStoreCapacityProtocol {SQUARED, LINEAR, QUADRATIC, HIPERBOLIC};
    
    public static OpStoreIdProtocol idProtocol = OpStoreIdProtocol.ADAPTIVE;
    public static OpStoreCapacityProtocol capacityProtocol = OpStoreCapacityProtocol.QUADRATIC; 
    public static long maximumStorageCapacity = 100 * 1000 * 1000; // 100 GB
    
    /**
     * Contains methods to communicate with the pastry network 
     */
    private PastryEndpoint endpoint;
       
    /**
     * 
     */
    private StatisticsCollector statisticsCollector; // - only if statistics already updated 
       
    /**
     * 
     */
    private boolean isConnected = false;   

    /**
     * 
     */
    private Logger cdrmLogger;        
    
    /**
     * 
     */
    private FileStorageRetrievalManager fileStorageManager;

    /**
     * The point from which messages are routed in the network
     */
    private VirtualNode virtualNode;
   
    /**
     * Manages File Information Index storage and retrieval
     */
    private FileFragmentIndexManager fileIndexFragmentManager;
    
    /**
     * Manages Fragment storage location
     */
    private FragmentStorageManager fragmentStorageManager;
    
    private Id lastDepartedId;
    
    /**
     *  
     */
    public CdrmApp(SocketPastryNodeFactory nodeFactory, rice.pastry.Id nodeId, NodeHandle bootstrapHandle, StatisticsCollector statistics) {

    	/**
    	 * Creates the pastry and virtual nodes
    	 */
        PastryNode pastryNode;
        if (nodeId == null) 
            pastryNode = nodeFactory.newNode((rice.pastry.NodeHandle) bootstrapHandle);
        else 
            pastryNode = nodeFactory.newNode((rice.pastry.NodeHandle) bootstrapHandle, nodeId);        

        this.virtualNode = new VirtualNode(this, pastryNode);

        /** 
         * Creates a logger for this virtual Id node.
         */ 
        this.cdrmLogger = Logger.getLogger("cdrm." + pastryNode.getId().toString().substring(0, 9) + ">");
        this.cdrmLogger.info("CdrmApp created successfully!");
        this.statisticsCollector = statistics;
        
    	/**
    	 * Request side
    	 */
        this.fileStorageManager = new FileStorageRetrievalManager(virtualNode);
        
        /**
         * Storage side
         */        
        this.fileIndexFragmentManager = new FileFragmentIndexManager(virtualNode);
        this.fragmentStorageManager = new FragmentStorageManager(virtualNode);
        
		while ( virtualNode.getNode().isReady() == false ) {
			try { Thread.sleep(100); }
			catch (InterruptedException e) {}
		}
		
        /**
         * Request the CDRM information from the neighbors
         */
		LeafSet leafSet = virtualNode.getNode().getLeafSet();
		NodeHandle leftNode  = leafSet.get(-1);
		NodeHandle rightNode = leafSet.get(1);
		if (leftNode != null) {
			CdrmReplicaInfoMessage infoMessage = 
				new CdrmReplicaInfoMessage(virtualNode.getNode().getLocalNodeHandle(), CdrmInfoType.STORE_JOIN, CdrmInfoSide.LEFT, 0);
			virtualNode.sendDirectMessage(leftNode, infoMessage);
		}
		if (rightNode != null) {
			CdrmReplicaInfoMessage infoMessage = 
				new CdrmReplicaInfoMessage(virtualNode.getNode().getLocalNodeHandle(), CdrmInfoType.STORE_JOIN, CdrmInfoSide.RIGHT, 0);
			virtualNode.sendDirectMessage(rightNode, infoMessage);
		}		
        
    }
        

    /**
     * =================================================================================
     * Getters and Setters
     * =================================================================================
     */

    public void setConnected(boolean newState) { isConnected = newState;}
    public boolean isConnected() { return isConnected; }    

    public Logger getLogger() {return cdrmLogger;}
    
    public Endpoint getEndPoint() { return endpoint; }
    
    public AdrManagerImpl getAdrManager() { return fragmentStorageManager.getAdrManager(); }
    
    public VirtualNode getVirtualNode() { return virtualNode; }

    public FileStorageRetrievalManager getFileStorageRetrievalManager() { return fileStorageManager; }
    
    public Map<Id, FileFragmentIndex> getStoredFfiMap() {    	
    	return fileIndexFragmentManager.getStoredFfiMap();
    }
            
    /**
     * =================================================================================
     * Methods from pastry Application interface.
     * =================================================================================
     */

    /**
     * From pastry's Application interface
     * Called when the message reaches the destination node 
     */
    public void deliver(Id messageId, Message message) {
 
    	if (message instanceof StoreFragmentMessage) {            
    		StoreFragmentMessage storeMessage = (StoreFragmentMessage) message; 
    		if (storeMessage.isResponse)
                fileStorageManager.addFragmentStorageLocation( storeMessage.messageNumber, storeMessage.fragmentId, storeMessage.adrAddress);
    		
    		else {
    			boolean adrSelected = fragmentStorageManager.processStoreFragmentMessage(storeMessage);
    			if (statisticsCollector != null) {
    				if ( adrSelected ) statisticsCollector.setFragmentStored(this, storeMessage);
    				else statisticsCollector.addFragmentNotStored(storeMessage.messageNumber);
    			}
    		}
    	}

    	else if (message instanceof StoreFragmentListMessage) {
    		StoreFragmentListMessage storeMessage = (StoreFragmentListMessage) message;
    		if (storeMessage.isResponse)    			
    			for (int i=0; i<storeMessage.fragmentIdList.size(); i++)
    				fileStorageManager.addFragmentStorageLocation( storeMessage.messageNumber, storeMessage.fragmentIdList.get(i), storeMessage.adrAddressList.get(i));
    		else
    			fragmentStorageManager.processStoreFragmentListMessage(storeMessage);
    	}        

    	// Messages related to the storage and retrieval of FileInformationStructures
    	else if (message instanceof FileFragmentIndexMessage) {
    		FileFragmentIndexMessage fileIndexMessage = (FileFragmentIndexMessage) message;
    		if (statisticsCollector != null)
    			statisticsCollector.setFileInformationMessageDelivered(this, fileIndexMessage);
    		
    		if ( fileIndexMessage.isResponse )
    			fileStorageManager.setFileIndexResponseReceived(fileIndexMessage);
    		else
    			fileIndexFragmentManager.processFileIndexMessage(fileIndexMessage);
    	}
    	
    	else if (message instanceof CdrmReplicaInfoMessage) {
    		CdrmReplicaInfoMessage cdrmInfoMessage = (CdrmReplicaInfoMessage) message;
    		fileIndexFragmentManager.processCdrmReplicaInfoMessage(cdrmInfoMessage);
    	}
        
    }
        
    /**
     * This method is invoked to inform the application that the given node has
     * either joined or left the neighbor set of the local node, as the set would
     * be returned by the neighborSet call.
     *
     * @param handle The handle that has joined/left
     * @param joined Whether the node has joined or left
     */
    public void update(NodeHandle handle, boolean joined) {

    	Id ccwNodeId =  virtualNode.getVirtualApplication().getCcwNodeId();
    	if (joined == false && ccwNodeId.equals( handle.getId() ))
    		if (lastDepartedId == null || handle.getId().equals( lastDepartedId ) == false) {
    			
    			lastDepartedId = handle.getId();
    			System.err.println( "Node " + handle.getId() + " departure detected at " + virtualNode.getNode() );
    			fileIndexFragmentManager.createDepartureReplicaInfoMessage();
    		}

    	Id cwNodeId =  virtualNode.getVirtualApplication().getCwNodeId();
    	if (joined == false && cwNodeId.equals( handle.getId() ))
    		if (lastDepartedId == null || handle.getId().equals( lastDepartedId ) == false) {
    			
    			lastDepartedId = handle.getId();
    			System.err.println( "Node " + handle.getId() + " departure detected at " + virtualNode.getNode() );
    			fileIndexFragmentManager.createDepartureReplicaInfoMessage();
    		}

    }
    
    /**
     * From pastry's Application interface
     * Called when the a message is forwarded from 'node'
     */
    public boolean forward(RouteMessage routeMessage) {
       
        /**
         * Collects message routing statistics
         */        
    	if (statisticsCollector != null) {
    		Message message = routeMessage.getMessage();
    		if (message instanceof StoreFragmentMessage) {
    			StoreFragmentMessage storeFragmentMessage = (StoreFragmentMessage) message;            
    			statisticsCollector.setStoreFragmentMessageForwarded(
    					this, storeFragmentMessage, routeMessage.getNextHopHandle());
    		}
    		else if (message instanceof FileFragmentIndexMessage) {
    			FileFragmentIndexMessage fileInformationMessage = (FileFragmentIndexMessage) message;
    			statisticsCollector.setFileInformationMessageForwarded(
    					this, fileInformationMessage, routeMessage.getNextHopHandle());
    		}
    	}

        return true;
    }

    /**
     * =================================================================================
     * Other methods
     * =================================================================================
     */
    
    public void leaveNetwork () { }
    
    /**
     * Returns information regarding an Cdrm Id and the stored FileInformationStructures.
     * Used mainly for testing purposes.
     *  
     * @return CdrmTestInformation
     */
    public CdrmTestInformation getCdrmInformation() {

    	CdrmTestInformation cdrmTestInfo = new CdrmTestInformation();
        cdrmTestInfo.originalNodeId = virtualNode.getNode().getId();
        cdrmTestInfo.capacity = getAdrManager().evaluateCdrmCapacity();        
        cdrmTestInfo.lastUpdatedCapacity = getAdrManager().getLastUpdatedCdrmCapacity();
        cdrmTestInfo.protocol = idProtocol;
        cdrmTestInfo.fileInformationList = null;//cdrmDataManager.getFileInformationStructureList();        
        cdrmTestInfo.adaptiveNodeId = virtualNode.getVirtualSpace(1).getVirtualId();
        
        try{
        	VirtualSpace virtualSpace = virtualNode.getVirtualSpace(1);
        	if (virtualSpace != null) {
        		Distance distance = (Distance)virtualSpace.getCcwVirtualId().distanceFromId( virtualSpace.getVirtualId());
        		cdrmTestInfo.longIdRange = DistanceManipulator.convertDistanceToLong( distance );
        	}
        	else 
        		cdrmTestInfo.longIdRange = 0;
        	//System.out.println( virtualNode.getNode().getId() + " ==> " + virtualSpace.getCcwVirtualId() + " " + virtualSpace.getVirtualId() + " " + distance + " " + cdrmTestInfo.longIdRange );
        }
        catch (Exception e) {}
            
        return cdrmTestInfo;
    }
    
    /**
     * Returns a description of this node
     *
     * @return The id of this node
     */
    public String toString() {    
        return "CDRM " + virtualNode.getNode().getId();
    }

}
