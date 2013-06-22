package asct.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import asct.core.ApplicationControlFacade;
import asct.shared.AbstractGridApplication;
import asct.shared.BspGridApplication;
// { IMPI
import asct.shared.MpiGridApplication;
// } IMPI
import asct.shared.ExecutionRequestData;
import asct.shared.ExecutionRequestStatus;
import asct.shared.IExecutionListener;
import asct.shared.LoginCallbackHandler;
import asct.shared.ParametricCopyHolder;
import asct.shared.ParametricGridApplication;
import asct.shared.SequencialGridApplication;
import clusterManagement.ApplicationNotFoundException;
import clusterManagement.ApplicationRegistrationException;
import clusterManagement.BinaryCreationException;
import clusterManagement.BinaryNotFoundException;
import clusterManagement.DirectoryCreationException;
import clusterManagement.DirectoryNotEmptyException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;
import dataTypes.ApplicationDescription;
import dataTypes.ApplicationType;
import dataTypes.ContentDescription;

/**
 * A singleton controller responsible to create and remove diretories, binaries,
 * and applications within the ApplicationRepository through the
 * ApplicationControlFacade and excecute and manage the execution of
 * applications within the ApplicationRepository through the
 * ApplicationControlFacade.
 * 
 * @version 1.0 Apr 26, 2006
 * @author Ricardo Luiz de Andrade Abrantes
 */
public class ASCTController {

	private static final int INPUT_STRING_SIZE = (new String("inputFile"))
			.length();

	private static final int OUTPUT_STRING_SIZE = (new String("outputFile"))
			.length();

	private static final int APP_CONSTR_STRING_SIZE = (new String(
			"appConstraints")).length();

	private static final int APP_PREFS_STRING_SIZE = (new String(
			"appPreferences")).length();

	private static final int APP_ARGS_STRING_SIZE = (new String("appArgs"))
			.length();

	/** Singleton instance */
	private static ASCTController instance = null;

	/** ApplicationControlFacade */
	private ApplicationControlFacade facade = null;

	/**
	 * Instance constructor
	 */
	protected ASCTController(String localDirectory) {
		Properties asctProperties = new Properties();
		boolean isSecure = false, useOppStore = false;
		try {
			asctProperties.load(new FileInputStream("asct.conf"));
			String isSecurityEnabled = asctProperties
					.getProperty("isSecurityEnabled");
			String isOppStoreEnabled = asctProperties
					.getProperty("isOppStoreEnabled");

			if (isSecurityEnabled != null
					&& isSecurityEnabled.equalsIgnoreCase("true")) {
				System.out.println("[ASCTController] Security is enabled.");
				isSecure = true;
			}

			if (isOppStoreEnabled != null
					&& isOppStoreEnabled.equalsIgnoreCase("true")) {
				System.out.println("[ASCTController] OppStore is enabled.");
				useOppStore = true;
			}

		} catch (IOException e) {
			System.err
					.println("[ASCTController] WARNING: could not find 'asct.conf'. Using default values.");
		}

		AsctLoginDialog loginDialog = null;
		LoginCallbackHandler callbackHandler = null;

		if (isSecure) {
			loginDialog = new AsctLoginDialog();
			callbackHandler = new LoginCallbackHandler(loginDialog
					.getUsername(), loginDialog.getPassword());
		}

		facade = new ApplicationControlFacade(localDirectory, isSecure,
				useOppStore, callbackHandler);
	}
	
	/**
	 * Does Nothing, worths nothing, its test code and I am ashamed I need to put it here. 
	 */
	protected ASCTController(){
		//This worth nothing. It's just here so the singleton could be mocked properly. 
	}

	/**
	 * Gets a factory (singleton) instance TODO: The call to the ASCTController
	 * should not use those two static values (false,"/tmp"). Instead, is should
	 * receive it from the caller of the getInstance() method. TODO: ASCT should
	 * preserve the output directory contents
	 */
	public static synchronized ASCTController getInstance() {
		if (instance == null) {
			cleanOutputDirectory();
			instance = new ASCTController(getOutputDirectory());
		}
		return instance;
	}

	private static void cleanOutputDirectory() {
		File outputDirectory = new File(getOutputDirectory());
		if (outputDirectory.isDirectory()) {
			String[] children = outputDirectory.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(outputDirectory,
						children[i]));
				if (!success) {
					System.out.println("Error cleaning the output directory");
				}
			}
		}
	}

	/**
	 * Deletes all files and subdirectories under dir. If a deletion fails, the
	 * method stops attempting to delete and returns false.
	 * 
	 * @param dir
	 *            The dir.
	 * @return True if all deletions were successful, false otherwise.
	 */
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}

	public static String getOutputDirectory() {
		return new String(System.getProperty("user.home") + "/output/");
	}

	public void registerExecutionStateListener(IExecutionListener listener) {
		facade.registerExecutionStateListener(listener);
	}

	public ContentDescription[] listRootDirectoryContents()
			throws DirectoryNotFoundException, InvalidPathNameException,
			SecurityException {
		return facade.listRootDirectoryContents();
	}

	public ContentDescription[] listDirectoryContents(final String directoryPath)
			throws DirectoryNotFoundException, InvalidPathNameException,
			SecurityException {
		return facade.listDirectoryContents(directoryPath);
	}

	/**
	 * @param directoryPath
	 *            the relative path from the repository root
	 * @throws SecurityException
	 */
	public void createDirectory(final String directoryPath)
			throws DirectoryCreationException, InvalidPathNameException,
			SecurityException {
		facade.createDirectory(directoryPath);
	}

	/**
	 * @param directoryPath
	 *            the relative path from the repository root
	 * @throws SecurityException
	 */
	public void removeDirectory(final String directoryPath)
			throws DirectoryNotFoundException, DirectoryNotEmptyException,
			InvalidPathNameException, SecurityException {
		facade.removeDirectory(directoryPath);
	}

	/**
	 * @param basePath
	 *            the relative path from the repository root
	 * @param applicationName
	 *            the name of the application
	 * @throws SecurityException
	 */
	public void registerApplication(final String basePath,
			final String applicationName)
			throws ApplicationRegistrationException,
			DirectoryCreationException, InvalidPathNameException,
			SecurityException {
		facade.registerApplication(basePath, applicationName);
	}

	/**
	 * @param basePath
	 *            the relative path from the repository root
	 * @param applicationName
	 *            the name of the application
	 * @throws SecurityException
	 */
	public void unregisterApplication(final String basePath,
			final String applicationName) throws ApplicationNotFoundException,
			DirectoryNotFoundException, DirectoryNotEmptyException,
			InvalidPathNameException, SecurityException {
		facade.unregisterApplication(basePath, applicationName);
	}

	/**
	 * @param localFilePath
	 *            the local path of the file to be uploaded
	 * @param remoteBasePath
	 *            the relative path from the repository root
	 * @param applicationName
	 *            the name of the application
	 * @param platform
	 *            os name plus hardware (eg. Linux_i686)
	 * @throws SecurityException
	 * @throws FileNotFoundException
	 */
	public void uploadBinary(final String localFilePath,
			final String remoteBasePath, final String applicationName,
			final String platform) throws BinaryCreationException,
			ApplicationNotFoundException, DirectoryNotFoundException,
			InvalidPathNameException, SecurityException, FileNotFoundException {
		facade.uploadBinary(localFilePath, remoteBasePath, applicationName,
				platform);
	}

	/**
	 * @param basePath
	 *            the relative path from the repository root
	 * @param applicationName
	 *            the name of the application
	 * @param fileName
	 *            the name of the file to be deleted
	 * @throws SecurityException
	 */
	public void deleteBinary(final String basePath,
			final String applicationName, final String fileName)
			throws ApplicationNotFoundException, DirectoryNotFoundException,
			BinaryNotFoundException, InvalidPathNameException,
			SecurityException {
		facade.deleteBinary(basePath, applicationName, fileName);
	}

	/**
	 * Request the execution of an application
	 * 
	 * @return new request status or null if unknown
	 * @throws SecurityException
	 */
	public ExecutionRequestStatus executeApplication(String basePath,
			String applicationName, String applicationArguments,
			String applicationContraints, String applicationPreferences,
			ApplicationType applicationType, String binaryNames,
			String[] inputFiles, String[] outputFiles,
			ParametricCopyHolder[] parametricCopies, int numberOfTasks,
			boolean forceDifferentMachines)
			throws ApplicationNotFoundException, DirectoryNotFoundException,
			InvalidPathNameException, SecurityException {
		ExecutionRequestStatus requestStatus = null;
		StringTokenizer tokens = new StringTokenizer(binaryNames);
		int numberOfBinaries = tokens.countTokens();
		ExecutionRequestData data = new ExecutionRequestData();

		String binaryIds[] = new String[numberOfBinaries];
		for (int count = 0; count < numberOfBinaries; count++) {
			binaryIds[count] = tokens.nextToken();
			System.out.println("bin ID " + binaryIds[count]);

		}

		AbstractGridApplication application;
		ApplicationDescription applicationDescrption = new ApplicationDescription(
				basePath, applicationName, (new Integer(numberOfBinaries))
						.toString(), binaryIds);
		if (applicationType.value() == ApplicationType.bsp.value()) {
			application = new BspGridApplication(applicationName, basePath);
		}
		// { IMPI
		else if (applicationType.value() == ApplicationType.mpi.value()) {
			application = new MpiGridApplication(applicationName, basePath);
		}
		// } IMPI

		else if (applicationType.value() == ApplicationType.parametric.value()) {
			application = new ParametricGridApplication(applicationName,
					basePath);
		} else {
			application = new SequencialGridApplication(applicationName,
					basePath);
		}
		application.setDescription(applicationDescrption);

		data.setApplication(application);

		System.out.println("=====>>>");
		System.out.println("app name " + applicationName);
		System.out.println("base path " + basePath);

		data.setArguments(applicationArguments);
		System.out.println("apparg " + applicationArguments);
		data.setInputFiles(inputFiles);
		System.out.println("input " + inputFiles.length);
		data.setOutputFileNames(outputFiles);
		System.out.println("outpu " + outputFiles.length);
		data.setNumberOfTasks(numberOfTasks);
		System.out.println("numOfTask " + numberOfTasks);
		data.setNumberOfCopies(parametricCopies.length);
		System.out.println("numOfparamCopies " + parametricCopies.length);
		data.setParametricCopies(parametricCopies);
		data.setForceDifferentMachines(forceDifferentMachines);
		System.out.println("forceDiff " + forceDifferentMachines);
		data.setConstraints(applicationContraints);
		System.out.println("appCons " + applicationContraints);
		data.setPreferences(applicationPreferences);
		System.out.println("app Pref " + applicationPreferences);
		System.out.println("<<<=======");
		/**
		 * Upload InputFiles to OppStore
		 */
		String[] inputKeys = facade.uploadFilesToOppStore(inputFiles);
		if (inputKeys != null)
			data.setInputFiles(inputKeys);

		/**
		 * Requests application execution
		 */
		requestStatus = facade.executeApplication(data);

		return requestStatus;
	}

	/**
	 * Kills the execution of an application
	 * 
	 * @param requestId
	 *            id of the request to be killed
	 */
	public void killApplication(final String requestId) {
		facade.killApplication(requestId);
	}

	/**
	 * Gets the results of an execution
	 * 
	 * @param execReqId
	 *            id of the request to get results
	 */
	public void getApplicationResults(String execReqId, String[] outputFiles) {
		facade.getApplicationResults(execReqId, outputFiles);
	}

	public ExecutionRequestDescriptorComposite readExecutionRequestDescriptorFile(
			BufferedReader descFile) throws FileNotFoundException, IOException {
		ExecutionRequestDescriptorComposite requestDescriptor = new ExecutionRequestDescriptorComposite();

		while (true) {
			String line = null;
			line = descFile.readLine();
			if (line == null)
				break;
			StringTokenizer st = new StringTokenizer(line);
			if (st.hasMoreTokens()) {
				String option = st.nextToken();
				// Parsing appType-----------------------------
				if (option.equals("appType")) {
					if (st.hasMoreTokens()) {
						String appType = st.nextToken();
						if (appType.equals("BSP")) {
							requestDescriptor
									.setApplicationType(ApplicationType.bsp);
						} else if (appType.equals("PARAMETRIC")) {
							requestDescriptor
									.setApplicationType(ApplicationType.parametric);
						} else if (appType.equals("REGULAR")) {
							requestDescriptor
									.setApplicationType(ApplicationType.regular);
						} else if (appType.equals("MPI")) {
							requestDescriptor
									.setApplicationType(ApplicationType.mpi);
						}
					}
				}
				// Parsing ApplicationName------------------------------------
				else if (option.equals("appName")) {
					if (st.hasMoreTokens())
						requestDescriptor.setApplicationName(st.nextToken());
				}
				// Parsing applicationBasePath--------------------------------
				else if (option.equals("applicationBasePath")) {
					if (st.hasMoreTokens()) {
						requestDescriptor
								.setApplicationBasePath(st.nextToken());
					}
				}
				// Parsing application binaries-------------------------------
				else if (option.equals("binaries")) {
					if (st.hasMoreTokens()) {
						requestDescriptor
								.setApplicationBinaries(st.nextToken());
					}
				}
				// Parsing appConstraints-----------------------------
				else if (option.equals("appConstraints")) {
					if (st.hasMoreTokens())
						requestDescriptor.setApplicationConstraints(line
								.substring(APP_CONSTR_STRING_SIZE + 1));
				}
				// Parsing appPreferences-----------------------------
				else if (option.equals("appPreferences")) {
					if (st.hasMoreTokens())
						requestDescriptor.setApplicationPreferences(line
								.substring(APP_PREFS_STRING_SIZE + 1));
				}
				// Parsing appArgs------------------------------------
				else if (option.equals("appArgs")) {
					if (st.hasMoreTokens())
						requestDescriptor.setApplicationArguments(line
								.substring(APP_ARGS_STRING_SIZE + 1));
				}

				// Parsing bsp number of
				// tasks------------------------------------
				else if (option.equals("taskNum")) {
					if (st.hasMoreTokens())
						if (requestDescriptor.getApplicationType() == ApplicationType.bsp)
							requestDescriptor.setBspNumberOfTasks((new Integer(
									st.nextToken())).intValue());
						else if (requestDescriptor.getApplicationType() == ApplicationType.mpi)
							requestDescriptor.setMpiNumberOfTasks((new Integer(
									st.nextToken())).intValue());
				}
				// Parsing inputFile------------------------------------
				else if (option.equals("inputFile")) {
					String filename = line.substring(INPUT_STRING_SIZE + 1);
					requestDescriptor.addInputFile(filename);
				}
				// Parsing outputFile------------------------------------
				else if (option.equals("outputFile")) {
					String filename = line.substring(OUTPUT_STRING_SIZE + 1);
					requestDescriptor.addOutputFile(filename);
				}
				// Parsing appCopy------------------------------------
				else if (option.equals("appCopy")) {
					if (requestDescriptor.getApplicationType() == ApplicationType.parametric) {
						ExecutionRequestDescriptorComponent copyHolder = new ExecutionRequestDescriptorComponent();
						while (true) {
							String newLine = null;
							try {
								newLine = descFile.readLine();
							} catch (IOException fnfe) {
								System.err
										.println("RequestExecutionPanel::readExecDescriptor-->> IO error");
								System.exit(-1);
							}
							if (newLine == null)
								break;
							StringTokenizer newSt = new StringTokenizer(newLine);
							if (newSt.hasMoreTokens()) {
								String newOption = newSt.nextToken();
								if (newOption.equals("appArgs")) {
									if (newSt.hasMoreTokens())
										copyHolder
												.setApplicationArguments(newLine
														.substring(APP_ARGS_STRING_SIZE + 1));
								}
								// Parsing
								// inputFile------------------------------------
								else if (newOption.equals("inputFile")) {
									String filename = newLine
											.substring(INPUT_STRING_SIZE + 1);
									copyHolder.addInputFile(filename);
								}
								// Parsing
								// outputFile-----------------------------------
								else if (newOption.equals("outputFile")) {
									String filename = newLine
											.substring(OUTPUT_STRING_SIZE + 1);
									copyHolder.addOutputFile(filename);
								} else if (newOption.equals("endCopy")) {
									requestDescriptor
											.addParametricApplicationCopies(copyHolder);
									break;
								}
							}// if(newSt.hasMoreTokens())
						}// while(true)
					}// if(isParametric)
				}// else if(option.equals("appCopy"))
				else if (option.equals("shouldForceDifferentMachines")) {
					if (st.hasMoreTokens() && (st.nextToken().equals("true"))) {
						requestDescriptor.setShouldForceDifferentMachines(true);
					}
				}
			}// if(st.hasMoreTokens())
		}// while(true)
		try {
			descFile.close();
		} catch (IOException fnfe) {
			System.err
					.println("RequestExecutionPanel::readExecDescriptor-->> IO error");
			System.exit(-1);
		}
		return requestDescriptor;
	}

	public void writeExecutionRequestDescriptorFile(
			ExecutionRequestDescriptorComposite requestDescriptor,
			PrintWriter descFile) {

		// Writing appType-------------------------------------------------
		descFile.print("appType ");
		if (requestDescriptor.getApplicationType() == ApplicationType.regular)
			descFile.println("REGULAR");
		else if (requestDescriptor.getApplicationType() == ApplicationType.bsp)
			descFile.println("BSP");
		else if (requestDescriptor.getApplicationType() == ApplicationType.parametric)
			descFile.println("PARAMETRIC");
		else if (requestDescriptor.getApplicationType() == ApplicationType.mpi)
			descFile.println("MPI");
		// Writing appName-------------------------------------------------
		String appName = requestDescriptor.getApplicationName();
		if (!appName.equals(""))
			descFile.println("appName " + appName);
		// Writing
		// applicationBasePath-------------------------------------------------
		descFile.println("applicationBasePath "
				+ requestDescriptor.getApplicationBasePath());
		// Writing binaries-------------------------------------------------
		descFile.println("binaries "
				+ requestDescriptor.getApplicationBinaries());
		// Writing appConstraints--------------------------------------
		descFile.println("appConstraints "
				+ requestDescriptor.getApplicationConstraints());
		// Writing appPreferences--------------------------------------
		descFile.println("appPreferences "
				+ requestDescriptor.getApplicationPreferences());
		// Writing taskNum & appArgs & NeededFiles------------------------
		if (requestDescriptor.getApplicationType() == ApplicationType.bsp) {
			descFile.println("taskNum " + requestDescriptor.getNumberOfTasks());
		}
		if (requestDescriptor.getApplicationType() == ApplicationType.mpi) {
			descFile.println("taskNum " + requestDescriptor.getNumberOfTasks());
		}
		if (requestDescriptor.getApplicationType() == ApplicationType.bsp
				|| requestDescriptor.getApplicationType() == ApplicationType.regular
				|| requestDescriptor.getApplicationType() == ApplicationType.mpi) {
			descFile.println("appArgs "
					+ requestDescriptor.getApplicationArguments());
			for (Iterator<String> i = requestDescriptor.getInputFiles(); i
					.hasNext();)
				descFile.println("inputFile " + i.next());
			for (Iterator<String> i = requestDescriptor.getOutputFiles(); i
					.hasNext();)
				descFile.println("outputFile " + i.next());
		}
		if (requestDescriptor.getApplicationType() == ApplicationType.parametric) {
			for (Iterator<ExecutionRequestDescriptorComponent> i = requestDescriptor
					.getParametricApplicationCopies(); i.hasNext();) {
				ExecutionRequestDescriptorComponent copy = i.next();
				descFile.println("appCopy");

				descFile.println("appArgs " + copy.getApplicationArguments());
				for (Iterator<String> iterator = copy.getInputFiles(); iterator
						.hasNext();)
					descFile.println("inputFile " + iterator.next());
				for (Iterator<String> iterator = copy.getOutputFiles(); iterator
						.hasNext();)
					descFile.println("outputFile " + iterator.next());
				descFile.println("endCopy");
			}
		}

		descFile.println("shouldForceDifferentMachines "
				+ requestDescriptor.getShouldForceDifferentMachines());

		descFile.close();
	}
}
