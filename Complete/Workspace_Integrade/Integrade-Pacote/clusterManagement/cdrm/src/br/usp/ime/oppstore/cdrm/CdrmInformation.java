package br.usp.ime.oppstore.cdrm;

import java.io.Serializable;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

public class CdrmInformation implements Serializable {

    private static final long serialVersionUID = 4870030885517742732L;

    public Id adaptiveId;
    public Id originalNodeId;
    public NodeHandle nodeHandle;
    public double capacity;
    
    public CdrmInformation (Id originalId, Id adaptiveId, NodeHandle nodeHandle, double capacity) {
        this.originalNodeId = originalId;
        this.adaptiveId = adaptiveId;
        this.nodeHandle = nodeHandle;
        this.capacity = capacity;
    }
    
    public CdrmInformation (CdrmInformation cdrmInfo) {
        this.originalNodeId = cdrmInfo.originalNodeId;
        this.adaptiveId = cdrmInfo.adaptiveId;
        this.nodeHandle = cdrmInfo.nodeHandle;
        this.capacity = cdrmInfo.capacity;        
    }
    
}
