package org.integrade.portlets;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;

import org.gridlab.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridlab.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridlab.gridsphere.provider.portlet.jsr.ActionPortlet;
import org.gridlab.gridsphere.provider.portletui.beans.FileInputBean;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxItemBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;

import asct.core.ApplicationControlFacade;
import asct.shared.AbstractGridApplication;
import asct.shared.BspExecutionData;
import asct.shared.BspGridApplication;
import asct.shared.MpiExecutionData;
import asct.shared.MpiGridApplication;
import asct.shared.ExecutionRequestData;
import asct.shared.ExecutionRequestStatus;
import asct.shared.ParametricCopyHolder;
import asct.shared.ParametricExecutiondata;
import asct.shared.ParametricGridApplication;
import asct.shared.SequencialGridApplication;
import clusterManagement.ApplicationNotFoundException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;
import dataTypes.kindOfItens;


/**
 * This class manages all the form filling to submit jobs to the grid, including input file
 * uploads.
 * It has quite a lot of code, but most of it is just to fill forms.
 * @author Lundberg
 *
 */
public class JobSubmission extends ActionPortlet {
    
	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		DEFAULT_VIEW_PAGE = "submission";
	}
	
	public void uploadInputFile(ActionFormEvent event) throws PortletException {
		try {
			FileInputBean fb = event.getFileInputBean("userfile");
			LocalRepository local = (LocalRepository)event.getActionRequest().getPortletSession().getAttribute("localRepository");
			File f = local.createInputFile(fb.getFileName(), (Vector<String>)event.getActionRequest().getPortletSession().getAttribute("inputFiles"));
			fb.getFileItem().write(f);
			setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
		}
		catch (IOException e) {
			event.getActionResponse().setRenderParameter("erro", "Error:" +  e.getMessage());
			setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void deleteInputFile(ActionFormEvent event) throws PortletException {
		String target = event.getAction().getParameter("target");
		Vector<String> inputFiles = (Vector<String>)event.getActionRequest().getPortletSession().getAttribute("inputFiles");
		String file;
		for(int i = 0; i < inputFiles.size(); i++) {
			file = inputFiles.get(i);
			if(file.compareTo(target) == 0) {
				inputFiles.remove(i);
				((LocalRepository)event.getActionRequest().getPortletSession().getAttribute("localRepository")).deleteFile(target.substring(1));
				break;
			}
		}
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void removeOutput(ActionFormEvent event) throws PortletException {
		int t = Integer.parseInt(event.getAction().getParameter("target"));
		Vector v = (Vector) event.getActionRequest().getPortletSession().getAttribute("outputFiles");
		if(t < v.size()) v.remove(t);
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void addOutput(ActionFormEvent event) throws PortletException {
		String fileName = event.getTextFieldBean("outputFileName").getValue();
		if(fileName == null) {
			setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
			return;
		}
		else if(fileName.length() == 0) {
			setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
			return;
		}
		Vector v = (Vector) event.getActionRequest().getPortletSession().getAttribute("outputFiles");
		event.getTextFieldBean("outputFileName").setValue("");
		v.add(fileName);
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void removeParametricCopy(ActionFormEvent event) throws PortletException {
		int t = Integer.parseInt(event.getAction().getParameter("target"));
		Vector v = (Vector) event.getActionRequest().getPortletSession().getAttribute("parametricCopies");
		if(t < v.size()) v.remove(t);
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void addParametricCopy(ActionFormEvent event) throws PortletException {
		PortletSession session = event.getActionRequest().getPortletSession();
		String copyName = event.getTextFieldBean("parametricCopyName").getValue();
		if(copyName == null)
			setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
		if(copyName.length() == 0)
			setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
		Vector v = (Vector) session.getAttribute("parametricCopies");
		ParametricCopyHolder pc = new ParametricCopyHolder();
		
		Object[] src = ((Vector)session.getAttribute("outputFiles")).toArray();
		String[] array = new String[src.length];
		System.arraycopy(src, 0, array, 0, src.length);
		pc.setOutputFiles(array);
		
		Vector<String> inputFileList = ((Vector<String>)session.getAttribute("inputFiles"));
		Vector<String> selectedInputFiles = new Vector<String>();
		for(Iterator<String> it = inputFileList.iterator(); it.hasNext();) {
			String s = it.next();
			if(s.charAt(0) == 'T')
				selectedInputFiles.add(s.substring(1));
		}
		src = selectedInputFiles.toArray();
		array = new String[src.length];
		System.arraycopy(src, 0, array, 0, src.length);
		String inputPath = ((LocalRepository)session.getAttribute("localRepository")).getInputPath();
		for(int i = 0; i < array.length; i++)
			array[i] = inputPath + array[i];
		pc.setInputFiles(array);
		
		event.getActionRequest().getPortletSession().setAttribute("outputFiles", new Vector());
		
		TextFieldBean tfb = event.getTextFieldBean("arguments");
		pc.setArguments(tfb.getValue());
		tfb.setValue(null);
		event.getTextFieldBean("outputFileName").setValue("");
/*TODO*/pc.setCopyId(1);
		v.add(copyName);
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void submit(ActionFormEvent event) throws PortletException {
		ExecutionRequestData req = this.makeRequestForm(event);
		if (req != null){
		    event.getActionRequest().getPortletSession().setAttribute("outputFiles", new Vector());
		    event.getTextFieldBean("arguments").setValue("");
		    event.getTextFieldBean("constraints").setValue("");
		    event.getTextFieldBean("preferences").setValue("");
		    try {
			
			ExecutionRequestStatus state = ((ApplicationControlFacade)event.getActionRequest().getPortletSession().getAttribute("asct")).executeApplication(req);
			((Hashtable)event.getActionRequest().getPortletSession().getAttribute("jobs")).put(state.getRequestId(), state);
		    } catch (ApplicationNotFoundException e) {
			throw new PortletException(e);
		    } catch (DirectoryNotFoundException e) {
			throw new PortletException(e);
		    } catch (InvalidPathNameException e) {
			throw new PortletException(e);
		    } catch (SecurityException e) {
			throw new PortletException(e);
		    }
		}
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void setAppType(ActionFormEvent event) throws PortletException {
		String appType = event.getAction().getParameter("appType");
		event.getActionRequest().getPortletSession().setAttribute("appType", appType);
		if(appType.compareTo("parametric") == 0) {
			event.getActionRequest().getPortletSession().setAttribute("parametricCopies", new Vector());
		}
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void toggleInputFile(ActionFormEvent event) throws PortletException {
		String target = event.getAction().getParameter("target");
		Vector<String> inputFiles = (Vector<String>)event.getActionRequest().getPortletSession().getAttribute("inputFiles");
		String file;
		for(int i = 0; i < inputFiles.size(); i++) {
			file = inputFiles.get(i);
			if(file.compareTo(target) == 0) {
				inputFiles.set(i, ((file.charAt(0) == 'F') ? "T" : "F") + file.substring(1));
				break;
			}
		}
		setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
	}
	
	public void submission(RenderFormEvent event) throws PortletException {
		PortletSession session = event.getRenderRequest().getPortletSession();
		ApplicationControlFacade asct = (ApplicationControlFacade) session.getAttribute("asct");
		String appType = (String) session.getAttribute("appType");
		if(asct == null) {
			String userName = event.getRenderRequest().getRemoteUser();
			LocalRepository local = new LocalRepository(userName);
			asct = AsctHandler.getInstance().getAsct(userName);
			Hashtable requests = AsctHandler.getInstance().getJobs(userName);
			try {
				session.setAttribute("repositoryTree", new RepositoryFile(asct));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			session.setAttribute("asct", asct);
			appType = "binary";
			session.setAttribute("appType", appType);
			session.setAttribute("outputFiles", new Vector());
			session.setAttribute("inputFiles", this.inputList(local));
			session.setAttribute("localRepository", local);
			session.setAttribute("jobs", requests);
		}
		LocalRepository local = (LocalRepository) session.getAttribute("localRepository");
		String outputPath = local.getOutputPath();
		outputPath = outputPath.substring(outputPath.indexOf("/integrade"));
		event.getRenderRequest().setAttribute("outputPath", outputPath);
		Vector v = (Vector) session.getAttribute("parametricCopies");
		event.getRenderRequest().setAttribute("parametricCopies", v);
		event.getRenderRequest().setAttribute("outputFiles", session.getAttribute("outputFiles"));
		event.getRenderRequest().setAttribute("inputFiles", session.getAttribute("inputFiles"));
		event.getRenderRequest().setAttribute("appType", appType);
		event.getRenderRequest().setAttribute("jobs", ((Hashtable)session.getAttribute("jobs")).elements());
		//String userName = event.getRenderRequest().getRemoteUser();
		//Hashtable requests = AsctHandler.getInstance().getJobs(userName);
		//event.getRenderRequest().setAttribute("jobs", requests.elements());		

		if(v != null) {
			event.getTextFieldBean("parametricCopyName").setValue(Integer.toString(v.size() + 1));
		}
		try {
			this.applicationListBox(event);
		} catch (DirectoryNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPathNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PortletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setNextState(event.getRenderRequest(), "JobSubmission/jobsubmission.jsp");
	}
	
	private void applicationListBox(RenderFormEvent event) throws PortletException, DirectoryNotFoundException, InvalidPathNameException, SecurityException {
		ListBoxBean listBoxBean = event.getListBoxBean("applications");
		RepositoryFile root = (RepositoryFile) event.getRenderRequest().getPortletSession().getAttribute("repositoryTree");
		Vector<RepositoryFile> v = root.fileList();
		ListBoxItemBean b;
		for(int i = 0; i < v.size(); i++) {
			if(v.get(i).getKind() == kindOfItens.applicationDirectory) {
				b = new ListBoxItemBean();
				b.setValue(v.get(i).getPath());
				listBoxBean.addBean(b);
			}
		}
	}
	
	private Vector<String> inputList(LocalRepository local) throws PortletException {
		Vector<String> v = local.getInputFileList();
		for(int i = 0; i < v.size(); i++)
			v.set(i, "F" + (String)v.get(i));
		return v;
	}

	private ExecutionRequestData makeRequestForm(ActionFormEvent event) {
		ExecutionRequestData req;
		AbstractGridApplication aga;
		PortletSession session = event.getActionRequest().getPortletSession();
		String appType = (String)session.getAttribute("appType");
		String appPath = event.getListBoxBean("applications").getSelectedValue();
		String appName = appPath.substring(appPath.lastIndexOf('/') + 1);
		int i = appPath.lastIndexOf('/');
		if(i == -1) appPath = "";
		else appPath = appPath.substring(0, i);
		
		if(appType.compareTo("bsp") == 0) {
			req = new BspExecutionData();
			aga = new BspGridApplication(appName, appPath);
		}
		if(appType.compareTo("mpi") == 0) {
			req = new MpiExecutionData();
			aga = new MpiGridApplication(appName, appPath);
		}
		else if(appType.compareTo("parametric") == 0) {
			req = new ParametricExecutiondata();
			aga = new ParametricGridApplication(appName, appPath);
			((ParametricExecutiondata)req).setParametricCopies((ParametricCopyHolder[]) session.getAttribute("parametricCopies"));
		}
		else {
			req = new ExecutionRequestData();
			aga = new SequencialGridApplication(appName, appPath);
		}
		req.setApplication(aga);
		Object[] src = ((Vector)session.getAttribute("outputFiles")).toArray();
		String[] array = new String[src.length];
		System.arraycopy(src, 0, array, 0, src.length);
		req.setOutputFileNames(array);
		Vector<String> inputFileList = ((Vector<String>)session.getAttribute("inputFiles"));
		Vector<String> selectedInputFiles = new Vector<String>();
		for(Iterator<String> it = inputFileList.iterator(); it.hasNext();) {
			String s = it.next();
			if(s.charAt(0) == 'T')
				selectedInputFiles.add(s.substring(1));
		}
		src = selectedInputFiles.toArray();
		array = new String[src.length];
		System.arraycopy(src, 0, array, 0, src.length);
		String inputPath = ((LocalRepository)session.getAttribute("localRepository")).getInputPath();
		for(i = 0; i < array.length; i++)
			array[i] = inputPath + array[i];
		req.setInputFiles(array);
		
		String buff;
		buff = event.getTextFieldBean("arguments").getValue();
		if(buff != null)
			req.setArguments(buff);
		buff = event.getTextFieldBean("constraints").getValue();
		if(buff != null)
			req.setConstraints(buff);
		buff = event.getTextFieldBean("preferences").getValue();
		if(buff != null)
			req.setPreferences(buff);

		if(appType.compareTo("bsp") == 0 || appType.compareTo("mpi") == 0) {
		    buff = event.getTextFieldBean("numberOfTasks").getValue();
		    int numberOfTasks = -1;
		    try {
			numberOfTasks = Integer.parseInt(buff);
		    }
		    catch (NumberFormatException e) {
   		       event.getActionRequest().getPortletSession().setAttribute("errorMessage","Invalid number of tasks!");
		       req = null;
		       e.printStackTrace();
		    }
		    if(req != null)
			req.setNumberOfTasks( numberOfTasks );
		}

		return req;
	}
}
