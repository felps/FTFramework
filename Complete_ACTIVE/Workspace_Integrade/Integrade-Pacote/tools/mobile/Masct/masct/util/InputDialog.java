package masct.util;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.TextField;

public class InputDialog extends Dialog {

	private static final long serialVersionUID = 1L;
	private Label label = null;
	private TextField textFieldInput = null;
	private Button buttonOK = null;
	private Button buttonCancel = null;
	/**
	 * @param owner
	 */
	public InputDialog(Frame owner, String text) {
		super(owner,text, true);
		initialize(text);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(String text) {
		label = new Label();
		label.setBounds(new Rectangle(14, 20, 194, 21));
		label.setText(text);
		this.setLayout(null);
		this.setSize(219, 116);
		this.add(label, null);
		this.add(getTextFieldInput(), null);
		this.add(getButtonOK(), null);
		this.add(getButtonCancel(), null);
	}

	/**
	 * This method initializes textFieldInput	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTextFieldInput() {
		if (textFieldInput == null) {
			textFieldInput = new TextField();
			textFieldInput.setBounds(new Rectangle(14, 46, 194, 24));
			textFieldInput.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return textFieldInput;
	}

	/**
	 * This method initializes buttonOK	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new Button();
			buttonOK.setBounds(new Rectangle(14, 77, 73, 20));
			buttonOK.setLabel("OK");
			buttonOK.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return buttonOK;			
			
	}

	/**
	 * This method initializes buttonCancel	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getButtonCancel() {
		if (buttonCancel == null) {
			buttonCancel = new Button();
			buttonCancel.setBounds(new Rectangle(135, 75, 73, 20));
			buttonCancel.setLabel("Cancel");
			buttonCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					clearInput();
					setVisible(false);
				}
			});
		}
		return buttonCancel;
	}
	
	public String getInput() {
		return textFieldInput.getText();
	}	

	private void clearInput() {
		textFieldInput.setText("");
	}

	
}  //  @jve:decl-index=0:visual-constraint="10,15"
