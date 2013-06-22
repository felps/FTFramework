/**
 * 
 */
package asct.ui;

import java.awt.Color;
import java.awt.Component;

import asct.shared.ApplicationState;
import asct.shared.ExecutionRequestStatus;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A Cell renderer that knows how to draw ExecutionRequestStatus objects 
 * into the ExecutingApplicationsPanel.
 * @author randrade
 *
 */
public class ExecutionRequestStatusRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor
	 */
	public ExecutionRequestStatusRenderer() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		Color foreground = Color.black;

		setText(value.toString());
		if (value.getClass().getName().equals("asct.shared.ExecutionRequestStatus")){
			if (((ExecutionRequestStatus)value).getApplicationState() == ApplicationState.EXECUTING){
				foreground = Color.blue;
			}
			else if (((ExecutionRequestStatus)value).getApplicationState() == ApplicationState.TERMINATED){
				foreground = Color.red;
			}
			else if (((ExecutionRequestStatus)value).getApplicationState() == ApplicationState.FINISHED){
				foreground = Color.green;
			}
			else if (((ExecutionRequestStatus)value).getApplicationState() == ApplicationState.REFUSED){
				foreground = Color.orange;
			}
		}
    
		//else System.err.println(value.getClass().getName());
		setForeground(foreground);
		return this;
	}

}
