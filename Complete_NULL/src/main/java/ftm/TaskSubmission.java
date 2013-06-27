package ftm;

import defaultTypes.Task;

public interface TaskSubmission extends java.rmi.Remote{

	public void submitTask(Task task) throws java.rmi.RemoteException, InterruptedException ;
	
	public boolean isFinished(Task task) throws java.rmi.RemoteException;
	
	public boolean isSubmitted(Task task) throws java.rmi.RemoteException;
	
	public void ftecFinished(int ftecId) throws java.rmi.RemoteException;
	
}
