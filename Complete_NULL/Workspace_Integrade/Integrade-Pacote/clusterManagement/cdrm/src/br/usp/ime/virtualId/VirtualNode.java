package br.usp.ime.virtualId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.PastryNode;
import br.usp.ime.virtualId.message.VirtualIdMessage;
import br.usp.ime.virtualId.protocol.ProtocolObserver;
import br.usp.ime.virtualId.protocol.VirtualIdProtocol;
import br.usp.ime.virtualId.protocol.VirtualIdProtocolManager;
import br.usp.ime.virtualId.protocol.VirtualMessageDispatcher;

/**
 * A Virtual Endpoint, by which applications can route and receive messages in the virtual spaces.
 * @author rcamargo
 *
 */
public class VirtualNode {
    
    /**
     * Manages messages from co-existing virtual id spaces. 
     */
    private VirtualApplication virtualApplication;
    
    private List<VirtualSpace> virtualSpaceList;
        
    private PastryNode node;
    
    private Endpoint endpoint;
    
    private VirtualMessageRouter messageRouter;
    
    private VirtualIdProtocolManager protocolManager;
    
    private AtomicInteger nextMessageNumber = new AtomicInteger(1);
    
    private VirtualMessageDispatcher messageDispatcher;
    
    /**
     * Performs logging for this virtual node, including the related classes.
     * Is called "virtualId<id>".
     */
    private Logger logger;
    
    public VirtualNode( Application virtualSpaceZeroApp, PastryNode node ) {

    	this.protocolManager = new VirtualIdProtocolManager();

        this.messageDispatcher = new VirtualMessageDispatcher(this);
        this.messageDispatcher.start();
        
        /** 
         * Creates a logger for this virtual Id node.
         */ 
        this.logger = Logger.getLogger("virtualNode." + node.getId().toString().substring(0, 9) + ">");

    	/**
    	 * Configure virtual application and lists.
    	 */
    	this.virtualSpaceList = new ArrayList<VirtualSpace>();        
        this.virtualSpaceList.add( 0, new VirtualSpace(0, virtualSpaceZeroApp, null, null, node.getId(), 0.0) );
        this.virtualApplication = new VirtualApplication(this, virtualSpaceList);
     
        /**
         * Configures pastry node and endpoint;
         */
        this.node = node;
        this.endpoint = node.buildEndpoint(virtualApplication, "virtualApp");
        this.endpoint.register();        
        this.virtualApplication.setEndpoint(endpoint);
        
        this.messageRouter = new VirtualMessageRouter(endpoint);
        this.messageRouter.start();
        
    }

    //-------------------------------------------------------
    // Getters
    //-------------------------------------------------------
    
    //public Endpoint getEndpoint() { return endpoint; }
    public PastryNode getNode() { return node; }    
    
    public VirtualMessageDispatcher getMessageDispatcher() { return messageDispatcher; }
    
    public Logger getLogger() { return logger; }
    
    public Endpoint getEndpoint() { return endpoint; }
    
    public VirtualIdProtocolManager getProtocolManager() { return protocolManager; }
    
    public VirtualApplication getVirtualApplication() { return virtualApplication; }

    // TODO: A race condition may occur between virtualSpaces
    public int getNextMessageNumber() {return nextMessageNumber.getAndIncrement(); }

    public VirtualSpace getVirtualSpace(int virtualSpaceNumber) {
    	if ( virtualSpaceNumber < virtualSpaceList.size() )
    		return virtualSpaceList.get(virtualSpaceNumber);
    	else 
    		return null;
    }

    //-------------------------------------------------------
    // Message routing
    //-------------------------------------------------------

    public void routeMessage(Id destinationId, VirtualIdMessage message) {
    	message.messageVirtualId = destinationId;
        messageRouter.route(destinationId, message, null);
    }

    public void sendDirectMessage(NodeHandle destinationHandle, VirtualIdMessage message) {
    	message.messageVirtualId = null;
    	messageRouter.route(null, message, destinationHandle);
    }
    
    public void joinVirtualSpace( int virtualSpaceNumber, Application application, double capacity, ProtocolObserver observer ) {        
                	
    	VirtualLeafSet virtualLeafSet = new VirtualLeafSet(this);
    	VirtualNeighborSet virtualNeighborSet = new VirtualNeighborSet( node.getLeafSet().maxSize()+1, virtualSpaceNumber, this);
    	VirtualSpace virtualSpace = new VirtualSpace(virtualSpaceNumber, application, virtualLeafSet, virtualNeighborSet, node.getId(), capacity);

        virtualSpaceList.add(virtualSpaceNumber, virtualSpace);
    	VirtualIdProtocol joiningProtocol = protocolManager.createJoiningProtocol(this, virtualSpace);
    	if (observer != null)
    		joiningProtocol.addProtocolCompletionObserver(observer);

    	logger.info("Joining virtual space " + virtualSpaceNumber + " with capacity " + capacity + ".");

		joiningProtocol.startProtocol(node);
		
    }
        
    public void leaveVirtualSpace( int virtualSpaceNumber ) {
    	logger.fatal("Method leaveVirtualSpace is not implemented.");
        assert (false);
    }
    
    public void updateNodeCapacity( int virtualSpaceNumber, double capacity, ProtocolObserver observer ) {
    	
    	VirtualSpace virtualSpace = getVirtualSpace( virtualSpaceNumber );
    	virtualSpace.setCapacity(capacity);
    	VirtualIdProtocol updatingProtocol = protocolManager.createUpdateProtocol(this, virtualSpace);
    	if (observer != null)
    		updatingProtocol.addProtocolCompletionObserver(observer);

    	logger.info("Updating node capacity at virtual space " + virtualSpaceNumber + " with new capacity " + capacity + ".");
    	
    	updatingProtocol.startProtocol( node );

    }    
}
