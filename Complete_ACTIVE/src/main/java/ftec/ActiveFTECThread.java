package ftec;

import java.rmi.RemoteException;

public class ActiveFTECThread implements Runnable {
	public ActiveFTEC parent;
	public int uid;

	@Override
	public void run() {
		Process runningInstance = null;
		runningInstance = parent.startExecution(
				parent.submitedTask.getBinaryLocation(),
				parent.submitedTask.getAppDescriptionFile());
		if (parent.waitAndVerifyCompletion(runningInstance)) {
			parent.endExecution(runningInstance);
		}

		try {
			parent.reportFinishedFtec(uid, parent.submitedTask.getName(),
					parent.submitedTask.getWorkflow());
		} catch (RemoteException e) {
			System.out.println("Erro 1: run do retry ftec");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Erro 2: run do retry ftec");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}