package br.usp.ime.virtualId;

import br.usp.ime.virtualId.protocol.VirtualIdProtocol;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

public class VirtualSpace {

    private VirtualNeighborSet virtualNeighborSet;
    private VirtualLeafSet virtualLeafSet;
    private Application virtualApp;
    private int virtualSpaceNumber;
    
    private VirtualIdProtocol currentProtocol;
    
    private Id virtualId;
    private double capacity;
    
    //-------------------------------------------------------------------------------
    
    public VirtualSpace (int virtualSpaceNumber, Application app, 
    		VirtualLeafSet leafSet, VirtualNeighborSet neighborSet, Id virtualId, double capacity) {
        this.virtualId = virtualId;
    	this.virtualSpaceNumber = virtualSpaceNumber;
        this.virtualApp = app;
        this.virtualLeafSet = leafSet;
        this.virtualNeighborSet = neighborSet;
        this.capacity = capacity;
        this.currentProtocol = null;
    }
    
    //-------------------------------------------------------------------------------
    
    public void setCurrentProtocol(VirtualIdProtocol protocol) { this.currentProtocol = protocol; }
    public VirtualIdProtocol getCurrentProtocol() { return currentProtocol; }
    
    public void setVirtualId(Id virtualId) { this.virtualId = virtualId; }    
    public Id getVirtualId() { return virtualId; }
    public Id getCcwVirtualId() { 
    	Id ccwVirtualId = virtualNeighborSet.getCcwVirtualId(virtualId); 
    	if (ccwVirtualId == null) return null;
    	return ccwVirtualId;
    }
    
    public int getVirtualSpaceNumber() { return virtualSpaceNumber; }
    
    public void setCapacity (double capacity) { this.capacity = capacity; }
    public double getCapacity() { return capacity; }    
   
    public VirtualNeighborSet getVirtualNeighborSet() { return virtualNeighborSet; }
    
    public VirtualLeafSet getVirtualLeafSet() { return virtualLeafSet; }
    
    public Application getApplication() { return virtualApp; }
}
