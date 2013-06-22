package br.usp.ime.oppstore.message;

import java.util.HashMap;

import br.usp.ime.oppstore.FileFragmentIndex;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;

/**
 * Communicates with the Pastry network
 *
 * @version April, 30 of 2007
 * @author Raphael Y. de Camargo
 */
public class CdrmReplicaInfoMessage extends CdrmMessage {
    
    private static final long serialVersionUID = 5746248244038506602L;

    public HashMap<Id, FileFragmentIndex> ffiMap;
            
    public enum CdrmInfoSide {LEFT, RIGHT};    
    public CdrmInfoSide cdrmInfoSide;
    
    public enum CdrmInfoType {STORE_JOIN, REMOVAL_JOIN, STORE_DEPART};
    public CdrmInfoType cdrmInfoType;
    
    /**
     * @param fileId
     * @param sourceCdrmHandle
     * @param fileFragmentIndex The FileInformationStructure to be stored  
     */
    public CdrmReplicaInfoMessage ( NodeHandle sourceCdrmHandle, CdrmInfoType infoType, CdrmInfoSide infoSide, int requestNumber ) {        
        super(sourceCdrmHandle, FileFragmentIndexMessage.ffiRoutingSpaceNumber, requestNumber);
        
        this.cdrmInfoSide = infoSide;
        this.cdrmInfoType = infoType;
        this.ffiMap = new HashMap<Id, FileFragmentIndex>();
    }
    
    public CdrmReplicaInfoMessage ( CdrmReplicaInfoMessage message ) {        
        super(message.sourceHandle, FileFragmentIndexMessage.ffiRoutingSpaceNumber, message.messageNumber);
        
        this.cdrmInfoSide = message.cdrmInfoSide;
        this.cdrmInfoType = message.cdrmInfoType;
        this.ffiMap = new HashMap<Id, FileFragmentIndex>();
        this.ffiMap.putAll( message.ffiMap );
    }    


    /**
     * From pastry's Message interface
     *
     * @return The priority of this message
     */
    public byte getPriority() {
        return Message.LOW_PRIORITY;
    }
        
    public String toString() {
        return "CdrmReplicaInfoMessage(" + messageNumber + ")";
    }
}
