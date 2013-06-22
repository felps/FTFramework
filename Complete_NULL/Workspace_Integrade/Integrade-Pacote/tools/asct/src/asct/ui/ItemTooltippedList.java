package asct.ui;

import javax.swing.JList;
import javax.swing.ListModel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import asct.shared.ExecutionRequestStatus;
import asct.shared.ApplicationState;

//=============================================================================
class ItemTooltippedList extends JList {


	ItemTooltippedList(ListModel listModel) {
		super(listModel);
		setToolTipText("");
		ExecutionRequestStatusRenderer renderer = new ExecutionRequestStatusRenderer();
		setCellRenderer(renderer);
	}

	// -------------------------------------------------------------------------
	public String getToolTipText(MouseEvent e) {
      int itemIndex = locationToIndex(e.getPoint());
		if (itemIndex > -1) {
         Object currentObject = (this.getModel()).getElementAt(itemIndex);

         /* Setting a special message if the application turns orange in the Executin Applications
          * Pannel. Needed the 'instanceof' because this class is also used for the Input Files List
          * Panel. If anyone comes up with a better solution without 'instanceof', please remove it */
			if (currentObject instanceof ExecutionRequestStatus) {
            ExecutionRequestStatus mouseOverExecution = (ExecutionRequestStatus) currentObject;
            /* Verifies if the execution was refused, but also if the mouse is EXACTLY over the
             * execution name */
            if (mouseOverExecution.getApplicationState().equals(ApplicationState.REFUSED) &&
                  (this.getCellBounds(itemIndex, itemIndex)).contains(e.getPoint())) {

               return new String("Execution refused (not enough nodes found for this execution)");
            }
         }
         //ListModel lm = (ListModel) getModel();
			return new String("Right click for options");
			/*((TooltipAble) lm
					.getElementAt(locationToIndex(e.getPoint())))
					.getToolTipText();*/
		} else
			return null;
	}

}// class

