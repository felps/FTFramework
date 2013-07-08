package asct.ui;

import asct.shared.ApplicationState;
import asct.shared.IExecutionListener;
import dataTypes.ExecutionRequestId;

public class FinishedExecutionListener implements IExecutionListener {
	private boolean isFinished = false;

	private boolean isRefused;

	@Override
	public void updateStatus(ExecutionRequestId execRequestId,
			ApplicationState applicationStatus) {
		if (applicationStatus == ApplicationState.FINISHED) {
			isFinished = true;
		} else if (applicationStatus == ApplicationState.REFUSED) {
			isRefused = true;
		}

	}

	public boolean hasFinished() {
		return isFinished;
	}
	
	public boolean hasBeenRefused() {
		return isRefused;
	}

}
