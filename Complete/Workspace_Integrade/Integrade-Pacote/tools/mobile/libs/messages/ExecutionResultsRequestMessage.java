package messages;

import moca.core.proxy.message.DefaultMessage;

/**
 * This class contains the submition Id to request the execution results of the specific 
 * finished execution application. The clients use this class to request that service
 * on Grid Proxy.
 * 
 * @author Eduardo Viana
 */
public class ExecutionResultsRequestMessage extends DefaultMessage {
	
	
	
	/* ---------------------------------------------------------------------------- */
	/* Attributes */
	/* ---------------------------------------------------------------------------- */
	
	// The submition id to request the execution results of the specific 
	// finished execution application.
	long submitionId;	
	
	
	
	
	
	
	/* ---------------------------------------------------------------------------- */
	/* Constuctors */
	/* ---------------------------------------------------------------------------- */
	
	/**
	 * The default constructor.
	 * Instatiates an ExecutionResultsRequestMessage object with empty sender, addressee, 
	 * message type, data type and data.
	 */
	public ExecutionResultsRequestMessage(){
		super("", "", 0, "", null);
	}
		
	/**
	 * Another constructor.
	 * Instatiates an ExecutionResultsRequestMessage object with values to sender, addressee, 
	 * message type, data type and data.
	 * 
	 * @param sender is the client name reference  
	 * @param addressee is the addressee to receive the message
	 * @param msgType is the message type
	 * @param dataType is the type of extra data content
	 * @param data the extra data content
	 */
	public ExecutionResultsRequestMessage(String sender, String addressee, int msgType, 
			String dataType, byte []data) {
		super(sender, addressee, msgType, dataType, data);
	}
	
	
	
	
	
	
	
	/* ---------------------------------------------------------------------------- */
	/* Getters and Setters Methods */
	/* ---------------------------------------------------------------------------- */
	
	
	/**
	 * Get the Submition Id sent into ExecutionResultsRequestMessage object.
	 * @return the submition Id
	 */
	public long getSubmitionId() {
		return submitionId;
	}
	
	/**
	 * Set the Submition Id to be sent into ExecutionResultsRequestMessage object.
	 * @param submitionId
	 */
	public void setSubmitionId(long submitionId) {
		this.submitionId = submitionId;
	}
}
