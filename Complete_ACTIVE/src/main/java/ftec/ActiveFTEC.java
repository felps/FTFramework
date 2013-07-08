package ftec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import wfm.WorkflowControl;

import defaultTypes.*;

//import java.io.IOException;

public class ActiveFTEC extends Ftec implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean complete;
	private int tries;
	// private final int defaultErrorCode = 0;
	// private String taskName;
	// private String workflowName;
	private String appBinaryLocation;
	private String inputFileLocation;
	private String configFile;
	private int uid;
	private Long threadId;
	@SuppressWarnings("unused")
	private String threadName;
	private ActiveFTECThread data = new ActiveFTECThread();


	public ActiveFTEC() throws RemoteException {
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
		return "Active";
	}

	@Override
	public void run() {
		for (int i = 0; i < this.tries; i++) {
			ActiveFTECThread thread1 = new ActiveFTECThread();
			thread1.uid = this.uid;
			thread1.parent = this;
			new Thread(thread1).start();
		}
			
	}

	@Override
	public Long startFtecThread(String configFile, Task task, int uid) {

		ActiveFTEC ftec;
		try {
			ftec = new ActiveFTEC();
		} catch (RemoteException e) {
			System.out.println("Erro: RetryFTEC > startFtecThread");
			e.printStackTrace();
			return -1L;
		}

		ftec.setUid(uid);
		ftec.submitedTask = task;

		// Create a thread to FTEC
		Thread ftecExecution = new Thread(ftec);
		ftecExecution.setName("Executor Tarefa: " + task.getName() + " ID: "
				+ ftecExecution.getName() + "\"");

		System.out.println("Retry FTEC > startThread");
		System.out.println(ftec.submitedTask.toString());
		System.out.println(ftec.submitedTask.getName());
		System.out.println(ftec.submitedTask.getWorkflow());

		// Store its id
		ftec.threadId = ftecExecution.getId();
		ftec.threadName = ftecExecution.getName();

		// Start the thread
		ftecExecution.start();

		return ftec.threadId;
	}

	@Override
	protected Process startExecution(String appBinaryLocation,
			String inputFileLocation) {
		Process runningApp = null;
		try {
			runningApp = Runtime.getRuntime().exec(
					"zenity --info --text \"Esta_e_a_tarefa_"
							+ this.submitedTask.getName() + "_do_Workflow_"
							+ this.submitedTask.getWorkflow() + "\"");
		} catch (IOException e) {
			System.out.println("Erro na criação do Processo!");
			e.printStackTrace();
		}
		return runningApp;
	}

	// @Override protected void waitCompletion(Process runningApp) { try {
	// runningApp.waitFor(); } catch (InterruptedException e) {
	// e.printStackTrace(); } try { Thread.sleep(5000); } catch
	// (InterruptedException e) { e.printStackTrace(); } }
	//

	protected Boolean waitAndVerifyCompletion(Process runningApp) {
		Boolean success = false;

		BufferedReader br = new BufferedReader(new InputStreamReader(
				runningApp.getInputStream()));

		try {
			while (br.ready()) {
				String str = br.readLine();
				System.out.println(str);
				if (str.contains("Exec Finished!!!")) {
					success = true;
				}
			}
			runningApp.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	@Override
	protected void endExecution(Process runningApp) {
		super.endExecution(runningApp);
		if (runningApp.exitValue() == 0)
			this.complete = true;
	}

	@Override
	protected synchronized void reportFinishedFtecToWFM() {
		WorkflowControl wfm = getWorkflowControl();

		try {
			wfm.executeTask(this.submitedTask.getWorkflow(),
					this.submitedTask.getName());
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
			c = (WorkflowControl) Naming.lookup("rmi://localhost/WFMService");
		} catch (MalformedURLException murle) {
			System.out.println();
			System.out.println("MalformedURLException");
			System.out.println(murle);
		} catch (RemoteException re) {
			System.out.println();
			System.out.println("RemoteException");
			System.out.println(re);
		} catch (NotBoundException nbe) {
			System.out.println();
			System.out.println("NotBoundException");
			System.out.println(nbe);
		} catch (java.lang.ArithmeticException ae) {
			System.out.println();
			System.out.println("java.lang.ArithmeticException");
			System.out.println(ae);
		}
		return c;

	}
}
