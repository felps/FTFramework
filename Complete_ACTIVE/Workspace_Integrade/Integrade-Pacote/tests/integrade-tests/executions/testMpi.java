package executions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import asct.ui.ASCTController;

import tools.CommonApplicationTest;


public class testMpi {


	private String applicationName;

	private CommonApplicationTest app;

	@Before
	public void setUpBefore() {
		// Mudar para arquivo de configuraćão
		applicationName = "mpi";
		app = CommonApplicationTest.generate(applicationName,ASCTController.getInstance());		
		
	}

	@After
	public void tearDown(){
		app.tearDown();	
	}

	@Test
	public void runMpiTest() {
		try {
			app.setup();
			app.test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

}
