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

import masct.util.Copy;
import masct.util.InputDialog;

public class CopyPanel extends Panel implements ActionListener {

	private Label argumentsLabel = null;	
	private Label inputFilesLabel = null;
	private Label outputFiLesLabel = null;
	private Label addCopyLabel = null;
	private List inputFilesList = null;
	private List outputFilesList = null;
	private Checkbox stdoutChk = null;
	private Checkbox stderrChk = null;
	private TextField argumentsTextField = null;
	private Button addInputFilesButton = null;
	private Button removeInputFilsButton = null;
	private Button addOutputFilesButton = null;
	private Button removeOutputFilesButton = null;
	private Button okButton = null;
	private Button cancelButton = null;	
	
	private int numberOfCopies = 0;
	
	public Vector inputFiles;
	public Vector outputFiles;
	
	public Copy copy;
	

	
	/**
	 * This is the default constructor
	 */
	public CopyPanel() {
		super();
		
		copy = null;
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
		
		cancelButton = new Button();
		cancelButton.setBounds(new java.awt.Rectangle(115,130,73,20));
		cancelButton.addActionListener( this );
		cancelButton.setLabel("Cancel");
		
		okButton = new Button();
		okButton.setBounds(new java.awt.Rectangle(28,130,73,20));
		okButton.addActionListener( this );
		okButton.setLabel("Ok");
		
		addCopyLabel = new Label();
		addCopyLabel.setBounds(new java.awt.Rectangle(0,0,220,18));
		addCopyLabel.setBackground(java.awt.Color.black);
		addCopyLabel.setAlignment(Label.CENTER);
		addCopyLabel.setForeground(java.awt.Color.white);
		addCopyLabel.setText("Add Copy");		
		addCopyLabel.setVisible(true);
		
		removeOutputFilesButton = new Button();
		removeOutputFilesButton.setBounds(new java.awt.Rectangle(200,111,18,18));
		removeOutputFilesButton.addActionListener( this );
		removeOutputFilesButton.setLabel("-");
		
		addOutputFilesButton = new Button();
		addOutputFilesButton.setBounds(new java.awt.Rectangle(200,93,18,18));
		addOutputFilesButton.addActionListener( this );
		addOutputFilesButton.setLabel("+");
		
		removeInputFilsButton = new Button();
		removeInputFilsButton.setBounds(new java.awt.Rectangle(200,55,18,18));
		removeInputFilsButton.addActionListener( this );
		removeInputFilsButton.setLabel("-");
		
		addInputFilesButton = new Button();
		addInputFilesButton.setBounds(new java.awt.Rectangle(200,37,18,18));
		addInputFilesButton.addActionListener( this );
		addInputFilesButton.setLabel("+");
		
		outputFiLesLabel = new Label();
		outputFiLesLabel.setBounds(new java.awt.Rectangle(0,75,73,18));
		outputFiLesLabel.setText("Output Files:");
		
		inputFilesLabel = new Label();
		inputFilesLabel.setBounds(new java.awt.Rectangle(0,37,73,18));
		inputFilesLabel.setText("Input Files:");
		
		argumentsLabel = new Label();
		argumentsLabel.setBounds(new java.awt.Rectangle(0,19,73,18));
		argumentsLabel.setText("Arguments:");
		
		this.setLayout(null);
		this.setBackground(java.awt.Color.white);
		this.setBounds(new java.awt.Rectangle(0,0,280,150));
		this.add(argumentsLabel, null);
		this.add(getArgFld(), null);
		this.add(inputFilesLabel, null);
		this.add(outputFiLesLabel, null);
		
		ScrollPane ifPanel = new ScrollPane();
		ifPanel.add(getInputFLst());
		ifPanel.setBounds(getInputFLst().getBounds());
		this.add(ifPanel);
		
		ScrollPane ofPanel = new ScrollPane();
		ofPanel.add(getOutputFLst());
		ofPanel.setBounds(getOutputFLst().getBounds());
		this.add(ofPanel);

		this.add(getStdoutChk(), null);
		this.add(getStderrChk(), null);
		this.add(addInputFilesButton, null);
		this.add(removeInputFilsButton, null);
		this.add(addOutputFilesButton, null);
		this.add(removeOutputFilesButton, null);
		this.add(addCopyLabel, null);		
		this.add(okButton, null);
		this.add(cancelButton, null);
		
		this.setVisible(true);
	}

	/**
	 * This method initializes argFld	
	 * 	
	 * @return javax.swing.TextField	
	 */
	public TextField getArgFld() {
		if (argumentsTextField == null) {
			argumentsTextField = new TextField();
			argumentsTextField.setBounds(new java.awt.Rectangle(74,19,120,18));
		}
		return argumentsTextField;
	}

	/**
	 * This method initializes inputFLst	
	 * 	
	 * @return javax.swing.List	
	 */
	public List getInputFLst() {
		if (inputFilesList == null) {
			inputFilesList = new List();
			inputFilesList.setBounds(new java.awt.Rectangle(74,37,120,35));
		}
		return inputFilesList;
	}

	/**
	 * This method initializes outputFLst	
	 * 	
	 * @return javax.swing.List	
	 */
	public List getOutputFLst() {
		if (outputFilesList == null) {
			outputFilesList = new List();
			outputFilesList.setBounds(new java.awt.Rectangle(74,93,120,35));
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
			stdoutChk.setBounds(new java.awt.Rectangle(74,75,60,18));
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
			stderrChk.setBounds(new java.awt.Rectangle(136,75,80,18));
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
		if( arg0.getSource() == removeInputFilsButton ){
			
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
		
		if( arg0.getSource() == okButton ){
			
			if( copy == null ){
				
				copy = new Copy();
				copy.arg = getArgFld().getText();
				copy.input = new Vector( inputFiles );
				copy.output = new Vector( outputFiles );
				copy.stderr = getStderrChk().getState();
				copy.stdout = getStdoutChk().getState();
				
				Masct.getInstance().getRemoteExecutionPanel().getParametric().getApplicationCopiesPanel().copies.add( numberOfCopies, copy );

				copy.name = Integer.toString(numberOfCopies);
				numberOfCopies++;
				
				Masct.getInstance().getRemoteExecutionPanel().getParametric().getApplicationCopiesPanel().getCopiesLst().add( copy.name );
			

				
			}
			else{
				
				copy.arg = getArgFld().getText();
				copy.input = new Vector( inputFiles );
				copy.output = new Vector( outputFiles );
				copy.stderr = getStderrChk().getState();
				copy.stdout = getStdoutChk().getState();
				int index = Integer.parseInt( copy.name );
				Masct.getInstance().getRemoteExecutionPanel().getParametric().getApplicationCopiesPanel().copies.add(index, copy);
			}
			
			clearAll();
			this.setVisible(false);
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getApplicationCopiesPanel().setVisible(true);
		}
		if( arg0.getSource() == cancelButton ){
	
			clearAll();
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getApplicationCopiesPanel().setVisible(true);
		}
	
	}

	public int getNumberOfCopies() {
		return numberOfCopies;
	}

	public void setNumberOfCopies(int numberOfCopies) {
		this.numberOfCopies = numberOfCopies;
	}

}
