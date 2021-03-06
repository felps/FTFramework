package wsct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logging.MyLogger;

import org.yaml.snakeyaml.Yaml;

import wfm.WorkflowControl;
import defaultTypes.*;

public class Wsct {

	private static Set<Task> allTasks = new HashSet<Task>();

	public static void main(String args[]) throws FileNotFoundException,
			RemoteException, MalformedURLException, NotBoundException {
		boolean file_exists = false;
		Workflow workflow = null;
		String file = args[1];
		String name = "workflow1";
		InputStream input = null;

		while (!file_exists) {

			// System.console().readLine("Entre com o nome do arquivo", file);
			// Arquivo do grm
			// file =
			// "/home/aluno/FelipePontes/Desenvolvimento/Complete/teste.yaml";
			// Arquivo Local
			// file =
			// "/home/felps/Dropbox/Desenvolvimento/Workspace_Integrade/Complete/teste.yaml";
			file_exists = true;

			input = new FileInputStream(new File(file));
		}
		if (input != null) {
			workflow = loadYamlWorkflow(input, name);
			String time = getTime();
			System.out.println("Workflow submitted at " + time + ": "
					+ workflow.toString());
			WorkflowControl wfm = getRemoteObject();
			if (wfm != null) {
				// wfm.helloWorld();
				wfm.submitWorkflow(workflow);
				System.out.println("Workflow submetido com sucesso!");
				MyLogger.getLogger().info("Workflow submetido com sucesso!");
			} else {
				System.out.println("WSCT: Erro de RMI");
				MyLogger.getLogger().error("WSCT: Erro de RMI");
			}
		}
	}

	private static String getTime() {
		String time;
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		time = sdf.format(cal.getTime());
		return time;
	}

	@SuppressWarnings("unchecked")
	public static Workflow loadYamlWorkflow(InputStream input, String name) {
		// TODO Auto-generated method stub
		Yaml yaml = new Yaml();
		Workflow workflow = new Workflow();
		workflow.setName(name);

		for (Object data : yaml.loadAll(input)) {
			System.out.println(data);
			Map m = (Map) data;

			Task task = new Task();
			allTasks.add(task);

			task.setWorkflow(name);
			task.setBinaryLocation("" + m.get("binary"));
			task.setInputFile("" + m.get("inputfile"));
			task.setOutputFile("" + m.get("outputfile"));
			task.setName("" + m.get("name"));

			List dependencyList = (List) m.get("dependencies");

			if (dependencyList != null) {
				for (int i = 0; i < dependencyList.size(); i++) {
					task.setDependsOn(getPreviousTask(""
							+ dependencyList.get(i)));
				}
			}

			System.out.println("ID: " + task.toString() + "\nName: "
					+ task.getName() + "\n Binary: " + task.getBinaryLocation()
					+ "\n Input: " + task.getAppDescriptionFile()
					+ "\n Output: " + task.getOutputFile()
					+ "\n Dependencies: " + task.getDependsOn().toString()
					+ "\n\n");
			MyLogger.getLogger().info(
					"ID: " + task.toString() + "\nName: " + task.getName()
							+ "\n Binary: " + task.getBinaryLocation()
							+ "\n Input: " + task.getAppDescriptionFile()
							+ "\n Output: " + task.getOutputFile()
							+ "\n Dependencies: "
							+ task.getDependsOn().toString() + "\n\n");

			workflow.setTasks(task);
		}
		return workflow;
	}

	private static Task getPreviousTask(String name) {
		// TODO Auto-generated method stub
		Task task;
		Iterator<Task> iter = allTasks.iterator();

		for (; iter.hasNext();) {
			task = iter.next();
			if (task.getName().equalsIgnoreCase(name))
				return task;
		}

		return null;

	}

	public static WorkflowControl getRemoteObject() {
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
