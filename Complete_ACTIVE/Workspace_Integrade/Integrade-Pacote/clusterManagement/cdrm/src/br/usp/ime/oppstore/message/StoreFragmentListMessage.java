package br.usp.ime.oppstore.message;

import java.util.Vector;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;

/**
 * Communicates with the Pastry network
 *
 * @version February, 09 of 2006
 * @author Raphael Camargo
 */
public class StoreFragmentListMessage extends CdrmMessage {
       
    private static final long serialVersionUID = 6889502391111528099L;

    public static final int fragmentRoutingSpaceNumber = 1;
    
    public String brokerAddress;
    public Vector<String> adrAddressList;
    
    public Id fileId;
    public Vector<Id> fragmentIdList;   
    public Vector<Long> fragmentSizeList;
    public int timeoutMinutes;
        
    public StoreFragmentListMessage (Id fileId, Vector<Id> fragmentIdList, Vector<Long> fragmentSizeList, 
            NodeHandle sourceCdrmHandle, int messageNumber, int timeoutMinutes, String brokerAddress) {
        
        super(sourceCdrmHandle, fragmentRoutingSpaceNumber, messageNumber);
    
        this.fileId = fileId;
        this.fragmentIdList = fragmentIdList;
        this.fragmentSizeList = fragmentSizeList;        
        this.adrAddressList = new Vector<String>();
        this.timeoutMinutes = timeoutMinutes;
        this.brokerAddress = brokerAddress;
    }
    
    public void addAdrAddress(String adrAddress) {
        this.adrAddressList.add( adrAddress );        
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
