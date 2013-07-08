/**
 * @(#)ApplicationRepositoryManager.java		Dec 20, 2005
 *
 * Copyleft
 */
package asct.core.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import asct.core.corba.ApplicationRepositoryStubWrapper;
import asct.shared.AbstractGridApplication;
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
import dataTypes.BinaryDescription;
import dataTypes.ContentDescription;

/**
 * Class description goes here.
 * 
 * @version 1.0 Dec 20, 2005
 * @author Eduardo Guerra and Eudenia Xavier
 */
public class ApplicationRepositoryManager {

	/** */
	private ApplicationRepositoryStubWrapper appRepStubWrapper;
	
	/**
	 * Constructor.
	 */
	public ApplicationRepositoryManager(
				ApplicationRepositoryStubWrapper appRepStubWrapper) {
		this.appRepStubWrapper  = appRepStubWrapper;
	}

	/**
	 * @throws SecurityException 
	 * 
	 * */
	public ContentDescription[] listRootDirectoryContents() 
			throws DirectoryNotFoundException, InvalidPathNameException, SecurityException {
		return listDirectoryContents("/");
	}

	/**
	 * @param directoryPath the directory path starting at repository root 
	 * @throws SecurityException 
	 * */
	public ContentDescription[] listDirectoryContents(String directoryPath) 
			throws DirectoryNotFoundException, InvalidPathNameException, SecurityException{
		return appRepStubWrapper.listDirectoryContents(directoryPath);
	}

	/**
	 * @throws SecurityException 
	 * 
	 * */
	public void createDirectory(String directoryPath) 
				throws DirectoryCreationException, InvalidPathNameException, SecurityException {
		if (directoryPath != null && directoryPath.length() != 0) {
			appRepStubWrapper.createDirectory(directoryPath);
		}
	}

	/**
	 * @param directoryPath - relative path of the new dir from the root path 
	 * @throws SecurityException 
	 * */
	public void removeDirectory(String directoryPath) 
				throws DirectoryNotFoundException, DirectoryNotEmptyException,
					InvalidPathNameException, SecurityException {
		appRepStubWrapper.removeDirectory(directoryPath);
	}

	/**
	 * @throws SecurityException 
	 * 
	 * */
	public void registerApplication(String basePath, String applicationName) 
					throws ApplicationRegistrationException, 
						DirectoryCreationException, InvalidPathNameException, SecurityException {
		if (applicationName != null && applicationName.length() != 0) {
			appRepStubWrapper.registerApplication(basePath, 
							applicationName);
		}
	}
	
	/**
	 * @throws SecurityException 
	 * 
	 * */
	public void unregisterApplication(String basePath, 
					String applicationName) 
			throws ApplicationNotFoundException, DirectoryNotFoundException,
					DirectoryNotEmptyException, InvalidPathNameException, SecurityException {

	   appRepStubWrapper.unregisterApplication(basePath, applicationName);
	}

	/**
	 * @param localFilePath
	 * @param applicationName
	 * @param platform os plus hardware (eg. Linux_i686)
	 * @throws SecurityException 
	 * @throws FileNotFoundException 
	 * */
	public void uploadBinary(String localFilePath, String remoteBasePath, 
					String applicationName, String platform) 
			throws BinaryCreationException, ApplicationNotFoundException,
					DirectoryNotFoundException, InvalidPathNameException, SecurityException, FileNotFoundException {
		FileInputStream fis = null;
		byte [] serializedFile = null;
		
		File selectedBinaryFile = new File(localFilePath);
		
			fis = new FileInputStream(selectedBinaryFile);
			serializedFile = new byte[(int) selectedBinaryFile.length()];
			System.out.println("Asct::uploadBinary-->>fileSize: " 
						+ selectedBinaryFile.length());
		try {
			fis.read(serializedFile);
			fis.close();
			
			BinaryDescription binDesc = new BinaryDescription();
			binDesc.basePath = remoteBasePath; 
			binDesc.applicationName  = applicationName;
			binDesc.binaryName = platform;
			binDesc.description  = ""; // NOT USED "AppDescription_appDescFile"
			
			this.appRepStubWrapper.uploadApplicationBinary(binDesc, 
						serializedFile);
		} catch (IOException e) {
			System.err.println("[ApplicationRepositoryManager] IOException: " 
					+ e.getMessage());
		}
	}

	/**
	 * @throws SecurityException 
	 * 
	 * */
	public void deleteBinary(String basePath, String applicationName, 
				String fileName) 
			throws ApplicationNotFoundException, DirectoryNotFoundException, 
				BinaryNotFoundException, InvalidPathNameException, SecurityException {
		appRepStubWrapper.deleteApplicationBinary(basePath, 
					applicationName, fileName);
	}
	
	/**
	 * @param application the aplication 
	 * @return AppDescription a descrition of application
	 * @throws SecurityException 
	 */
	public ApplicationDescription getAppDescription(
				final AbstractGridApplication application) 
			throws ApplicationNotFoundException, DirectoryNotFoundException, 
				InvalidPathNameException, SecurityException {
		
		ApplicationDescription appDesc = new ApplicationDescription();

		appDesc = appRepStubWrapper.getApplicationDescription(
						application.getBasePath(), 
						application.getName());
	    return appDesc;
	}

}
