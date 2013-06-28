package experiments;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.Test;

import wsct.Wsct;

public class Experiments {
//
//	@Test
//	public void trianaPartial() throws FileNotFoundException, RemoteException, MalformedURLException, NotBoundException, InterruptedException {
//		String[] args = {"", "./trianaParcial.yaml"};
//		Logger log = LogManager.getLogger("GLOBAL");
//		MyLogger.setLogger(log);
//		for(int i=0; i<10;i++){
//			MyLogger.getLogger().info("Starting " + i + "th evaulation");
//			//Wsct.main(args);
//			
//			MyLogger.getLogger().info("Sleeping for " + 10*60*1000 + "ms");
//			
//			Thread.sleep(10*60*1000);
//		}
//	}
	
	@Test
	public void tavernaBio() throws FileNotFoundException, RemoteException, MalformedURLException, NotBoundException {
		String[] args = {"", "./trianaParcial.yaml"};
		Wsct.main(args);

	}
}
