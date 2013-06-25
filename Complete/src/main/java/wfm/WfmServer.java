package wfm;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import logging.MyLogger;

import defaultTypes.*;

public class WfmServer {
	
	private static WfmImpl wfm;
	
	public WfmServer() {
		try {
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e1) {
			MyLogger.getLogger().warn("Registry not created. Perhaps previously created.");
			System.out.println("Registry not created. Perhaps previously created.");
		}
		
		try {
	       wfm = new WfmImpl();
	       Naming.rebind("rmi://localhost:1099/WFMService", wfm);
	     } catch (Exception e) {
	    	 MyLogger.getLogger().error("Trouble: " + e);
	       System.out.println("Trouble: " + e);
	     }
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws RemoteException, InterruptedException {
		
		new WfmServer();
        List<Workflow> runningWorkflows = null;
        
        if(wfm == null) {
        	MyLogger.getLogger().error("Não foi possível iniciar o servidor WFM. Encerrando...");
        	System.out.println("Não foi possível iniciar o servidor WFM. Encerrando...");
        	return;
        }
        	
		while (true) {
			System.out.println("WFM: Acordei");
			
			runningWorkflows = wfm.listWorkflows();
			
			for (Iterator iterator = runningWorkflows.iterator(); iterator.hasNext();) {
				Workflow workflow = (Workflow) iterator.next();
				
				System.out.println(workflow.getName());
				
				HashSet<Task> workflowTasks = workflow.getAllTasks();
				
				for (Iterator iterator2 = workflowTasks.iterator(); iterator2
						.hasNext();) {
					Task task = (Task) iterator2.next();
					
					System.out.println("Tarefa:    "+ task.getName());
					System.out.println("   Status: " + task.isComplete());
					
					MyLogger.getLogger().info("Tarefa:    "+ task.getName());
					MyLogger.getLogger().info("   Status: " + task.isComplete());
				}

				System.out.println("-----------------------------------");
				MyLogger.getLogger().info("-----------------------------------");
				wfm.evaluateWorkflow(workflow.getName());
			}
			Thread.sleep(10000);
		}
	}
}
