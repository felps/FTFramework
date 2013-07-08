package defaultTypes;

import java.io.Serializable;
import java.util.Iterator;
import java.util.HashSet;

public class Task implements Serializable {
	
	//VARIABLES
	private static final long serialVersionUID = 1L;
	private String name;
	private String workflow;
	private String binaryType;
	private String appDescritionFile;
	private String outputFile;
	private	HashSet<Task> dependsOn;
	private boolean complete;

	//CONSTRUCTOR
	public Task(){
		this.dependsOn = new HashSet<Task>() ;
		this.name = "undefined";
		this.binaryType = "undefined";
		this.complete = false;
	}

	//GETTERS AND SETTERS

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}
	
	public String getWorkflow() {
		return workflow;
	}
	
	public String getAppDescriptionFile() {
		return appDescritionFile;
	}

	public void setInputFile(String inputFile) {
		this.appDescritionFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete() {
		this.complete = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getDependsOn(Task task) {
		if(this.dependsOn.contains(task))
			return true;
		else
			return false;
	}
	
	public HashSet<Task> getDependsOn() {
		return this.dependsOn;
	}

	public void setDependsOn(Task task) {
		this.dependsOn.add(task);
	}

	public String getBinaryLocation() {
		return binaryType;
	}

	public void setBinaryLocation(String binaryLocation) {
		this.binaryType = binaryLocation;
	}
	
	public boolean isReady(){
		Task task;
		boolean ready;
		
		if ( this.isComplete() )
			ready = false;
		else
			ready = true;
		
		Iterator<Task> iter = this.dependsOn.iterator();
		
		for (; iter.hasNext();){
			task = iter.next();
			if (!task.isComplete()){
				ready = false;
			}
		}
		return ready;
	}
	
	
}
