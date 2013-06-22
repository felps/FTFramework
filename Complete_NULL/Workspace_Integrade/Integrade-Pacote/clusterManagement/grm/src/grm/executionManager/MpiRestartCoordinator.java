// { IMPI
package grm.executionManager;

import grm.executionManager.ExecutionInformation;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CORBA.ORB;

import resourceProviders.Lrm;
import resourceProviders.LrmHelper;
import clusterManagement.Grm;
import dataTypes.ApplicationExecutionInformation;
import dataTypes.MpiConnectInformation;
import dataTypes.ExecutionRequestId;
import dataTypes.ProcessExecutionInformation;
import java.util.StringTokenizer;

/** A class is created for every restart 
*
* IME/USP
*
* Changed by Marcelo de Castro
* INF/UFG
*/
public class MpiRestartCoordinator extends RestartCoordinator {

    private MpiConnectInformation[] resp;
        
    //---------------------------------------------------------------------------------------
    
    public MpiRestartCoordinator(ORB orb, Grm grm, int nProcesses) {               
        super(orb, grm, nProcesses);
        resp = new MpiConnectInformation[this.nProcesses];
    }

    //-------------------------------------------------------------------------------------
    
    public MpiConnectInformation[] registerMpiNode(ExecutionRequestId executionRequestId, String kvs, int rank)
	{
		resp[rank] = new MpiConnectInformation(kvs, executionRequestId.processId);

		try {	
			barrier.await();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// return kvs information for all process of the mpi
		return resp;

    }
}

//-----------------------------------------------------------------------------------------
 

// } IMPI
