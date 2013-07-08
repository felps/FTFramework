/**
 * @version $Id: JaasAuthModule.java 4496 2006-02-08 20:27:04Z wehrens $
 */
package org.gridlab.gridsphere.services.core.security.auth.modules.impl;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.services.core.security.auth.AuthenticationException;
import org.gridlab.gridsphere.services.core.security.auth.modules.LoginAuthModule;
import org.gridlab.gridsphere.services.core.security.auth.modules.impl.descriptor.AuthModuleDefinition;


public class JaasAuthModule extends BaseAuthModule implements LoginAuthModule {

    //private String contextName = "Gridsphere";
    //private String contextName = "com.sun.security.jgss.initiate";
    private String contextName = "GetLoginNameUnix";
    
    private PortletLog log = SportletLog.getInstance(JaasAuthModule.class);

    public JaasAuthModule(AuthModuleDefinition moduleDef) {
        super(moduleDef);
    }

    public void checkAuthentication(User user, String password) throws AuthenticationException {

		String username = user.getUserName();

        if (log.isDebugEnabled()) log.debug("beginning authentication for '" + username + "'");

		LoginContext loginContext;
		System.out.println("JaasAuthModule username: " + username);
		JaasCallbackHandler handler = new JaasCallbackHandler(username, password);
		System.out.println("JaasAuthModule handler username1: " + handler.getUsername());
		// Create the LoginContext
		try{
			loginContext = new LoginContext(contextName, handler);
			System.out.println("JaasAuthModule handler username2: " + handler.getUsername());
			if (log.isDebugEnabled()) log.debug("got loginContext");
		} catch (SecurityException e) {
		    if (log.isDebugEnabled()) log.debug("SecurityException: " + e);
			throw new AuthenticationException("key4", e);
		} catch (LoginException e) {
		    if (log.isDebugEnabled()) log.debug("LoginException: " + e);
      		throw new AuthenticationException("key4", e);
		}

		// Attempt login
		try{
			loginContext.login();
			System.out.println("JaasAuthModule handler username3: " + handler.getUsername());
			if (log.isDebugEnabled()) log.debug("login successful");
		} catch (FailedLoginException e) {
		    if (log.isDebugEnabled()) log.debug("login failed: " + e.getMessage());
			throw new AuthenticationException("key4", e);
		} catch (AccountExpiredException e) {
		    if (log.isDebugEnabled()) log.debug("account expired");
			throw new AuthenticationException("key1");
		} catch (CredentialExpiredException e) {
		    if (log.isDebugEnabled()) log.debug("credentials expired");
			throw new AuthenticationException("key2", e);
		} catch (Exception e) {
		    if (log.isDebugEnabled()) log.debug("unexpected failure: " + e.getMessage());
			throw new AuthenticationException("key3", e);
		}
		
		/* InteGrade
		 * If the program reaches this line, the login was sucessful.
		 * The handler must be kept, so that it can be used later to create the user's
		 * ApplicationControlFacade.
		 * Lundberg*/
		PersistenceManagerRdbms pm = PersistenceManagerFactory.createPersistenceManagerRdbms("integrade");
		try {
			pm.getSession();
			JaasCallbackHandler h = (JaasCallbackHandler) pm.restore("from "
					+ JaasCallbackHandler.class.getName()
					+ " where "
					+ "husername='" + handler.getUsername() + "'");
			if(h == null)
				pm.create(handler);
			else {
				h.setPassword(handler.getPassword());
				pm.saveOrUpdate(h);
			}
			pm.closeSession();
		} catch (PersistenceManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public String getContextName() {
        return contextName;
    }
    public void setContextName(String contextName) {
        if (log.isDebugEnabled()) log.debug("setting contextName = '" + contextName + "'");
        this.contextName = contextName;
    }
    
}
