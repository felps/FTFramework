package gridproxy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * TCPProperties reads gridproxy.properties file of communication
 * configuration to Proxy host and Proxy and GridProxy ports.
 * 
 * @author Diego Gomes
 */
public class GridProxyProperties extends Properties {
	
	private static final long serialVersionUID = -721653596031450104L;

	/**
	 * Load File stream.
	 */
	public GridProxyProperties() {
		super();
		
		try{
			this.load(new FileInputStream("gridproxy.properties"));
		} catch(FileNotFoundException e){
			JOptionPane.showMessageDialog(null,"gridproxy.properties file cannot be found.","Error found",JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Grid Proxy could not read gridproxy.properties file.\n please check permission.","Error found",JOptionPane.ERROR_MESSAGE);
			System.exit(1) ;
		}
	}
	
	/**
	 * Get the GridProxy port listener.
	 * 
	 * @return int
	 */
	public int GetGridProxyPort(){
		
		int gridProxyPort = Integer.parseInt(getProperty("gridproxy.port"));
		return gridProxyPort;
	}
	
	/**
	 * Get the ProxyAdapter port listener.
	 * 
	 * @return int
	 */
	public int GetProxyAdapterPort(){
		
		int proxyAdapterPort = Integer.parseInt(getProperty("proxyadapter.port"));
	   	return proxyAdapterPort;
	}
	
	/**
	 * Get the ProxyAdapter host. 
	 * 
	 * @return String
	 */
	public String GetProxyAdapterHost(){
		
		String proxyAdapterHost = getProperty("proxyadapter.host");
	   	return proxyAdapterHost;
	}
	
	
	/**
	 * Get the ProxyAdapter host. 
	 * 
	 * @return String
	 */
	public boolean isDatabaseCreated(){
		
		String databaseExists = getProperty("gridproxy.database");
		if( databaseExists == null ){
			
			return false;
		}
		if( databaseExists.equals("true") ){
			
			return true;
		}
		return false;
	}
	
	
	/**
	 * Get the ProxyAdapter host. 
	 * 
	 * @return String
	 */
	public boolean setDatabaseCreated( boolean status ){
		
		this.put("gridproxy.database", String.valueOf( status ) );
		try {
		
			this.store( new FileOutputStream("gridproxy.properties"), "Este arquivo contém propriedades de comunicacao e de base de dados" );
			return true;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
}

