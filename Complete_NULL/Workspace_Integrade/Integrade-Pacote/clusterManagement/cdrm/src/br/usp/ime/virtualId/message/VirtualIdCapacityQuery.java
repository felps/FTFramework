package br.usp.ime.virtualId.message;

import br.usp.ime.virtualId.NodeInformation;
import br.usp.ime.virtualId.message.VirtualIdUpdateMessage.LeafSetSide;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.Id;

public class VirtualIdCapacityQuery extends VirtualIdProtocolMessage {

	private static final long serialVersionUID = -6621184723739667536L;

	public LeafSetSide leafSetSide;
	public double capacity;
	public Id virtualId;
	public NodeInformation[] nodeInformationArray;
	
	public VirtualIdCapacityQuery(NodeHandle messageSourceHandle, int protocolSpaceNumber, int messageNumber) {
		super(messageSourceHandle, protocolSpaceNumber, messageNumber);
		
	}


}
