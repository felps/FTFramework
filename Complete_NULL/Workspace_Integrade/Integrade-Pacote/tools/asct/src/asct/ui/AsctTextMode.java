package asct.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import clusterManagement.ApplicationNotFoundException;
import clusterManagement.ApplicationRegistrationException;
import clusterManagement.BinaryCreationException;
import clusterManagement.DirectoryCreationException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;

import asct.shared.ExecutionData;
import asct.shared.ExecutionRequestStatus;
import asct.shared.ParametricCopyHolder;

import dataTypes.ApplicationType;

public class AsctTextMode {
	private ASCTController controller;

	private String inputFilename = null;

	private String binaryPath = null;

	private String outputPath = null;

	FinishedExecutionListener executionListener;

	public AsctTextMode(String inputFilename, String binaryPath,
			String outputPath) {
		this.inputFilename = inputFilename;
		this.binaryPath = binaryPath;
		this.outputPath = outputPath;

		controller = ASCTController.getInstance();
		executionListener = new FinishedExecutionListener();
		controller.registerExecutionStateListener(executionListener);
	}

	public void run() {
		ExecutionRequestDescriptorComposite executionDescriptor = null;

		if (inputFilename == null || binaryPath == null) {
			return;
		}

		try {
			BufferedReader descFile = new BufferedReader(
					(new InputStreamReader(new FileInputStream(inputFilename))));
			executionDescriptor = controller
					.readExecutionRequestDescriptorFile(descFile);
		} catch (FileNotFoundException e1) {
			System.err.println("Execution descriptor file not found!");
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String basePath = executionDescriptor.getApplicationBasePath();

		String binary = executionDescriptor.getApplicationBinaries();

		String applicationName = executionDescriptor.getApplicationName();

		String applicationArguments = executionDescriptor
				.getApplicationArguments();
		if (applicationArguments == null) {
			applicationArguments = "";
		}
		String applicationConstraints = executionDescriptor
				.getApplicationConstraints();
		if (applicationConstraints == null) {
			applicationConstraints = "";
		}
		String applicationPreferences = executionDescriptor
				.getApplicationPreferences();
		if (applicationPreferences == null) {
			applicationPreferences = "";
		}

		ApplicationType applicationType = executionDescriptor
				.getApplicationType();

		boolean shouldForceDifferentMachines = executionDescriptor
				.getShouldForceDifferentMachines();

		int numberOfTasks;
		if (executionDescriptor.getApplicationType() == ApplicationType.bsp) {
			numberOfTasks = executionDescriptor.getNumberOfTasks();
		} else if (executionDescriptor.getApplicationType() == ApplicationType.mpi) {
			numberOfTasks = executionDescriptor.getNumberOfTasks();
		} else {
			numberOfTasks = 1;
		}

		try {
			controller.createDirectory(basePath);
		} catch (DirectoryCreationException e) {
			System.out
					.println("[Warning] Problem creating directory. Probably it already exists. Continuing...");
		} catch (InvalidPathNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			controller.registerApplication(basePath, applicationName);
		} catch (ApplicationRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DirectoryCreationException e) {
			System.out
					.println("[Warning] Problem creating directory. Probably it already exists. Continuing...");
		} catch (InvalidPathNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			controller.uploadBinary(binaryPath, basePath, applicationName,
					binary);
		} catch (BinaryCreationException e) {
			System.out
					.println("[Warning] Problem creating binary. Probably it already exists. Continuing...");
		} catch (ApplicationNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DirectoryNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPathNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<String> inputFilesList = new ArrayList<String>();
		for (Iterator<String> i = executionDescriptor.getInputFiles(); i
				.hasNext();) {
			inputFilesList.add(i.next());
		}
		String[] inputFiles = new String[inputFilesList.size()];
		inputFilesList.toArray(inputFiles);

		ArrayList<String> outputFilesList = new ArrayList<String>();
		for (Iterator<String> i = executionDescriptor.getOutputFiles(); i
				.hasNext();) {
			outputFilesList.add(i.next());
		}
		String[] outputFiles = new String[outputFilesList.size()];
		outputFilesList.toArray(outputFiles);

		ArrayList<ParametricCopyHolder> parametricCopiesList = new ArrayList<ParametricCopyHolder>();
		for (Iterator<ExecutionRequestDescriptorComponent> i = executionDescriptor
				.getParametricApplicationCopies(); i.hasNext();) {

			ArrayList<String> output = new ArrayList<String>();
			for (Iterator<String> j = executionDescriptor.getOutputFiles(); j
					.hasNext();) {
				output.add(j.next());
			}

			ArrayList<String> input = new ArrayList<String>();
			for (Iterator<String> j = executionDescriptor.getInputFiles(); j
					.hasNext();) {
				input.add(j.next());
			}

			String[] inputArray = new String[input.size()];
			String[] outputArray = new String[output.size()];

			input.toArray(inputArray);
			output.toArray(outputArray);

			ParametricCopyHolder pc = new ParametricCopyHolder(0, i.next()
					.getApplicationArguments(), inputArray, outputArray);
			parametricCopiesList.add(pc);
		}
		ParametricCopyHolder[] parametricCopies = new ParametricCopyHolder[parametricCopiesList
				.size()];
		parametricCopiesList.toArray(parametricCopies);

		// Requesting the execution
		try {
			ExecutionRequestStatus requestStatus = controller
					.executeApplication(basePath, applicationName,
							applicationArguments, applicationConstraints,
							applicationPreferences, applicationType, binary,
							inputFiles, outputFiles, parametricCopies,
							numberOfTasks, shouldForceDifferentMachines);

			while (!executionListener.hasFinished()) {
				if (executionListener.hasBeenRefused()) {
					System.out.println("The application has been refused.");
					return;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			controller
					.getApplicationResults(requestStatus.getRequestId(), null);

		} catch (ApplicationNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DirectoryNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPathNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int count = 0;
		String resultsBasePathName;

		if (outputPath == null) {
			resultsBasePathName = System.getProperty("user.home")
					+ "/output_text/";
		} else {
			resultsBasePathName = outputPath + "/";
		}

		if (!(new File(resultsBasePathName).exists())) {
			boolean hasCreated = (new File(resultsBasePathName)).mkdir();
			if (!hasCreated) {
				return;
			}
		}

		// String directoryName = executionDescriptor.getApplicationName();
		// if (directoryName == null || directoryName.trim().length() == 0) {
		// directoryName = "execution";
		// }

		// String resultsPathName = resultsBasePathName + directoryName + "_";
		String resultsPathName = resultsBasePathName;

		while ((new File(resultsPathName + String.valueOf(count))).exists()) {
			count++;
		}

		boolean hasCreated = (new File(resultsPathName + String.valueOf(count)))
				.mkdir();
		if (hasCreated == true) {
			resultsPathName = resultsPathName + (String.valueOf(count));
			String output = ASCTController.getOutputDirectory();

			copyDirectory(new File(output + "0/"), new File(resultsPathName));

			// System.out.println("/bin/cp -r " + output + "0/* " +
			// resultsPathName);
			// try {
			// System.out.println("/bin/cp -r " + output + "0/* "
			// + resultsPathName + "/");
			// Process proc = Runtime.getRuntime()
			// .exec(
			// "/bin/cp -r " + output + "0/* "
			// + resultsPathName + "/");
			// try {
			// proc.waitFor();
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}

		System.out.println("The results of your application were copied to "
				+ resultsPathName);
	}

	private void copyDirectory(File srcPath, File dstPath) {
		if (srcPath.isDirectory()) {
			if (!dstPath.exists()) {
				dstPath.mkdir();
			}

			String files[] = srcPath.list();

			for (int i = 0; i < files.length; i++) {
				copyDirectory(new File(srcPath, files[i]), new File(dstPath,
						files[i]));
			}

		} else {
			if (!srcPath.exists()) {
				return;
			} else {
				InputStream in = null;
				OutputStream out = null;
				try {
					in = new FileInputStream(srcPath);
					out = new FileOutputStream(dstPath);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				try {
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		String filename = null;
		String binaryPath = null;
		String outputPath = null;

		for (int i = 0; i < args.length; i++) {
			// Input configuration file
			if (args[i].equals("-i")) {
				filename = args[i + 1];
			}
			// Path to binary to be executed
			else if (args[i].equals("-b")) {
				binaryPath = args[i + 1];
			}
			// Path to the results directory
			else if (args[i].equals("-o")) {
				outputPath = args[i + 1];
			}
		}
		if (binaryPath == null || filename == null) {
			System.out
					.println("Usage: java AsctTextMode -i <execution descriptor filename> -b <binary> [-o <results pathname>]");
		} else {
			AsctTextMode asctText = new AsctTextMode(filename, binaryPath,
					outputPath);
			asctText.run();
			System.exit(0);
		}
	}
}
