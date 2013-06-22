package masct.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Fabytes
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MasctProperties extends Properties {
	
	String fileName;
	
	public MasctProperties() {
		super();
		
		
		fileName = "../masct.properties";
		//fileName = "\\My Documents\\java\\masct.properties";
		try {
			
			this.load(new FileInputStream( fileName ));
		}catch (FileNotFoundException e){
			try {
				this.store( new FileOutputStream( fileName ), "Dados do cliente Masct" );
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public int getMasctPort(){
		
		int port = Integer.parseInt(getProperty("masct.port"));
		return port;
	}
	
	public String getMasctUser(){
		
		String user = getProperty("masct.user");
		return user;
	}
	
	public String getMasctPassword(){
		
		String password = getProperty("masct.password");
		return password;
	}
	
	public String getMasctFromDate(){
		
		String fromDate = getProperty("masct.from.date");
		return fromDate;
	}
	
	public String getMasctToDate(){
		
		String toDate = getProperty("masct.to.date");
		return toDate;
	}
	
	public int getProxyPort(){
		
		int port = Integer.parseInt(getProperty("proxy.port"));
		return port;
	}
	
	
	public String getProxyHost(){
		
		String serverHost = getProperty("proxy.host");
		return serverHost;
	}
	
	
	/**
	 * 
	 * 
	 * @return String
	 */
	public boolean save(int masctPort, String masctUser, String masctPass, String masctFromDate, 
			String masctToDate, int proxyPort, String proxyHost ){
		
		this.put("masct.port", ""+masctPort );
		this.put("masct.user", masctUser);
		this.put("masct.password", masctPass);
		this.put("masct.from.date", masctFromDate);
		this.put("masct.to.date", masctToDate);
		this.put("proxy.port", ""+proxyPort);
		this.put("proxy.host", proxyHost);
		
		try {
		
			this.store( new FileOutputStream( fileName ), "Dados do cliente Masct" );
			return true;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
}
