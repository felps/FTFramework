package org.integrade.portlets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.portlet.PortletException;

/** The local repository acts as a file buffer between the user and the grid.
 * It stores the uploaded input files and receives the output files once they're ready,
 * packs it and makes then available for downloading by the user.
 * @author Lundberg*/

public class LocalRepository {
	private final String PATH = System.getenv("PWD") + "/webapps/integrade/localRepository/";
	private String userName;
	
	public LocalRepository(String userName) {
		this.userName = userName;
		this.userDir();
	}
	
	public void deleteFile(String fileName) throws PortletException {
		File f = new File(this.getInputPath() + fileName);
		if(f.exists())
			f.delete();
	}

	private void userDir() {
		File dir;
		dir = new File(PATH + userName);
		if(!dir.exists()) dir.mkdirs();
		dir = new File(this.getInputPath());
		if(!dir.exists()) dir.mkdir();
		dir = new File(this.getTempPath());
		if(!dir.exists()) dir.mkdir();
		dir = new File(this.getOutputPath());
		if(!dir.exists()) dir.mkdir();
	}
	
	public String getInputPath() {
		return PATH + userName + "/inputFiles/";
	}
	
	public String getOutputPath() {
		return PATH + userName + "/outputFiles/";
	}
	public String getTempPath() {
		return PATH + userName + "/temp/";
	}
	public Vector<String> getInputFileList() {
		String path = this.getInputPath();
		File f = new File(path);
		File[] list = f.listFiles();
		Vector<String> v = new Vector<String>();
		for(int i = 0; i < list.length; i++)
			v.add(list[i].getName());
		return v;
	}
	
	public String[] getInputPaths(String[] files) {
		String path = this.getInputPath();
		File f = new File(path);
		File[] list = f.listFiles();
		String[] s = new String[list.length];
		for(int i = 0; i < list.length; i++)
			s[i] = list[i].getAbsolutePath();
		return s;
	}
	
	public String[] getOutputPaths(String[] files) {
		String path = this.getOutputPath();
		File f = new File(path);
		File[] list = f.listFiles();
		String[] s = new String[list.length];
		for(int i = 0; i < list.length; i++)
			s[i] = list[i].getAbsolutePath();
		return s;
	}
	
	public void packJobOutput(String requestId) throws FileNotFoundException, IOException {
		Process child = Runtime.getRuntime().exec("/bin/bash");
		BufferedWriter outCommand = new BufferedWriter(new
				OutputStreamWriter(child.getOutputStream()));
		outCommand.write(
				"cd " + this.getTempPath() + "\n" + 
				"tar -zcvf " + this.getOutputPath() + requestId + ".tar.gz " + requestId + "\n"
				"rm -rf " + requestId + "\n");
		outCommand.flush();
	}
	
	public File createInputFile(String fileName, Vector<String> fileList) throws IOException {
		File f = new File(this.getInputPath() + fileName);
		if(f.exists())
			f.delete();
		else
			fileList.add("T" + fileName);
		f.createNewFile();
		return f;
	}
}
