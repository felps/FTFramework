package asct.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import asct.shared.ExecutionRequestStatus;
import asct.shared.ParametricCopyHolder;
import clusterManagement.ApplicationNotFoundException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;
import dataTypes.ApplicationType;

/**
 * RequestExecutionPanel - Allows a user to choose the application type and
 * configure the execution details.
 * 
 * @author Andrei Goldchleger
 * @date February/2004
 */
class ExecutionRequestPanel extends JPanel {

	private JButton submitButton;

	private JButton cancelButton;

	private JRadioButton regularButton;

	private JRadioButton bspButton;

	private JRadioButton parametricButton;

	private JRadioButton lastSelectedButton;

	// { IMPI
	private JRadioButton mpiButton;

	// } IMPI

	RegularPanel regularPanel;

	BspPanel bspPanel;

	// { IMPI
	MpiPanel mpiPanel;

	// } IMPI

	ParametricPanel parametricPanel;

	JPanel middleBottomPanel;

	MultiOptionHolder optionHolder;

	private JButton loadButton;

	private JButton saveButton;

	private JButton helpButton;

	String lastSelectedDir;

	// Constructor-------------------------------------------------------
	ExecutionRequestPanel(String applicationName_, String applicationBasePath_,
			String applicationBinaries) {//

		lastSelectedDir = ".";

		optionHolder = new MultiOptionHolder();

		optionHolder.addOption("Name: ");
		optionHolder.setValue("Name: ", applicationName_);
		optionHolder.toggleField("Name: ", false);
		optionHolder.addOption("Base path: ");
		if (!applicationBasePath_.equals("")) {
			optionHolder.setValue("Base path: ", applicationBasePath_);
			optionHolder.toggleField("Base path: ", false);
		}
		optionHolder.addOption("Binaries: ");
		if (!applicationBasePath_.equals("")) {
			optionHolder.setValue("Binaries: ", applicationBinaries);
			optionHolder.toggleField("Binaries: ", false);
		}
		optionHolder.addOption("Constraints: ");
		optionHolder.addOption("Preferences: ");

		// Radio
		// buttons--------------------------------------------------------------
		JPanel radioPanel = new JPanel();// new GridLayout(0,4)
		regularButton = new JRadioButton("Regular");
		bspButton = new JRadioButton("BSP");
		// { IMPI
		mpiButton = new JRadioButton("MPI");
		// } IMPI
		parametricButton = new JRadioButton("Parametric");
		radioPanel.add(new JLabel("Application type: "));
		radioPanel.add(regularButton);
		radioPanel.add(bspButton);
		// { IMPI
		radioPanel.add(mpiButton);
		// } IMPI
		radioPanel.add(parametricButton);
		ButtonGroup group = new ButtonGroup();
		group.add(regularButton);
		group.add(bspButton);
		// { IMPI
		group.add(mpiButton);
		// } IMPI

		group.add(parametricButton);
		regularButton.setSelected(true);
		lastSelectedButton = regularButton;

		regularButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchPanel();
			}
		});
		bspButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchPanel();
			}
		});
		// { IMPI
		mpiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchPanel();
			}
		});
		// } IMPI
		parametricButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchPanel();
			}
		});

		// Load/Save---------------------------------------------------------------

		loadButton = new JButton("Load");
		saveButton = new JButton("Save");
		helpButton = new JButton("?");

		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readExecDescriptor();
			}
		});
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeExecDescriptor();
			}
		});

		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHelpDialog();
			}
		});

		JPanel execDescPanel = new JPanel();
		execDescPanel.add(new JLabel("Execution descriptor: "));
		execDescPanel.add(loadButton);
		execDescPanel.add(saveButton);

		JPanel upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(execDescPanel, BorderLayout.CENTER);
		upperPanel.add(helpButton, BorderLayout.EAST);

		regularPanel = new RegularPanel();
		bspPanel = new BspPanel();
		// { IMPI
		mpiPanel = new MpiPanel();
		// } IMPI
		parametricPanel = new ParametricPanel();

		JPanel middlePanel = new JPanel(new BorderLayout());
		middlePanel.add(radioPanel, BorderLayout.NORTH);

		middleBottomPanel = new JPanel(new CardLayout());
		middleBottomPanel.add("regular", regularPanel);
		middleBottomPanel.add("bsp", bspPanel);
		// { IMPI
		middleBottomPanel.add("mpi", mpiPanel);
		// } IMPI
		middleBottomPanel.add("parametric", parametricPanel);
		((CardLayout) middleBottomPanel.getLayout()).show(middleBottomPanel,
				"regular");

		middlePanel.add(middleBottomPanel, BorderLayout.CENTER);

		// Submit/Cancel Buttons
		JPanel bottonPanel = new JPanel();
		bottonPanel.setLayout(new GridLayout(0, 2));
		submitButton = new JButton("Submit");
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Container rootContainer = getTopLevelAncestor();
				if (rootContainer instanceof JDialog)
					((JDialog) rootContainer).dispose();
			}
		});
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ExecutionRequestStatus requestStatus;
					requestStatus = ASCTController.getInstance()
							.executeApplication(getApplicationBasePath(),
									getApplicationName(),
									getApplicationArguments(),
									getApplicationConstraints(),
									getApplicationPreferences(),
									getApplicationType(),
									getApplicationBinaries(), getInputFiles(),
									getOutputFiles(), getCopies(),
									getTaskNum(),
									shouldForceDifferentMachines());

					ExecutingApplicationsPanel callerPanel = ((ExecutionRequestDialog) getTopLevelAncestor())
							.getCallerPanel();
					callerPanel.addItem(requestStatus);

				} catch (ApplicationNotFoundException e1) {
					JOptionPane.showConfirmDialog(null, e1.getMessage(),
							"Exception Occured", JOptionPane.OK_OPTION);
					e1.printStackTrace();
				} catch (DirectoryNotFoundException e1) {
					JOptionPane.showConfirmDialog(null, e1.getMessage(),
							"Exception Occured", JOptionPane.OK_OPTION);
					e1.printStackTrace();
				} catch (InvalidPathNameException e1) {
					JOptionPane.showConfirmDialog(null, e1.getMessage(),
							"Exception Occured", JOptionPane.OK_OPTION);
					e1.printStackTrace();
				} catch (SecurityException e1) {
					JOptionPane.showConfirmDialog(null, e1.getMessage(),
							"Exception Occured", JOptionPane.OK_OPTION);
					e1.printStackTrace();
				}
				Container rootContainer = getTopLevelAncestor();
				if (rootContainer instanceof JDialog)
					((JDialog) rootContainer).dispose();
			}
		});
		bottonPanel.add(submitButton);
		bottonPanel.add(cancelButton);

		// final assembly
		this.setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(upperPanel, BorderLayout.NORTH);
		topPanel.add(optionHolder, BorderLayout.SOUTH);

		this.add(topPanel, BorderLayout.NORTH);
		this.add(middlePanel, BorderLayout.CENTER);
		this.add(bottonPanel, BorderLayout.SOUTH);
		this.setVisible(true);
	}

	// --------------------------------------------------------------------
	// Opens a dialog with some help information about ASCT usage
	void showHelpDialog() {
		final JDialog helpDialog = new JDialog();
		helpDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				helpDialog.dispose();
			}
		});

		// Try to open the AsctHelp.txt file
		try {
			JTextArea helpTextArea = new JTextArea(10, 20);
			JScrollPane helpTextAreaScroll = new JScrollPane(helpTextArea);
			helpTextArea.setLineWrap(true);
			helpTextArea.setWrapStyleWord(true);
			helpTextArea.setEditable(false);
			helpTextArea.setMargin(new Insets(5, 5, 5, 5));
			helpTextArea.setSize(600, 800);

			// Reads the file into the BufferedReader
			FileReader fr;
			BufferedReader br;
			fr = new FileReader("../AsctHelp.txt");
			br = new BufferedReader(fr);

			// Reads from the buffered reader into the TextArea
			String line;
			try {
				while ((line = br.readLine()) != null) {
					helpTextArea.append(line + "\n");
				}
			} catch (IOException ioe) {
				System.out.println("IO Exception.");
				return;
			}

			// Rewinds the TextArea
			helpTextArea.setCaretPosition(0);

			// Set final adjustements and shows the dialog
			helpDialog.setSize(650, 700);
			helpDialog.setLocation(new Point(300, 100));
			helpDialog.setTitle("Asct Help");
			helpDialog.add(helpTextAreaScroll);
			helpDialog.setVisible(true);

		} catch (FileNotFoundException fnfe) {
			System.out
					.println("ERROR: Asct help file (AsctHelp.txt) not found.");
			JLabel errorMessage = new JLabel(
					"Sorry, the help file could not be found", JLabel.CENTER);
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					helpDialog.setVisible(false);
					helpDialog.setResizable(false);
					helpDialog.dispose();
				}
			});

			// Shows a dialog informing the error
			helpDialog.setSize(400, 100);
			helpDialog.setLocation(new Point(500, 350));
			helpDialog.setTitle("Help not available");
			helpDialog.setLayout(new BorderLayout());
			helpDialog.add(errorMessage, BorderLayout.CENTER);
			helpDialog.add(closeButton, BorderLayout.SOUTH);
			helpDialog.setVisible(true);
		}
	}

	// --------------------------------------------------------------------
	private void switchPanel() {

		if (regularButton.isSelected())
			((CardLayout) middleBottomPanel.getLayout()).show(
					middleBottomPanel, "regular");
		else if (bspButton.isSelected())
			((CardLayout) middleBottomPanel.getLayout()).show(
					middleBottomPanel, "bsp");
		// { IMPI
		else if (mpiButton.isSelected())
			((CardLayout) middleBottomPanel.getLayout()).show(
					middleBottomPanel, "mpi");
		// } IMPI
		else if (parametricButton.isSelected())
			((CardLayout) middleBottomPanel.getLayout()).show(
					middleBottomPanel, "parametric");

	}

	// --------------------------------------------------------------------

	public String getApplicationName() {
		return optionHolder.getValue("Name: ");
	}

	public String getApplicationBasePath() {
		return optionHolder.getValue("Base path: ");
	}

	public String getApplicationConstraints() {
		return optionHolder.getValue("Constraints: ");
	}

	public String getApplicationPreferences() {
		return optionHolder.getValue("Preferences: ");
	}

	public String getApplicationBinaries() {
		return optionHolder.getValue("Binaries: ");
	}

	public JButton submitButton() {
		return submitButton;
	}

	public JButton cancelButton() {
		return cancelButton;
	}

	public ApplicationType getApplicationType() {
		if (regularButton.isSelected())
			return ApplicationType.regular;
		else if (bspButton.isSelected())
			return ApplicationType.bsp;
		// { IMPI
		else if (mpiButton.isSelected())
			return ApplicationType.mpi;
		// } IMPI
		else if (parametricButton.isSelected())
			return ApplicationType.parametric;
		// TODO: Create an Exception to throw, instead of return null
		else
			return null;
	}

	public String getApplicationArguments() {
		if (regularButton.isSelected())
			return regularPanel.appArgs();
		else if (bspButton.isSelected())
			return bspPanel.appArgs();
		// { IMPI
		else if (mpiButton.isSelected())
			return mpiPanel.appArgs();
		// } IMPI
		return "";
	}

	public String[] getOutputFiles() {
		if (regularButton.isSelected()) {
			return regularPanel.outputFiles();
		} else if (bspButton.isSelected()) {
			return bspPanel.outputFiles();
		}
		// { IMPI
		else if (mpiButton.isSelected()) {
			return mpiPanel.outputFiles();
		}
		// } IMPI
		else
			return new String[] {};
	}

	public boolean shouldForceDifferentMachines() {
		if (parametricButton.isSelected())
			return parametricPanel.shouldForceDifferentMachines();
		if (bspButton.isSelected())
			return bspPanel.shouldForceDifferentMachines();
		// { IMPI
		if (mpiButton.isSelected())
			return mpiPanel.shouldForceDifferentMachines();
		// } IMPI
		return false;
	}

	public boolean isRegular() {
		return regularButton.isSelected();
	}

	public boolean isBsp() {
		return bspButton.isSelected();
	}

	// { IMPI
	public boolean isMpi() {
		return mpiButton.isSelected();
	}

	// } IMPI

	public boolean isParametric() {
		return parametricButton.isSelected();
	}

	// ---------------------------------------------------------------------------
	public String[] getInputFiles() {

		if (regularButton.isSelected())
			return regularPanel.inputFiles();
		else if (bspButton.isSelected())
			return bspPanel.inputFiles();
		// { IMPI
		else if (mpiButton.isSelected())
			return mpiPanel.inputFiles();
		// } IMPI
		else
			return new String[] {};
	}

	// ---------------------------------------------------------------------------
	public int getTaskNum() {

		if (bspButton.isSelected())
			return bspPanel.taskNum();
		// { IMPI
		else if (mpiButton.isSelected())
			return mpiPanel.taskNum();
		// } IMPI
		return 0;

	}

	// ---------------------------------------------------------------------------
	public ParametricCopyHolder[] getCopies() {

		if (parametricButton.isSelected())
			return parametricPanel.getCopies();
		return new ParametricCopyHolder[] {};

	}

	// Reset Fields. Called when loading an Execution Descriptor.
	private void reset() {
		optionHolder.setValue("Name: ", "");
		optionHolder.setValue("Base path: ", "");
		optionHolder.setValue("Constraints: ", "");
		optionHolder.setValue("Preferences: ", "");
	}

	// ---------------------------------------------------------------------------
	private void readExecDescriptor() {

		JFileChooser fileChooser = new JFileChooser(".");
		fileChooser.setDialogTitle("Load Descriptor");
		fileChooser.setApproveButtonText("Load");
		if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;

		ExecutionRequestDescriptorComposite requestDescriptor;
		try {
			BufferedReader descFile = new BufferedReader(
					(new InputStreamReader(new FileInputStream(fileChooser
							.getSelectedFile().getAbsolutePath()))));
			requestDescriptor = ASCTController.getInstance()
					.readExecutionRequestDescriptorFile(descFile);

			boolean isRegular = false;
			boolean isParametric = false;
			boolean isBsp = false;
			// { IMPI
			boolean isMpi = false;
			// } IMPI

			// Clearing all panels
			this.reset();
			parametricPanel.reset();
			bspPanel.reset();
			// { IMPI
			mpiPanel.reset();
			// } IMPI
			regularPanel.reset();

			// Checking appType-----------------------------
			if (requestDescriptor.getApplicationType() == ApplicationType.bsp) {
				isBsp = true;
				bspButton.doClick();
			}
			// { IMPI
			else if (requestDescriptor.getApplicationType() == ApplicationType.mpi) {
				isMpi = true;
				mpiButton.doClick();
				// } IMPI
			} else if (requestDescriptor.getApplicationType() == ApplicationType.parametric) {
				isParametric = true;
				parametricButton.doClick();
			} else if (requestDescriptor.getApplicationType() == ApplicationType.regular) {
				isRegular = true;
				regularButton.doClick();
			}
			// Checking ApplicationName------------------------------------
			optionHolder.setValue("Name: ", requestDescriptor
					.getApplicationName());
			// Getting applicationBasePath--------------------------------------
			optionHolder.setValue("Base path: ", requestDescriptor
					.getApplicationBasePath());
			optionHolder.toggleField("Base path: ", true);
			optionHolder.setValue("Binaries: ", requestDescriptor
					.getApplicationBinaries());
			optionHolder.toggleField("Binaries: ", false);
			// Getting appConstraints-----------------------------
			optionHolder.setValue("Constraints: ", requestDescriptor
					.getApplicationConstraints());
			// Getting appPreferences-----------------------------
			optionHolder.setValue("Preferences: ", requestDescriptor
					.getApplicationPreferences());
			// Getting the option "Force different machines"
			if (isParametric)
				parametricPanel
						.setShouldForceDifferentMachines(requestDescriptor
								.getShouldForceDifferentMachines());
			else if (isBsp)
				bspPanel.setShouldForceDifferentMachines(requestDescriptor
						.getShouldForceDifferentMachines());
			// { IMPI
			else if (isMpi)
				mpiPanel.setShouldForceDifferentMachines(requestDescriptor
						.getShouldForceDifferentMachines());
			// } IMPI

			// Getting appArgs------------------------------------
			if (isRegular)
				regularPanel.appArgs(requestDescriptor
						.getApplicationArguments());
			else if (isBsp)
				bspPanel.appArgs(requestDescriptor.getApplicationArguments());
			// { IMPI
			else if (isMpi)
				mpiPanel.appArgs(requestDescriptor.getApplicationArguments());
			// } IMPI

			// Getting taskNum------------------------------------
			if (isBsp)
				bspPanel.taskNum((new Integer(requestDescriptor
						.getNumberOfTasks())).toString());
			// { IMPI
			// FIXME: implementar o m�todo getMpiNumberOfTasks da classe
			// ExecutionRequestDescriptorComposite
			else if (isMpi)
				mpiPanel.taskNum((new Integer(requestDescriptor
						.getNumberOfTasks())).toString());
			// } IMPI

			// Getting inputFile------------------------------------
			if (isRegular) {
				for (Iterator<String> i = requestDescriptor.getInputFiles(); i
						.hasNext();)
					regularPanel.addInputFile(i.next());
			} else if (isBsp) {
				for (Iterator<String> i = requestDescriptor.getInputFiles(); i
						.hasNext();)
					bspPanel.addInputFile(i.next());
			}
			// { IMPI
			else if (isMpi)
				for (Iterator<String> i = requestDescriptor.getInputFiles(); i
						.hasNext();)
					mpiPanel.addInputFile(i.next());
			// } IMPI

			// Getting outputFile------------------------------------

			if (isRegular) {
				for (Iterator<String> i = requestDescriptor.getOutputFiles(); i
						.hasNext();)
					regularPanel.addOutputFile(i.next());
			} else if (isBsp) {
				for (Iterator<String> i = requestDescriptor.getOutputFiles(); i
						.hasNext();)
					bspPanel.addOutputFile(i.next());
			}
			// { IMPI
			else if (isMpi)
				for (Iterator<String> i = requestDescriptor.getOutputFiles(); i
						.hasNext();)
					mpiPanel.addOutputFile(i.next());
			// } IMPI

			// Getting appCopy------------------------------------
			if (isParametric) {
				for (Iterator<ExecutionRequestDescriptorComponent> i = requestDescriptor
						.getParametricApplicationCopies(); i.hasNext();) {
					
					ExecutionRequestDescriptorComponent copyData;
					copyData = i.next();
					ParametricCopyHolder copyHolder = new ParametricCopyHolder();
					ArrayList<String> inputFilesArrayList = new ArrayList<String>();
					ArrayList<String> outputFilesArrayList = new ArrayList<String>();
					
					for (Iterator<String> inputFileIterator = copyData
							.getInputFiles(); inputFileIterator.hasNext();) {
						inputFilesArrayList.add(inputFileIterator.next());
					}
					
					for (Iterator<String> outputFileIterator = copyData
							.getOutputFiles(); outputFileIterator.hasNext();) {
						outputFilesArrayList.add(outputFileIterator.next());
					}
					
					String[] inputFilesArray = new String[inputFilesArrayList
							.size()];
					String[] outputFilesArray = new String[outputFilesArrayList
							.size()];
					inputFilesArrayList.toArray(inputFilesArray);
					outputFilesArrayList.toArray(outputFilesArray);
					copyHolder.setInputFiles(inputFilesArray);
					copyHolder.setOutputFiles(outputFilesArray);
					parametricPanel.addCopy(copyHolder);
				}
			}// if(isParametric)
		} catch (FileNotFoundException fnfe) {
			Object[] options = new Object[] { "Dismiss" };
			JOptionPane.showOptionDialog(this, "File Not Found: \n"
					+ fileChooser.getSelectedFile().getPath() + "\n",
					"File Not Found", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null,// default icon
					options, options[0]);
		} catch (IOException e) {
			Object[] options = new Object[] { "Dismiss" };
			JOptionPane.showOptionDialog(this, "I/O Error", "I/O Error",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null,// default
					// icon
					options, options[0]);
			System.exit(-1);
		}

	}// Method

	// ---------------------------------------------------------------------------
	private void writeExecDescriptor() {

		JFileChooser fileChooser = new JFileChooser(".");
		fileChooser.setDialogTitle("Save Descriptor");
		fileChooser.setApproveButtonText("Save");
		if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		ExecutionRequestDescriptorComposite requestDescriptor = new ExecutionRequestDescriptorComposite();

		// Writing appType-------------------------------------------------
		if (regularButton.isSelected())
			requestDescriptor.setApplicationType(ApplicationType.regular);
		else if (bspButton.isSelected())
			requestDescriptor.setApplicationType(ApplicationType.bsp);
		// { IMPI
		else if (mpiButton.isSelected())
			requestDescriptor.setApplicationType(ApplicationType.mpi);
		// } IMPI

		else if (parametricButton.isSelected())
			requestDescriptor.setApplicationType(ApplicationType.parametric);
		// Writing appName-------------------------------------------------
		requestDescriptor.setApplicationName(optionHolder.getValue("Name: "));

		// Writing
		// applicationBasePath-------------------------------------------------
		requestDescriptor.setApplicationBasePath(optionHolder
				.getValue("Base path: "));
		// Writing binaries-------------------------------------------------
		requestDescriptor.setApplicationBinaries(optionHolder
				.getValue("Binaries: "));
		// Writing appConstraints--------------------------------------
		requestDescriptor.setApplicationConstraints(optionHolder
				.getValue("Constraints: "));
		// Writing appPreferences--------------------------------------
		requestDescriptor.setApplicationPreferences(optionHolder
				.getValue("Preferences: "));

		requestDescriptor
				.setShouldForceDifferentMachines(shouldForceDifferentMachines());

		// Writing taskNum & appArgs & NeededFiles------------------------
		if (bspButton.isSelected()) {
			requestDescriptor.setBspNumberOfTasks(bspPanel.taskNum());
			requestDescriptor.setApplicationArguments(bspPanel.appArgs());
			String[] inputFilesArray = bspPanel.inputFiles();
			for (int i = 0; i < inputFilesArray.length; i++)
				requestDescriptor.addInputFile(inputFilesArray[i]);
			String[] outputFilesArray = bspPanel.outputFiles();
			for (int i = 0; i < outputFilesArray.length; i++)
				requestDescriptor.addOutputFile(outputFilesArray[i]);
		}
		// { IMPI
		if (mpiButton.isSelected()) {
			requestDescriptor.setMpiNumberOfTasks(mpiPanel.taskNum());
			requestDescriptor.setApplicationArguments(mpiPanel.appArgs());
			String[] inputFilesArray = mpiPanel.inputFiles();
			for (int i = 0; i < inputFilesArray.length; i++)
				requestDescriptor.addInputFile(inputFilesArray[i]);
			String[] outputFilesArray = mpiPanel.outputFiles();
			for (int i = 0; i < outputFilesArray.length; i++)
				requestDescriptor.addOutputFile(outputFilesArray[i]);
		}
		// } IMPI
		if (regularButton.isSelected()) {
			requestDescriptor.setApplicationArguments(regularPanel.appArgs());
			String[] inputFilesArray = regularPanel.inputFiles();
			for (int i = 0; i < inputFilesArray.length; i++)
				requestDescriptor.addInputFile(inputFilesArray[i]);
			String[] outputFilesArray = regularPanel.outputFiles();
			for (int i = 0; i < outputFilesArray.length; i++)
				requestDescriptor.addOutputFile(outputFilesArray[i]);
		}
		if (parametricButton.isSelected()) {
			ParametricCopyHolder[] copies = parametricPanel.getCopies();
			for (int i = 0; i < copies.length; i++) {
				ExecutionRequestDescriptorComponent copy = new ExecutionRequestDescriptorComponent();
				copy.setApplicationArguments(copies[i].getArguments());
				String[] inputFilesArray = copies[i].getInputFiles();
				for (int j = 0; j < inputFilesArray.length; j++)
					copy.addInputFile(inputFilesArray[j]);
				String[] outputFilesArray = copies[i].getOutputFiles();
				for (int j = 0; j < outputFilesArray.length; j++)
					copy.addOutputFile(outputFilesArray[j]);
				requestDescriptor.addParametricApplicationCopies(copy);
			}
		}
		try {
			PrintWriter descFile = new PrintWriter(new FileOutputStream(
					new File(fileChooser.getSelectedFile().getAbsolutePath())));
			(ASCTController.getInstance()).writeExecutionRequestDescriptorFile(
					requestDescriptor, descFile);
		} catch (FileNotFoundException fnfe) {
			Object[] options = new Object[] { "Dismiss" };
			JOptionPane.showOptionDialog(this, "File Not Found: \n"
					+ fileChooser.getSelectedFile().getPath() + "\n",
					"File Not Found", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null,// default icon
					options, options[0]);
		} catch (IOException e) {
			Object[] options = new Object[] { "Dismiss" };
			JOptionPane.showOptionDialog(this, "I/O error: \n"
					+ fileChooser.getSelectedFile().getPath() + "\n",
					"I/O Error", JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE, null,// default icon
					options, options[0]);
			e.printStackTrace();
		}

	}// method

}// class

