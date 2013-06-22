package masct.gui;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import masct.util.Copy;

public class ApplicationCopiesPanel extends Panel implements ActionListener {
	private Label copiesLabel = null;
	private List copiesList = null;
	private Button addCopyButton = null;
	private Button removeCopyButton = null;
	private Button editCopyBtn = null;
	private Checkbox forceDiffNodesChk = null;
	
	public Vector copies;
	
	/**
	 * This is the default constructor
	 */
	public ApplicationCopiesPanel() {
		super();
		
		copies = new Vector();
		
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		
		editCopyBtn = new Button();
		editCopyBtn.setBounds(new java.awt.Rectangle(190,56,18,18));
		editCopyBtn.addActionListener( this );
		editCopyBtn.setLabel("...");
		
		removeCopyButton = new Button();
		removeCopyButton.setBounds(new java.awt.Rectangle(190,74,18,18));
		removeCopyButton.addActionListener(this);
		removeCopyButton.setLabel("-");
		
		addCopyButton = new Button();
		addCopyButton.setBounds(new java.awt.Rectangle(190,38,18,18));
		addCopyButton.addActionListener(this);
		addCopyButton.setLabel("+");
		
		copiesLabel = new Label();
		copiesLabel.setBounds(new Rectangle(10, 38, 50, 18));
		copiesLabel.setText("Copies: ");
		
		this.setLayout(null);
		this.setBackground(java.awt.Color.white);
		this.setBounds(new java.awt.Rectangle(0,0,230,160));
		this.add(copiesLabel, null);
		
		ScrollPane copyPanel = new ScrollPane();
		copyPanel.setBounds(getCopiesLst().getBounds());
		copyPanel.add(getCopiesLst(), getCopiesLst().getName());
		this.add(copyPanel);
		
		this.add(addCopyButton, null);
		this.add(removeCopyButton, null);
		this.add(editCopyBtn, null);
		this.add(getForceDiffNodesChk(), null);
	}

	/**
	 * This method initializes inputFLst	
	 * 	
	 * @return java.awt.List	
	 */
	public List getCopiesLst() {
		if (copiesList == null) {
			copiesList = new List();
			copiesList.setBounds(new java.awt.Rectangle(60,38,130,90));
		}
		return copiesList;
	}

	/**
	 * This method initializes forceDiffNodesChk	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	public Checkbox getForceDiffNodesChk() {
		if (forceDiffNodesChk == null) {
			forceDiffNodesChk = new Checkbox();
			forceDiffNodesChk.setBounds(new Rectangle(10, 9, 211, 18));
			forceDiffNodesChk.setBackground(java.awt.Color.white);
			forceDiffNodesChk.setLabel("Force Copies in Different Nodes");
		}
		return forceDiffNodesChk;
	}	

	public void clearAll() {
		
		getForceDiffNodesChk().setState(false);
		copies.clear();		
		getCopiesLst().removeAll();	
	}

	public void actionPerformed(ActionEvent arg0) {
		if( arg0.getSource() == addCopyButton ){
			
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().copy = null;
			this.setVisible( false );
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().setVisible(true);
			
		}
		if( arg0.getSource() == removeCopyButton ){
			
			int index = Integer.parseInt( (String) getCopiesLst().getSelectedItem() );
			getCopiesLst().removeAll();
			copies.remove( index );
			
			System.out.println("copies size: " + copies.size() );
			for(int i=0; i < copies.size(); i++){
				
				Copy copy = (Copy) copies.get(i);
				copy.name = Integer.toString(i);
				copies.setElementAt(copy, i);			
				
				getCopiesLst().add( copy.name );
			}
			
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().setNumberOfCopies(copies.size() );
			
		}
		if( arg0.getSource() == editCopyBtn ){
			
			int index = Integer.parseInt( (String)getCopiesLst().getSelectedItem() );
			Copy copy = (Copy)copies.get(index);
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().copy = copy;
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().getArgFld().setText( copy.arg );
			
			Object[] inputArray = copy.input.toArray();
			
			for(int i=0; i < inputArray.length; i++ ){
				Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().getInputFLst().add( (String) inputArray[i] );
			}
			
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().inputFiles = copy.input;
			
			Object[] outputArray = copy.output.toArray();
			
			for(int i=0; i < outputArray.length; i++ ){
				Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().getOutputFLst().add( (String) outputArray[i] );
			}
			
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().outputFiles = copy.output;
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().getStdoutChk().setState( copy.stdout );
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().getStderrChk().setState( copy.stderr );
			
			this.setVisible( false );
			Masct.getInstance().getRemoteExecutionPanel().getParametric().getCopy().setVisible(true);
		}		
		
	}	

}
