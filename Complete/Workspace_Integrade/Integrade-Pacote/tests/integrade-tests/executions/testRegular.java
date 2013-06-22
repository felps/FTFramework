package executions;

import inspectors.OutputInspector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tools.CommonApplicationTest;
import tools.FinishedExecutionListener;
import asct.shared.ExecutionRequestStatus;
import asct.shared.ParametricCopyHolder;
import asct.ui.ASCTController;
import asct.ui.ExecutionRequestDescriptorComposite;
import dataTypes.ApplicationType;

public class testRegular {

	private OutputInspector outputInspector;

	private ASCTController asct;

	private FinishedExecutionListener listener;
	private String basePath;
	private String applicationName;	
	private CommonApplicationTest app;

	private String binary;

	@Before
	public void setUpBefore() {
		// Mudar para arquivo de configuraćão		
		applicationName = "regular";		
		app = CommonApplicationTest.generate(applicationName,ASCTController.getInstance());	
		
	}
	
	
	@After
	public void tearDown(){
		app.tearDown();	
	}

	@Test
	public void testRegular() {
		try {
			app.setup();
			app.test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
