package wfm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

import defaultTypes.Task;
import defaultTypes.Workflow;

import junit.framework.TestCase;

public class TestWFM extends TestCase {

	private static Set<Task> allTasks = new HashSet<Task>();
	private WorkflowControl control = null;

	public TestWFM() throws RemoteException {
		super();
		this.control = getRemoteObject();
	}
	public final void testListWorkflows() throws FileNotFoundException, RemoteException, MalformedURLException, NotBoundException {
		testSubmitWorkflow();	
		this.control.listWorkflows();
	}

	public final void testSubmitWorkflow() throws FileNotFoundException, RemoteException, MalformedURLException, NotBoundException {

	boolean file_exists = false;
	Workflow workflow = null;
	String file = "";
	String name = "workflow1";
	InputStream input = null;
	
	while(!file_exists){
		
		//System.console().readLine("Entre com o nome do arquivo", file);
		file = "/home/felps/Documentos/Desenvolvimento/Workspace_Integrade/Learning/src/teste.yaml";
		file_exists = true;
		
		input = new FileInputStream(new File(file));
	}
	if (input != null){
		workflow = loadYamlWorkflow(input, name);
		//System.out.println(workflow.toString());
		if (control != null) {
			control.submitWorkflow(workflow);
		} else {
			System.out.println("WSCT: Erro de RMI");
			assertEquals(true, false);
		}
	}
}

@SuppressWarnings("unchecked")
public static Workflow loadYamlWorkflow(InputStream input, String name) {
	// TODO Auto-generated method stub
	Yaml yaml = new Yaml();
	Workflow workflow = new Workflow();
	workflow.setName(name);
	
	for (Object data : yaml.loadAll(input)) {
	    //System.out.println(data);
	    Map m = (Map) data;
	    
		Task task = new Task();
		allTasks.add(task);
		
		task.setWorkflow(name);
		task.setBinaryLocation(""+m.get("binary"));
		task.setInputFile(""+m.get("inputfile"));
		task.setOutputFile(""+m.get("outputfile"));
		task.setName(""+m.get("name"));
		
		List dependencyList = (List)m.get("dependencies");

		if (dependencyList != null){
			for (int i=0; i< dependencyList.size(); i++ ){
				task.setDependsOn(getPreviousTask(""+dependencyList.get(i)));
			}
		}
		
		//System.out.println("ID: "+ task.toString() + "\nName: " + task.getName() + "\n Binary: " + task.getBinaryLocation() + "\n Input: " + task.getInputFile() + "\n Output: " + task.getOutputFile() + "\n Dependencies: " + task.getDependsOn().toString() +"\n\n" );
		workflow.setTasks(task);
	}
	return workflow;
}


private static Task getPreviousTask(String name) {
	// TODO Auto-generated method stub
	Task task;
    Iterator<Task> iter = allTasks.iterator();
	
	
	for (; iter.hasNext();){
		 task = iter.next();
		if (task.getName().equalsIgnoreCase(name))
			return task;
	}
	
	return null;

}

public static WorkflowControl getRemoteObject() throws RemoteException { 
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
