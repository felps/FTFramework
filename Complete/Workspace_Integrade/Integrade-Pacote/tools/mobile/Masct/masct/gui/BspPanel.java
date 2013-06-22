package masct.gui;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import masct.util.InputDialog;
import java.awt.Rectangle;

public class BspPanel extends Panel implements ActionListener{
	private Label argumentsLabel = null;
	private TextField argumentsTextField = null;
	private Label inputFilesLabel = null;
	private Label outputFilesLabel = null;
	private List inputFilesList = null;
	private List outputFilesList = null;
	private Checkbox stdoutChk = null;
	private Checkbox stderrChk = null;
	private Button addInputFilesButton = null;
	private Button removeInputFilesButton = null;
	private Button addOutputFilesButton = null;
	private Button removeOutputFilesButton = null;
	private Label numberOfTasksLabel = null;
	private TextField numberOfTasksTextField = null;
	private Checkbox forceDiffNodesChk = null;
	
	public Vector inputFiles;
	public Vector outputFiles;
	
	/**
	 * This is the default constructor
	 */
	public BspPanel() {
		super();
		
		inputFiles = new Vector();
		outputFiles = new Vector();
		
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		removeOutputFilesButton = new Button();
		removeOutputFilesButton.setBounds(new Rectangle(200, 127, 18, 18));

		removeOutputFilesButton.addActionListener( this );
		removeOutputFilesButton.setLabel("-");
		
		addOutputFilesButton = new Button();
		addOutputFilesButton.setBounds(new Rectangle(200, 109, 18, 18));

		addOutputFilesButton.addActionListener( this );
		addOutputFilesButton.setLabel("+");
		
		removeInputFilesButton = new Button();
		removeInputFilesButton.setBounds(new Rectangle(200, 73, 18, 18));

		removeInputFilesButton.addActionListener( this );
		removeInputFilesButton.setLabel("-");
		
		addInputFilesButton = new Button();
		addInputFilesButton.setBounds(new Rectangle(200, 55, 18, 18));

		addInputFilesButton.addActionListener( this );
		addInputFilesButton.setLabel("+");
		
		outputFilesLabel = new Label();
		outputFilesLabel.setBounds(new java.awt.Rectangle(0,91,73,18));
		outputFilesLabel.setText("Output Files:");
		inputFilesLabel = new Label();
		inputFilesLabel.setBounds(new java.awt.Rectangle(0,55,73,18));
		inputFilesLabel.setText("Input Files:");
		argumentsLabel = new Label();
		argumentsLabel.setBounds(new Rectangle(0, 18, 73, 18));
		argumentsLabel.setText("Arguments:");
		numberOfTasksLabel = new Label();
		numberOfTasksLabel.setBounds(new Rectangle(0, 0, 73, 18));
		numberOfTasksLabel.setText("N. of Tasks:");
		
		this.setLayout(null);
		this.setBackground(java.awt.Color.white);
		this.setBounds(new java.awt.Rectangle(0,0,230,160));		
		this.add(argumentsLabel, null);
		this.add(getArgFld(), null);
		this.add(inputFilesLabel, null);
		this.add(outputFilesLabel, null);
		
		ScrollPane ifPanel = new ScrollPane();
		ifPanel.setBounds(getInputFLst().getBounds());
		ifPanel.add(getInputFLst());		
		this.add( ifPanel );
		
		ScrollPane ofPanel = new ScrollPane();
		ofPanel.setBounds(getOutputFLst().getBounds());
		ofPanel.add(getOutputFLst());
		
		this.add( ofPanel );
		
		this.add(getStdoutChk(), null);
		this.add(getStderrChk(), null);
		this.add(addInputFilesButton, null);
		this.add(removeInputFilesButton, null);
		this.add(addOutputFilesButton, null);
		this.add(removeOutputFilesButton, null);
		this.add(numberOfTasksLabel, null);
		this.add(getNumOfTasksFld(), null);
		this.add(getForceDiffNodesChk(), null);

	}

	/**
	 * This method initializes argFld	
	 * 	
	 * @return java.awt.TextField	
	 */
	public TextField getArgFld() {
		if (argumentsTextField == null) {
			argumentsTextField = new TextField();
			argumentsTextField.setBounds(new java.awt.Rectangle(74,18,120,18));
		}
		return argumentsTextField;
	}

	/**
	 * This method initializes inputFLst	
	 * 	
	 * @return java.awt.List	
	 */
	private List getInputFLst() {
		if (inputFilesList == null) {
			inputFilesList = new List();
			inputFilesList.setBounds(new Rectangle(74, 56, 120, 36));
		}
		return inputFilesList;
	}

	/**
	 * This method initializes outputFLst	
	 * 	
	 * @return java.awt.List	
	 */
	private List getOutputFLst() {
		if (outputFilesList == null) {
			outputFilesList = new List();
			outputFilesList.setBounds(new java.awt.Rectangle(74,109,120,36));
		}
		return outputFilesList;
	}

	/**
	 * This method initializes stdoutChk	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	public Checkbox getStdoutChk() {
		if (stdoutChk == null) {
			stdoutChk = new Checkbox();
			stdoutChk.setBounds(new java.awt.Rectangle(74,91,60,18));
			stdoutChk.setBackground(java.awt.Color.white);
			stdoutChk.setLabel("stdout");
			stdoutChk.setState(true);
		}
		return stdoutChk;
	}

	/**
	 * This method initializes stderrChk	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	public Checkbox getStderrChk() {
		if (stderrChk == null) {
			stderrChk = new Checkbox();
			stderrChk.setBounds(new Rectangle(136, 91, 80, 18));
			stderrChk.setBackground(java.awt.Color.white);
			stderrChk.setLabel("stderr");
			stderrChk.setState(true);
		}
		return stderrChk;
	}

	/**
	 * This method initializes numOfTasksFld	
	 * 	
	 * @return java.awt.TextField	
	 */
	public TextField getNumOfTasksFld() {
		if (numberOfTasksTextField == null) {
			numberOfTasksTextField = new TextField();
			numberOfTasksTextField.setBounds(new Rectangle(74, 0, 120, 18));
		}
		return numberOfTasksTextField;
	}

	/**
	 * This method initializes forceDiffNodesChk	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	public Checkbox getForceDiffNodesChk() {
		if (forceDiffNodesChk == null) {
			forceDiffNodesChk = new Checkbox();
			forceDiffNodesChk.setBounds(new java.awt.Rectangle(0,36,220,18));
			forceDiffNodesChk.setBackground(java.awt.Color.white);
			forceDiffNodesChk.setLabel("Force Copies in Different Nodes");
		}
		return forceDiffNodesChk;
	}
	
	public void clearAll() {
		
		getNumOfTasksFld().setText("");
		getArgFld().setText("");
		getForceDiffNodesChk().setState(false);
		inputFiles.clear();
		getStdoutChk().setState(true);
		getStderrChk().setState(true);
		outputFiles.clear();

		getInputFLst().removeAll();
		getOutputFLst().removeAll();

		
	}

	public void actionPerformed(ActionEvent arg0) {
		if( arg0.getSource() == addInputFilesButton ){
			
			InputDialog inputFilesDialog = new InputDialog(Masct.getInstance(),"Enter input file name");
			inputFilesDialog.setVisible(true);
			String in = inputFilesDialog.getInput();
			if( in != null ){
				if( ! in.equals("") ){
					inputFiles.add( in );
					getInputFLst().add(in);
				}
			}
		}
		if( arg0.getSource() == removeInputFilesButton ){
			
			inputFiles.remove( getInputFLst().getSelectedItem() );
			getInputFLst().remove(getInputFLst().getSelectedItem());
			
		}
		if( arg0.getSource() == addOutputFilesButton ){			
			
			InputDialog outputFilesDialog = new InputDialog(Masct.getInstance(),"Enter output file name");
			outputFilesDialog.setVisible(true);
			String out = outputFilesDialog.getInput();			

			if( out != null ){
				if( ! out.equals("") ){
					outputFiles.add( out );
					getOutputFLst().add(out);

				}
			}
			
		}
		if( arg0.getSource() == removeOutputFilesButton ){
			
			outputFiles.remove( getOutputFLst().getSelectedItem() );
			getOutputFLst().remove(getOutputFLst().getSelectedItem());			
		}		

		
	}
	
}