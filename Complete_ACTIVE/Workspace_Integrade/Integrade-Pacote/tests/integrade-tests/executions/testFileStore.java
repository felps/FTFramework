package executions;

import inspectors.OutputInspector;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import tools.FinishedExecutionListener;
import asct.ui.ASCTController;
import clusterManagement.ApplicationNotFoundException;
import clusterManagement.BinaryNotFoundException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;
import dataTypes.ContentDescription;

public class testFileStore {

	private OutputInspector outputInspector = null;
	private ASCTController controller;
	private FinishedExecutionListener listener;

	@Before
	public void setUpBefore() {
		outputInspector = new OutputInspector();
		listener = new FinishedExecutionListener();
		controller = ASCTController.getInstance();
		controller.registerExecutionStateListener(listener);
	}

	@Test
	public void testAddRemove() {
		String basePath = "/test/";
		String applicationName = "pwd";
		String binary = "/bin/pwd";
		String platform = "Linux_i686";

		try {
			controller.createDirectory(basePath);
		} catch (Exception e) {
			Assert.fail("Can't create directory");
		}

		try {
			controller.registerApplication(basePath, applicationName);
		} catch (Exception e) {
			Assert.fail("Can't Register Application");
		}

		try {
			controller
					.uploadBinary(binary, basePath, applicationName, platform);
		} catch (Exception e) {
			Assert.fail("Can't Upload Binary");
		}
		
		ContentDescription[] contents = null;
		
		try {
			controller.deleteBinary("test","pwd","Linux_i686");	
		} catch (ApplicationNotFoundException e) {
			Assert.fail("Application Not Found");
		} catch (DirectoryNotFoundException e) {
			Assert.fail("Can't find directory");
		} catch (BinaryNotFoundException e) {
			Assert.fail("Binary not Found");
		} catch (InvalidPathNameException e) {
			Assert.fail("Invalid path Name");
		} catch (SecurityException e) {
			Assert.fail("Security exception ^.^ ");
		}
		
		
		try {
			controller.unregisterApplication("test", "pwd");
		} catch (Exception e1) {
			Assert.fail("Unregister application ended abnormaly");
		}
		
		try {
			controller.removeDirectory("test");
		} catch (Exception e1) {
			Assert.fail("RemoveDirectory Failed");
		}
		
	}

	
}
