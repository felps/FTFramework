package br.usp.ime.virtualId.message;

import rice.p2p.commonapi.NodeHandle;

public class VirtualIdProtocolMessage extends VirtualIdMessage {

	private static final long serialVersionUID = 5495211982981279730L;
	
	public int protocolSpaceNumber;
	
	public boolean needResponse = true;
	
	public int dispatcherMessageIdentifier = -1;

	public VirtualIdProtocolMessage(NodeHandle messageSourceHandle, int protocolSpaceNumber, int messageNumber) {
		super(messageSourceHandle, 0, messageNumber);
		this.protocolSpaceNumber = protocolSpaceNumber;
	}

	public boolean equals(Object obj) {
		if (obj instanceof VirtualIdProtocolMessage) {
			VirtualIdProtocolMessage message = (VirtualIdProtocolMessage) obj;

			if (this.dispatcherMessageIdentifier == message.dispatcherMessageIdentifier && 
					this.sourceHandle.getId() == message.sourceHandle.getId())
				return true;					
		}
		
		return false;
	}
	
	public String toString() {
		 return this.sourceHandle.getId() + String.valueOf(dispatcherMessageIdentifier); 
	}
	
	public String toString(NodeHandle sourceHandle) {
		 return sourceHandle.getId() + String.valueOf(dispatcherMessageIdentifier); 
	}
	
	public byte getPriority() { return HIGH_PRIORITY; }
}
