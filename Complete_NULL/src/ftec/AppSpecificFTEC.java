package ftec;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import wfm.WorkflowControl;

import defaultTypes.Task;

import appSpecificFTEC.*;

@SuppressWarnings("serial")
public class AppSpecificFTEC extends Ftec {

	private static final long serialVersionUID = 1L;
	private Boolean complete;
	private String appBinaryLocation;
	private String inputFileLocation;
	private String configFile;
	private int uid;
	public Long threadId;
	@SuppressWarnings("unused")
	private String threadName;
	

	public AppSpecificFTEC() throws RemoteException {
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
		return "NULL";
	}
	


	@Override
	public void run() {
		Process runningInstance = null;

		System.out.println("Aplicativo submetido as:");
       	printSysDate();

       	while(!this.complete){
	       	runningInstance = this.startExecution(	this.submitedTask.getBinaryLocation(), 
					this.submitedTask.getAppDescriptionFile(), 
					this.getTask().getOutputFile()
				 );
	
			this.waitCompletion(runningInstance);
			this.endExecution(runningInstance);
       	}
       	
		try {
			reportFinishedFtec(this.uid, this.submitedTask.getName(), this.submitedTask.getWorkflow());
		} catch (RemoteException e) {
			System.out.println("Erro 1: run do NULL ftec");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Erro 2: run do NULL ftec");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		System.out.println("Aplicativo concluido as:");
       	printSysDate();
	}

	@Override
	public Long startFtecThread(String configFile, Task task, int uid) {

		AppSpecificFTEC ftec;
		try {
			ftec = new AppSpecificFTEC();
		} catch (RemoteException e) {
			System.out.println("Erro: NullFTEC > startFtecThread");
			e.printStackTrace();
			return -1L;
		}
		
		ftec.setUid(uid);
		ftec.submitedTask = task;
		
		//Create a thread to FTEC
		Thread ftecExecution = new Thread(ftec);
		ftecExecution.setName("Executor Tarefa: " + task.getName() + " ID: " + ftecExecution.getName() + "\"");
		
		System.out.println("AppSpecificFTEC > startThread");
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
	
	protected void endExecution(Process runningApp) {
		if (runningApp.exitValue() == 0){
			this.complete = true;
			super.endExecution(runningApp);
		}
		else
		{
			// Get the descriptor for the Blast App
			String appDescriptorName = this.getTask().getAppDescriptionFile();
			
			// Parse it
			AppDescriptor appDescriptor = AppSpecLib.parseDescriptor(appDescriptorName);
			
			// inputFasta is the input file with ".fasta" in its name
			String inputFastaFile = appDescriptor.getFastaFile();
			
			// blastOutputfile is the file with ".out" in its name
			String blastOutputFilename = appDescriptor.getOutputFile();
			
			//Create new fasta input file
			String newFastaInputFilename = appDescriptor.getOutputFile().concat("_new");
			AppSpecLib.evaluateOutputAndCreateNewFasta(inputFastaFile, blastOutputFilename, newFastaInputFilename);
			
			// Set it in the descriptor
			appDescriptor.setFastaFile(newFastaInputFilename);
			
			// Write it to a file
			String newDescriptorFile = appDescriptorName;
			appDescriptor.dumpAppDescriptor(newDescriptorFile);
			
			// set the local variables to a new execution
			this.configFile = newDescriptorFile;
			
			//TODO: Setar o argumento ou preservar o nome do arquivo fasta!
			
			//All set for a new execution;
		}
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
			System.out.println("Null ftec > reportFinishedToWfm");
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
