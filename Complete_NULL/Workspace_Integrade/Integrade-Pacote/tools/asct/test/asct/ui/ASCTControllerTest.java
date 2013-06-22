package asct.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.JUnit4TestAdapter;
import mocks.BufferedReaderMock;
import mocks.PrintWriterMock;

import org.junit.Test;

import asct.ui.ASCTController;
import asct.ui.ExecutionRequestDescriptorComponent;
import asct.ui.ExecutionRequestDescriptorComposite;
import dataTypes.ApplicationType;

public class ASCTControllerTest {

	@Test
	public void testWriteExecutionRequestDescriptorFile() {
		String expectedFile = null;
		PrintWriter actualFile = null;

		expectedFile = "";
		actualFile = new PrintWriterMock(new StringWriter());

		expectedFile += "appType REGULAR\n";
		expectedFile += "appName TestApp\n";
		expectedFile += "applicationBasePath /some/path\n";
		expectedFile += "binaries some binaries\n";
		expectedFile += "appConstraints some constraints\n";
		expectedFile += "appPreferences some preferences\n";
		expectedFile += "appArgs --test\n";
		expectedFile += "shouldForceDifferentMachines true\n";

		ExecutionRequestDescriptorComposite requestDescriptor = new ExecutionRequestDescriptorComposite();

		requestDescriptor.setApplicationName("TestApp");
		requestDescriptor.setApplicationType(ApplicationType.regular);
		requestDescriptor.setApplicationBasePath("/some/path");
		requestDescriptor.setApplicationBinaries("some binaries");
		requestDescriptor.setApplicationConstraints("some constraints");
		requestDescriptor.setApplicationPreferences("some preferences");
		requestDescriptor.setApplicationArguments("--test");
		requestDescriptor.setShouldForceDifferentMachines(true);

		ASCTController.getInstance().writeExecutionRequestDescriptorFile(
				requestDescriptor, actualFile);

		assertEquals("Arguments should be equals.", expectedFile,
				((PrintWriterMock) actualFile).getContent());

	}

	@Test
	public void testReadExecutionRequestDescriptorFile() {
		ExecutionRequestDescriptorComponent appCopy = new ExecutionRequestDescriptorComponent();
		appCopy.setApplicationArguments("appCopyArg0");
		appCopy.addOutputFile("stdout");

		ExecutionRequestDescriptorComposite expectedDescriptor = new ExecutionRequestDescriptorComposite();
		expectedDescriptor.setApplicationName("TestApp");
		expectedDescriptor.setApplicationType(ApplicationType.parametric);
		expectedDescriptor.setApplicationBasePath("/some/path");
		expectedDescriptor.setApplicationBinaries("someBinary");
		expectedDescriptor.setApplicationConstraints("some constraints");
		expectedDescriptor.setApplicationPreferences("some preferences");
		expectedDescriptor.setApplicationArguments("--test");
		expectedDescriptor.addParametricApplicationCopies(appCopy);
		expectedDescriptor.setShouldForceDifferentMachines(true);

		BufferedReaderMock buffer = new BufferedReaderMock(new StringReader(""));

		buffer.writeLine("appType PARAMETRIC");
		buffer.writeLine("appName TestApp");
		buffer.writeLine("applicationBasePath /some/path");
		buffer.writeLine("binaries someBinary");
		buffer.writeLine("appConstraints some constraints");
		buffer.writeLine("appPreferences some preferences");
		buffer.writeLine("appArgs --test\n");
		buffer.writeLine("shouldForceDifferentMachines true\n");
		buffer.writeLine("appCopy");
		buffer.writeLine("appArgs appCopyArg0");
		buffer.writeLine("outputFile stdout");
		buffer.writeLine("endCopy");

		ExecutionRequestDescriptorComposite actualDescriptor = null;

		try {
			actualDescriptor = ASCTController
					.getInstance()
					.readExecutionRequestDescriptorFile((BufferedReader) buffer);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Should not throw an exception.");
		}

		assertTrue(actualDescriptor.equals(expectedDescriptor));
	}
	
	// Must be here to be tested by automaticTests
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ASCTControllerTest.class);
	}
}
