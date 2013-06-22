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
public class KillApplicationRequestMessage extends DefaultMessage {
	
	long submitionId;
	
	public KillApplicationRequestMessage(){
		super("", "", 0, "", null);
	}
		
	public KillApplicationRequestMessage(String sender, String addressee, int msgType, 
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
