package experiments;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.Test;

import wsct.Wsct;

public class Experiments {

//	@Test
//	public void tavernaBio() throws FileNotFoundException, RemoteException, MalformedURLException, NotBoundException {
//		String[] args = {"", "./tavernaBio.yaml"};
//		Wsct.main(args);
//
//	}	
//	
//	@Test
//	public void trianaPartial() throws FileNotFoundException, RemoteException, MalformedURLException, NotBoundException {
//		String[] args = {"", "./trianaParcial.yaml"};
//		Wsct.main(args);
//
//	}
//	

	@Test
	public void embrapaBio() throws FileNotFoundException, RemoteException, MalformedURLException, NotBoundException {
		String[] args = {"", "./embrapaBio.yaml"};
		Wsct.main(args);

	}
}
