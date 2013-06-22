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
public class SubmitApplicationResponseMessage extends DefaultMessage {
	
	long submitionId;
	
	
	public SubmitApplicationResponseMessage(){
		super("", "", 0, "", null);
	}
	
	
	/**
	 * @param addressee
	 * @param sender
	 * @param subject
	 * @param msgType
	 * @param dataType
	 * @param object
	 */
	public SubmitApplicationResponseMessage(String sender, String addressee, int msgType, 
			String dataType, byte []data) {
		super(sender, addressee, msgType, dataType, data);
	}
	
	
	
	
	
	public long getSubmitionId() {
		return submitionId;
	}
	public void setSubmitionId(long submitionId) {
		this.submitionId = submitionId;
	}
}
