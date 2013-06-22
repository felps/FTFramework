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
public class OutputFileRequestMessage extends DefaultMessage {
	
	String fileName;
	
	
	public OutputFileRequestMessage(){
		super("", "", 0, "", null);
	}
		
	public OutputFileRequestMessage(String sender, String addressee, int msgType, 
			String dataType, byte []data) {
		super(sender, addressee, msgType, dataType, data);
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
