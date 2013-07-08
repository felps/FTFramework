package asct.ui;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * A login screen to AsctGui
 */
public class AsctLoginDialog extends JDialog  {

	private static final long serialVersionUID = 1L;
	private JPanel contentPanel = null;
	private JLabel nameLabel = null;
	private JTextField loginField = null;
	private JLabel passwordLabel = null;
	private JPasswordField passwordField = null;
	private JButton okButton = null;
	private JButton calcelButton = null;
    String name = "";
    String password = "";
	private JLabel imageLabel = null;
	/**
	 * This method initializes login	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getLogin() {
		if (loginField == null) {
			loginField = new JTextField();
			loginField.setLocation(123, 98);
			loginField.setText("");
			loginField.setSize(122, 19);
		}
		return loginField;
	}

	/**
	 * This method initializes passwordField	
	 * 	
	 * @return javax.swing.JPasswordField	
	 */    
	private JPasswordField getPasswordField() {
		if (passwordField == null) {
			passwordField = new JPasswordField();
			passwordField.setLocation(123, 128);
			passwordField.setText("");
			passwordField.setSize(122, 19);
		}
		return passwordField;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setSelected(true);
			okButton.setBounds(30, 159, 87, 29);
			okButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					name = loginField.getText()+"/ARSC";
					password = new String(passwordField.getPassword()); 
					setVisible(false);
				}
			});
			okButton.setText("OK");
		}
		return okButton;
	}

	/**
	 * This method initializes calcelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getCalcelButton() {
		if (calcelButton == null) {
			calcelButton = new JButton();
			calcelButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					System.exit(0);
				}
			});
			calcelButton.setText("Cancel");
			calcelButton.setBounds(158, 159, 87, 29);
		}
		return calcelButton;
	}
	
	/**
	 * This is the default constructor
	 */
	public AsctLoginDialog() {
		super((JFrame)null,true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(276, 227);
		this.setResizable(false);
		this.setName("arscLoginFrame");
		this.setContentPane(getJContentPane());
		this.setTitle("InteGrade");
		this.setModal(true);
		this.setVisible(true);
	}

	/**
	 * This method initializes contentPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (contentPanel == null) {
			imageLabel = new JLabel();
			imageLabel.setBounds(35, 9, 203, 81);
			imageLabel.setIcon(new ImageIcon("images/logon.jpg"));
			imageLabel.setText("");
			passwordLabel = new JLabel();
			passwordLabel.setBounds(30, 127, 76, 20);
			passwordLabel.setText("Password:");
			nameLabel = new JLabel();
			nameLabel.setBounds(30, 95, 76, 24);
			nameLabel.setText("Login:");
			contentPanel = new JPanel();
			contentPanel.setLayout(null);
			contentPanel.add(imageLabel, null);
			contentPanel.add(getCalcelButton(), null);
			contentPanel.add(getOkButton(), null);
			contentPanel.add(nameLabel, null);
			contentPanel.add(getLogin(), null);
			contentPanel.add(passwordLabel, null);
			contentPanel.add(getPasswordField(), null);
			getRootPane().setDefaultButton(okButton);
		}
		return contentPanel;
	}
	public void resetPassword()
	{
		passwordField.setText("");
		setVisible(true);
	}

	public String getUsername(){
		return getLogin().getText();
	}
	
	public String getPassword(){
		return String.valueOf(getPasswordField().getPassword());
	}
	

}  //  @jve:decl-index=0:visual-constraint="12,8"
