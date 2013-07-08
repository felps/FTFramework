package br.usp.ime.oppstore.adrmanager;

import java.util.Collection;

import br.usp.ime.oppstore.adrmanager.AdrInformationStructure.AdrLiveness;

public class AdrLivenessMonitor extends Thread {
	
	private static long sleepTime = 5 * 1000;
	private static long alocationTime = 30 * 1000; //  1 * 60 * 1000
	private static long removalTime   = 60 * 1000; // 60 * 60 * 1000
	
	private Collection< AdrInformationStructure > adrInfoCollection;
	private AdrManagerImpl adrManagerImpl;
	
	public AdrLivenessMonitor( AdrManagerImpl adrManagerImpl, Collection< AdrInformationStructure > adrInfoCollection ) {
		
		this.adrManagerImpl = adrManagerImpl;
		this.adrInfoCollection = adrInfoCollection;
	}
	
	public void run() {
	
		adrManagerImpl.getLogger().info("AdrLivenessMonitor started.");
		
		if (true) return;
		
		while (true) {

			try { Thread.sleep( sleepTime ); } 
			catch (InterruptedException e) {}

			long currentTime = System.currentTimeMillis();
			//new AdrInformationStructure[adrInfoCollection.size()] = new AdrInformationStructure[adrInfoCollection.size()];
			AdrInformationStructure[] adrInfoArray = adrInfoCollection.toArray( new AdrInformationStructure[0] );
			for ( AdrInformationStructure adrInfo : adrInfoArray ) {
				if ( currentTime - adrInfo.lastUpdateTime > removalTime ) {
					adrManagerImpl.getLogger().debug("AdrLivenessMonitor: Marking ADR " +  adrInfo.adrAddress + " as DEPARTED.");
					adrManagerImpl.setAdrLiveness(adrInfo, AdrLiveness.DEPARTED);
				}
				else if ( currentTime - adrInfo.lastUpdateTime > alocationTime ) {
					adrManagerImpl.getLogger().debug("AdrLivenessMonitor: Marking ADR " +  adrInfo.adrAddress + " as UNRESPONSIVE.");
					adrManagerImpl.setAdrLiveness(adrInfo, AdrLiveness.UNRESPONSIVE);
				}

			}
		}
		
	}
}
