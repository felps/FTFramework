package asct.ui;

import java.util.Iterator;
import java.util.LinkedList;

import dataTypes.ApplicationType;

/**
 * This class is an abstraction of the execution request descriptor file. It
 * represents the composite element in the Composite Pattern.
 * 
 * @author randrade
 * 
 */
public class ExecutionRequestDescriptorComposite extends
		ExecutionRequestDescriptorComponent {

	private String applicationName;

	private ApplicationType applicationType;

	private String applicationBasePath;

	private String applicationBinaries;

	private String applicationConstraints;

	private String applicationPreferences;

	private int numberOfTasks = 1;

	private LinkedList<ExecutionRequestDescriptorComponent> parametricApplicationCopies;

	private boolean shouldForceDifferentMachines = false;

	/**
	 * A default constructor
	 */
	public ExecutionRequestDescriptorComposite() {
		parametricApplicationCopies = new LinkedList<ExecutionRequestDescriptorComponent>();
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public ApplicationType getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(ApplicationType applicationType) {
		this.applicationType = applicationType;
	}

	public String getApplicationBasePath() {
		return applicationBasePath;
	}

	public void setApplicationBasePath(String applicationBasePath) {
		this.applicationBasePath = applicationBasePath;
	}

	public String getApplicationBinaries() {
		return applicationBinaries;
	}

	public void setApplicationBinaries(String applicationBinaries) {
		this.applicationBinaries = applicationBinaries;
	}

	public String getApplicationConstraints() {
		return applicationConstraints;
	}

	public void setApplicationConstraints(String applicationConstraints) {
		this.applicationConstraints = applicationConstraints;
	}

	public String getApplicationPreferences() {
		return applicationPreferences;
	}

	public void setApplicationPreferences(String applicationPreferences) {
		this.applicationPreferences = applicationPreferences;
	}

	public int getNumberOfTasks() {
		return numberOfTasks;
	}

	public void setBspNumberOfTasks(int bspNumberOfTasks) {
		numberOfTasks = bspNumberOfTasks;
	}

	public void setMpiNumberOfTasks(int mpiNumberOfTasks) {
		numberOfTasks = mpiNumberOfTasks;
	}

	public Iterator<ExecutionRequestDescriptorComponent> getParametricApplicationCopies() {
		if (parametricApplicationCopies == null)
			parametricApplicationCopies = new LinkedList<ExecutionRequestDescriptorComponent>();
		return parametricApplicationCopies.iterator();
	}

	public int getSizeOfParametricApplicationCopies() {
		if (parametricApplicationCopies == null)
			parametricApplicationCopies = new LinkedList<ExecutionRequestDescriptorComponent>();
		return parametricApplicationCopies.size();
	}

	public void addParametricApplicationCopies(
			ExecutionRequestDescriptorComponent applicationCopy) {
		this.parametricApplicationCopies.add(applicationCopy);
	}

	public boolean getShouldForceDifferentMachines() {
		return shouldForceDifferentMachines;
	}

	public void setShouldForceDifferentMachines(
			boolean shouldForceDifferentMachines) {
		this.shouldForceDifferentMachines = shouldForceDifferentMachines;
	}

	@Override
	public boolean equals(Object arg0) {
		boolean areEquals = true;
		ExecutionRequestDescriptorComposite object = (ExecutionRequestDescriptorComposite) arg0;

		if (!applicationName.equals(object.getApplicationName())) {
			areEquals = false;
		}

		if (!applicationBasePath.equals(object.getApplicationBasePath())) {
			areEquals = false;
		}

		if (!applicationBinaries.equals(object.getApplicationBinaries())) {
			areEquals = false;
		}

		if (!applicationConstraints.equals(object.getApplicationConstraints())) {
			areEquals = false;
		}

		if (!applicationPreferences.equals(object.getApplicationPreferences())) {
			areEquals = false;
		}

		if (numberOfTasks != object.getNumberOfTasks()) {
			areEquals = false;
		}

		if (shouldForceDifferentMachines != object
				.getShouldForceDifferentMachines()) {
			areEquals = false;
		}

		if (parametricApplicationCopies.size() != object
				.getSizeOfParametricApplicationCopies()) {			
			areEquals = false;
		}

		for (ExecutionRequestDescriptorComponent copy : parametricApplicationCopies) {
			if (!copy.equals(object.getParametricApplicationCopies().next())) {
				areEquals = false;
			}
		}

		if (applicationType.value() != object.getApplicationType().value()) {
			areEquals = false;
		}

		return areEquals;
	}
}
