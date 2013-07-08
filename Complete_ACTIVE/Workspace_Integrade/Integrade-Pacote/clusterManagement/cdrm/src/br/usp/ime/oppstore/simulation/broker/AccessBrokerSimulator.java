package br.usp.ime.oppstore.simulation.broker;

import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import rice.pastry.NodeIdFactory;
import br.usp.ime.oppstore.cdrm.CdrmRequestsImpl;
import br.usp.ime.oppstore.corba.AccessBrokerPOA;
import br.usp.ime.oppstore.simulation.ClusterAdrSimulationMap;
import br.usp.ime.oppstore.simulation.OppStoreSimulatorRemote;
import br.usp.ime.oppstore.simulation.adr.AdrUnavailableException;
import br.usp.ime.oppstore.simulation.adr.ClusterAdrs;
import br.usp.ime.oppstore.simulation.adr.FragmentNotStoredException;
import br.usp.ime.oppstore.simulation.adr.ClusterAdrSimulator.AdrState;
import br.usp.ime.oppstore.simulation.broker.BrokerRequestManager.BrokerFileInformation;
import br.usp.ime.oppstore.simulation.broker.StorageRetrievalThread.ExperimentType;
import br.usp.ime.oppstore.statistics.BrokerStatisticsCollector;

public class AccessBrokerSimulator extends AccessBrokerPOA implements
		AccessBrokerSimulatorRemote {

	OppStoreSimulatorRemote opStoreSimulatorRemote;

	BrokerRequestManager brokerRequestManager;

	BrokerStatisticsCollector brokerStatistics;

	CdrmRequestsImpl cdrmImpl;

	AtomicInteger nResponses;

	AtomicInteger nUploadResponses;

	AtomicInteger nActiveStorageRequests;

	AtomicInteger nActiveRetrievalRequests;

	private Logger logger;

	int brokerNumber;

	int previousRequestNumber = 0;
	
	private int nFilesToStore = 0;
	
	String accessBrokerIor;

	/**
	 * For each file, contains the total number of fragments
	 */
	Vector<Integer> nFragmentsTotalVector;

	/**
	 * For each file, contains the number of recovered fragments int[]
	 * references the several simultaneous experiments
	 */
	Vector<int[]> nFragmentsUptimeVector;

	Vector<int[]> nFragmentsIdleVector;

	// ----------------------------------------------------------------------------------
	public AccessBrokerSimulator(
			OppStoreSimulatorRemote opStoreSimulatorRemote,
			CdrmRequestsImpl cdrmImpl, NodeIdFactory nodeIdFactory,
			BrokerStatisticsCollector statisticsCollector, int brokerNumber) {

		this.brokerNumber = brokerNumber;
		this.cdrmImpl = cdrmImpl;
		this.brokerRequestManager = new BrokerRequestManager(nodeIdFactory);
		this.opStoreSimulatorRemote = opStoreSimulatorRemote;
		this.brokerStatistics = statisticsCollector;
		this.logger = Logger.getLogger("broker.<" + brokerNumber + ">");

		logger.info("Broker created successfully!");
	}

	public void setAccessBrokerIor(String accessBrokerIor) {
		this.accessBrokerIor = accessBrokerIor;
	}

	public String getAccessBrokerIor() {
		return accessBrokerIor;
	}

	public Logger getLogger() {
		return logger;
	}

	// -----------------------------------------------------------------
	// From AccessBroker interface
	// -----------------------------------------------------------------
	public void uploadFragments( int requestNumber, String[] adrAddresses ) {

		BrokerFileInformation fileInfo = brokerRequestManager.getBrokerFileInformation(requestNumber);
				
		// For each key, transfers the fragment to the target ADR node
		Vector<Integer> notStoredFragmentIndexList = new Vector<Integer>();
		
		assert ( fileInfo.fragmentKeyList.length == adrAddresses.length );
		logger.debug("Received upload message " + requestNumber + "." );

		for (int i = 0; i < fileInfo.fragmentKeyList.length; i++) {

			if (adrAddresses[i] == null) {
				notStoredFragmentIndexList.add(i);
				// System.out.println("Removing null fragment " + i + ".");
				continue;
			}

			int adrNumberPos = adrAddresses[i].indexOf(':');
			String adrAddress = adrAddresses[i].substring(0, adrNumberPos);
			String adrPort = adrAddresses[i].substring(adrNumberPos + 1);

			ClusterAdrs clusterAdrStub = ClusterAdrSimulationMap.getClusterAdr(adrAddress);
			int adrNumber = Integer.valueOf(adrPort);

			try {
				clusterAdrStub.storeFragment(adrNumber, fileInfo.fragmentKeyList[i],
						fileInfo.fragmentKeyList[i], fileInfo.fragmentSizeList[i]);
			} catch (RemoteException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (AdrUnavailableException e) {
				notStoredFragmentIndexList.add(i);
			}
		}

		int[] notStoredArray = new int[notStoredFragmentIndexList.size()];
		for (int i = 0; i < notStoredFragmentIndexList.size(); i++)
			notStoredArray[i] = notStoredFragmentIndexList.get(i);

		// Notifies the CDRM that the transfer has finished
		cdrmImpl.setFragmentStorageFinished(requestNumber, notStoredArray, fileInfo.fragmentKeyList, fileInfo.fileKey);

	}

	public void setFileStorageRequestCompleted(int requestNumber) {

		BrokerFileInformation fileInfo = brokerRequestManager.getBrokerFileInformation(requestNumber);
		
		int nResponses = this.nResponses.incrementAndGet();

		// System.out.println("File Storage Complete");
		nActiveStorageRequests.decrementAndGet();
		brokerRequestManager.setFileStored(fileInfo.fileKey);

		if (nResponses == nFilesToStore)
			this.notifyStorageRequestCompleted();
	}

	public void downloadFragments(int requestNumber, String[] adrAddresses, byte[][] fragmentKeyList, int fileSize, int[] fragmentSizeList, int neededFragments) {

		logger.debug("downloading fragments for requestNumber " + requestNumber + ".");
		
		previousRequestNumber = requestNumber;
		
		int nResponses = this.nResponses.incrementAndGet();

		// Total number of fragments for a given file
		int nFragmentsTotal = fragmentKeyList.length;

		int[] nFragmentsUptime = null;
		int[] nFragmentsIdle = null;

		// Tries to download the fragments
		for (int i = 0; i < fragmentKeyList.length; i++) {
			try {

				if (adrAddresses[i] == null)
					continue;

				int adrNumberPos = adrAddresses[i].indexOf(':');
				String adrAddress = adrAddresses[i].substring(0, adrNumberPos);
				String adrPort = adrAddresses[i].substring(adrNumberPos + 1);

				ClusterAdrs clusterAdrStub = ClusterAdrSimulationMap.getClusterAdr(adrAddress);
				int adrNumber = Integer.valueOf(adrPort);

				byte[] data = clusterAdrStub.getFragment(adrNumber, fragmentKeyList[i]);

				// checks if the dowloaded fragments are correct
				for (int pos = 0; pos < fragmentKeyList[i].length; pos++)
					assert ( fragmentKeyList[i][pos] == data[pos] );

				// Gets the status of the machines, which is only simulated
				AdrState[] adrStateList = clusterAdrStub.getStatus(adrNumber);

				if (nFragmentsUptime == null)
					nFragmentsUptime = new int[adrStateList.length];
				if (nFragmentsIdle == null)
					nFragmentsIdle = new int[adrStateList.length];

				for (int stateIndex = 0; stateIndex < adrStateList.length; stateIndex++) {
					
					AdrState adrState = adrStateList[stateIndex];
					if (adrState == AdrState.OCCUPIED) {
						nFragmentsUptime[stateIndex]++;
					} else if (adrState == AdrState.IDLE) {
						nFragmentsUptime[stateIndex]++;
						nFragmentsIdle[stateIndex]++;
					}
				}


			} catch (RemoteException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (AdrUnavailableException e) {
				System.out.println("AccessBrokerSimulator::downloadFragments -> ERROR: captured AdrUnavailableException.");
			} catch (FragmentNotStoredException e) {
				System.out.println("AccessBrokerSimulator::downloadFragments -> ERROR: A fragment was not found.");
			}
		}

		nFragmentsTotalVector.add(nFragmentsTotal);
		nFragmentsUptimeVector.add(nFragmentsUptime);
		nFragmentsIdleVector.add(nFragmentsIdle);

		nActiveRetrievalRequests.decrementAndGet();
		if (nResponses == brokerRequestManager.getNumberOfFiles())
			this.notifyRetrievalRequestCompleted();
	}

	public void setFileRetrievalRequestFailed(int requestNumber) {

		// TODO: Insert in the statistics
		int nResponses = this.nResponses.incrementAndGet();
		
		//BrokerFileInformation fileInfo = brokerRequestManager.getBrokerFileInformation(requestNumber);
		System.err.println("Failed to retrieve file...");

		nActiveRetrievalRequests.decrementAndGet();
		if (nResponses == brokerRequestManager.getNumberOfFiles())
			this.notifyRetrievalRequestCompleted();
	}

	// ---------------------------------------------------------------------

	private void notifyRetrievalRequestCompleted() {

		System.out.println("Retrieval requests finished!");

		// Notifies the Simulation Controller
		boolean simulatorNotified = false;
		while (simulatorNotified == false) {
			try {
				// Should send the results to the statistics server

				opStoreSimulatorRemote.setRetrievalRequestCompleted(this);
				simulatorNotified = true;
			} catch (RemoteException e) {
				System.out
						.println("RemoteException in notifyRetrievalRequestCompleted");
				System.out.println(e.getMessage());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}

		brokerStatistics.setAccessBrokerRetrievalData(brokerNumber, nFragmentsTotalVector, nFragmentsUptimeVector, nFragmentsIdleVector);
		brokerStatistics.printRetrievalData();

	}

	private void notifyStorageRequestCompleted() {

		System.out.println("Storage requests finished!");

		// Notifies the Simulation Controller
		boolean simulatorNotified = false;
		while (simulatorNotified == false) {
			try {
				opStoreSimulatorRemote.setStorageRequestCompleted(this);
				simulatorNotified = true;
			} catch (RemoteException e) {
				System.out.println("RemoteException in notifyRetrievalRequestCompleted");
				System.out.println(e.getMessage());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

	// -----------------------------------------------------------------
	// From AccessBrokerSimulatorRemote interfce
	// -----------------------------------------------------------------
	public void storeFiles(int nFiles, int nFragments, int nNeededFragments, int baseFileSize) throws RemoteException {

		byte[][] keyListToStore = this.addFiles(nFiles, nFragments, nNeededFragments, baseFileSize);
		
		this.nResponses = new AtomicInteger(0);
		this.nUploadResponses = new AtomicInteger(0);
		this.nActiveStorageRequests = new AtomicInteger(0);
		this.nFilesToStore = keyListToStore.length;

		this.logger.info("Creating storageRetrieval Thread.");

		(new StorageRetrievalThread(ExperimentType.storage, this, keyListToStore)).start();
	}

	public void retrieveStoredFiles() throws RemoteException {

		// class the clusterAdrManager to change ADR state;

		nResponses = new AtomicInteger(0);
		nActiveRetrievalRequests = new AtomicInteger(0);

		(new StorageRetrievalThread(ExperimentType.retrieval, this, null)).start();
	}

	public byte[][] addFiles(int nFiles, int nFragments, int nNeededFragments, int baseFileSize) throws RemoteException {

		logger.info("Adding " + nFiles + " with " + nFragments + " out of "
				+ nNeededFragments + " fragments and baseFileSize " + baseFileSize + ".");

		byte[][] keyListToStore = new byte[nFiles][];
		     
		for (int fileNumber = 0; fileNumber < nFiles; fileNumber++) {
			byte[] fileKey = brokerRequestManager.createFileKey();
			byte[][] fragmentKeyList = brokerRequestManager.createFragmentKeyList(nFragments);
			int[] fragmentSizeList = new int[fragmentKeyList.length];

			double nextRandom = Math.random();
			int fileSize = baseFileSize;
			if (nextRandom < 0.2)
				fileSize *= 1;
			else if (nextRandom < 0.4)
				fileSize *= 10;
			else if (nextRandom < 0.6)
				fileSize *= 100;
			else if (nextRandom < 0.8)
				fileSize *= 1000;
			else
				fileSize *= 10000;
			
			/**
			 * This version does not overload the ADRs. The objective is to select ADRs based only on availability.
			 */
			fileSize = 12; // 12kB
			
			int fragmentSize = fileSize / nNeededFragments;
			for (int fragmentIndex = 0; fragmentIndex < fragmentSizeList.length; fragmentIndex++)
				fragmentSizeList[fragmentIndex] = fragmentSize;

			brokerRequestManager.addFileInformation(fileKey, fragmentKeyList, fileSize, fragmentSizeList, nNeededFragments);
			keyListToStore[fileNumber] = fileKey;
		}
		
		return keyListToStore;
	}

	public void setFileStorageRequestFailed(int requestId) {
		// TODO Auto-generated method stub
		
	}

	public void removeFragments(int requestId, String[] adrAddresses,
			byte[][] fragmentKeyList) {
		// TODO Auto-generated method stub
		
	}

	public void renewFragmentLeases(int requestId, String[] adrAddresses,
			byte[][] fragmentKeyList, int timeout) {
		// TODO Auto-generated method stub
		
	}
}
