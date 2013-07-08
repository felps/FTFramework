package br.usp.ime.oppstore.adrmanager;

import rice.p2p.commonapi.Application;
import br.usp.ime.oppstore.cdrm.CdrmApp;
import br.usp.ime.oppstore.message.StoreFragmentMessage;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;

public class CapacityUpdatedThread extends Thread {

	private long sleepTime;
	private double lowerCapacity;
	private double higherCapacity;
	private AdrManagerImpl adrManager;
	private VirtualNode virtualNode;
	
	public CapacityUpdatedThread (VirtualNode virtualNode, AdrManagerImpl adrManager, long sleepTime, double lowerCapacity, double higherCapacity) {
		this.sleepTime  = sleepTime;
		this.adrManager = adrManager;
		this.lowerCapacity  = lowerCapacity;
		this.higherCapacity = higherCapacity;
		this.virtualNode = virtualNode;
	}
	
	public void run() {
	
		if (CdrmApp.idProtocol == CdrmApp.OpStoreIdProtocol.PASTRY)
			return;

		try { Thread.sleep( sleepTime ); } 
		catch (InterruptedException e) { }
				
		double currentCapacity = adrManager.evaluateCdrmCapacity();
		if (lowerCapacity < currentCapacity && currentCapacity < higherCapacity)
			return;
				
		adrManager.getLogger().info( "Cdrm capacity changed to " + currentCapacity + ".");
		adrManager.setLastUpdatedCdrmCapacity( currentCapacity );
		
		VirtualSpace virtualSpace = virtualNode.getVirtualSpace( StoreFragmentMessage.fragmentRoutingSpaceNumber );
		if ( virtualSpace != null ) {
			virtualNode.updateNodeCapacity(virtualSpace.getVirtualSpaceNumber(), currentCapacity, null);
		}
		else {
			adrManager.getLogger().info( "Joining VirtualSpace " + StoreFragmentMessage.fragmentRoutingSpaceNumber + ".");
			Application cdrmApp = virtualNode.getVirtualSpace(0).getApplication();
			virtualNode.joinVirtualSpace(StoreFragmentMessage.fragmentRoutingSpaceNumber, cdrmApp, currentCapacity, null);
		}
		
	}
	
}
