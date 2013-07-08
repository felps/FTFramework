/*
 * Created on 16/12/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gridproxy;

import messages.ExecutionResultsRequestMessage;
import messages.ExecutionResultsResponseMessage;
import messages.ExecutionStatusRequestMessage;
import messages.ExecutionStatusResponseMessage;
import messages.KillApplicationRequestMessage;
import messages.KillApplicationResponseMessage;
import messages.OutputFileRequestMessage;
import messages.OutputFileResponseMessage;
import messages.RepositoryListRequestMessage;
import messages.RepositoryListResponseMessage;
import messages.SpecificExecutionStatusRequestMessage;
import messages.SpecificExecutionStatusResponseMessage;
import messages.SubmitApplicationRequestMessage;
import messages.SubmitApplicationResponseMessage;

/**
 * @author Diego Gomes
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface GridProxyInterface {
	
	
	
	/**
	 * @param message
	 * @return
	 */
	public RepositoryListResponseMessage getRepositoryList(RepositoryListRequestMessage rlm);
	
	/**
	 * @param message
	 * @return
	 */
	public SubmitApplicationResponseMessage submitApplication(SubmitApplicationRequestMessage sam);
	
	
	/**
	 * @param user
	 * @param message
	 * @return
	 */
	public ExecutionStatusResponseMessage getExecutionStatus(ExecutionStatusRequestMessage esm);
	
	/**
	 * @param message
	 * @return
	 */
	public SpecificExecutionStatusResponseMessage getSpecificExecutionStatus(SpecificExecutionStatusRequestMessage sesm);
	
	/**
	 * @param message
	 * @return
	 */
	public ExecutionResultsResponseMessage getExecutionResults(ExecutionResultsRequestMessage erm);

	
	/**
	 * @param message
	 * @return
	 */
	public OutputFileResponseMessage getOutputFile(OutputFileRequestMessage ofm);
	
	
	/**
	 * @param message
	 */
	public KillApplicationResponseMessage killApplication(KillApplicationRequestMessage kam);
	
	

}
