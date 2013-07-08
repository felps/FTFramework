/*
 * Created on 04/01/2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package messages;

import moca.core.proxy.message.DefaultMessage;

/**
 * @author eduardo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExecutionStatusRequestMessage extends DefaultMessage {
	
	String fromDate;
	String toDate;
	
	
	public ExecutionStatusRequestMessage(){
		super("", "", 0, "", null);
	}
		
	public ExecutionStatusRequestMessage(String sender, String addressee, int msgType, 
			String dataType, byte []data) {
		super(sender, addressee, msgType, dataType, data);
	}
	
	public String getFromDate() {
		return fromDate;
	}
	
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	
	public String getToDate() {
		return toDate;
	}
	
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
}
