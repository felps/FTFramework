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
public class SubmitApplicationRequestMessage extends DefaultMessage {
	
	
	
	// aspectos comuns de execução
	String basePath;
	String applicationName;
	String applicationPreferences;
	String applicationContraints;
	String binaryNames;
	int applicationType;
	
	// aspectos especificos do tipo de execução
	String applicationArguments;				// Regular and BSP
	String inputFiles[];						// Regular and BSP
	String outputFiles[];						// Regular and BSP
	
	ParametricCopy parametricCopies[];			// Parametric 
	int numberOfTasks = 0;						// BSP
	boolean forceDifferentMachines;				// BSP and Parametric
	
	
	
	public SubmitApplicationRequestMessage(){
		super("", "", 0, "", null);
	}
	
	/**
	 * @param addressee
	 * @param sender
	 * @param msgType
	 * @param dataType
	 * @param object
	 */
	public SubmitApplicationRequestMessage(String sender, String addressee, int msgType, 
			String dataType, byte data[]) {
		super(sender, addressee, msgType, dataType, data);
	}
	
	
	
	public String getApplicationArguments() {
		return applicationArguments;
	}

	public void setApplicationArguments(String applicationArguments) {
		this.applicationArguments = applicationArguments;
	}

	public String getApplicationContraints() {
		return applicationContraints;
	}

	public void setApplicationContraints(String applicationContraints) {
		this.applicationContraints = applicationContraints;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationPreferences() {
		return applicationPreferences;
	}

	public void setApplicationPreferences(String applicationPreferences) {
		this.applicationPreferences = applicationPreferences;
	}

	public int getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(int applicationType) {
		this.applicationType = applicationType;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getBinaryNames() {
		return binaryNames;
	}

	public void setBinaryNames(String binaryNames) {
		this.binaryNames = binaryNames;
	}

	public boolean isForceDifferentMachines() {
		return forceDifferentMachines;
	}

	public void setForceDifferentMachines(boolean forceDifferentMachines) {
		this.forceDifferentMachines = forceDifferentMachines;
	}

	public String[] getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(String[] inputFiles) {
		this.inputFiles = inputFiles;
	}

	public int getNumberOfTasks() {
		return numberOfTasks;
	}

	public void setNumberOfTasks(int numberOfTasks) {
		this.numberOfTasks = numberOfTasks;
	}

	public String[] getOutputFiles() {
		return outputFiles;
	}

	public void setOutputFiles(String[] outputFiles) {
		this.outputFiles = outputFiles;
	}

	public ParametricCopy[] getParametricCopies() {
		return parametricCopies;
	}

	public void setParametricCopies(ParametricCopy parametricCopies[]) {
		this.parametricCopies = parametricCopies;
	}
	
}
