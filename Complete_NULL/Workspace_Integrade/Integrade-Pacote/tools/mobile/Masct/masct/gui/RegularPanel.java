package masct.gui;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import masct.util.InputDialog;

public class RegularPanel extends Panel implements ActionListener {
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
	private Button removeOutpuFilesButton = null;
	
	public Vector inputFiles;
	public Vector outputFiles;
	
	/**
	 * This is the default constructor
	 */
	public RegularPanel() {
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
		
		removeOutpuFilesButton = new Button();
		removeOutpuFilesButton.setBounds(new java.awt.Rectangle(200,108,18,18));
		removeOutpuFilesButton.addActionListener( this );
		removeOutpuFilesButton.setLabel("-");
		
		addOutputFilesButton = new Button();
		addOutputFilesButton.setBounds(new java.awt.Rectangle(200,90,18,18));
		addOutputFilesButton.addActionListener( this );
		addOutputFilesButton.setLabel("+");
		
		removeInputFilesButton = new Button();
		removeInputFilesButton.setBounds(new java.awt.Rectangle(200,36,18,18));
		removeInputFilesButton.addActionListener( this );
		removeInputFilesButton.setLabel("-");
		
		addInputFilesButton = new Button();
		addInputFilesButton.setBounds(new java.awt.Rectangle(200,18,18,18));
		addInputFilesButton.addActionListener( this );
		addInputFilesButton.setLabel("+");
		
		outputFilesLabel = new Label();
		outputFilesLabel.setBounds(new Rectangle(0, 70, 70, 18));
		outputFilesLabel.setText("Output Files:");
		
		inputFilesLabel = new Label();
		inputFilesLabel.setBounds(new Rectangle(0, 18, 68, 18));
		inputFilesLabel.setText("Input Files:");
		
		argumentsLabel = new Label();
		argumentsLabel.setBounds(new Rectangle(0, 0, 68, 18));
		argumentsLabel.setText("Arguments:");
		
		this.setLayout(null);
		this.setBackground(java.awt.Color.white);
		this.setBounds(new java.awt.Rectangle(0,0,230,160));
		this.add(argumentsLabel, null);
		this.add(getArgFld(), null);
		this.add(inputFilesLabel, null);
		this.add(outputFilesLabel, null);
		
		ScrollPane ifPanel = new ScrollPane();
		ifPanel.setBounds(getInputFLst().getBounds());
		ifPanel.add(getInputFLst(), getInputFLst().getName());
		this.add(ifPanel);
		
		ScrollPane ofPanel = new ScrollPane();
		ofPanel.setBounds(getOutputFLst().getBounds());
		ofPanel.add(getOutputFLst(), getOutputFLst().getName());
		this.add(ofPanel);
		
		this.add(getStdoutChk(), null);
		this.add(getStderrChk(), null);
		this.add(addInputFilesButton, null);
		this.add(removeInputFilesButton, null);
		this.add(addOutputFilesButton, null);
		this.add(removeOutpuFilesButton, null);
	}

	/**
	 * This method initializes argFld	
	 * 	
	 * @return javax.swing.TextField	
	 */
	public TextField getArgFld() {
		if (argumentsTextField == null) {
			argumentsTextField = new TextField();
			argumentsTextField.setBounds(new Rectangle(73, 0, 120, 18));
		}
		return argumentsTextField;
	}

	/**
	 * This method initializes inputFLst	
	 * 	
	 * @return javax.swing.List	
	 */
	private List getInputFLst() {
		if (inputFilesList == null) {
			inputFilesList = new List();
			inputFilesList.setBounds(new java.awt.Rectangle(73,18,120,55));
		}
		return inputFilesList;
	}

	/**
	 * This method initializes outputFLst	
	 * 	
	 * @return javax.swing.List	
	 */
	private List getOutputFLst() {
		if (outputFilesList == null) {
			outputFilesList = new List();
			outputFilesList.setBounds(new java.awt.Rectangle(73,90,120,55));
		}
		return outputFilesList;
	}

	/**
	 * This method initializes stdoutChk	
	 * 	
	 * @return javax.swing.Checkbox	
	 */
	public Checkbox getStdoutChk() {
		if (stdoutChk == null) {
			stdoutChk = new Checkbox();
			stdoutChk.setBounds(new Rectangle(73, 70, 58, 18));
			stdoutChk.setBackground(java.awt.Color.white);
			stdoutChk.setLabel("stdout");
			stdoutChk.setState(true);
		}
		return stdoutChk;
	}

	/**
	 * This method initializes stderrChk	
	 * 	
	 * @return javax.swing.Checkbox	
	 */
	public Checkbox getStderrChk() {
		if (stderrChk == null) {
			stderrChk = new Checkbox();
			stderrChk.setBounds(new Rectangle(138, 70, 58, 18));
			stderrChk.setBackground(java.awt.Color.white);
			stderrChk.setLabel("stderr");
			stderrChk.setState(true);
		}
		return stderrChk;
	}	

	public void clearAll() {
		
		getArgFld().setText("");
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
		if( arg0.getSource() == removeOutpuFilesButton ){
			
			outputFiles.remove( getOutputFLst().getSelectedItem() );
			getOutputFLst().remove(getOutputFLst().getSelectedItem());			
		}		
		
	}
	
}