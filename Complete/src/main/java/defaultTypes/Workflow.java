package defaultTypes;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

public class Workflow implements Serializable{

	/**
	 * 
	 */
	//VARIABLES
	private String name;
	private HashSet<Task> tasks;
	private static final long serialVersionUID = 1L;
	
	//CONSTRUCTOR
	public Workflow(){
		this.tasks = new HashSet<Task>();
		this.name = "undefined";
	}

	//GETTERS AND SETTERS

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isComplete() {
		Task task;
		
		Iterator<Task> iter = this.tasks.iterator();
		
		for (; iter.hasNext();){
			task = iter.next();
			if (!task.isComplete())
				return false;
		}
		return true;
	
	}

	public void setTasks(Task task) {
		this.tasks.add(task);
	}
	
	public Task getTask(String name) {
		Task task;

		Iterator<Task> iter = this.tasks.iterator();
		
		for (; iter.hasNext();){
			task = iter.next();
			if (task.getName().equalsIgnoreCase(name))
				return task;
		}
		return null;
	}

	public HashSet<Task> getAllTasks() {
		return tasks;
	}

	public HashSet<Task> getReadyTasks() {
		Task task = new Task();
		HashSet<Task> readyTasks = new HashSet<Task>();
		
		Iterator<Task> iter = this.tasks.iterator();
		
		for (; iter.hasNext();){
			task = iter.next();
			if (task.isReady())
				readyTasks.add(task);
		}
		
		return readyTasks;
	}
	
}
