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
public class KillApplicationResponseMessage extends DefaultMessage {
	
	public KillApplicationResponseMessage(){
		super("","",0,"",null);
	}
	
	/**
	 * @param addressee
	 * @param sender
	 * @param msgType
	 * @param dataType
	 * @param object
	 */
	public KillApplicationResponseMessage(String sender, String addressee, int msgType, 
			String dataType, byte data[]) {
		super( sender, addressee, msgType, dataType, data );
	}

}
