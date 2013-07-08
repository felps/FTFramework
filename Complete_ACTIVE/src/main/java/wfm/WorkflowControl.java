package wfm;

import java.util.List;
import java.util.Set;

import defaultTypes.*;

public interface WorkflowControl extends java.rmi.Remote{

	public void helloWorld() throws java.rmi.RemoteException;
	
	public void submitWorkflow(Workflow workflow) throws java.rmi.RemoteException;

	public void endWorkflow(String name) throws java.rmi.RemoteException;
	
	public void killWorkflow(String name)throws java.rmi.RemoteException;
	
	public Workflow getWorkflow(String name)throws java.rmi.RemoteException;
	
	public Set<Task> getReadyTasks(String name)throws java.rmi.RemoteException;
	
	public void executeTask(String workflowName, String taskName) throws java.rmi.RemoteException, InterruptedException;
	
	public void evaluateWorkflow(String name) throws InterruptedException, java.rmi.RemoteException;
	
	public List<Workflow> listWorkflows() throws java.rmi.RemoteException;
}
