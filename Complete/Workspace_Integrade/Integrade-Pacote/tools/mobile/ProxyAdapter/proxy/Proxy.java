package proxy;

import moca.core.proxy.ProxyException;
import moca.core.proxy.ProxyFramework;
import moca.core.proxy.communication.CommunicationProtocol;
import moca.core.proxy.development.MessageListener;
import moca.core.proxy.message.Message;



/**
 * @author diego
 *
 * 
 * 
 */
public class Proxy {
	
	
	private ProxyFramework proxy = null;
	
	
	public Proxy() {
		
		configureProxy();
	}
	
	public void configureProxy() {
		
		
		System.out.println("Initializing ProxyAdapter.");
		
		
		proxy = ProxyFramework.getInstance();
		proxy.addServerMsgListener(new ServerMsgListener());
		proxy.addClientMsgListener(new  ClientMsgListener());
		proxy.init(CommunicationProtocol.TCP, CommunicationProtocol.ASYNC);
		
		
	}
	
	
	private class ClientMsgListener implements MessageListener{
		
		
		public void onReceiveMessage(Message data, CommunicationProtocol protocol) {
			
			if(data.getMsgType() ==  Message.CONTROL){
				if( !proxy.isClientRegistered( protocol.getAddress() ) ){
					proxy.addClient(data.getSender() , protocol, true);
					System.out.println("Client " + data.getSender() + " registered.");
				}				
			}else if( !proxy.isClientRegistered( protocol.getAddress() ) ){
					proxy.addClient(data.getSender(), protocol, true);
					System.out.println("Client " + data.getSender() + " registered.");
				  }
				
				System.out.println("ProxyAdapter sending message from "
						+ data.getSender() + " to " + data.getAddressee());
				
				System.out.println("ProxyAdapter sending message from "
						+ data.getClientAddress() + " to " + data.getAddressee());
				
				

				proxy.sendMsgToServer(data, protocol);
			
		}
		
	}
	
	private class ServerMsgListener implements MessageListener{

		public void onReceiveMessage(Message data, CommunicationProtocol protocol) {			

			if(data.getMsgType() != Message.CONTROL){
				
				try {
					
					System.out.println("ProxyAdapter receiving message from: " + data.getSender());
					
					System.out.println("ProxyAdapter sending message to client: " + data.getAddressee());					
								
					
					proxy.sendMsgToClient( data );
					
					
				} catch (ProxyException e) {
					e.printStackTrace();
				}
				
			}
			
		}
	}
	
	
	public void freeResources(){
		proxy.freeResources();
	}
	
	
	public static void main(String[] args) {
		
		new Proxy();
	}
	
}
