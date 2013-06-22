package portalTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.SeleniumException;

public class PortalActionsCaseTest extends SeleneseTestCase {
	@Before
	public void setUp() throws Exception {
		setUp("http://aguia1.ime.usp.br:8080", "*chrome");
		selenium.open("/gridsphere/gridsphere?cid");
		selenium.click("username");
		selenium.type("username", "selenium");
		selenium.type("password", "Cz3aeLZX");
		selenium.click("//input[@value='Login']");
		selenium.waitForPageToLoad("30000");
	}

	// TODO replace click arguments with link names
	@Test
	public void testApplicationSubmission () {
		uploadApplicationBinary();
		submitApplication();
	}

	private void submitApplication() {
		selenium.click("ui_lb_applications_");
		selenium.select("ui_lb_applications_", "label=/testApplication");
		selenium.type("ui_tf_outputFileName_", "stdout");
		selenium.click("gs_action=addOutput");
		selenium.waitForPageToLoad("30000");
		
		//TODO Fix the way the counting is done
		int i = 2;
		try {
			for (; i < 200; i++) {
				selenium.click("//div[@id='83']/div[2]/table/tbody/tr/td/table/tbody/tr/td[2]/fieldset/table/tbody/tr[" +i+ "]/td[2]");
			}
		}
		catch (SeleniumException e) {}
		
		selenium.click("gs_action=submit");
		selenium.waitForPageToLoad("30000");
		selenium.click("//div[@id='83']/div[2]/table/tbody/tr/td/table/tbody/tr/td[2]/fieldset/table/tbody/tr[" +i+ "]/td[2]");
	}

	private void uploadApplicationBinary() {
		selenium.open("/gridsphere/gridsphere?cid=login");
		selenium.click("//div[@id='page']/div[2]/ul/li[2]/a/span");
		selenium.waitForPageToLoad("30000");
		selenium.click("//input[@value='New application']");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui_tf_name_");
		selenium.type("ui_tf_name_", "testApplication");
		selenium.click("gs_action=registerApplication");
		selenium.waitForPageToLoad("30000");
		selenium.click("//input[@value='Upload']");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui_fi_userfile_");
		selenium.type("ui_fi_userfile_", "/bin/pwd");
		selenium.click("gs_action=uploadBinary");
		selenium.waitForPageToLoad("30000");
		selenium.click("//div[@id='page']/div[3]/ul/li[2]/a/span");
		selenium.waitForPageToLoad("30000");
	}

	@Test
	public void testCreateApplicationDirectoryWithSameNameIsNotAllowed() {
		uploadApplicationBinary();
		selenium.click("link=Repository");
		selenium.waitForPageToLoad("30000");
		selenium.click("//input[@value='New application']");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui_tf_name_");
		selenium.type("ui_tf_name_", "testApplication");
		selenium.click("gs_action=registerApplication");
		selenium.waitForPageToLoad("30000");
		assertTrue("Could not create another directory with the same name",
				selenium.isTextPresent("Error"));
	}

	@After
	public void testLogout() throws Exception {
		selenium.click("page");
		selenium.click("link=Logout");
		selenium.waitForPageToLoad("30000");
	}

	// TODO Logout tests before user logs on the portal
	// TODO Verify existence of output file

}
