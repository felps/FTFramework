package br.usp.ime.oppstore.adrmanager;

import java.util.HashMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;

public class FragmentLivenessMonitor extends Thread {

	StoredFragmentManager fragmentManager;

	TreeMap<Long, Id> livenessMap;
	
	HashMap<Id, Long> fragmentIdToLivenessMap;
	
	Logger logger;
	
	public FragmentLivenessMonitor( StoredFragmentManager fragmentManager ) {
		
		this.livenessMap = new TreeMap<Long, Id>();
		this.fragmentIdToLivenessMap = new HashMap<Id, Long>();
		this.fragmentManager = fragmentManager;
		this.logger = fragmentManager.logger;
	}
		
	public void addLivenessTimeout ( int timeoutMinutes, Id fragmentId ) {
		
		long absoluteTimeOut = System.currentTimeMillis() + ( timeoutMinutes * 60 * 1000 );
		synchronized (livenessMap) {
			livenessMap.put( absoluteTimeOut, fragmentId );
			fragmentIdToLivenessMap.put( fragmentId, absoluteTimeOut );
		}		
	}
	
	public Id removeLivenessTimeout ( Id fragmentId ) {

		synchronized (livenessMap) {
			Long absoluteTimeOut = fragmentIdToLivenessMap.remove( fragmentId );
			return livenessMap.remove( absoluteTimeOut );			
		}				
	}

	public void renewLivenessTimeout ( int timeoutMinutes, Id fragmentId ) {
		
		removeLivenessTimeout(fragmentId);
		addLivenessTimeout(timeoutMinutes, fragmentId);
	}

	public void run() {
	
		//System.out.println("Fragment monitor started!");
		
		while (true) {
			try { Thread.sleep(30 * 1000); } catch (InterruptedException e) {}
			
			long currentTime = System.currentTimeMillis();
			synchronized (livenessMap) {

				while( livenessMap.size() > 0 && currentTime > livenessMap.firstKey() ) { 
					
					Id fragmentId = livenessMap.remove( livenessMap.firstKey() );
					AdrInformationStructure adrInfo = fragmentManager.getAdrInfo( fragmentId );
					if (adrInfo != null) {
						logger.debug("Adding fragment " + fragmentId + " to removal list of ADR " + adrInfo.adrAddress + ".");
						adrInfo.fragmentRemovalList.add( fragmentId );
					}
				}
			}	
		}
	}
}
