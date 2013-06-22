package org.integrade.portlets;

import java.io.File;
import java.io.IOException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;

import org.gridlab.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridlab.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridlab.gridsphere.provider.portlet.jsr.ActionPortlet;
import org.gridlab.gridsphere.provider.portletui.beans.FileInputBean;

import asct.core.ApplicationControlFacade;
import clusterManagement.ApplicationNotFoundException;
import clusterManagement.ApplicationRegistrationException;
import clusterManagement.BinaryCreationException;
import clusterManagement.DirectoryCreationException;
import clusterManagement.DirectoryNotEmptyException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;

/**
 * This portlet lets the user deal with a RepositoryFile structure, representing the remote file
 * system.
 * @author Lundberg
 *
 */
public class RepositoryBrowser extends ActionPortlet {
	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		DEFAULT_VIEW_PAGE = "prepare";
	}
	
	public void uploadBinary(ActionFormEvent event) throws PortletException {
		File f;
		try {
			FileInputBean fb = event.getFileInputBean("userfile");
			String fileName = fb.getFileName();
			if(fileName.length() == 0) {
				event.getActionResponse().setRenderParameter("errorMessage", "Error: no file chosen for upload");
				setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
				return;
			}
			LocalRepository local = (LocalRepository) event.getActionRequest().getPortletSession().getAttribute("localRepository");
			f = new File(local.getTempPath() + fileName);
			f.createNewFile();
			fb.getFileItem().write(f);
			RepositoryFile root = (RepositoryFile)event.getActionRequest().getPortletSession().getAttribute("repositoryTree");
			root.uploadBinary(f);
			f.delete();
		} catch (IOException e) {
			this.criticalError(event);
			e.printStackTrace();
		} catch (BinaryCreationException e) {
			this.criticalError(event);
			e.printStackTrace();
		} catch (ApplicationNotFoundException e) {
			this.repositoryChangedError(event);
			e.printStackTrace();
		} catch (DirectoryNotFoundException e) {
			this.repositoryChangedError(event);
			e.printStackTrace();
		} catch (InvalidPathNameException e) {
			this.repositoryChangedError(event);
			e.printStackTrace();
		} catch (SecurityException e) {
			this.criticalError(event);
			e.printStackTrace();
		} catch (ParentApplicationNotFoundException e) {
			event.getActionResponse().setRenderParameter("errorMessage", "Error: a binary must be inside an application.");
		} catch (Exception e) {
			this.criticalError(event);
			e.printStackTrace();
		}
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void createDirectory(ActionFormEvent event) throws PortletException {
		String dirName = event.getTextFieldBean("name").getValue();
		RepositoryFile root = (RepositoryFile)event.getActionRequest().getPortletSession().getAttribute("repositoryTree");
		if(dirName.length() > 0) {
			try {
				root.createDirectory(dirName);
			} catch (DirectoryCreationException e) {
				this.criticalError(event);
				e.printStackTrace();
			} catch (InvalidPathNameException e) {
				this.repositoryChangedError(event);
				e.printStackTrace();
			} catch (SecurityException e) {
				this.criticalError(event);
				e.printStackTrace();
			}
		}
		else 
			event.getActionResponse().setRenderParameter("errorMessage", "Error: new directory name not entered");
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void registerApplication(ActionFormEvent event) throws PortletException {
		String appName = event.getTextFieldBean("name").getValue();
		RepositoryFile root = (RepositoryFile)event.getActionRequest().getPortletSession().getAttribute("repositoryTree");
		if(appName.length() > 0) {
			try {
				root.registerApplication(appName);
			} catch (ApplicationRegistrationException e) {
				this.criticalError(event);
				e.printStackTrace();
			} catch (DirectoryCreationException e) {
				this.criticalError(event);
				e.printStackTrace();
			} catch (InvalidPathNameException e) {
				this.repositoryChangedError(event);
				e.printStackTrace();
			} catch (SecurityException e) {
				this.criticalError(event);
				e.printStackTrace();
			} 
		}
		else
			event.getActionResponse().setRenderParameter("errorMessage", "Error: new application name not entered.");
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void delete(ActionFormEvent event) throws PortletException {
		RepositoryFile root = (RepositoryFile)event.getActionRequest().getPortletSession().getAttribute("repositoryTree");
		try {
			root.delete();
		} catch (DirectoryNotEmptyException e) {
			event.getActionResponse().setRenderParameter("errorMessage", "A directory must be empty to be deleted.");
			e.printStackTrace();
		} catch (InvalidPathNameException e) {
			this.repositoryChangedError(event);
			e.printStackTrace();
		} catch (Exception e) {
			this.criticalError(event);
			e.printStackTrace();
		} 
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void selectItem(ActionFormEvent event) throws PortletException {
		String target = (String) event.getAction().getParameter("target");
		RepositoryFile root = (RepositoryFile) event.getActionRequest().getPortletSession().getAttribute("repositoryTree");
		root.selectFile(target);
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void callPage(ActionFormEvent event) throws PortletException {
		String callAction = event.getAction().getParameter("callAction");
		String nextPage = event.getAction().getParameter("nextPage");
		event.getActionResponse().setRenderParameter("nextPage", nextPage);
		if(callAction != null) {
			event.getActionResponse().setRenderParameter("callAction", callAction);
		}
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void toggleTreeKnot(ActionFormEvent event) throws PortletException {
		String target = event.getAction().getParameter("target");
		RepositoryFile root = (RepositoryFile) event.getActionRequest().getPortletSession().getAttribute("repositoryTree");
		root.toggleOpened(target);
		if(root.selectedFile().getPath().startsWith(target))
			root.selectFile(target);
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	public void prepare(RenderFormEvent event) throws PortletException {
		PortletSession session = event.getRenderRequest().getPortletSession();
		ApplicationControlFacade asct = (ApplicationControlFacade) session.getAttribute("asct");
		if(asct == null) {
			String userName = event.getRenderRequest().getRemoteUser();
			LocalRepository local = new LocalRepository(userName);
			asct = AsctHandler.getInstance().getAsct(userName);
			session.setAttribute("asct", asct);
			session.setAttribute("localRepository", local);
			try {
				session.setAttribute("repositoryTree", new RepositoryFile(asct));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		RepositoryFile root = ((RepositoryFile)session.getAttribute("repositoryTree"));
		try {
			root.refresh(false);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		event.getRenderRequest().setAttribute("repositoryTree", root.fileTree());
		String s = event.getRenderRequest().getParameter("errorMessage");
		if(s == null)
			event.getTextBean("errorMessage").setValue("");
		else
			event.getTextBean("errorMessage").setValue(s);
		
		s = event.getRenderRequest().getParameter("nextPage");
		if(s == null)
			setNextState(event.getRenderRequest(), "FileManager/filemanager.jsp");
		else {
			setNextState(event.getRenderRequest(), "FileManager/" + s);
			s = event.getRenderRequest().getParameter("callAction");
			if(s != null) {
				event.getRenderRequest().setAttribute("callAction", s);
				if(s.compareTo("createDirectory") == 0)
					event.getTextBean("explanation").setValue("Name of the new directory");
				else if(s.compareTo("registerApplication") == 0)
					event.getTextBean("explanation").setValue("Name of the new application");
				else
					event.getTextBean("explanation").setValue("Name of the new item");
			}
		}

	}
	
	private void criticalError(ActionFormEvent event) {
		event.getActionResponse().setRenderParameter("errorMessage", "Error: please contact portal administrator if the problem persists.");
	}
	
	private void repositoryChangedError(ActionFormEvent event) {
		event.getActionResponse().setRenderParameter("errorMessage", "The repository changed since you refreshed your page, and your action was not possible.");
	}
}
