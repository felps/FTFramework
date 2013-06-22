package br.usp.ime.virtualId.protocol;

import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.message.VirtualIdProtocolMessage;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

/**
 * Sends a message to a target node.
 * This class waits for the target node to respond to the original request.
 * If the target node does not respond in a given time, the dispatcher retries sending the message up to N times.
 * If it cannot deliver the message, it can callback a registered Interface to notify the impossiblity of message delivery. 
 * 
 * All the virtualIdProtocolMessages pass through this interface, when sending, receiving, and responding the messages.
 * 
 * @author rcamargo
 *
 */
public class VirtualMessageDispatcher extends Thread {
	
	// We already have the RequestResponseController.
	// We would only need to generalize that class
	
	class VirtualIdProtocolMessageInfo {
		VirtualIdProtocolMessage message;
		NodeHandle targetHandle;
		VirtualIdProtocol protocol;
		
		int nRetries;
		int currentRetry;
		long waitTime;
		
	};
	
	/**
	 * Contains a map from message identifiers to the sent messages.
	 * Is used to resend messages which received no responses.
	 */
	private TreeMap<Long, VirtualIdProtocolMessageInfo> sentMessageMap;	
	
	private HashSet<Integer> receivedResponseSet;
	
    private Lock mapLock = new ReentrantLock(); 

    private AtomicInteger nextMessageIndentifier = new AtomicInteger(0);
   
    private VirtualNode virtualNode;
	    
	public VirtualMessageDispatcher( VirtualNode virtualNode ) {
		
		this.sentMessageMap      = new TreeMap<Long, VirtualIdProtocolMessageInfo>();
		this.receivedResponseSet = new HashSet<Integer>();
		this.virtualNode         = virtualNode;
	}
	
    /**
     * Starts thread
     */
    public void run () {
        while (true) {
            
        	try {Thread.sleep(1000); } catch (Exception e) {}
            long currentTime = System.currentTimeMillis();            
            
            mapLock.lock();
            while (sentMessageMap.size() > 0 && sentMessageMap.firstKey() < currentTime) {
            	
                VirtualIdProtocolMessageInfo messageInfo = sentMessageMap.remove( sentMessageMap.firstKey() );
                if ( receivedResponseSet.contains( messageInfo.message.dispatcherMessageIdentifier ) == false) {

                    virtualNode.getLogger().debug("VirtualMessageDispatcher -> Wait time expired. Resending message at " + virtualNode.getNode().getId() + " to " + messageInfo.targetHandle.getId() );
                	virtualNode.sendDirectMessage(messageInfo.targetHandle, messageInfo.message);                	
                	messageInfo.currentRetry++;
                	
                	if (messageInfo.currentRetry < messageInfo.nRetries ) {
                		
                		long resendTime = currentTime + messageInfo.waitTime;
                		while ( sentMessageMap.containsKey( resendTime ) )
                			resendTime++;		
                		sentMessageMap.put(resendTime, messageInfo);
                	}           
                	else {
                		messageInfo.protocol.setMessageNotDelivered( messageInfo.targetHandle );
                	}                		
                }                                    
            }
            mapLock.unlock();
        }
    }

	   
	private void addRequestToMap( NodeHandle targetHandle, VirtualIdProtocolMessage message, long waitTime, int nRetries, VirtualIdProtocol protocol ) {
		
		VirtualIdProtocolMessageInfo messageInfo = new VirtualIdProtocolMessageInfo();
		messageInfo.message      = message;
		messageInfo.targetHandle = targetHandle;
		messageInfo.currentRetry = 0;
		messageInfo.nRetries     = nRetries;
		messageInfo.waitTime     = waitTime;
		messageInfo.protocol     = protocol;
		
        mapLock.lock();
		long resendTime = System.currentTimeMillis() + waitTime;
		while ( sentMessageMap.containsKey( resendTime ) )
			resendTime++;		
		sentMessageMap.put(resendTime, messageInfo);
		mapLock.unlock();
	}

	public boolean setMessageReceived ( VirtualIdProtocolMessage sourceMessage ) {

		return receivedResponseSet.add( sourceMessage.dispatcherMessageIdentifier );		
	}

	/**
	 * Sends a message directly to node targetHandle;
	 * Retries the message sends if it does not receives a confirmation.
	 */
	public void sendDirectMessage( NodeHandle targetHandle, VirtualIdProtocolMessage message, boolean retry, VirtualIdProtocol protocol ) {
		
		message.dispatcherMessageIdentifier = nextMessageIndentifier.getAndIncrement();
		
		if (retry == true) { 
			
			long waitTime = 5000;
			int nRetries  = 3;
			addRequestToMap(targetHandle, message, waitTime, nRetries, protocol);
		}
		
		virtualNode.sendDirectMessage(targetHandle, message);
	}

	/**
	 * Routes a message to node with id targetId. 
	 * Does not retry message sending.
	 */
	public void routeMessage( Id targetId, VirtualIdProtocolMessage message ) {
		
		message.dispatcherMessageIdentifier = nextMessageIndentifier.getAndIncrement();
		virtualNode.routeMessage(targetId, message);
	}

}
