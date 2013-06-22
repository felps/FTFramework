package masct.gui;

import java.awt.Button;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import masct.util.MasctClientAPI;
import messages.OutputFileRequestMessage;
import moca.core.proxy.message.DefaultMessage;

public class ExecutionResultsPanel extends Panel implements ActionListener {
	private Label resultsListLabel = null;
	private List resultsList = null;
	private Button getOutputFileButton = null;
	private Button cancelButton = null;

	
	public String remoteDir;
	public String executionDir;
	public String nodeDir[];
	public String outputFileName[];
	
	/**
	 * This is the default constructor
	 */
	public ExecutionResultsPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		cancelButton = new Button();
		cancelButton.setBounds(new Rectangle(156, 240, 60, 20));
		cancelButton.setLabel("Cancel");
		cancelButton.addActionListener( this );
		
		getOutputFileButton = new Button();
		getOutputFileButton.setBounds(new Rectangle(10, 240, 130, 20));
		getOutputFileButton.setLabel("Get Output File");
		getOutputFileButton.addActionListener( this );
		
		resultsListLabel = new Label();
		resultsListLabel.setBounds(new java.awt.Rectangle(10, 10, 200, 18));
		resultsListLabel.setText("Results List:");
		
		this.setLayout(null);
		this.setSize(220, 280);
		this.setBackground(java.awt.Color.white);
		this.add(resultsListLabel, null);
		
		ScrollPane resPanel = new ScrollPane();
		resPanel.add(getResultsLst());
		resPanel.setBounds(getResultsLst().getBounds());
		this.add(resPanel);
		
		this.add(getOutputFileButton, null);
		this.add(cancelButton, null);
	}

	/**
	 * This method initializes ResultsLst	
	 * 	
	 * @return javax.swing.JList	
	 */
	public List getResultsLst() {
		if (resultsList == null) {
			resultsList = new List();
			resultsList.setBounds(new java.awt.Rectangle(10,28,205,210));
		}
		return resultsList;
	}

	public void actionPerformed(ActionEvent e) {
		if( e.getSource() == getOutputFileButton ){
			
			// Pega o item selecionado
			String nodeName = nodeDir[ getResultsLst().getSelectedIndex() ];
			String fileName = outputFileName[ getResultsLst().getSelectedIndex() ];
			
			
			OutputFileRequestMessage requisition = new OutputFileRequestMessage(
					Masct.getInstance().getUser(), "Grid Proxy", 0, DefaultMessage.OBJECT, null);
			
			requisition.setFileName( remoteDir+"/"+executionDir+"/"+nodeName+"/"+fileName );
			
			MasctClientAPI.getInstance().send( requisition );
			
			
		}
		else if( e.getSource() == cancelButton ){
			
			getResultsLst().removeAll();			
			Masct.getInstance().getExecutionResultsScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getMainMenuBar() );
			Masct.getInstance().getExecutionStateScroll().setVisible( true );
		}
		
	}
	
}
