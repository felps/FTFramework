package messages;

import moca.core.proxy.message.DefaultMessage;

/**
 * This class contains the array of results, the remote directory name, execution directory,
 * array of nodes directory and output file names array to response the request for specific 
 * finished execution application results. The server uses this class to response to clients.
 * 
 * @author Eduardo Viana
 */
public class ExecutionResultsResponseMessage extends DefaultMessage {
	
	
	
	/* ---------------------------------------------------------------------------- */
	/* Attributes */
	/* ---------------------------------------------------------------------------- */
	
	String []results;			// Array of Complete path of results
	String remoteDir;			// The remote directory name 
	String executionDir;		// The execution directory of submition
	String nodeDir[];			// Array of node directories
	String outputFileName[];	// Array of output file names
	
	
	
	
	
	
	/* ---------------------------------------------------------------------------- */
	/* Constructors */
	/* ---------------------------------------------------------------------------- */
		
	
	/**
	 * The default constructor.
	 * Instatiates an ExecutionResultsResponseMessage object with empty sender, addressee, 
	 * message type, data type and data.
	 */
	public ExecutionResultsResponseMessage(){
		super("","",0,"",null);
	}
	
	/**
	 * Another constructor.
	 * Instatiates an ExecutionResultsResponseMessage object with values to sender, addressee, 
	 * message type, data type and data.
	 * 
	 * @param sender is the client name reference  
	 * @param addressee is the addressee to receive the message
	 * @param msgType is the message type
	 * @param dataType is the type of extra data content
	 * @param data the extra data content
	 */
	public ExecutionResultsResponseMessage(String sender, String addressee, int msgType, 
			String dataType, byte data[]) {
		super(sender, addressee, msgType, dataType, data);
	}
	
	
	
	
	
	
	/* ---------------------------------------------------------------------------- */
	/* Getters and Setters Methods */
	/* ---------------------------------------------------------------------------- */
	
	
	/**
	 * Get the Array of Complete path of results
	 * @return array of String with complete path names of results
	 */
	public String[] getResults() {
		return results;
	}
	
	/**
	 * Set the Array of Complete path of results
	 * @param results is the array of String with complete path names of results
	 */
	public void setResults(String[] results) {
		
		this.results = results;
		
		if( results != null ){
			
			String details[] = results[0].split("/");
			remoteDir = details[1];
			executionDir = details[2];
			
			nodeDir = new String[ results.length ];
			outputFileName = new String[ results.length ];
			for( int i=0; i<results.length; i++ ){
				System.out.println( "results:"+results[i]);
				details = results[i].split("/");
				nodeDir[i] = details[3];
				outputFileName[i] = details[4];
			}
			
		}
		
		
	}
	
	/**
	 * Get the name of execution Dir
	 * @return the name of execution directory of submition
	 */
	public String getExecutionDir() {
		return executionDir;
	}
	
	/**
	 * Set the name of execution Dir
	 * @param executionDir the name of execution directory of submition
	 * 
	 */
	public void setExecutionDir(String executionDir) {
		this.executionDir = executionDir;
	}
	
	/**
	 * Get the array of node dirs.
	 * The node is related with each execution node result.
	 * @return array of String with node directories to bsp and parametric applications
	 */
	public String[] getNodeDir() {
		return nodeDir;
	}
	
	/**
	 * Set the array of node dirs.
	 * The node is related with each execution node result.
	 * @param nodeDir is the array of String with node directories
	 */
	public void setNodeDir(String[] nodeDir) {
		this.nodeDir = nodeDir;
	}
	
	/**
	 * Get the array of output file names.
	 * @return the array of output file names.
	 */
	public String[] getOutputFileName() {
		return outputFileName;
	}
	
	/**
	 * Set the array of output file names.
	 * @param outputFileName is the array of output file names.
	 */
	public void setOutputFileName(String[] outputFileName) {
		this.outputFileName = outputFileName;
	}
	
	/**
	 * Get the name of remote directory base where all results are stored.
	 * @return the remote diretctory name
	 */
	public String getRemoteDir() {
		return remoteDir;
	}

	/**
	 * Set the name of remote directory base where all results are stored.
	 * @param remoteDir is the remote diretctory name
	 */
	public void setRemoteDir(String remoteDir) {
		this.remoteDir = remoteDir;
	}
	
}
