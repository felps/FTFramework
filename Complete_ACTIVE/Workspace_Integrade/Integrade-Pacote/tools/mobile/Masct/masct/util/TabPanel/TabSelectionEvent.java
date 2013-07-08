
package masct.util.TabPanel;

/**
 *  <p>Use this code at your own risk!  MageLang Institute is not
 *  responsible for any damage caused directly or indirectly through
 *  use of this code.
 *  <p><p>
 *  <b>SOFTWARE RIGHTS</b>
 *  <p>
 *  TabSplitter, version 2.0, Scott Stanchfield, MageLang Institute
 *  <p>
 *  We reserve no legal rights to this code--it is fully in the
 *  public domain. An individual or company may do whatever
 *  they wish with source code distributed with it, including
 *  including the incorporation of it into commerical software.
 *
 *  <p>However, this code <i>cannot</i> be sold as a standalone product.
 *  <p>
 *  We encourage users to develop software with this code. However,
 *  we do ask that credit is given to us for developing it
 *  By "credit", we mean that if you use these components or
 *  incorporate any source code into one of your programs
 *  (commercial product, research project, or otherwise) that
 *  you acknowledge this fact somewhere in the documentation,
 *  research report, etc... If you like these components and have
 *  developed a nice tool with the output, please mention that
 *  you developed it using these components. In addition, we ask that
 *  the headers remain intact in our source code. As long as these
 *  guidelines are kept, we expect to continue enhancing this
 *  system and expect to make other tools available as they are
 *  completed.
 *  <p>
 *  The MageLang Support Classes Gang:
 *  @version TabSplitter 2.0, MageLang Insitute, Jan 18, 1998
 *  @author <a href="http:www.scruz.net/~thetick">Scott Stanchfield</a>, <a href=http://www.MageLang.com>MageLang Institute</a>
 */
import java.util.EventObject;
import java.awt.Component;

public class TabSelectionEvent extends EventObject {
	private Object visibleComponent;
	private int    physicalTab;
	private String selectedName;
	private int    visibleComponentNum[];

/**
 * TabSelectionEvent constructor comment.
 * @param source java.lang.Object
 */
public TabSelectionEvent(Object source, Object visibleComponent,
	                       int physicalTab, String selectedName,
	                       int visibleComponentNum[]) {
	super(source);
	this.visibleComponent = visibleComponent;
	this.physicalTab = physicalTab;
	this.selectedName = selectedName;
	this.visibleComponentNum = visibleComponentNum;
}
	public int getPhysicalTab() {
		return physicalTab;
	}	
	public String getSelectedName() {
		return selectedName;
	}	
	public Object getVisibleComponent() {
		return visibleComponent;
	}	
	public String toString() {
		String lineSep = System.getProperty("line.separator");
		
		String nums="";
		for(int i=0;i<visibleComponentNum.length;i++) {
			if (i == 0)
				nums += visibleComponentNum[i];
			else
				nums += "," + visibleComponentNum[i];
		}	

		String comps="";
		if (visibleComponent instanceof Component)
			comps = visibleComponent.toString();
		else {
			for(int i=0;i<((Object[])visibleComponent).length;i++) {
				if (i == 0)
					comps += ((Object[])visibleComponent)[i];
				else
					comps += "," + lineSep + ((Object[])visibleComponent)[i];
			}	
			comps = "{" + comps + "}";
		}	
		
		return getClass().getName()+" [" + physicalTab +
		       ", \""+selectedName+"\", {"+nums+"}, "+
		       lineSep + comps +"]";
	}	
	public int[] visibleComponentNum() {
		return visibleComponentNum;
	}	
}