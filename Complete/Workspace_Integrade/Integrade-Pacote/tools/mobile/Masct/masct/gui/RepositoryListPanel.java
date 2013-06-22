
package masct.gui;

import java.awt.Button;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RepositoryListPanel extends Panel implements ActionListener {

	private Label appRepListLabel = null;
	private List appRepList = null;
	private Button okButton = null;
	private Button cancelButton = null;
	
	/**
	 * This is the default constructor
	 */
	public RepositoryListPanel() {
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
		cancelButton.setBounds(new Rectangle(120, 240, 73, 20));		
		cancelButton.setForeground(new java.awt.Color(51,51,51));
		cancelButton.setLabel("Cancel");
		cancelButton.addActionListener(this);
		okButton = new Button();
		okButton.setBounds(new Rectangle(30, 240, 73, 20));		
		okButton.setLabel("Ok");
		okButton.addActionListener(this);
		appRepListLabel = new Label();
		appRepListLabel.setBounds(new java.awt.Rectangle(10,10,200,18));
		appRepListLabel.setText("Application Repository List:");
		this.setLayout(null);
		this.setSize(220, 280);
		this.setBackground(java.awt.Color.white);
		this.add(appRepListLabel, null);
		ScrollPane scrollPanel = new ScrollPane();
		scrollPanel.add(getJList());
		scrollPanel.setBounds( getJList().getBounds() );
		this.add( scrollPanel );		
		this.add(okButton, null);
		this.add(cancelButton, null);
	}

	/**
	 * This method initializes jList	
	 * 	
	 * @return java.awt.List	
	 */
	public List getJList() {
		if (appRepList == null) {
			appRepList = new List();
			appRepList.setBounds(new java.awt.Rectangle(10,28,205,210));
		}
		return appRepList;
	}	


	public void actionPerformed(ActionEvent arg0) {

		if( arg0.getSource() == okButton ){
			
			String item = (String) getJList().getSelectedItem();			
			
			String base = item.substring( 0, item.lastIndexOf("/") );
			
			Masct.getInstance().getRemoteExecutionPanel().getBaseFld().setText( base.substring( 0, base.lastIndexOf("/")+1 ) );
			Masct.getInstance().getRemoteExecutionPanel().getAppNameFld().setText( base.substring( base.lastIndexOf("/")+1 ) + " ("+ item.substring( item.lastIndexOf("/")+1 ) +")" );
			
			getJList().removeAll();			
			Masct.getInstance().getRepositoryListScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getREMenuBar() );
			Masct.getInstance().getRemoteExecutionScroll().setVisible( true );
			
		}
		if( arg0.getSource() == cancelButton ){
			
			getJList().removeAll();			
			Masct.getInstance().getRepositoryListScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getREMenuBar() );
			Masct.getInstance().getRemoteExecutionScroll().setVisible( true );
			
		}
		
		
	}


}
