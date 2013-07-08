package br.usp.ime.virtualId;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import br.usp.ime.virtualId.message.VirtualIdMessage;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

public class VirtualMessageRouter extends Thread {

	private Endpoint endpoint;
	
	class MessageRoutingInfo {
		Id destinationId;
		VirtualIdMessage message;
		NodeHandle targetHandle;
	}
	
	LinkedBlockingQueue<MessageRoutingInfo> routingQueue;
	
	public VirtualMessageRouter( Endpoint endpoint ) {
		this.endpoint = endpoint;
		this.routingQueue = new LinkedBlockingQueue<MessageRoutingInfo>();
	}
	
	public void run() {
		MessageRoutingInfo messageInfo = null;
		while (true) {
			try { messageInfo = routingQueue.poll(100, TimeUnit.SECONDS); } catch (InterruptedException e) {}
			if (messageInfo != null)
				this.endpoint.route( messageInfo.destinationId, messageInfo.message, messageInfo.targetHandle );
		}
	}
	
	public void route(Id destinationId, VirtualIdMessage message, NodeHandle targetHandle) {
		MessageRoutingInfo messageInfo = new MessageRoutingInfo();
		messageInfo.destinationId = destinationId;
		messageInfo.message       = message;
		messageInfo.targetHandle  = targetHandle;
		
		//System.out.println("Queeing message:" + messageInfo.destinationId + " " + messageInfo.message + " " + messageInfo.targetHandle);
		
		this.routingQueue.offer( messageInfo );
	}
	
}
