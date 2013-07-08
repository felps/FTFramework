package br.usp.ime.oppstore.simulation.broker;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AccessBrokerSimulatorRemote extends Remote {

    /**
     * Sets list of files that will be stored
     * @param nFiles
     * @param nFragments
     * @param nNeededFragments
     * @throws RemoteException
     */
    //public void addFiles (int nFiles, int nFragments, int nNeededFragments, int fragmentSize) throws RemoteException; 

    /**
     * Stores the created files on the distributed storage
     * @throws RemoteException
     */
    public void storeFiles(int nFiles, int nFragments, int nNeededFragments, int fragmentSize) throws RemoteException;
    
    /**
     * Retrieves the created files from the distributed storage
     * @throws RemoteException
     */
    public void retrieveStoredFiles() throws RemoteException;
}
