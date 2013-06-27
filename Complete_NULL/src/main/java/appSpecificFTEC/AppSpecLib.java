package appSpecificFTEC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import appSpecificFTEC.ReverseFileReader;;

public class AppSpecLib {

	public AppSpecLib(String filename) {

	}
	
	public static void evaluateOutputAndCreateNewFasta(String inputFastaFile, String blastOutputFilename, String newFastaInputFilename){
		boolean found = false;
		//*Workaround:
	    //* input: originalTask Task
	    //* 1) Get partial output
		
		ReverseFileReader inputFile; 
		inputFile = openOutputFile(blastOutputFilename);

		//* 2) read it backwards
		while(!found){
			String lastLine = getLastLine(inputFile);
			
	    //* 3) Evaluate each line
	    //* 3.1) If line starts by "Query=<sequence_name>" then
			if(lastLine.startsWith("Query= ")){
			    //* <sequence_name> was the last evaluated query
		
				FileReader inputFasta;
				
				try {
					inputFasta = new FileReader(new File(inputFastaFile));
				} catch (FileNotFoundException e) {
					System.out.println("Input Fasta File not Found");
					e.printStackTrace();
					return;
				}
				
				//* 4) Scan the input for "Query= <sequence_name>"
			    String lastEvaluatedSequence;
				lastEvaluatedSequence = lastLine.substring("Query= ".length());
				
				int lastLineEvaluated; 
				lastLineEvaluated = scanInputForNextSequence(inputFasta, lastEvaluatedSequence);
				
				//* 5) <sequence_name> was the last evaluated query
				createNewInput(inputFastaFile, lastLineEvaluated, newFastaInputFilename);
			}
		}
	    //* 6) Resubmit the task configured for the new file 
		//output: String configFile, Task task

	}

	private static void createNewInput(String inputFilename, int lastLine, String outputFilename) {

		FileWriter output = null;
		try {
			output = new FileWriter(new File(outputFilename));

		} catch (FileNotFoundException e) {
			System.out.println("Output file not found");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Erro criando outputFile");
			e.printStackTrace();
			return;
		}

		FileReader input = null;
		try {
			input = new FileReader(new File(inputFilename));

		} catch (FileNotFoundException e) {
			System.out.println("Open old Input");
			e.printStackTrace();
			return;
		}
		
		String lastlineRead = "none";
		for(int i=0; i<lastLine; i++) {
			try {
				lastlineRead = readline(input);
			} catch (IOException e) {
				System.out.println("Problemas lendo linhas previas");
				System.out.println(lastlineRead);
				e.printStackTrace();
			}
		}
		try {
		
			while (input.ready()){
				lastlineRead = readline(input);
				output.write(lastlineRead);
				output.write('\n');
			}
		
		} catch (IOException e) {
			System.out.println("Problemas lendo linhas previas");
			System.out.println(lastlineRead);
			e.printStackTrace();
		}
		
	}

	private static int scanInputForNextSequence(FileReader inputFasta, String lastEvaluatedSequence) {
		//Check Fasta File for a line starting with a '>' and followed by <sequence_name>
		String currentLine = "";
		boolean found = false;
		int lineNumber = 0;

		try {
			while (inputFasta.ready()){
				currentLine = readline(inputFasta);
				// If the substring Starts a new sequence
				if (currentLine.charAt(0) == '>'){
					// If we have not found the lastEvaluatedSequence
					if(!found){
						// Check to see if this is the one
						if (currentLine.substring(1).compareTo(lastEvaluatedSequence)==0){
							found = true;
							return lineNumber;
						}
						else
							lineNumber++;
					}
				}
				else lineNumber++;
				
			}
		} catch (IOException e) {
			System.out.println("Erro 100");
			e.printStackTrace();
		}
		
		return -1;
	}

	private static String readline(FileReader input) throws IOException{
		int nextChar =  input.read();
		String line = "";
		// If this is a line break or carrige return, stop looking
		while (nextChar != 13 && nextChar != 10 ) {
			String currentChar = ""+(char) nextChar;
			line = line.concat(currentChar);
			nextChar = input.read();
		}
		return line;
	}
	
	private static ReverseFileReader openOutputFile(String filename) {
		try {
			return new ReverseFileReader(filename);
		} catch (Exception e) {
			System.out.printf("Erro na abertura do arquivo %s ");
			e.printStackTrace();
		}
		return null;
	}

	private static String getLastLine(ReverseFileReader outputFile) {
		try {
			return outputFile.readLine();
		} catch (Exception e) {
			System.out.println("Erro na abertura do arquivo Blast Output");
			e.printStackTrace();
		}
		return null;
	}

	public static AppDescriptor parseDescriptor(String appDescriptorFile) {
		AppDescriptor appDescriptor = new AppDescriptor(appDescriptorFile);
		return appDescriptor;
	}
}