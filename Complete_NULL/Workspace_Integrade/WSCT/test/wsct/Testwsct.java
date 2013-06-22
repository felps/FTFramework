package wsct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import wfm.wfm;
import defaultTypes.*;

import junit.framework.TestCase;

public class Testwsct extends TestCase {

	public void testLoadYamlAndExecution() throws FileNotFoundException {
		Workflow workflow = new Workflow();
		String file = "";
		InputStream input;
		
		file = "/home/felps/Documentos/Desenvolvimento/Workspace_Integrade/Learning/src/teste.yaml";
		String name = "TesteGlobal";
		input = new FileInputStream(new File(file));
		if (input != null)
			workflow = Wsct.loadYamlWorkflow(input, name);
		
		wfm.submitWorkflow(workflow);
		
		//Testar se as 8 tarefas foram incluidas
		assertEquals(8, workflow.getAllTasks().size());
		System.out.println(workflow.getAllTasks().size() + " atividades adicionadas corretamente");

		System.out.println("\niniciando execução\n");
		System.out.println("Tarefas prontas: " + wfm.getWorkflow(name).getReadyTasks().toString());
		
		//Evolução do Workflow
		
		//Execução da atividade stub
		workflow.getTask("0").setComplete();
		
		//Verificar se constam 2 atividades como prontas.
		Set<Task> tasks = wfm.getWorkflow(name).getReadyTasks();
		
		Iterator<Task> iter = tasks.iterator();
		
		for (; iter.hasNext();){
			Task task;
			
			task = iter.next();
			if (!task.getName().equalsIgnoreCase("1") && 
				!task.getName().equalsIgnoreCase("4")   ) {
				fail("Wrong activity evaluated as ready:" + task.getName() + " no passo 1");				
			}
		}
		
		//Conclusão da atividade 1
		workflow.getTask("1").setComplete();
		System.out.println("Tarefa 1 concluiu");
		System.out.println("Tarefas prontas: " + wfm.getWorkflow(name).getReadyTasks().toString());
		
		//As Atividades 2, 3 e 4 devem estar prontas
		for (; iter.hasNext();){
			Task task;
			
			task = iter.next();
			if (!task.getName().equalsIgnoreCase("2") &&
				!task.getName().equalsIgnoreCase("3") &&
				!task.getName().equalsIgnoreCase("4")   ) {
				fail("Wrong activity evaluated as ready;" + task.getName() + " no passo 2");				
			}
		}
		
		//Conclusão da atividade 4
		workflow.getTask("4").setComplete();
		System.out.println("Tarefa 4 concluiu");
		System.out.println("Tarefas prontas: " + wfm.getWorkflow(name).getReadyTasks().toString());

		//As Atividades 2, 3 e 5 devem estar prontas
		for (; iter.hasNext();){
			Task task;
			
			task = iter.next();
			if (!task.getName().equalsIgnoreCase("2") &&
				!task.getName().equalsIgnoreCase("3") &&
				!task.getName().equalsIgnoreCase("5")   ) {
				fail("Wrong activity evaluated as ready;" + task.getName() + " no passo 3");				
			}
		}
		
		//Conclusão da atividade 2
		workflow.getTask("2").setComplete();
		System.out.println("A tarefa 2 concluiu.");
		System.out.println("Tarefas prontas: " + wfm.getWorkflow(name).getReadyTasks().toString());
		
		//As Atividades 3 e 5 devem estar prontas
		for (; iter.hasNext();){
			Task task;
			
			task = iter.next();
			if (!task.getName().equalsIgnoreCase("3") &&
				!task.getName().equalsIgnoreCase("5")   ) {
				fail("Wrong activity evaluated as ready;" + task.getName() + " no passo 4");				
			}
		}
		
		//Conclusão da atividade 5
		workflow.getTask("5").setComplete();
		System.out.println("A tarefa 5 concluiu.");
		System.out.println("Tarefas prontas: " + wfm.getWorkflow(name).getReadyTasks().toString());
		
		//Apenas a Atividades 3 deve estar pronta
		for (; iter.hasNext();){
			Task task;
			
			task = iter.next();
			if (!task.getName().equalsIgnoreCase("3")   ) {
				fail("Wrong activity evaluated as ready;" + task.getName() + " no passo 5");				
			}
		}
		
		//Conclusão da atividade 3
		workflow.getTask("3").setComplete();
		System.out.println("A tarefa 3 concluiu");
		System.out.println("Tarefas prontas: " + wfm.getWorkflow(name).getReadyTasks().toString());
		
		//As Atividades 6 devem estar prontas
		for (; iter.hasNext();){
			Task task;
			
			task = iter.next();
			if (!task.getName().equalsIgnoreCase("6")   ) {
				fail("Wrong activity evaluated as ready;" + task.getName() + " no passo 6");				
			}
		}
		
		//Conclusão da atividade 6
		workflow.getTask("6").setComplete();
		System.out.println("A Tarefa 6 concluiu.");
		System.out.println("Tarefas prontas: " + wfm.getWorkflow(name).getReadyTasks().toString());
		
		//As Atividades 7 devem estar prontas
		for (; iter.hasNext();){
			Task task;
			
			task = iter.next();
			if (!task.getName().equalsIgnoreCase("7")   ) {
				fail("Wrong activity evaluated as ready;" + task.getName() + " no passo 7");				
			}
		}
		
		//Conclusão da atividade 7
		workflow.getTask("7").setComplete();
		System.out.println("A Tarefa 7 Concluiu.");
		System.out.println("Tarefas prontas: " + wfm.getWorkflow(name).getReadyTasks().toString());
		
		//Workflow Acabou
		assertEquals(true, wfm.getWorkflow(name).isComplete());
		wfm.endWorkflow(name);
		if ( wfm.getWorkflow(name).isComplete() )
		System.out.println("Workflow concluído com sucesso!");
	}

	public void testGlobalAleatorio() throws FileNotFoundException {
		Workflow workflow = new Workflow();
		String file = "";
		InputStream input;
		String name = "TesteGlobal";
		file = "/home/felps/Documentos/Desenvolvimento/Workspace_Integrade/Learning/src/teste.yaml";
		
		input = new FileInputStream(new File(file));
		if (input != null)
			workflow = Wsct.loadYamlWorkflow(input, name);
		
		//Testar se as 8 tarefas foram incluidas
		assertEquals(8, workflow.getAllTasks().size());
		System.out.println(workflow.getAllTasks().size() + " atividades adicionadas corretamente");

		System.out.println("\niniciando execução\n");
		
		Set<Task> tasks;
		tasks = workflow.getReadyTasks();
		this.printReadyTasks(workflow);

		wfm.submitWorkflow(workflow);
		
		//Evolução do Workflow
		while(!wfm.getWorkflow(name).isComplete()){
			tasks = wfm.getWorkflow(name).getReadyTasks();
			
			//Conclusão de atividade aleatoria
			//o indice eh sorteado entre 1 e indice
			int chosen = (int) Math.ceil((Math.random()*tasks.size()));
			
			//this.printTask(chosen, tasks);
			this.completeTask(chosen, tasks);
			this.printReadyTasks(wfm.getWorkflow(name));
		}
		
		//Workflow Acabou
		assertEquals(true, wfm.getWorkflow(name).isComplete());
		
		if ( wfm.getWorkflow(name).isComplete() )
		System.out.println("Workflow concluído com sucesso!");
	}
	
	private void completeTask(int index, Set<Task> tasks) {
		Task task;
		Iterator<Task> iter = tasks.iterator();
		
		for (int i=0; i<index && iter.hasNext(); i++){
			task = iter.next();
			if (i == index-1) {
				task.setComplete();
				System.out.println("Tarefa "+task.getName()+" concluída");
			}
			}
		
	}
	
	private void printReadyTasks(Workflow workflow) {
		Set<Task> tasks = workflow.getReadyTasks();
		Task task;
		
		Iterator<Task> iter = tasks.iterator();
		
		System.out.print("Tarefas prontas: ");
		for (; iter.hasNext();){
			task = iter.next();
			System.out.print(task.getName()+ " ");
		}
		System.out.println();
	}
	


}
