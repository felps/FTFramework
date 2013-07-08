package br.usp.ime.oppstore.simulation;

import java.rmi.Remote;
import java.rmi.RemoteException;

import br.usp.ime.oppstore.simulation.broker.AccessBrokerSimulatorRemote;

public interface OppStoreSimulatorRemote extends Remote {

    CdrmBootstrapInformation getBootstrapNodeAddress () throws RemoteException; 
    
    /**
     * Notifies that a remote machine started all of its CDRMs and AcessBrokers
     * @param numberOfCdrms TODO
     * @throws RemoteException
     */
    void addAccessBrokerRemoteList(AccessBrokerSimulatorRemote[] accessBrokerRemoteList) throws RemoteException;

    /**
     * Notifies that an access broker has finished all the storage requests from a round 
     * @throws RemoteException
     */
    void setStorageRequestCompleted(AccessBrokerSimulatorRemote accessBrokerRemote) throws RemoteException;

    /**
     * Notifies that an access broker has finished all the retrieval requests from a round 
     * @throws RemoteException
     */
    void setRetrievalRequestCompleted(AccessBrokerSimulatorRemote accessBrokerRemote) throws RemoteException;
    
    void simulateFileStorage(int nFiles, int nFragments, int nNeededFragments) throws RemoteException;
    
    void simulateFileRetrieval() throws RemoteException;
 
}
