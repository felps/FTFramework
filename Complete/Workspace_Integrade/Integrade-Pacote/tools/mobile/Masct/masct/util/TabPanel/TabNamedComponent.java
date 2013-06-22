
package masct.util.TabPanel;

/** If you implement this interface and add an instance of your class to a
 *  TabPanel, the text that appears on the tab for that component will
 *  be determined by the getTabName() method.
 *
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
public interface TabNamedComponent {

	/** This method is called to determine the text that will appear on a tab */
	public String getTabName();
}