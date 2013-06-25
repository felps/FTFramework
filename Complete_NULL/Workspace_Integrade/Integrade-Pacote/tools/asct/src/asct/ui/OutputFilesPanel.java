package asct.ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JList;

import javax.swing.JPanel;

import java.io.File;
import java.io.IOException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.border.TitledBorder;

import java.util.ArrayList;

import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JDialog;

import java.awt.Point;

//Class----------------------------------------------------------------------

/**
 * OutputFilesPanel - A Panel that stores a file list, allowing users to specify
 * any output files generated by his application, so that they can be later
 * retrieved from the LRM.
 * 
 * @author Andrei Goldchleger
 * @date March/2004
 * 
 */
class OutputFilesPanel extends JPanel {

	// Fields------------------------------------------------------------------
	private JButton addButton;

	private JButton removeButton;

	private JCheckBox stderrCheck;

	private JCheckBox stdoutCheck;

	private JScrollPane fileScrollPane;

	private DefaultListModel fileListModel;

	private JList fileList;

	// Costructor--------------------------------------------------------------
	OutputFilesPanel() {

		// Top Panel(Add/Remove)
		JPanel topPanel = new JPanel();
		stderrCheck = new JCheckBox("stderr");
		stdoutCheck = new JCheckBox("stdout");
		addButton = new JButton("Add");
		removeButton = new JButton("Remove");

		topPanel.add(stderrCheck);
		topPanel.add(stdoutCheck);
		topPanel.add(new JLabel("Other:"));
		topPanel.add(addButton);
		topPanel.add(removeButton);

		// Setting button listeners
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addFile();
			}
		});

		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeFile();
			}
		});

		removeButton.setEnabled(false);
		// Botton Panel
		this.fileScrollPane = new JScrollPane();
		this.fileListModel = new DefaultListModel();
		this.fileList = new JList(fileListModel);
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileScrollPane.getViewport().add(fileList, null);

		// Final assembly
		this.setBorder(new TitledBorder("Output Files"));

		this.setLayout(new BorderLayout());
		this.add(topPanel, BorderLayout.NORTH);
		this.add(fileScrollPane, BorderLayout.CENTER);

	}

	// Methods----------------------------------------------------------------
	private void addFile() {

		final JDialog addFileDialog;
		Container rootContainer = this.getTopLevelAncestor();
		if (rootContainer instanceof JDialog)
			addFileDialog = new JDialog((JDialog) rootContainer,
					"Add Output File", true);
		else
			// Assuming it is a JFrame
			addFileDialog = new JDialog((JFrame) rootContainer,
					"Add Output File", true);
		final AddFilePanel addFilePanel = new AddFilePanel();
		addFilePanel.addButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!addFilePanel.getFilename().equals("")) {
					fileListModel.addElement(addFilePanel.getFilename());
					removeButton.setEnabled(true);
				}
				addFileDialog.dispose();

			}
		});

		addFilePanel.cancelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addFileDialog.dispose();
			}
		});
		addFileDialog.setLocation(new Point(
				this.getTopLevelAncestor().getX() + 50, this
						.getTopLevelAncestor().getX() + 50));
		addFileDialog.setContentPane(addFilePanel);
		addFileDialog.pack();
		addFileDialog.setVisible(true);
	}

	// -----------------------------------------------------------------------
	private void removeFile() {

		if (fileList.getSelectedIndex() >= 0) {
			fileListModel.remove(fileList.getSelectedIndex());
			if (fileListModel.size() == 0)
				removeButton.setEnabled(false);

		}
	}

	// -----------------------------------------------------------------------
	public String[] getFilenames() {

		ArrayList filenames = new ArrayList();
		for (int i = 0; i < fileListModel.size(); i++)
			filenames.add(fileListModel.elementAt(i));
		if (stderrCheck.isSelected())
			filenames.add("stderr");
		if (stdoutCheck.isSelected())
			filenames.add("stdout");
		String[] names = new String[filenames.size()];
		filenames.toArray(names);
		return names;

	}

	// -----------------------------------------------------------------------
	public void setFilenames(String[] filenames) {
		fileListModel.removeAllElements();
		for (int i = 0; i < filenames.length; i++)
			addOutputFile(filenames[i]);
		if (filenames.length > 0)
			removeButton.setEnabled(true);
	}

	// -----------------------------------------------------------------------
	public void addOutputFile(String filename) {
		if (filename.equals("stderr"))
			stderrCheck.setSelected(true);
		else if (filename.equals("stdout"))
			stdoutCheck.setSelected(true);
		else
			fileListModel.addElement(filename);
	}

	// -----------------------------------------------------------------------
	public void reset() {
		fileListModel.removeAllElements();
		removeButton.setEnabled(false);
		stderrCheck.setSelected(false);
		stdoutCheck.setSelected(false);
	}

}// class

// ========================================================================

class AddFilePanel extends JPanel {

	private JButton addButton;

	private JButton cancelButton;

	private JTextField fileNameField;

	AddFilePanel() {

		addButton = new JButton("Add");
		cancelButton = new JButton("Cancel");
		fileNameField = new JTextField();

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(new JLabel("Filename: "), BorderLayout.WEST);
		topPanel.add(fileNameField, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new GridLayout(1, 0));
		bottomPanel.add(addButton);
		bottomPanel.add(cancelButton);

		this.setLayout(new BorderLayout());
		this.add(topPanel, BorderLayout.NORTH);
		this.add(bottomPanel, BorderLayout.CENTER);
	}

	public JButton addButton() {
		return addButton;
	}

	public JButton cancelButton() {
		return cancelButton;
	}

	public String getFilename() {
		return fileNameField.getText();
	}

}// class