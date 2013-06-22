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
public class OutputFileResponseMessage extends DefaultMessage {
	
	String filePath;
	String fileName;
	String remoteDir;
	String executionDir;
	String nodeName;
	byte fileContent[];
	
	
	public OutputFileResponseMessage(){
		super("", "", 0, "", null);
	}
	
	
	/**
	 * @param addressee
	 * @param sender
	 * @param msgType
	 * @param dataType
	 * @param object
	 */
	public OutputFileResponseMessage(String sender, String addressee, int msgType, 
			String dataType, byte []data) {
		super(sender, addressee, msgType, dataType, data);
	}
	
	public String getExecutionDir() {
		return executionDir;
	}


	public void setExecutionDir(String executionDir) {
		this.executionDir = executionDir;
	}


	public String getFilePath() {
		return filePath;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
		
		if( filePath != null ){
			
			String []tokens = filePath.split("/");
			this.remoteDir = tokens[0];
			this.executionDir = tokens[1];
			this.nodeName = tokens[2];
			this.fileName = tokens[3];
		}
		
	}


	public String getNodeName() {
		return nodeName;
	}


	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}


	public String getRemoteDir() {
		return remoteDir;
	}
	
	public void setRemoteDir(String remoteDir) {
		this.remoteDir = remoteDir;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public byte[] getFileContent() {
		return fileContent;
	}


	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
}
