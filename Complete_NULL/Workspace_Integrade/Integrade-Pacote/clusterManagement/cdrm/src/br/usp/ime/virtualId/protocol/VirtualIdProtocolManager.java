package br.usp.ime.virtualId.protocol;

import rice.p2p.commonapi.Id;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;

public class VirtualIdProtocolManager {
	
    private VirtualIdProtocol virtualIdProtocol = null;
    
    // JoiningProtocol    
    public VirtualIdProtocol createJoiningProtocol (VirtualNode virtualNode, VirtualSpace virtualSpace) {
        virtualIdProtocol = new VirtualIdJoiningProtocol(virtualNode, virtualSpace);
        virtualSpace.setCurrentProtocol( virtualIdProtocol );
        return virtualIdProtocol;
    }

    // DepartureProtocol    
    public VirtualIdProtocol createDepartureProtocol (VirtualNode virtualNode, VirtualSpace virtualSpace, Id leavingNodeId) {
        virtualIdProtocol = new VirtualIdDepartureProtocol(virtualNode, virtualSpace, leavingNodeId);
        virtualSpace.setCurrentProtocol( virtualIdProtocol );
        return virtualIdProtocol;
    }
    
    // UpdateProtocol
    public VirtualIdProtocol createUpdateProtocol ( VirtualNode virtualNode, VirtualSpace virtualSpace ) {
    	virtualIdProtocol = new VirtualIdUpdateProtocol( virtualNode, virtualSpace );
    	virtualSpace.setCurrentProtocol( virtualIdProtocol );
    	return virtualIdProtocol;    	
    }
    
    public void finishProtocol() { this.virtualIdProtocol = null; }
    
}
