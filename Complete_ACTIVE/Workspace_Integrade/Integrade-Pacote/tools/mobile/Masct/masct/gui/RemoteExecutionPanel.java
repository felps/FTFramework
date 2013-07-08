package masct.gui;

import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import masct.util.Copy;
import masct.util.MasctClientAPI;
import masct.util.TabPanel.TabPanel;
import messages.ParametricCopy;
import messages.RepositoryListRequestMessage;
import messages.SubmitApplicationRequestMessage;
import moca.core.proxy.message.DefaultMessage;

public class RemoteExecutionPanel extends Panel implements ActionListener {
	private TabPanel typeAppTabPanel = null;
	private RegularPanel regularPanel = null;
	private BspPanel bsp = null;
	private ParametricPanel parametric = null;
	private Button okButton = null;
	private Button cancelButton = null;
	private Button appListButton = null;	
	private Label baseLabel = null;
	private Label appNameLabel = null;
	private Label preferencesLabel = null;
	private Label constrainstsLabel = null;
	private TextField baseTextField = null;
	private TextField appNameTextField = null;
	private TextField preferencesTextField = null;
	private TextField constraintsTextField = null;

	
	private static final int _regular = 0;
	public static final int _parametric = 1;
	public static final int _bsp = 2;
	/**
	 * This is the default constructor
	 */
	public RemoteExecutionPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		
        appListButton = new Button();
        appListButton.setBounds(new Rectangle(205, 1, 18, 18));
        appListButton.addActionListener(this);
        appListButton.setLabel("...");
        
        constrainstsLabel = new Label();
        constrainstsLabel.setBounds(new Rectangle(10,54,90,18));
        constrainstsLabel.setText("Constraints:");
        
        preferencesLabel = new Label();
        preferencesLabel.setBounds(new Rectangle(10,36,90,18));
        preferencesLabel.setText("Preferences:");
        
        appNameLabel = new Label();
        appNameLabel.setBounds(new Rectangle(10,18,90,18));
        appNameLabel.setText("App Name:");
        
        baseLabel = new Label();
        baseLabel.setBounds(new Rectangle(10,0,90,18));
        baseLabel.setText("App Base Path:");
        
        cancelButton = new Button();
        cancelButton.setBounds(new Rectangle(139, 252, 73, 20));
        cancelButton.addActionListener(this);
        cancelButton.setLabel("Cancel");
        
        okButton = new Button();
        okButton.setBounds(new Rectangle(28, 252, 73, 20));
        okButton.addActionListener(this);
        okButton.setLabel("Send");
        
        this.setLayout(null);
        this.setBounds(new Rectangle(0, 0, 240, 280));
        this.setBackground(java.awt.Color.white);        
        this.add(getTypeAppTabPnl(), null);
        this.add(okButton, null);
        this.add(cancelButton, null);
        this.add(baseLabel, null);
        this.add(appNameLabel, null);
        this.add(preferencesLabel, null);
        this.add(constrainstsLabel, null);
        this.add(getBaseFld(), null);
        this.add(getAppNameFld(), null);
        this.add(getPrefsFld(), null);
        this.add(getConstrFld(), null);
        this.add(appListButton, null);        

	}

	/**
	 * This method initializes typeAppTabPnl	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	public TabPanel getTypeAppTabPnl() {
		if (typeAppTabPanel == null) {
			typeAppTabPanel = new TabPanel();
			typeAppTabPanel.setBounds(new java.awt.Rectangle(4,70,230,180));
			typeAppTabPanel.add(getRegular());
			typeAppTabPanel.add(getBsp());
			typeAppTabPanel.add(getParametric());
			typeAppTabPanel.setTabText(new String[]{"Regular","BSP","Parametric"});
			
		}
		return typeAppTabPanel;
	}

	/**
	 * This method initializes regular	
	 * 	
	 * @return regPnl	
	 */
	public RegularPanel getRegular() {
		if (regularPanel == null) {
			regularPanel = new RegularPanel();
		}
		return regularPanel;
	}

	/**
	 * This method initializes bsp	
	 * 	
	 * @return bspPnl	
	 */
	public BspPanel getBsp() {
		if (bsp == null) {
			bsp = new BspPanel();
		}
		return bsp;
	}

	/**
	 * This method initializes parametric	
	 * 	
	 * @return paramPnl	
	 */
	public ParametricPanel getParametric() {
		if (parametric == null) {
			parametric = new ParametricPanel();
		}
		return parametric;
	}

	/**
	 * This method initializes baseFld	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public TextField getBaseFld() {
		if (baseTextField == null) {
			baseTextField = new TextField();
			baseTextField.setBounds(new Rectangle(100, 0, 103, 18));
		}
		return baseTextField;
	}

	/**
	 * This method initializes appNameFld	
	 * 	
	 * @return javax.swing.TextField	
	 */
	public TextField getAppNameFld() {
		if (appNameTextField == null) {
			appNameTextField = new TextField();
			appNameTextField.setBounds(new Rectangle(100, 18, 103, 18));
		}
		return appNameTextField;
	}

	/**
	 * This method initializes prefsFld	
	 * 	
	 * @return javax.swing.TextField	
	 */
	public TextField getPrefsFld() {
		if (preferencesTextField == null) {
			preferencesTextField = new TextField();
			preferencesTextField.setBounds(new Rectangle(100, 36, 103, 18));
		}
		return preferencesTextField;
	}

	/**
	 * This method initializes constrFld	
	 * 	
	 * @return javax.swing.TextField	
	 */
	public TextField getConstrFld() {
		if (constraintsTextField == null) {
			constraintsTextField = new TextField();
			constraintsTextField.setBounds(new Rectangle(100, 54, 103, 18));
		}
		return constraintsTextField;
	}
	
	
	public void clearAll(){
		
		getBaseFld().setText("");
		getAppNameFld().setText("");
		getPrefsFld().setText("");
		getConstrFld().setText("");
		RegularPanel reg = (RegularPanel)typeAppTabPanel.getComponent(0);
		reg.clearAll();
		BspPanel bsp = (BspPanel)typeAppTabPanel.getComponent(1);
		bsp.clearAll();
		ParametricPanel parametric = (ParametricPanel)typeAppTabPanel.getComponent(2);
		parametric.clearAll();
	}	

	
	/**
	 * @return
	 */
	private SubmitApplicationRequestMessage generateRequisition() {
		
		SubmitApplicationRequestMessage request = new SubmitApplicationRequestMessage( 
				Masct.getInstance().getUser(), "Grid Proxy", DefaultMessage.DATA, DefaultMessage.OBJECT, null );
		
		String appName = getAppNameFld().getText();
	
		
		String appExecType = getAppNameFld().getText();
		String binaryNames = new String();
		binaryNames = appExecType.substring( appExecType.indexOf("(")+1, appExecType.indexOf(")") );
		
		request.setApplicationName( appName.substring(0, appName.indexOf("(")-1 ) );
	
		request.setBasePath( getBaseFld().getText() );
	
		request.setApplicationContraints( getConstrFld().getText() );
	
		request.setApplicationPreferences( getPrefsFld().getText() );
	
		request.setBinaryNames( binaryNames );
	
		
		if( typeAppTabPanel.getSelectedTabNum() == 0 ){
			
			RegularPanel reg = (RegularPanel) typeAppTabPanel.getComponent(0);
			
			// Arquivos de Entrada
			String[] inputFiles = new String[reg.inputFiles.size()];
			for( int i=0; i<reg.inputFiles.size(); i++ ){
				
				inputFiles[i] = (String)reg.inputFiles.get(i);
			}
			
			// Arquivos de Saida
			int numOutFiles = reg.outputFiles.size();
			if( reg.getStdoutChk().getState() ) numOutFiles++;
			if( reg.getStderrChk().getState() ) numOutFiles++;
			String []outputFiles = new String[ numOutFiles ];
			
			int count = 0;
			if( reg.getStdoutChk().getState() ) outputFiles[count++] = "stdout";
			if( reg.getStderrChk().getState() ) outputFiles[count++] = "stderr";
			for( int i=0; i<reg.outputFiles.size(); i++ ){
				outputFiles[count++] = (String)reg.outputFiles.get(i);
			}
			
			request.setApplicationType( _regular );
			request.setApplicationArguments( reg.getArgFld().getText() );
			request.setInputFiles( inputFiles );
			request.setOutputFiles(outputFiles);
			
			request.setNumberOfTasks( 0 );		
			request.setParametricCopies(new ParametricCopy[0]);
			
		}
		if( typeAppTabPanel.getSelectedTabNum() == 1 ){
			
			BspPanel bsp = (BspPanel) typeAppTabPanel.getComponent(1);
			
			// Arquivos de Entrada
			String[] inputFiles = new String[bsp.inputFiles.size()];
			for( int i=0; i<bsp.inputFiles.size(); i++ ){
				
				inputFiles[i] = (String)bsp.inputFiles.get(i);
			}
			
			// Arquivos de Saida
			int numOutFiles = bsp.outputFiles.size();
			if( bsp.getStdoutChk().getState() ) numOutFiles++;
			if( bsp.getStderrChk().getState() ) numOutFiles++;
			String[] outputFiles = new String[ numOutFiles ];
			
			int count = 0;
			if( bsp.getStdoutChk().getState() ) outputFiles[count++] = "stdout";
			if( bsp.getStderrChk().getState() ) outputFiles[count++] = "stderr";
			for( int i=0; i<bsp.outputFiles.size(); i++ ){
				outputFiles[count++] = (String)bsp.outputFiles.get(i);
			}
			
			request.setApplicationType( _bsp );
			request.setApplicationArguments( bsp.getArgFld().getText() );
			request.setInputFiles( inputFiles );
			request.setOutputFiles(outputFiles);
			request.setNumberOfTasks( Integer.parseInt( bsp.getNumOfTasksFld().getText() ) );
			request.setForceDifferentMachines( bsp.getForceDiffNodesChk().getState() );
			request.setParametricCopies(new ParametricCopy[0]);
			
		}
		if( typeAppTabPanel.getSelectedTabNum() == 2 ){
			
			ParametricPanel param = (ParametricPanel) typeAppTabPanel.getComponent(2);
			
			ParametricCopy[] parametricCopies = new ParametricCopy[parametric.getApplicationCopiesPanel().copies.size()];
			for(int i=0; i<parametricCopies.length; i++){
				
				parametricCopies[i] = new ParametricCopy();
				Copy copy = (Copy) parametric.getApplicationCopiesPanel().copies.get(i);
				
				parametricCopies[i].setArguments( copy.arg );	// Copy Arguments
				
				// Copy Input Files
				String inputs[] = new String[copy.input.size()];
				for(int j=0; j<copy.input.size(); j++){
					inputs[j] = (String) copy.input.get(j);
				}
				parametricCopies[i].setInputFiles( inputs );
				
				// Copy Output Files
				int numOutFilesCopy = copy.output.size();
				if( copy.stdout ) numOutFilesCopy++;
				if( copy.stderr ) numOutFilesCopy++;
				String outputs[] = new String[ numOutFilesCopy ];
				
				int count = 0;
				if( copy.stdout ) outputs[count++] = "stdout"; 
				if( copy.stderr ) outputs[count++] = "stderr";
				for(int j=0; j<copy.output.size(); j++){
					outputs[count++] = (String)copy.output.get(j);
				}
				parametricCopies[i].setOutputFiles( outputs );
				
			}
			request.setNumberOfTasks( 0 );
			request.setApplicationType( _parametric );
			request.setParametricCopies(parametricCopies);
			request.setForceDifferentMachines( parametric.getApplicationCopiesPanel().getForceDiffNodesChk().getState() );
		}
		
		return request;
		
	}



	public void actionPerformed(ActionEvent arg0) {
		if( arg0.getSource() == appListButton ){
			
			RepositoryListRequestMessage requisition = new RepositoryListRequestMessage( 
					Masct.getInstance().getUser(), "Grid Proxy", DefaultMessage.DATA, DefaultMessage.OBJECT, null);
			
			MasctClientAPI.getInstance().send( requisition );
			
		}
		if( arg0.getSource() == okButton ){
			
			
			SubmitApplicationRequestMessage requisition = generateRequisition();
			
			MasctClientAPI.getInstance().send( requisition );
			
			/*JOptionPane.showMessageDialog(null, "The Requestion was sent!", 
					"Requestion Message", JOptionPane.INFORMATION_MESSAGE);*/
			clearAll();
			Masct.getInstance().getRemoteExecutionScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getMainMenuBar() );
			Masct.getInstance().getPanel().setVisible(true);
			
		}
		if( arg0.getSource() == cancelButton ){
			
			clearAll();
			Masct.getInstance().getRemoteExecutionScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getMainMenuBar() );
			Masct.getInstance().getPanel().setVisible(true);
		}
	}
		
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
