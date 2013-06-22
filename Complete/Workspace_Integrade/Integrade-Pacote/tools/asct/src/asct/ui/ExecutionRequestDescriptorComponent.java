package asct.ui;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class is an abstraction of some key fields of the execution request
 * descriptor file. It represents the component element in the Composite
 * Pattern.
 */
public class ExecutionRequestDescriptorComponent {

	private String applicationArguments;

	private LinkedList<String> inputFileList;

	private LinkedList<String> outputFileList;

	public ExecutionRequestDescriptorComponent() {
		inputFileList = new LinkedList<String>();
		outputFileList = new LinkedList<String>();
	}

	public String getApplicationArguments() {
		return applicationArguments;
	}

	public void setApplicationArguments(String applicationArguments) {
		this.applicationArguments = applicationArguments;
	}

	public Iterator<String> getInputFiles() {
		return inputFileList.iterator();
	}

	public void addInputFile(String inputFile) {
		this.inputFileList.add(inputFile);
	}

	public Iterator<String> getOutputFiles() {
		return outputFileList.iterator();
	}

	public void addOutputFile(String outputFile) {
		this.outputFileList.add(outputFile);
	}

	@Override
	public boolean equals(Object arg0) {
		boolean areEquals = true;
		ExecutionRequestDescriptorComponent object = (ExecutionRequestDescriptorComponent) arg0;

		if (!applicationArguments.equals(object.getApplicationArguments())) {
			areEquals = false;
		}

		for (String input : inputFileList) {
			if (!input.equals(object.getInputFiles().next())) {
				areEquals = false;
			}
		}

		for (String output : outputFileList) {
			if (!output.equals(object.getOutputFiles().next())) {
				areEquals = false;
			}
		}

		return areEquals;
	}

}
