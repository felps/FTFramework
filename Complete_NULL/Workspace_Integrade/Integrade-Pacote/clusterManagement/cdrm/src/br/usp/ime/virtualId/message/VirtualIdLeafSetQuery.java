package br.usp.ime.virtualId.message;

import br.usp.ime.virtualId.NodeInformation;
import br.usp.ime.virtualId.protocol.VirtualIdProtocol.ProtocolType;
import rice.p2p.commonapi.NodeHandle;

public class VirtualIdLeafSetQuery extends VirtualIdProtocolMessage {

	private static final long serialVersionUID = -6021461258660605164L;
	
	public enum LeafSetQuerySide {LEFT, RIGHT};
	public LeafSetQuerySide leafSetSide;
	public ProtocolType protocolType = ProtocolType.JOINING;
	
	public NodeInformation[] nodeInfoArray;
	
	public VirtualIdLeafSetQuery(NodeHandle messageSourceHandle, int protocolSpaceNumber, int messageNumber) {
		super(messageSourceHandle, protocolSpaceNumber, messageNumber);
	}

}
