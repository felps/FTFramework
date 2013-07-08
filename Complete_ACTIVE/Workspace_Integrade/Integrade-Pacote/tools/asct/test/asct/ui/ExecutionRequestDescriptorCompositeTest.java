package asct.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import dataTypes.ApplicationType;

public class ExecutionRequestDescriptorCompositeTest {

	ExecutionRequestDescriptorComposite a = new ExecutionRequestDescriptorComposite();

	ExecutionRequestDescriptorComposite b = new ExecutionRequestDescriptorComposite();

	ExecutionRequestDescriptorComposite c = new ExecutionRequestDescriptorComposite();

	ExecutionRequestDescriptorComposite d = new ExecutionRequestDescriptorComposite();

	@Before
	public void setUp() throws Exception {

		a.setApplicationName("TestApp");
		a.setApplicationType(ApplicationType.regular);
		a.setApplicationBasePath("/some/path");
		a.setApplicationBinaries("some binaries");
		a.setApplicationConstraints("some constraints");
		a.setApplicationPreferences("some preferences");
		a.setApplicationArguments("--test");
		a.setShouldForceDifferentMachines(true);

		b.setApplicationName("TestApp");
		b.setApplicationType(ApplicationType.regular);
		b.setApplicationBasePath("/some/path");
		b.setApplicationBinaries("some binaries");
		b.setApplicationConstraints("some constraints");
		b.setApplicationPreferences("some preferences");
		b.setApplicationArguments("--test");
		b.setShouldForceDifferentMachines(true);

		c.setApplicationName("TestApp");
		c.setApplicationType(ApplicationType.regular);
		c.setApplicationBasePath("/some/path");
		c.setApplicationBinaries("some binaries");
		c.setApplicationConstraints("some constraints");
		c.setApplicationPreferences("some preferences");
		c.setApplicationArguments("--test");
		c.setShouldForceDifferentMachines(true);

		d.setApplicationName("AnotherTestApp");
		d.setApplicationType(ApplicationType.regular);
		d.setApplicationBasePath("/some/path");
		d.setApplicationBinaries("some binaries");
		d.setApplicationConstraints("some constraints");
		d.setApplicationPreferences("some preferences");
		d.setApplicationArguments("--test");
		d.setShouldForceDifferentMachines(true);
	}

	@Test
	public void testEquals() throws Exception {
		assertEquals("a and b should be equals.", a, b);
		assertEquals("b and c should be equals.", b, c);
		assertEquals("a and c should be equals.", a, c);
		assertFalse("a and d should not be equals", a.equals(d));
	}
	
	// Must be here to be tested by automaticTests
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ExecutionRequestDescriptorCompositeTest.class);
	}
}
