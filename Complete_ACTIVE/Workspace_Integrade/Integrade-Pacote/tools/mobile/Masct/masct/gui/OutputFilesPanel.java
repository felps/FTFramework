package masct.gui;

import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OutputFilesPanel extends Panel implements ActionListener {
	
	public String fileName;
	private Label outputFilesListLabel = null;	
	private TextArea resultsTextArea = null;
	private Button cancelButton = null;
	
	
	
	/**
	 * This is the default constructor
	 */
	public OutputFilesPanel() {
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

		outputFilesListLabel = new Label();
		outputFilesListLabel.setBounds(new java.awt.Rectangle(10, 10, 200, 18));
		outputFilesListLabel.setText("OutputFiles:");
		
		this.setLayout(null);
		this.setSize(220, 280);
		this.setBackground(java.awt.Color.white);		
		this.add(outputFilesListLabel, null);
		this.add(cancelButton, null);		
		

	}
	
	/**
	 * This method initializes ResultsLst	
	 * 	
	 * @return javax.swing.JList	
	 */
	public TextArea getResultsTextArea() {
		if ( resultsTextArea == null) {
			resultsTextArea = new TextArea();
			resultsTextArea.setBounds(new java.awt.Rectangle(10,28,205,210));
		}
		return resultsTextArea;
	}
	
	public void setFile(String file){		
			
		this.add(getResultsTextArea());
		resultsTextArea.setText( file );
		
		
	}

	public void actionPerformed(ActionEvent e) {
		
		Masct.getInstance().getOutputFilesScroll().setVisible(false);
		Masct.getInstance().setMenuBar( Masct.getInstance().getERMenuBar() );
		Masct.getInstance().getExecutionResultsScroll().setVisible(true);
		
	}	
	
}
