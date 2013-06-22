package grm.executionManager;

import grm.executionManager.ExecutionInformation;

import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CORBA.ORB;

import resourceProviders.Lrm;
import resourceProviders.LrmHelper;
import clusterManagement.Grm;
import dataTypes.ApplicationExecutionInformation;
import dataTypes.BspProcessZeroInformation;
import dataTypes.ExecutionRequestId;
import dataTypes.ProcessExecutionInformation;

/** A class is created for every restart */
public class BspRestartCoordinator extends RestartCoordinator {
    
    private String bspProcessZeroIor;    
        
    //---------------------------------------------------------------------------------------
    
    public BspRestartCoordinator(ORB orb, Grm grm, int nProcesses) {
	super(orb, grm, nProcesses);
        bspProcessZeroIor = null;
    }
    
    //-------------------------------------------------------------------------------------
    
    public BspProcessZeroInformation registerBspNode(ExecutionRequestId executionRequestId, String bspProxyIor){                
        
        int nodeId = Integer.parseInt(executionRequestId.processId);        
        if(nodeId == 0) bspProcessZeroIor = bspProxyIor;
        
        /** Synchronization point */
        if (ExecutionManagerDebugFlag.debug) 
            System.err.println("RegisterBspNode: waiting for BSP processes, nodeId: " + nodeId);
        try {barrier.await();}
        catch(BrokenBarrierException broken) {
            System.err.println("RegisterBspNode: Barrier was broken.");
        }
        catch(Exception e) {e.printStackTrace();};
        if (nodeId == 0)
            System.err.println("RegisterBspNode: returning Process Zero IOR");               
        
        if (nodeId == 0) return new BspProcessZeroInformation(true, bspProcessZeroIor);
        else return new BspProcessZeroInformation(false, bspProcessZeroIor);
        
    }
}

//-----------------------------------------------------------------------------------------
