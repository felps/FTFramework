package messages;

import java.io.Serializable;

public class ParametricCopy implements Serializable {
	
	String Arguments;
	String InputFiles[];
	String OutputFiles[];
	
	
	public String getArguments() {
		return Arguments;
	}
	public void setArguments(String arguments) {
		Arguments = arguments;
	}
	public String[] getInputFiles() {
		return InputFiles;
	}
	public void setInputFiles(String[] inputFiles) {
		InputFiles = inputFiles;
	}
	public String[] getOutputFiles() {
		return OutputFiles;
	}
	public void setOutputFiles(String[] outputFiles) {
		OutputFiles = outputFiles;
	}
	
	

}
