package br.usp.ime.oppstore.message;

import br.usp.ime.oppstore.FileFragmentIndex;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;

/**
 * Communicates with the Pastry network
 *
 * @version February, 09 of 2006
 * @author Raphael Camargo
 */
public class FileFragmentIndexMessage extends CdrmMessage {
    
	public enum RequestType {STORAGE, RETRIEVAL, REMOVAL, RENEWAL};
	
    private static final long serialVersionUID = 5746248244038506602L;

    public static final int ffiRoutingSpaceNumber = 0;
    
    public Id fileId;
    public int numberOfReplicas;
    public FileFragmentIndex fileFragmentIndex;
    
    public RequestType requestType;
    public int newTimeout;
            
    public boolean isReplicaRequest;

    /**
     * 
     * @param fileId
     * @param sourceCdrmHandle
     * @param fileFragmentIndex The FileInformationStructure to be stored  
     */
    public FileFragmentIndexMessage ( 
            Id fileId, FileFragmentIndex fileFragmentIndex, RequestType requestType, 
            int numberOfReplicas, NodeHandle sourceCdrmHandle, int requestNumber ) {        
        super(sourceCdrmHandle, ffiRoutingSpaceNumber, requestNumber);
        
        this.requestType = requestType;
        this.numberOfReplicas = numberOfReplicas;
        this.fileId = fileId;
        this.fileFragmentIndex = fileFragmentIndex;
        
        this.isReplicaRequest = false;
    }    

    public FileFragmentIndexMessage (FileFragmentIndexMessage fileInformationMessage) {
        super(fileInformationMessage.sourceHandle, ffiRoutingSpaceNumber, fileInformationMessage.messageNumber);
        
        this.fileId = fileInformationMessage.fileId; 
        this.numberOfReplicas = fileInformationMessage.numberOfReplicas; 
        this.fileFragmentIndex = fileInformationMessage.fileFragmentIndex;            
        this.requestType = fileInformationMessage.requestType;
        
        this.isReplicaRequest = fileInformationMessage.isReplicaRequest;
        this.isResponse = fileInformationMessage.isResponse;
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
        return "StoreFileInformationMessage(" + messageNumber + ")";
    }

}
