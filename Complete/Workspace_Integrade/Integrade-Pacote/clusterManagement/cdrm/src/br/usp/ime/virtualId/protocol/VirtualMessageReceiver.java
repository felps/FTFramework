package br.usp.ime.virtualId.protocol;

import java.util.HashMap;

import rice.p2p.commonapi.NodeHandle;

import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.message.VirtualIdProtocolMessage;

public class VirtualMessageReceiver {

    /**
     * Need to remove messages from this map;
     */
    HashMap<String, VirtualIdProtocolMessage> receivedMessageSet;

    private VirtualNode virtualNode;
	    
	public VirtualMessageReceiver( VirtualNode virtualNode ) {
		
		//this.sentMessageMap     = new TreeMap<Long, VirtualIdProtocolMessageInfo>();
		this.receivedMessageSet = new HashMap<String, VirtualIdProtocolMessage>();
		this.virtualNode        = virtualNode;
	}

	/**
	 * Adds the message to the received message list.
	 * If the message is succesfully added, return true.
	 * Otherwise, if the message already was in the list, return false.
	 */
	public boolean setMessageReceived ( VirtualIdProtocolMessage sourceMessage ) {
		
		/**
		 * The message was already received. Try to resend the response to the source handle. 
		 */
		if ( receivedMessageSet.containsKey( sourceMessage.toString() ) ) {
			
			virtualNode.getLogger().debug("Message already received from " + sourceMessage.sourceHandle.getId() + " " + sourceMessage.dispatcherMessageIdentifier + ".");
			if (sourceMessage.needResponse == true) {
				virtualNode.getLogger().debug("Resending response to " + sourceMessage.sourceHandle.getId() + ".");
				sendResponse( sourceMessage.sourceHandle, receivedMessageSet.get( sourceMessage.toString() ) );
			}
			return false;
		}
		
		/**
		 * The message was not received and will be processed by the current node. 
		 */
		else {
			receivedMessageSet.put( sourceMessage.toString(), sourceMessage );
			return true;
		}
	}
    
	public void sendResponse( NodeHandle targetHandle, VirtualIdProtocolMessage message ) {
				
		/**
		 * Updates the message in the leafSet with the new one
		 */
		VirtualIdProtocolMessage previousMessage = receivedMessageSet.put ( message.toString(targetHandle), message );
		assert (previousMessage != null);
		message.isResponse = true;
		
		virtualNode.sendDirectMessage(targetHandle, message);
	}

}
