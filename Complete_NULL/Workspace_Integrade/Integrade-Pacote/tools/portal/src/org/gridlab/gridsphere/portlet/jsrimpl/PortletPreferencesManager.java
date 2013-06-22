/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @version $Id: PortletPreferencesManager.java 4889 2006-06-28 18:36:43Z novotny $
 */
package org.gridlab.gridsphere.portlet.jsrimpl;

import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.impl.SportletLog;

import javax.portlet.PreferencesValidator;

/**
 * The <code>PortletPreferencesManager</code> provides a a singleton implementation of the <code>PortletDataManager</code>
 * used for loading and storing <code>PortletData</code>.
 */
public class PortletPreferencesManager {

    private static PortletLog log = SportletLog.getInstance(PortletPreferencesManager.class);
    private static PersistenceManagerRdbms pm = null;
    private PreferencesValidator validator = null;
    private boolean isRender = false;
    private String userId = null;
    private String portletId = null;
    private org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletPreferences prefsDesc = null;
    /**
     * Default instantiation is disallowed
     */
    public PortletPreferencesManager() {
        pm = PersistenceManagerFactory.createGridSphereRdbms();    
    }

    public void setPreferencesDesc(org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletPreferences prefsDesc) {
        this.prefsDesc = prefsDesc;
    }

    public void setValidator(PreferencesValidator validator) {
        this.validator = validator;
    }

    public void setIsRender(boolean isRender) {
        this.isRender = isRender;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPortletId(String portletId) {
        this.portletId = portletId;
    }

    /**
     * Returns the users portlet data for the specified portlet
     *
     * @param appPortlet the JSR application portlet
     * @param user       the <code>User</code>
     * @return the PortletPreferences for this portlet or null if none exists.
     */
    public javax.portlet.PortletPreferences getPortletPreferences() {
        PortletPreferencesImpl prefs = null;

        try {
            if (userId != null) {
                String command =
                        "select u from " + PortletPreferencesImpl.class.getName() + " u where u.userId='" + userId + "' and u.portletId='" + portletId + "'";
                prefs = (PortletPreferencesImpl) pm.restore(command);
            } else {
                userId = "500";
            }

            if (prefs == null) {
                // we have no prefs in the db so create one from the xml...
                log.debug("No prefs exist-- storing prefs for user: " + userId + " portlet: " + portletId);
                prefs = new PortletPreferencesImpl();
                prefs.setPortletId(portletId);
                prefs.setUserId(userId);
            } else {
                log.debug("Retrieved prefs for user: " + userId + " portlet: " + portletId);
            }
            prefs.setPersistenceManager(pm);

            if (prefsDesc != null) prefs.setPreferencesDesc(prefsDesc);
            if (validator != null) prefs.setValidator(validator);

            prefs.setRender(isRender);
        } catch (Exception e) {
            log.error("Error attempting to restore persistent preferences: ", e);
        }
        return prefs;
    }

}
