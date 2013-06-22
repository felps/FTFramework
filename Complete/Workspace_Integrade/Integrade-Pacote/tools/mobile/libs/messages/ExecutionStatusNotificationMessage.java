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
public class ExecutionStatusNotificationMessage extends DefaultMessage {
	
	String statusNotification;
	String appName;
	long submitionId;
	
	
	/**
	 * @return Returns the appName.
	 */
	public String getAppName() {
		return appName;
	}
	/**
	 * @param appName The appName to set.
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
	 * @return Returns the submitionId.
	 */
	public long getSubmitionId() {
		return submitionId;
	}
	/**
	 * @param submitionId The submitionId to set.
	 */
	public void setSubmitionId(long submitionId) {
		this.submitionId = submitionId;
	}
	public ExecutionStatusNotificationMessage(){
		super("", "", 0, "", null);
	}
	
	/**
	 * @param client
	 * @param string
	 * @param i
	 * @param object
	 * @param object2
	 */
	public ExecutionStatusNotificationMessage(String sender, String addressee, int msgType, 
			String dataType, byte []data) {
		super(sender, addressee, msgType, dataType, data);
	}
	
	
	
	
	public String getStatusNotification() {
		return statusNotification;
	}
	public void setStatusNotification(String statusNotification) {
		this.statusNotification = statusNotification;
	}
}
