package masct.gui;

import java.awt.Button;
import java.awt.Label;
import java.awt.List;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import masct.util.MasctClientAPI;
import messages.ExecutionResultsRequestMessage;
import messages.KillApplicationRequestMessage;
import moca.core.proxy.message.DefaultMessage;


public class ExecutionStatePanel extends Panel implements ActionListener{

	private Panel parent;
	private Masct mainFrame;
	private MenuBar menu;
	private Label executionStatusLabel = null;
	private List executionStatusList = null;
	private Button okButton = null;
	private Button cancelButton = null;
	private Button killButton = null;

	/**
	 * This is the default constructor
	 */
	public ExecutionStatePanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		killButton = new Button();
		killButton.setBounds(new Rectangle(10, 240, 60, 20));
		killButton.setLabel("Kill");
		killButton.addActionListener( this );

		cancelButton = new Button();
		cancelButton.setBounds(new Rectangle(156, 240, 60, 20));
		cancelButton.setLabel("Cancel");
		cancelButton.addActionListener( this );

		okButton = new Button();
		okButton.setBounds(new Rectangle(83, 240, 60, 20));
		okButton.setLabel("Ok");
		okButton.addActionListener( this );

		executionStatusLabel = new Label();
		executionStatusLabel.setBounds(new java.awt.Rectangle(10,10,200,18));
		executionStatusLabel.setText("Execution Status List:");
		
		this.setLayout(null);
		this.setSize(220, 280);
		this.setBackground(java.awt.Color.white);
		this.add(executionStatusLabel, null);
		
		ScrollPane scrollPanel = new ScrollPane();
		scrollPanel.add(getExecStatusLst());
		scrollPanel.setBounds(getExecStatusLst().getBounds());
		
		this.add(scrollPanel);
		this.add(okButton, null);
		this.add(cancelButton, null);
		this.add(killButton, null);
	}

	public void setParentPanel(Panel p) {
		
		parent = p;
	}

	public void setMainFrame(Masct f) {
		
		mainFrame = f;
	}

	public void setMenuBar(MenuBar m) {
		
		menu = m;
	}

	/**
	 * This method initializes ExecStatusLst	
	 * 	
	 * @return javax.swing.List	
	 */
	public List getExecStatusLst() {
		if (executionStatusList == null) {
			executionStatusList = new List();
			executionStatusList.setBounds(new java.awt.Rectangle(10,28,205,210));		
		}
		return executionStatusList;
	}

	public void actionPerformed(ActionEvent arg0) {
		if( arg0.getSource() == okButton ){
			String token[] = new String[3]; 
			StringTokenizer st = new StringTokenizer((String) getExecStatusLst().getItem(getExecStatusLst().getSelectedIndex()),"-");
			int i =0;
			while (st.hasMoreTokens()){
				token[i] = st.nextToken();  
				i++;
			}
			
			if( token[2].equals("FINISHED") ){
				
				ExecutionResultsRequestMessage requisition = new ExecutionResultsRequestMessage(
						Masct.getInstance().getUser(), "Grid Proxy", 0, DefaultMessage.OBJECT, null);
				
				requisition.setSubmitionId( Long.parseLong( token[0] ) );
				
				MasctClientAPI.getInstance().send( requisition );
				
			}
			else{
				//JOptionPane.showMessageDialog(null, "Execution Not Finnished!", "Alert!", JOptionPane.WARNING_MESSAGE);
			}
			
		}
		else if( arg0.getSource() == cancelButton ){
			
			getExecStatusLst().removeAll();
			
			Masct.getInstance().getExecutionStateScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getMainMenuBar() );
			Masct.getInstance().getPanel().setVisible(true);
			
			//parent.setVisible(true);
			
		}
		else if( arg0.getSource() == killButton ){
			
			KillApplicationRequestMessage requisition = new KillApplicationRequestMessage(
					Masct.getInstance().getUser(), "Grid Proxy", 0, DefaultMessage.OBJECT, null);	
			
			MasctClientAPI.getInstance().send( requisition );
			
		}
		
	}	

}
