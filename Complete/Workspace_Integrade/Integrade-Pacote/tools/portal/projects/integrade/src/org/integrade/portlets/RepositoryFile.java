package org.integrade.portlets;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import clusterManagement.ApplicationNotFoundException;
import clusterManagement.ApplicationRegistrationException;
import clusterManagement.BinaryCreationException;
import clusterManagement.BinaryNotFoundException;
import clusterManagement.DirectoryCreationException;
import clusterManagement.DirectoryNotEmptyException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;

import dataTypes.ContentDescription;
import dataTypes.kindOfItens;

import asct.core.ApplicationControlFacade;

/**
 * When an object from the RepositoryFile is created, it accesses the
 * remote application repository through the ApplicationControlFacade
 * an creates an entire tree of files.
 *   Let is denote "node" a RepositoryFile that is a directory of
 * some kind, it may have "sons".
 *   Each node may be opened or not, making it easy to  deal with
 * large repositories, once only the opened parts of the tree will be
 * loaded.
 *   There is always one and only one RepositoryFile "selected" on
 * the tree. Commands given to manipulate the remote repository are
 * applied to the selected file, but it is also possible to send the
 * targeted file through parameter.
 * 
 * @author Lundberg
 *
 */

public class RepositoryFile {

	private kindOfItens kind;

	private boolean selected;

	private boolean opened;

	private String path;

	private RepositoryFile parent;

	private Vector<RepositoryFile> contents;

	private ApplicationControlFacade asct;

	/* Contructors */

	/**
	 * Builds the remote repository root.
	 * @throws SecurityException 
	 */
	public RepositoryFile(ApplicationControlFacade asct) throws SecurityException {
		this.selected = true;
		this.opened = true;
		this.path = "/";
		this.asct = asct;
		this.contents = new Vector<RepositoryFile>();
		this.kind = kindOfItens.rootDirectory;
		this.parent = null;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.refresh(false);
	}

	/* Builds a file, receiving it's content, parent and asct reference. */
	private RepositoryFile(RepositoryFile parent, ContentDescription content,
			ApplicationControlFacade asct) {
		this.selected = false;
		this.opened = false;
		this.path = content.fileName;
		this.parent = parent;
		if (this.path.charAt(0) != '/')
			this.path = "/" + this.path;
		this.asct = asct;
		this.contents = new Vector<RepositoryFile>();
		this.kind = content.kind;
	}

	/* File methods */

	/**
	 * @return opened state
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * Sets the opened state, if possible (only directories may be opened).
	 * @param opened
	 */
	public void setOpened(boolean opened) {
		if (this.kind == kindOfItens.applicationDirectory
				|| this.kind == kindOfItens.commonDirectory
				|| this.kind == kindOfItens.rootDirectory)
			this.opened = opened;
	}

	/**
	 * Toggles the opened state of a given directory.
	 * @param path
	 * @throws SecurityException 
	 * @throws InvalidPathNameException 
	 */
	public void toggleOpened(String path) {
		try {
			RepositoryFile file = this.findFile(path);
			if (file.isOpened())
				file.setOpened(false);
			else
				file.setOpened(true);
		} catch (InvalidPathNameException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the selected state.
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}
	/**
	 * Returns the file path
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the name of the RepositoryFile. ie: "/bin/matrix" has the name "matrix", the root's name is "/".
	 * @return
	 */
	public String getName() {
		if (this.kind == kindOfItens.rootDirectory)
			return "/";
		else
			return path.substring(path.lastIndexOf('/') + 1);
	}

	/**
	 * Checks if the RepositoryFile corresponds to the given path
	 * @param path
	 * @return
	 */
	public boolean equals(String path) {
		return (path.compareTo(this.path) == 0);
	}

	/**
	 * Returns the kind of the item.
	 * @return
	 */
	public kindOfItens getKind() {
		return kind;
	}

	/**
	 * If the RepositoryFile is a directory, returns it's path followed by "/".
	 * If the RepositoryFile is a common file, returns the base path of the parent directory. 
	 * @return
	 */
	public String getBasePath() {
		if (this.kind == kindOfItens.applicationDirectory
				|| this.kind == kindOfItens.commonDirectory)
			return path + "/";
		else if (this.kind == kindOfItens.rootDirectory)
			return path;
		else
			return parent.getBasePath();
	}

	/**
	 * Selects a file with the corresponding filePath.
	 * If the path is invalid, selects the tree element nearest
	 * to where it would be.
	 * ie: search "/bin/idontexist/myfile" would select "/bin"
	 * @param filePath
	 */
	public void selectFile(String filePath) {
		getRoot().selectFileP(filePath);
	}

	/**
	 * Returns the tree's root
	 * @return
	 */
	public RepositoryFile getRoot() {
		if (parent == null)
			return this;
		else
			return parent.getRoot();
	}

	/* See selectFile */
	private void selectFileP(String filePath) {
		if (this.equals(filePath)) {
			unselect();
			selected = true;
		} else {
			selected = false;
			boolean found = false;
			for (Iterator<RepositoryFile> it = this.contents.iterator(); it.hasNext();) {
				RepositoryFile f = it.next();
				if (filePath.startsWith(f.getPath())) {
					found = true;
					f.selectFileP(filePath);
				} else
					f.unselect();
			}
			if (!found)
				selected = true;
		}
	}

	/* Unselects all file in this subtree. */
	private void unselect() {
		selected = false;
		for (Iterator<RepositoryFile> it = this.contents.iterator(); it.hasNext();) {
			it.next().unselect();
		}
	}

	/* Tree access methods */

	
	public RepositoryFile selectedFile() {
		RepositoryFile root = getRoot();
		RepositoryFile sel = root.selectedFileP();
		if (sel == null) {
			selectFile(root.path);
			return root;
		}
		else
			return sel;
	}

	public Vector<RepositoryFile> fileTree() {
		Vector<RepositoryFile> tree = new Vector<RepositoryFile>();
		try {
			this.refresh(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		this.fileTreeR(tree);
		return tree;
	}

	public Vector<RepositoryFile> fileList() {
		Vector<RepositoryFile> tree = new Vector<RepositoryFile>();
		try {
			this.refresh(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		this.fileListR(tree);
		return tree;
	}

	public RepositoryFile findFile(String path) throws InvalidPathNameException, SecurityException {
		if (this.equals(path))
			return this;
		RepositoryFile c = null;
		if(path.startsWith(this.path)) {
			for (Iterator<RepositoryFile> it = contents.iterator(); it.hasNext();) {
				c = it.next();
				if (path.startsWith(c.getPath()))
					return c.findFile(path);
			}
			this.refresh(true);
			for (Iterator<RepositoryFile> it = contents.iterator(); it.hasNext();) {
				c = it.next();
				if (path.startsWith(c.getPath()))
					return c.findFile(path);
			}
		}
		else if(parent != null)
			parent.findFile(path);
		throw new InvalidPathNameException();
	}



	public void refresh(boolean fullScan) throws SecurityException {
		if (kind != kindOfItens.applicationDirectory
		 && kind != kindOfItens.rootDirectory
		 && kind != kindOfItens.commonDirectory)
			return;
		ContentDescription[] newContents;
		try {
			newContents = asct.listDirectoryContents(path);
		} catch (DirectoryNotFoundException e) {
			parent.refresh(fullScan);
			return;
		} catch (InvalidPathNameException e) {
			e.printStackTrace();
			return;
		} 
		Vector<RepositoryFile> v = new Vector<RepositoryFile>();
		RepositoryFile current = null;
		for (int i = 0; i < newContents.length; i++) {
			Iterator<RepositoryFile> it = contents.iterator();
			String s = (newContents[i].fileName.charAt(0) == '/') ? newContents[i].fileName : "/" + newContents[i].fileName;
			boolean found = false;
			while(it.hasNext() && !found) {
				current = it.next();
				if(current.equals(s)) {
					found = true;
				}
			}
			if(!found) {
				current = new RepositoryFile(this, newContents[i], this.asct);
			}
			v.addElement(current);
			if (current.isOpened() || fullScan)
				current.refresh(fullScan);
		}
		this.contents = v;
	}

	private RepositoryFile selectedFileP() {
		RepositoryFile result = null;
		if (this.isSelected())
			result = this;
		else {
			Iterator<RepositoryFile> it = contents.iterator();
			while (it.hasNext())
				if ((result = it.next().selectedFileP()) != null)
					break;
		}
		return result;
	}

	private void fileTreeR(Vector<RepositoryFile> tree) {
		tree.addElement(this);
		if (this.opened)
			for (Iterator<RepositoryFile> it = this.contents.iterator(); it
					.hasNext();)
				it.next().fileTreeR(tree);
	}

	private void fileListR(Vector<RepositoryFile> tree) {
		tree.addElement(this);
		for (Iterator<RepositoryFile> it = this.contents.iterator(); it
				.hasNext();)
			it.next().fileListR(tree);
	}

	private RepositoryFile getParent() {
		return parent;
	}

	private RepositoryFile getParentApplication()
			throws ParentApplicationNotFoundException {
		if (kind == kindOfItens.applicationDirectory)
			return this;
		else if (parent == null)
			throw new ParentApplicationNotFoundException();
		else
			return parent.getParentApplication();
	}

	/** ************************************* */
	/* Application Repository access methods */
	/**
	 * @throws SecurityException 
	 * @throws InvalidPathNameException 
	 * @throws DirectoryNotEmptyException  ************************************* 
	 * @throws ParentApplicationNotFoundException */

	public void delete() throws DirectoryNotEmptyException, InvalidPathNameException, SecurityException, ParentApplicationNotFoundException  {
		if (selected) {
			try {
				switch (kind.value()) {
				case kindOfItens._applicationDirectory:
					asct.unregisterApplication(parent.getPath(), getName());
					break;
				case kindOfItens._binaryFile:
					RepositoryFile parentApplication = getParentApplication();
					asct.deleteBinary(parentApplication.getParent().getName(),
							parentApplication.getName(), getName());
					break;
				case kindOfItens._commonDirectory:
					asct.removeDirectory(path);
					break;
				}
			} catch (ApplicationNotFoundException e) {
				System.out.println("Deleting an non existing application");
			} catch (DirectoryNotFoundException e) {
				System.out.println("Deleting an non existing directory");
			} catch (BinaryNotFoundException e) {
				System.out.println("Deleting an non existing binary");
			}
			if(parent != null)
				parent.refresh(false);
				selectFile(this.path);
		} else
			selectedFile().delete();
	}

	public void registerApplication(String name) throws ApplicationRegistrationException, DirectoryCreationException, InvalidPathNameException, SecurityException
			 {
		if (selected) {
			String base = getBasePath();
			asct.registerApplication(base, name);
			RepositoryFile b = (base.startsWith(this.path)) ? this : parent;
			b.setOpened(true);
			b.refresh(false);
			b.selectFileP(base + name);
		}
		else
			selectedFile().registerApplication(name);
	}

	public void createDirectory(String name) throws DirectoryCreationException, InvalidPathNameException, SecurityException {
		if (selected) {
			String base = getBasePath();
			asct.createDirectory(base + name);
			RepositoryFile b = (base.startsWith(this.path)) ? this : parent;
			b.setOpened(true);
			b.refresh(false);
			b.selectFileP(base + name);
		}
		else
			selectedFile().createDirectory(name);
	}

	public void uploadBinary(File binary) throws BinaryCreationException,
			ApplicationNotFoundException, DirectoryNotFoundException,
			InvalidPathNameException, SecurityException, IOException,
			ParentApplicationNotFoundException {
		if (selected) {
			RepositoryFile parApp = getParentApplication();
			asct.uploadBinary(binary.getCanonicalPath(), parApp.getParent()
					.getPath(), parApp.getName(), "Linux_i686");
		} else
			selectedFile().uploadBinary(binary);
	}
}
