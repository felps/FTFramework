package ftec;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import wfm.WorkflowControl;

import defaultTypes.*;

//import java.io.IOException;

public class RetryFTEC extends Ftec implements Runnable , Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Boolean complete;
	//private final int defaultErrorCode = 0;
	//private String taskName;
	//private String workflowName;
	private String appBinaryLocation;
	private String inputFileLocation;
	private String configFile;
	private int uid;
	private Long threadId;
	@SuppressWarnings("unused")
	private String threadName;
	
	
	public RetryFTEC() throws RemoteException {
		super();
		this.complete = false;
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getAppBinaryLocation() {
		return appBinaryLocation;
	}

	@Override
	public String getConfigFile() {
		// TODO Auto-generated method stub
		return configFile;
	}

	@Override
	public int getFtecId() {
		// TODO Auto-generated method stub
		return uid;
	}

	@Override
	public String getAppDescriptorFileLocation() {
		// TODO Auto-generated method stub
		return inputFileLocation;
	}

	@Override
	public Task getTask() {
		// TODO Auto-generated method stub
		return submitedTask;
	}

	@Override
	public String getFtecType() {
		return "Retry";
	}
	
	@Override
	public void run() {
		while(!this.complete) {
			Process runningInstance = null;
			runningInstance = 
				this.startExecution(this.submitedTask.getBinaryLocation(), this.submitedTask.getAppDescriptionFile(), this.getTask().getOutputFile());
			this.waitCompletion(runningInstance);
			this.endExecution(runningInstance);
		}
		
		try {
			reportFinishedFtec(this.uid, this.submitedTask.getName(), this.submitedTask.getWorkflow());
		} catch (RemoteException e) {
			System.out.println("Erro 1: run do retry ftec");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Erro 2: run do retry ftec");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Long startFtecThread(String configFile, Task task, int uid) {

		RetryFTEC ftec;
		try {
			ftec = new RetryFTEC();
		} catch (RemoteException e) {
			System.out.println("Erro: RetryFTEC > startFtecThread");
			e.printStackTrace();
			return -1L;
		}
		
		ftec.setUid(uid);
		ftec.submitedTask = task;
		
		//Create a thread to FTEC
		Thread ftecExecution = new Thread(ftec);
		ftecExecution.setName("Executor Tarefa: " + task.getName() + " ID: " + ftecExecution.getName() + "\"");
		
		System.out.println("Retry FTEC > startThread");
		System.out.println(ftec.submitedTask.toString());
		System.out.println(ftec.submitedTask.getName());
		System.out.println(ftec.submitedTask.getWorkflow());
		
		//Store its id
		ftec.threadId = ftecExecution.getId();
		ftec.threadName = ftecExecution.getName();
		
		//Start the thread
		ftecExecution.start();
		
		return ftec.threadId;
	}
	
	/*@Override
	protected Process startExecution(String appBinaryLocation, String inputFileLocation) {
		Process runningApp = null;
		try {
			runningApp = Runtime.getRuntime().exec("kdialog --msgbox \"Esta_e_a_tarefa_" + this.submitedTask.getName() + "_do_Workflow_" + this.submitedTask.getWorkflow() + "\"");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro na criação do Processo!");
			e.printStackTrace();
		}
		return runningApp;
	}
	
	@Override
	protected void waitCompletion(Process runningApp) {
		try {
			runningApp.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	*/
	protected void endExecution(Process runningApp) {
		super.endExecution(runningApp);
		if (runningApp.exitValue() == 0)
			this.complete = true;
	}
	
	@Override
	protected synchronized void reportFinishedFtecToWFM() {
		WorkflowControl wfm = getWorkflowControl();
		
		try {
			wfm.executeTask(this.submitedTask.getWorkflow(), this.submitedTask.getName());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro de RMI: FTEC > WFM");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Retry ftec > reportFinishedToWfm");
			e.printStackTrace();
		}
	}
	
	private WorkflowControl getWorkflowControl() {
		WorkflowControl c = null;
		try { 
        	c = (WorkflowControl)
                           Naming.lookup(
                 "rmi://localhost/WFMService");
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
        catch (
            java.lang.ArithmeticException
                                      ae) { 
            System.out.println(); 
            System.out.println(
             "java.lang.ArithmeticException"); 
            System.out.println(ae); 
        }
        return c;

	}
}
