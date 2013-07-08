package masct.gui;

import java.awt.Panel;

public class ParametricPanel extends Panel {
	private ApplicationCopiesPanel applicationCopiesPanel = null;
	private CopyPanel copy = null;

	/**
	 * This is the default constructor
	 */
	public ParametricPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(null);
        this.setBounds(new java.awt.Rectangle(0,0,280,165));
        this.setBackground(java.awt.Color.white);
        this.add(getApplicationCopiesPanel(), null);
        this.add(getCopy(), null);
	}

	/**
	 * This method initializes parametricPnl	
	 * 	
	 * @return paramPnl	
	 */
	public ApplicationCopiesPanel getApplicationCopiesPanel() {
		if (applicationCopiesPanel == null) {
			applicationCopiesPanel = new ApplicationCopiesPanel();
			applicationCopiesPanel.setVisible(true);
			applicationCopiesPanel.setBounds(new java.awt.Rectangle(0,0,280,150));
		}
		return applicationCopiesPanel;
	}

	/**
	 * This method initializes copy	
	 * 	
	 * @return copyPnl	
	 */
	public CopyPanel getCopy() {
		if (copy == null) {
			copy = new CopyPanel();
			copy.setVisible(false);
			copy.setBounds(new java.awt.Rectangle(0,0,280,150));
		}
		return copy;
	}

	public void clearAll() {		
		getApplicationCopiesPanel().clearAll();
		getCopy().clearAll();
		getCopy().setNumberOfCopies(0);
	}

	
}