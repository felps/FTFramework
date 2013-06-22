package appSpecificFTEC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import defaultTypes.Task;
import defaultTypes.Workflow;

public class AppDescriptor {
String			appType ;
String			appName ;
String 			applicationBasePath;
String 			binaries;
String 			appConstraints; 
String 			appPreferences; 
String 			appArgs;
HashSet<String> inputFile; 
HashSet<String> outputFile;
String 		shouldForceDifferentMachines;

	public AppDescriptor(String filename) {

		InputStream input = null;
		try {
			input = new FileInputStream(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

	    this.appType = readlineWithoutArgs(input);
	    this.appName = readlineWithoutArgs(input);
	    this.applicationBasePath = readlineWithoutArgs(input);
	    this.binaries = readlineWithoutArgs(input);
	    this.appConstraints = readlineWithoutArgs(input);
	    this.appPreferences = readlineWithoutArgs(input);
	    this.appArgs = readlineWithoutArgs(input);
	    
	    String line = readline(input);

	    while("inputFile".contentEquals(line.substring(0, "inputFile".length()-1 ) ) ){
			this.inputFile.add(line.substring( "inputFile".length(), line.length()-1)) ;
			line = readline(input);
		}

	    while("outputFile".contentEquals(line.substring(0, "outputFile".length()-1 ) ) ){
			this.outputFile.add(line.substring( "outputFile".length(), line.length()-1)) ;;
			line = readline(input);
		}

	    this.shouldForceDifferentMachines = readlineWithoutArgs(input);

	}

	private String readline(InputStream input) {
		String out = "";
		
		int nextChar;
		try {
			nextChar = input.read();
		
			while (nextChar != 10 && nextChar != 13){
				out = out + (char) nextChar;
				nextChar = input.read();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return out;
	}

	private String readlineWithoutArgs(InputStream input) {

		String line = readline(input);
		
		String [] words = line.split(" ");
		
		// Ignore the first word
		// Concatenate the others back
		String out = "";
		for (int i = 1; i< words.length; i++){
			out = out + " " + words[i];
		}
			
		return out;
	}

	public boolean dumpAppDescriptor(String outputFile) {

		FileOutputStream output = null;
		try {
			output = new FileOutputStream(new File(outputFile));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		printToFile(output, "appType ");
		printToFile(output, this.appType);
		
		printToFile(output, "appName ");
	    printToFile(output, this.appName);
	    
	    printToFile(output, "applicationBasePath");
	    printToFile(output, this.applicationBasePath );
	    
	    printToFile(output, "binaries ");
	    printToFile(output, this.binaries );
	    
	    printToFile(output, "appConstraints ");
	    printToFile(output, this.appConstraints );
	    
	    printToFile(output, "appPreferences ");
	    printToFile(output, this.appPreferences );
	    
	    printToFile(output, "appArgs ");
	    printToFile(output, this.appArgs );

	    for (Iterator inputFile = this.inputFile.iterator(); inputFile.hasNext();) {
			String inputFileName = (String) inputFile.next();

			printToFile(output, "inputFile ");
			printToFile(output, inputFileName);
		
	    }
	    
	    for (Iterator outputFileIter = this.outputFile.iterator(); outputFileIter.hasNext();) {
			String outputFileName = (String) outputFileIter.next();
			
			printToFile(output, "outputFile ");
			printToFile(output, outputFileName);
		
	    }

	    printToFile(output, "shouldForceDifferentMachines ");
	    printToFile(output, this.shouldForceDifferentMachines);

	    return true;
	}

	private void printToFile(FileOutputStream output, String text) {
		try {
		byte b;
		for (int i=0; i<text.length(); i++) {
			b = (byte) text.charAt(i);
			output.write(b);
		}
		}
		catch (Exception e) {
			System.out.println("printToFile");
			e.printStackTrace();
		}
	}

	public String getFastaFile() {
		// Search among the input files for the one with ".fasta"
		for (Iterator inputFile = this.inputFile.iterator(); inputFile.hasNext();) {
			String inputFileName = (String) inputFile.next();
			
			if (inputFileName.contains(".fasta")) {
				return inputFileName.substring("inputFile ".length());
			}
	    }
	    
		return null;
	}

	public void setFastaFile(String newFastaFile) {
		// Search among the input files for the one with ".fasta"
		for (Iterator inputFile = this.inputFile.iterator(); inputFile.hasNext();) {
			String inputFileName = (String) inputFile.next();
			
			if (inputFileName.contains(".fasta")) {
				this.inputFile.remove(inputFileName);
			}
			
			this.inputFile.add(newFastaFile);
	    }
	}

	
	public String getOutputFile() {
		// Search among the input files for the one with ".out"
		for (Iterator inputFile = this.inputFile.iterator(); inputFile.hasNext();) {
			String inputFileName = (String) inputFile.next();
			
			if (inputFileName.contains(".out")) {
				return inputFileName.substring("inputFile ".length());
			}
	    }
		return null;
	}

}
