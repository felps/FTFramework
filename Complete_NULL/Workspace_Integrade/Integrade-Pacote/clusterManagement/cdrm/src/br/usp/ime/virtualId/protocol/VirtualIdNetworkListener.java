package br.usp.ime.virtualId.protocol;

import java.net.InetSocketAddress;

import rice.pastry.NetworkListener;

public class VirtualIdNetworkListener implements NetworkListener {

	public void channelOpened(InetSocketAddress addr, int reason) {
		System.out.println("channelOpened");		
	}

	public void channelClosed(InetSocketAddress addr) {
		System.out.println("channelClosed");		
	}

	public void dataSent(Object message, InetSocketAddress address, int size, int type) {
		System.out.println("dataSent");		
	}

	public void dataReceived(Object message, InetSocketAddress address, int size, int type) {
		System.out.println("dataReceived");		
	}

}
