package org.integrade.portlets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import asct.shared.ApplicationState;
import asct.shared.ExecutionRequestStatus;
import asct.shared.IExecutionListener;
import dataTypes.ExecutionRequestId;

/*
 * A simple listener that waits for jobs to finish and packs the results for downloading.
 * Lundberg
 */
public class JobExecutionListener implements IExecutionListener {
	Hashtable jobs;
	LocalRepository local;
	
	public JobExecutionListener(Hashtable jobs, LocalRepository local) {
		this.jobs = jobs;
		this.local = local;
	}
	public void updateStatus(ExecutionRequestId id, ApplicationState state) {
		try {
			local.packJobOutput(id.requestId);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		((ExecutionRequestStatus)jobs.get(id.requestId)).setApplicationState(state);
	}
}
