package wfm;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import defaultTypes.*;
import ftm.TaskSubmission;

public class WfmImpl 
				extends java.rmi.server.UnicastRemoteObject 
				implements WorkflowControl{
	
	private static final long serialVersionUID = 1L;
	private  List<Workflow> runningWorkflows = new ArrayList<Workflow>();
	
	/*
	 * Constructor
	 */
	public WfmImpl() throws RemoteException {
		super();
	}
	
	/*
	 * Methods
	 */
	
	public void submitWorkflow(Workflow workflow) {
		runningWorkflows.add(workflow);
	}
	
	public  void endWorkflow(Workflow workflow){
		runningWorkflows.remove(workflow);
	}
	
	public  void endWorkflow(String name){
		List<Workflow> list = runningWorkflows;
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getName().equalsIgnoreCase(name) && list.get(i).isComplete()){
				list.remove(list.get(i));
				return;
			}
		}
	}
	
	public  void killWorkflow(String name){
		List<Workflow> list = runningWorkflows;
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getName().equalsIgnoreCase(name)){
				list.remove(list.get(i));
				return;
			}
		}
	}
	
	public  Workflow getWorkflow(String name){
		List<Workflow> list = runningWorkflows;
		Workflow originalWorkflow = null;
		
		for(int i=0; i<list.size(); i++){
			originalWorkflow = (Workflow)list.get(i);
			if (originalWorkflow.getName().equalsIgnoreCase(name)){
				return originalWorkflow;
			}
		}
		return null;
	}
	
	public  HashSet<Task> getReadyTasks(String name){
		Workflow workflow = null;
		List<Workflow> list = runningWorkflows;
		
		for(int i=0; i<list.size(); i++){
			workflow = list.get(i);
			if ((workflow.getName().equalsIgnoreCase(name))){
				return workflow.getReadyTasks();
			}
		}
		return null;	
	}
	
	public  void executeTask(String workflowName, String taskName) throws RemoteException, InterruptedException{
		List<Workflow> list = runningWorkflows;
		
		for(int i=0; i<list.size(); i++){
			if ((list.get(i).getName().equalsIgnoreCase(workflowName))){
				list.get(i).getTask(taskName).setComplete();
				this.evaluateWorkflow(list.get(i).getName());
			}
		}
		
	}
	
	public static boolean isFinished(Workflow workflow) {
		// Workflow is finished as long as all of its tasks are as well
		Iterator<Task> iter = workflow.getAllTasks().iterator();
		
		for(;iter.hasNext();){
			Task current= iter.next();
			
			// Any one task which is incomplete is enough for the entire workflow to be also
			if (!current.isComplete())	{
				return false;
			}
		}
		
		return true;
	}
	
	public void evaluateWorkflow(String name) throws InterruptedException, RemoteException{
		Workflow workflow = getWorkflow(name);
		HashSet<Task> readyTasks = workflow.getReadyTasks();
		
		Iterator<Task> iter = readyTasks.iterator();

		TaskSubmission ftm = getFtmObject();
		
		for(;iter.hasNext();){
			Task current= iter.next();
			
			if (!ftm.isSubmitted(current))	{
				submitTask(current);
			}
		}
		
		if (workflow.isComplete()) {
			endWorkflow(workflow.getName());
		}
		
	}
	
	private void submitTask(Task task) throws InterruptedException, RemoteException {
		//Submit Task to FTM
		TaskSubmission ftm = getFtmObject();
		
        if (ftm !=null) {
        	ftm.submitTask(task);
        } else {
        	System.out.println("Erro de RMI no WFM");
        }
	}
	
	public List<Workflow> listWorkflows() {
		/**for (Iterator<Workflow> iterator1 = runningWorkflows.iterator(); iterator1.hasNext();) {
			Workflow workflow = (Workflow) iterator1.next();
			/**System.out.println(workflow.toString());
			for (Iterator iterator2 = workflow.getAllTasks().iterator(); iterator2.hasNext();) {
				Task task = (Task) iterator2.next();
				System.out.println(task.toString());
			}
		}*/
		return runningWorkflows;
	}

	@Override
	public void helloWorld() throws RemoteException {
		System.out.println("Hello World");
	}
	
	private TaskSubmission getFtmObject() {
		TaskSubmission ftm = null;
		try { 
            ftm = (TaskSubmission)
                           Naming.lookup(
                 "rmi://localhost:1099/FTMService"); 
        } 
        catch (MalformedURLException murle) { 
            System.out.println(); 
            System.out.println(
              "MalformedURLException"); 
            System.out.println(murle); 
        } 
        catch (RemoteException re) { 
            System.out.println(); 
            System.out.println(
                        "RemoteException"); 
            System.out.println(re); 
        }

        catch (NotBoundException nbe) { 
            System.out.println(); 
            System.out.println(
                       "NotBoundException"); 
            System.out.println(nbe); 
        } 
        
        return ftm;
	}
}
