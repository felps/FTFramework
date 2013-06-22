package org.integrade.portlets;

import java.util.Hashtable;

import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
import org.gridlab.gridsphere.services.core.security.auth.modules.impl.JaasCallbackHandler;

import asct.core.ApplicationControlFacade;

/*
 * This singleton is resposible for storing the user's asct and active jobs.
 * Lundberg
 * */
public class AsctHandler {
	static AsctHandler instance = null;
	private Hashtable<String, ApplicationControlFacade> facades = null;
	private Hashtable<String, Hashtable> jobs = null;
	private AsctHandler() {
		facades = new Hashtable<String, ApplicationControlFacade>();
		jobs = new Hashtable<String, Hashtable>();
	}
	static public AsctHandler getInstance() {
		if(instance == null)
			instance = new AsctHandler();
		return instance;
	}
	/*
	 * Returns the user's application control facade. If it wasn't created before, then
	 * it gets the handler from the database, creates a new ApplicationControlFacade, a new
	 * job hashtable and registers a listener for executions. */
	public ApplicationControlFacade getAsct(String userName) {
		ApplicationControlFacade asct = facades.get(userName);
		if(asct == null) {
//			PersistenceManagerRdbms pm = PersistenceManagerFactory.createPersistenceManagerRdbms("integrade");
//			JaasCallbackHandler handler = null;
//			try {
//				pm.getSession();
//				handler = (JaasCallbackHandler) pm.restore("from "
//						+ JaasCallbackHandler.class.getName()
//						+ " where "
//						+ "husername='" + userName + "'");
//				handler.setUsername(handler.getUsername().substring(0, handler.getUsername().lastIndexOf('/')));
//				pm.closeSession();
//			} catch (PersistenceManagerException e) {
//				e.printStackTrace();
//			}
			LocalRepository local = new LocalRepository(userName);
			asct = new ApplicationControlFacade(local.getTempPath(), false, null);
			Hashtable userjobs = new Hashtable();
			asct.registerExecutionStateListener(new JobExecutionListener(userjobs, local));
			this.jobs.put(userName, userjobs);
			facades.put(userName, asct);
		}
		return asct;
	}
	public Hashtable getJobs(String userName) {
		Hashtable l = jobs.get(userName);
		if(l == null) {
			getAsct(userName);
			l = jobs.get(userName);
		}
		return l;
	}
}
