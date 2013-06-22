package masct.gui;

import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigurePanel extends Panel implements ActionListener {

	private Label userLabel = null;
	private Label passwordLabel = null;
	private Label proxyHostLabel = null;
	private Label proxyPortLabel = null;
	private Label masctPortLabel = null;
	private Label appExecStatusListLabel = null;
	private Label fromLabel = null;
	private Label toLabel = null;
	private Button okButton = null;
	private Button cancelButton = null;
	private TextField userTextField = null;
	private TextField passwordTextField = null;
	private TextField proxyIpTextField = null;
	private TextField proxyPortTextField = null;
	private TextField masctPortTextField = null;
	private TextField fromTextField = null;
	private TextField toTextField = null;
	
	
	/**
	 * This is the default constructor
	 */
	public ConfigurePanel() {
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
		cancelButton.setBounds(new Rectangle(125, 170, 73, 20));
		cancelButton.addActionListener(this);
		cancelButton.setLabel("Cancel");
		
		okButton = new Button();
		okButton.setBounds(new Rectangle(28, 170, 73, 20));
		okButton.addActionListener(this);
		okButton.setLabel("Ok");
		
		toLabel = new Label();
		toLabel.setBounds(new Rectangle(120, 129, 17, 18));
		toLabel.setText("to:");
		
		fromLabel = new Label();
		fromLabel.setBounds(new Rectangle(7, 128, 31, 18));
		fromLabel.setText("from:");
		
		appExecStatusListLabel = new Label();
		appExecStatusListLabel.setBounds(new Rectangle(6, 109, 232, 18));
		appExecStatusListLabel.setText("Application Execution Status Period:");
		
		proxyPortLabel = new Label();
		proxyPortLabel.setBounds(new java.awt.Rectangle(10,64,100,18));
		proxyPortLabel.setText("Proxy Port:");
		
		masctPortLabel = new Label();
		masctPortLabel.setBounds(new java.awt.Rectangle(10,82,100,18));
		masctPortLabel.setText("Masct Port:");
		
		proxyHostLabel = new Label();
		proxyHostLabel.setBounds(new java.awt.Rectangle(10,46,100,18));
		proxyHostLabel.setText("Proxy Host:");
		
		passwordLabel = new Label();
		passwordLabel.setBounds(new java.awt.Rectangle(10,28,100,18));
		passwordLabel.setText("Password:");
		
		userLabel = new Label();
		userLabel.setBounds(new java.awt.Rectangle(10,10,100,18));
		userLabel.setText("MAC Address:");
		
		this.setBackground(java.awt.Color.white);		
		this.setLayout(null);
		this.add(userLabel, null);
		this.add(passwordLabel, null);
		this.add(proxyHostLabel, null);
		this.add(proxyPortLabel, null);
		this.add(masctPortLabel, null);
		this.add(getToFld(), null);
		this.add(appExecStatusListLabel, null);
		this.add(fromLabel, null);
		this.add(toLabel, null);
		this.add(okButton, null);
		this.add(cancelButton, null);
		this.add(getUserFld(), null);
		this.add(getPassFld(), null);
		this.add(getProxyIpFld(), null);
		this.add(getProxyPortFld(), null);
		this.add(getMasctPortFld(), null);
		this.add(getFromFld(), null);		
		this.setSize(220, 240);
	}
	
	/**
	 * This method initializes userFld	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getUserFld() {
		if (userTextField == null) {
			userTextField = new TextField();
			userTextField.setBounds(new Rectangle(110, 10, 105, 18));
			userTextField.setText("user");
		}
		return userTextField;
	}

	/**
	 * This method initializes passFld	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getPassFld() {
		if (passwordTextField == null) {
			passwordTextField = new TextField();
			passwordTextField.setBounds(new Rectangle(110, 28, 105, 18));
			passwordTextField.setText("user");
		}
		return passwordTextField;
	}

	/**
	 * This method initializes proxyIpFld	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getProxyIpFld() {
		if (proxyIpTextField == null) {
			proxyIpTextField = new TextField();
			proxyIpTextField.setBounds(new Rectangle(110, 46, 105, 18));
			proxyIpTextField.setText("127.0.0.1");
		}
		return proxyIpTextField;
	}

	/**
	 * This method initializes proxyPortFld	
	 * 	
	 * @return java.awt.JTextField	
	 */
	private TextField getProxyPortFld() {
		if (proxyPortTextField == null) {
			proxyPortTextField = new TextField();
			proxyPortTextField.setBounds(new Rectangle(110, 64, 105, 18));
			proxyPortTextField.setText("55112");
		}
		return proxyPortTextField;
	}
	
	/**
	 * This method initializes proxyPortFld	
	 * 	
	 * @return java.awt.JTextField	
	 */
	private TextField getMasctPortFld() {
		if (masctPortTextField == null) {
			masctPortTextField = new TextField();
			masctPortTextField.setBounds(new Rectangle(110, 82, 105, 18));
			masctPortTextField.setText("55333");
		}
		return masctPortTextField;
	}
	

	/**
	 * This method initializes fromFld	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getFromFld() {
		if (fromTextField == null) {
			fromTextField = new TextField();
			fromTextField.setBounds(new Rectangle(39, 128, 80, 18));
			fromTextField.setText( "01/12/2006" );
		}
		return fromTextField;
	}

	/**
	 * This method initializes toFld	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getToFld() {
		if (toTextField == null) {
			toTextField = new TextField();
			toTextField.setBounds(new Rectangle(136,128, 80, 18));
		}
		return toTextField;
	}
	
	
	public void clearAll() {
		
		userTextField.setText("");
		passwordTextField.setText("");
		proxyIpTextField.setText("");
		proxyPortTextField.setText("");
		masctPortTextField.setText("");
		fromTextField.setText("");
		toTextField.setText("");
		
	}

	/**
	 * @param masctport
	 * @param user
	 * @param pass
	 * @param from
	 * @param to
	 * @param proxyport
	 * @param proxyIp
	 */
	public void setProperties(int masctport, String user,String pass,
			String from, String to, int proxyport, String proxyIp) {
		
		userTextField.setText(user);
		passwordTextField.setText(pass);
		proxyIpTextField.setText(proxyIp);
		proxyPortTextField.setText(Integer.toString(proxyport));
		masctPortTextField.setText(Integer.toString(masctport));
		fromTextField.setText(from);
		toTextField.setText(to);
	}


	public void actionPerformed(ActionEvent arg0) {
		if( arg0.getSource() == okButton ){
			
			Masct.getInstance().saveProperties(
					Integer.parseInt( getMasctPortFld().getText()), getUserFld().getText(), 
					getPassFld().getText(), getFromFld().getText(), getToFld().getText(), 
					Integer.parseInt( getProxyPortFld().getText()), getProxyIpFld().getText());
			
			clearAll();
			Masct.getInstance().getConfigureScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getMainMenuBar() );
			Masct.getInstance().getPanel().setVisible(true);
			
			
			
		}
		else if( arg0.getSource() == cancelButton ){
			
			clearAll();			
			Masct.getInstance().getConfigureScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getMainMenuBar() );
			Masct.getInstance().getPanel().setVisible(true);
			
		}		
		
	}

}
