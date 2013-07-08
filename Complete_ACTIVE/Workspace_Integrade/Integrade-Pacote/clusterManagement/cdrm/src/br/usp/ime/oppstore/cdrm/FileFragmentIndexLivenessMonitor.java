package br.usp.ime.oppstore.cdrm;

import java.util.HashMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;

public class FileFragmentIndexLivenessMonitor extends Thread {

	FileFragmentIndexManager ffiManager;

	TreeMap<Long, Id> livenessMap;
	
	HashMap<Id, Long> fragmentIdToLivenessMap;
	
	Logger logger;
	
	public FileFragmentIndexLivenessMonitor( FileFragmentIndexManager ffiManager, Logger logger) {
		
		this.livenessMap = new TreeMap<Long, Id>();
		this.fragmentIdToLivenessMap = new HashMap<Id, Long>();
		this.ffiManager = ffiManager;
		this.logger = logger;
	}
		
	public void addLivenessTimeout ( int timeoutMinutes, Id fileId ) {
		
		logger.debug("FileFragmentIndexLivenessMonitor::addLivenessTimeout -> fileId: " + fileId + " timeoutMinutes: " + timeoutMinutes + ".");
		
		long absoluteTimeOut = System.currentTimeMillis() + ( timeoutMinutes * 60 * 1000 );
		synchronized (livenessMap) {
			livenessMap.put( absoluteTimeOut, fileId );
			fragmentIdToLivenessMap.put( fileId, absoluteTimeOut );
		}		
	}
	
	public Id removeLivenessTimeout ( Id fileId ) {

		synchronized (livenessMap) {
			Long absoluteTimeOut = fragmentIdToLivenessMap.remove( fileId );
			if (absoluteTimeOut != null)
				return livenessMap.remove( absoluteTimeOut );
			else
				return null;
		}				
	}

	public void renewLivenessTimeout ( int timeoutMinutes, Id fileId ) {
		
		removeLivenessTimeout(fileId);
		addLivenessTimeout(timeoutMinutes, fileId);
	}

	public void run() {
		
		while (true) {
			try { Thread.sleep(30 * 1000); } catch (InterruptedException e) {}			
			long currentTime = System.currentTimeMillis();
			
			synchronized (livenessMap) {
				while( livenessMap.size() > 0 && currentTime > livenessMap.firstKey() ) {

					Id fileId = livenessMap.remove( livenessMap.firstKey() );
					ffiManager.getStoredFfiMap().remove( fileId );
					logger.debug("FileFragmentIndexLivenessMonitor::run -> Removed FFI with fileId " + fileId + ".");
				}
			}			
		} // while (true)
	} // public void run()	
}
