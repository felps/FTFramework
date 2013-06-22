package asct.shared;

/**
 * A class structure that represents all the data needed to submit an
 * mpi application execution.
 * 
 * @author Marcelo de Castro
 * 
 */
public class MpiExecutionData extends ExecutionRequestData {
	private int numberOfTasks;
	
	public MpiExecutionData(){
		super();
		setNumberOfTasks(-1);
	}

	/**
	 * @return Returns the numberOfTasks.
	 */
	public int getNumberOfTasks() {
		return numberOfTasks;
	}

	/**
	 * Sets the numberOfTasks
	 * @param numberOfTasks The numberOfTasks to set.
	 */
	public void setNumberOfTasks(int mpiApplicationTaskNumber) {
		this.numberOfTasks = mpiApplicationTaskNumber;
	}
	
}
 
