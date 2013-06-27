package ftm;

import ftec.*;
import ftsm.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import defaultTypes.*;

public class FtmImpl 
				extends java.rmi.server.UnicastRemoteObject
				implements TaskSubmission{
	
	private static 	final 	long serialVersionUID = 1L;
	
	private final 	int SCHEDULED = 0;
	//private final 	int RUNNING = 4;
	private final	int COMPLETE = 8;
	
	private 		int	identifier = 0;
	
	private List<FtecRecord> runningFTECs;
	
	private class FtecRecord {
		public int 	ftecId;
		public int 	status;
		//public int 	solicitor;
		//public Long threadId;
		public Task task;
		public String strategy;
		public String configFile;
		
		private void addTask(Task task) {
			this.task = task;
		}
	}
	
	public FtmImpl() throws RemoteException {
		super();
		runningFTECs = new ArrayList<FtecRecord>();
		
	}

	private synchronized int getIdentifier() {
		identifier++;
		return (identifier - 1);
	}

	private static boolean sameTasks(Task task1, Task task2) {
		if(task1.getName().contentEquals(task2.getName())  && task1.getWorkflow().contentEquals(task2.getWorkflow()) ) {
			return true;
		} else {
			return false;
		}
	}
	public synchronized void submitTask(Task task) throws InterruptedException {
		// If the task is already scheduled, then do nothing
		for (int i = 0; i < runningFTECs.size(); i++) {
			if(sameTasks(task, runningFTECs.get(i).task)) {
				return;
			}
		}
		
		FtecRecord newFtec = new FtecRecord();
		
		newFtec.addTask(task);
		newFtec.ftecId = getIdentifier();
		newFtec.status = SCHEDULED;
		newFtec.configFile = "";
		
		//Define FTEC to be used
		try {
			newFtec.strategy = ftsm.DecisionTreeApp.getFtecName("decisionTree.yaml");
		} catch (IOException e) {
			System.out.println("Erro no parsing da arvore");
			e.printStackTrace();
		}
		
		runningFTECs.add(newFtec);
		
		System.out.println("FTM > submitTask");
		System.out.println(task.toString());
		System.out.println(task.getName());
		
		Ftec.requestFtecThread(newFtec.configFile, task, newFtec.strategy, newFtec.ftecId);
		
	}

	public synchronized boolean isFinished(Task task) {
		for (Iterator<FtecRecord> iterator = runningFTECs.iterator(); iterator.hasNext();) {
			FtecRecord record = (FtecRecord) iterator.next();
			if (record.task == task && record.status == COMPLETE) 
				return true;
		}
		return false;
	}
	
	public synchronized boolean isSubmitted(Task task) {
		for (Iterator<FtecRecord> iterator = runningFTECs.iterator(); iterator.hasNext();) {
			FtecRecord record = (FtecRecord) iterator.next();
			if (sameTasks(record.task , task)) {
				return true;
			}
		}
		return false;
	}
			
	public synchronized void ftecFinished(int ftecId) {
		for (Iterator<FtecRecord> iterator = runningFTECs.iterator(); iterator.hasNext();) {
			FtecRecord currentFtec = (FtecRecord) iterator.next();
			if (currentFtec.ftecId == ftecId) {
				currentFtec.status = COMPLETE;
				currentFtec.task.setComplete();
			}
		}
		
	}
}
