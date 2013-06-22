package ftec;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import defaultTypes.*;
import ftm.TaskSubmission;

public abstract class Ftec 
	extends java.rmi.server.UnicastRemoteObject
	implements Runnable, FtecInterface, Serializable{


	//----------------------------------//
	//VARIABLES							//
	//----------------------------------//
	protected String chosenFtecStrategy;
	protected int uid;
	protected Task submitedTask;
	private static final long serialVersionUID = 1L;
	private String IGHOME = "/home/felps/integrade/integrade";
	
	//DEBUG
	private static int timesRan = 1;
	
	//----------------------------------//
	//CONSTRUCTOR						//
	//----------------------------------//
	
	public Ftec() throws RemoteException {
		super();
		this.chosenFtecStrategy = this.getFtecType();
		this.submitedTask = null;
	}
	
	//----------------------------------//
	//GETTERS AND SETTERS				//
	//----------------------------------//

	//DEBUG 
	public static int getTimesRan() {
		return timesRan++;
	}
	
	public String getChosenFtecStrategy() {
		return chosenFtecStrategy;
	}
	
	public String getIGHOME() {
		return IGHOME;
	}

	public void setIGHOME(String integradeHOME) {
		IGHOME = integradeHOME;
	}

	public int getUid() {
		return uid;
	}
	
	public Task getSubmitedTask() {
		return submitedTask;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}
	
	public abstract String getFtecType();
	
	public abstract String getAppBinaryLocation();
	
	public abstract String getAppDescriptorFileLocation();
	
	public abstract String getConfigFile();
	
	public abstract int getFtecId();
	
	public abstract Task getTask();
	
	/*---------------------------------------
	 * Methods
	 * --------------------------------------
	 */

	@Override
	public void run() {
		Process runningApp = 
			this.startExecution(this.getAppBinaryLocation(), this.getAppDescriptorFileLocation());
		this.waitCompletion(runningApp);
		this.endExecution(runningApp);
		
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
	

	public abstract Long startFtecThread(String configFile, Task submittedTask, int uid);

	protected Process startExecution(String appBinaryType, String configFileLocation){
		//DEBUG
		System.out.println("Esta é a " + getTimesRan() + "-ésima vez que dah um startExecution");
		
		Process runningApp = null;
		
		try {
			runningApp = Runtime.getRuntime().exec( getIGHOME() + "/startservices.sh asctText -i "+ configFileLocation +" -b " + appBinaryType);
		} catch (IOException e) {
			System.out.println("Erro na submissão da tarefa " + submitedTask.getName());
			System.out.println("Comando: " + getIGHOME() + "/startservices.sh asctText -i "+ configFileLocation +" -b " + appBinaryType);
			e.printStackTrace();
		}
		System.out.println(getIGHOME() + "/startservices.sh asctText -i "+ configFileLocation +" -b " + appBinaryType);
		return runningApp;
	}
	
	protected void waitCompletion(Process runningApp){
		try {
			runningApp.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void endExecution(Process runningApp) {
		
		System.out.println("Aplicativo terminou com " + runningApp.exitValue());
		
		try {
			this.reportFinishedFtec(this.getFtecId(), this.getTask().getName(), this.getTask().getName());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected synchronized void reportFinishedFtec(int ftecId, String taskName, String workflowName) throws RemoteException, InterruptedException {
		reportFinishedFtecToFTM(ftecId);
		this.reportFinishedFtecToWFM();		
	}
	
	protected synchronized void reportFinishedFtecToWFM() {
		//Stub to allow overriding by Workflow-Enabled FTECs
	}

	protected static void reportFinishedFtecToFTM(int ftecId) throws InterruptedException, RemoteException {
		//Submit Task to FTM
		TaskSubmission ftm = getFtmObject();
			
		if (ftm !=null) {
        	ftm.ftecFinished(ftecId);
        } else {
        	System.out.println("Erro de RMI no FTEC > FTM");
        }
	}

	public static int requestFtecThread(String configFile, Task submittedTask, String chosenFtecService, int uid) {
		FtecInterface ftec = getFtecObject(chosenFtecService);
		try {
			ftec.startFtecThread(configFile, submittedTask, uid);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro: FTEC > requestFtecThread");
			e.printStackTrace();
			return -1;
		}
		
		System.out.println("FTEC > reqFtecThread");
		System.out.println(submittedTask.toString());
		System.out.println(submittedTask.getName());
		
		return uid;
	}
		
	public static FtecInterface getFtecObject(String ftecServiceName) {
		FtecInterface ftec = null;
		try { 
            ftec = (FtecInterface)
                           Naming.lookup(
                 "rmi://localhost:1099/"+ftecServiceName); 
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
        return ftec;
	}

	private static TaskSubmission getFtmObject() {
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
