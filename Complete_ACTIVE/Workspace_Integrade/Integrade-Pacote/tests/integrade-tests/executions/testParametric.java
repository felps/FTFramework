package executions;

import java.util.HashMap;

import inspectors.OutputInspector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tools.CommonApplicationTest;
import tools.FinishedExecutionListener;
import asct.ui.ASCTController;


public class testParametric {

	private OutputInspector outputInspector;

	private ASCTController asct;

	private FinishedExecutionListener listener;
	private String basePath;
	private String applicationName;
	private String platform;
	private CommonApplicationTest app;

	private String binary;

	//@Before
	public void setUpBefore() {
		
		applicationName = "parametric";
		//app = CommonApplicationTest.generate(applicationName);
	}
	
	
	//@After
	public void tearDown(){
		app.tearDown();	
	}

	//@Test
	public void testParametric() {
		try {
			app.setup();
			app.test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
