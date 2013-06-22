package br.usp.ime.oppstore.message;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;

/**
 * Communicates with the Pastry network
 *
 * @version February, 09 of 2006
 * @author Raphael Camargo
 */
public class StoreFragmentMessage extends CdrmMessage {
       
    private static final long serialVersionUID = 6889502391111528099L;
    
    public static final int fragmentRoutingSpaceNumber = 1;
    
    public String brokerAddress;
    public String adrAddress;
    public Id fileId;
    public Id fragmentId;   
    public int fragmentSize;
    public int nStorageTrials = 5;
    public int timeoutMinutes;
    public boolean isCacheRequest = false;
    
    public StoreFragmentMessage
    (Id fileId, Id fragmentId, int fragmentSize, NodeHandle sourceCdrmHandle, int messageNumber, int timeoutMinutes, String brokerAddress) {        
        super(sourceCdrmHandle, fragmentRoutingSpaceNumber, messageNumber);
    
        this.fileId = fileId;
        this.fragmentId = fragmentId;
        this.fragmentSize = fragmentSize;        
        this.timeoutMinutes = timeoutMinutes;
        this.brokerAddress = brokerAddress;
        this.adrAddress = null;
    }
    
    public void setAdrAddress(String adrAddress) {
        this.adrAddress = adrAddress;        
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
        return "StoreMessage(" + messageNumber + ")";
    }
}
