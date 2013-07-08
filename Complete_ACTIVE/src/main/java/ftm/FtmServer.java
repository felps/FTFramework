package ftm;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class FtmServer {

	private FtmImpl ftm;
	
	public FtmServer() throws InterruptedException {
		try {
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e1) {
			System.out.println("Registry not created. Perhaps previously created.");
		}
		try {
		       ftm = new FtmImpl();
		       Naming.rebind("rmi://localhost:1099/FTMService", ftm);
		     } catch (Exception e) {
		       System.out.println("Trouble: " + e);
		     }
	}
	
	public static void main(String[] args) throws RemoteException, InterruptedException {
		new FtmServer();
		/*while(true) {
			System.out.println("FTM: Acordei");
			Thread.sleep(15000);
		}*/
	}
}
