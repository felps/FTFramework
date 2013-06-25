package ftec;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class AppSpecificFTECServer {

	private AppSpecificFTEC ftec;
	
	public AppSpecificFTECServer() throws InterruptedException {
		try {
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e1) {
			System.out.println("Registry not created. Perhaps previously created.");
		}
		 
		try {
		       ftec = new AppSpecificFTEC();
		       Naming.rebind("rmi://localhost:1099/AppSpecific", ftec);
		     } catch (Exception e) {
		    	 System.out.println("Erro associando ao Registry");
		    	 System.out.println("Trouble: " + e);
		     }
	}
	
	public static void main(String[] args) throws RemoteException, InterruptedException {
		new AppSpecificFTECServer();
	}
}