package br.usp.ime.virtualId;

import java.io.Serializable;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

/**
 * Information about a node inside a single virtual space.
 * @author rcamargo
 *
 */
public class NodeInformation implements Serializable {

    private static final long serialVersionUID = 4870030885517742732L;

    private Id nodeId;
    private NodeHandle nodeHandle;

    private Id virtualId;
    private double capacity;
    
    public void setVirtualId(Id virtualId) { this.virtualId = virtualId; }
    public void setCapacity(double capacity) { this.capacity = capacity; }
    
    public Id getNodeId() { return nodeId; }    
    public Id getVirtualId() { return virtualId; }
    public NodeHandle getNodeHandle() { return nodeHandle; }
    public double getCapacity() { return capacity; }
    
    public NodeInformation (Id originalId, Id adaptiveId, NodeHandle nodeHandle, double capacity) {
        this.nodeId = originalId;
        this.virtualId = adaptiveId;
        this.nodeHandle = nodeHandle;
        this.capacity = capacity;
    }
    
    public NodeInformation (NodeInformation nodeInfo) {
        this.nodeId = nodeInfo.nodeId;
        this.virtualId = nodeInfo.virtualId;
        this.nodeHandle = nodeInfo.nodeHandle;
        this.capacity = nodeInfo.capacity;        
    }
    
}
