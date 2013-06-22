package br.usp.ime.oppstore.simulation.adr;

import java.rmi.Remote;
import java.rmi.RemoteException;

import br.usp.ime.oppstore.simulation.adr.ClusterAdrSimulator.AdrState;

public interface ClusterAdrs extends Remote {
	
	public void storeFragment(int adrNumber, byte[] fragmentKey, byte[] data, int dataSize) 
    throws AdrUnavailableException, RemoteException;

    public byte[] getFragment(int adrNumber, byte[] fragmentKey) 
    throws AdrUnavailableException, FragmentNotStoredException, RemoteException;
    
    public AdrState[] getStatus(int adrNumber)
    throws RemoteException;
}
