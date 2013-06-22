package masct.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import masct.gui.Masct;
import moca.core.proxy.message.Message;
import moca.protocol.net.TCPConnection;

public class MasctClientAPI {
	private static MasctClientAPI instance = null;	
	
	private TCPConnection me = null;
	private InetSocketAddress address = null;
	
	
	private MasctClientAPI(){
		
		MasctListener listener = new MasctListener();
		listener.start();
	}
	
	
	public static MasctClientAPI getInstance(){
		
		if( instance == null ){
			instance = new MasctClientAPI();
		}
		return instance;
	}
	
	
	public void send(Message msg){
		
		try{
			address = new InetSocketAddress(Masct.getInstance().getProxyIp(),
					Masct.getInstance().getProxyport());
			me = new TCPConnection( address );
			System.out.println(InetAddress.getLocalHost());			
			msg.setClientAddress(new InetSocketAddress( InetAddress.getLocalHost(), 5909)) ;
			me.send( msg );
			me.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
