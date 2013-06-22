package br.usp.ime.oppstore.simulation.simnode;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MachineEventControllerRemote extends Remote {
    
    public void dispatchNextEvents(double timeStep) throws RemoteException;

}
