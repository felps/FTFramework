package wfm;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import defaultTypes.*;

public class WfmServer {
	
	private static WfmImpl wfm;
	
	public WfmServer() {
		try {
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e1) {
			System.out.println("Registry not created. Perhaps previously created.");
		}
		
		try {
	       wfm = new WfmImpl();
	       Naming.rebind("rmi://localhost:1099/WFMService", wfm);
	     } catch (Exception e) {
	       System.out.println("Trouble: " + e);
	     }
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws RemoteException, InterruptedException {
		
		new WfmServer();
        List<Workflow> runningWorkflows = null;
        
        if(wfm == null) {
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
				}

				System.out.println("-----------------------------------");
				wfm.evaluateWorkflow(workflow.getName());
			}
			Thread.sleep(10000);
		}
	}
}
