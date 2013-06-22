package br.usp.ime.oppstore.message;

import rice.p2p.commonapi.NodeHandle;
import br.usp.ime.virtualId.message.VirtualIdMessage;

public class CdrmMessage extends VirtualIdMessage {
	private static final long serialVersionUID = 3808588076206043718L;

	public CdrmMessage(NodeHandle messageSourceHandle, int routingVirtualSpaceNumber, int messageNumber) {
		super(messageSourceHandle, routingVirtualSpaceNumber, messageNumber);
	}
}
