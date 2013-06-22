
package masct.util.TabPanel;

import java.awt.Panel; 
import java.awt.PopupMenu; 
import java.awt.MenuItem; 
import java.awt.Graphics; 
import java.awt.Color; 
import java.awt.Dimension; 
import java.awt.FontMetrics; 
import java.awt.Font; 
import java.awt.Component; 
import java.awt.Polygon; 
import java.util.Vector; 
import java.util.Hashtable; 
import java.awt.Insets; 
import java.awt.event.MouseEvent; 
import java.awt.event.MouseListener; 
import java.awt.event.ActionListener; 
import java.awt.event.ActionEvent; 
import java.awt.CardLayout; 
import java.awt.Container; 
import java.awt.Image; 
import java.awt.Rectangle; 
/**
 * TabPanel -- allows a user to select from several interface components
 * by clicking a tab at the top of the panel.
 *
 * Note that this class is only dependent on the JDK; no other class
 *   libraries or files are necessary
 *
 * <p>Each contained component is represented by a tab at the top of the
 * TabPanel, much like file folders in a file cabinet.  When a
 * tab is clicked, it becomes the "selected" tab and its associated
 * component will be displayed.
 * <p>There are two types of navigational aids provided with the
 * TabPanel.  If there are more tabs than can be displayed in the
 * current window, two triangle buttons will appear.  These buttons
 * will scroll the set of tabs left and right.
 * <p>There are also two buttons marked "+" and "-".  These
 * buttons move the user through each tab in succession.
 * <p>To properly set up a tab panel, you need to do two things:
 * <ul>
 *    <li>add components to the TabPanel, using an "add" method.
 *        <br>The order in which panels are added is the 
 *            order in which their tabs will appear.
 *    <li>set a tabText string array to represent the text that
 *        is displayed on each tab.
 * </ul>
 *
 * <p><b>Note:</b> It is extremely important that the user of this
 * tab panel not try to directly use the layoutmanger (via getLayout()
 * and setLayout() ).  These two methods could not be overridden
 * to prevent modification, as many GUI builders expect to use it.
 * If you want to switch between tabs, use the "next()" and "previous()"
 * methods provided by TabSplitter, <i>not</i> those of CardLayout.
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
 * @see SplitterLayout
 */
public class TabPanel extends Panel implements ActionListener, MouseListener {
	// The color of the border around the entire TabSplitter
	private Color borderColor = null;
	
	// The rectangle containing the +/-
	private Rectangle bothRect;
	
	// The number of the leftmost/rightmost visible tabs
	private int firstVisible = 0;
	private int lastVisible  = 0;
	
	// USed for double-buffering
	private transient Graphics g1;
	private transient Image image;
	
	// The amount of overlap in the tabs
	private int hslop = 4;

	// The popup menu	
	private PopupMenu popupMenu = null;
	// Used to determine if we need to reallocate the offscreen buffer
	private int lastH = 0;
	private int lastW = 0;
	
	// The directional arrows
	protected transient Polygon leftArrow;
	protected transient Polygon rightArrow;

	// The number of the currently-selected tab
	private int selected = 0;
	
	// The background color behind the tabs
	private Color tabBackground = null;
	
	// The colors to paint the tabs
	private Color[] tabColors = null;
	
	// The tab polygons
	private Vector tabs;
	
	// The text to display in the tabs
	private String[] tabText = null;
	
	// Are there more tabs than will fit?
	private boolean tooManyTabs = false;
	
	private int vslop = 4;
	
	// The menu items used to display the names of the tabs on the popup menu
	private MenuItem tabMenuItems[];
	
	private Font currentFont;
	private Font boldFont;
	private FontMetrics fm;
	private FontMetrics boldfm;
	private int h;
	protected boolean leftEnabled=false;
	protected boolean rightEnabled=false;
	
	private Hashtable explicitTabText = new Hashtable();
	protected transient java.util.Vector aTabSelectionListener = null;

	/** Constructor for the TabPanel */
	public TabPanel() {
		super();
		addMouseListener(this);
		tabs = new Vector();
		setBackground(Color.lightGray);
		setLayout(new CardLayout());
		popupMenu = new PopupMenu("TabPanel");
		add(popupMenu);
	}

	/** Handle the popup menu item selections 
	 *  @param e (java.awt.event.ActionEvent) -- the event that was fired to us
	 */
	public void actionPerformed(ActionEvent e) {
		// walk through the list of menu items
		// if the action came from one of them, show the
		//    corresponding tab component
		for(int i = tabMenuItems.length-1; i > -1; i--)
			if (e.getSource() == tabMenuItems[i]) {
				showPhysicalTab(i);
				break;
			}	
	}

	/** Adds a component to the TabPanel
 	 * @param comp (Component) -- the component to be added
 	 * @param constraints (Object) -- constraints on the component
 	 * @param index (int) -- at which position will the component be added (-1 means at end)
 	 */
	protected void addImpl(Component comp, Object constraints, int index) {
		// if no constraints were passed in, use getName()
		if (constraints == null)
			constraints = comp.getName();
		
		// The constraint must be a String
		else if (!(constraints instanceof String))
			throw new IllegalArgumentException("Constraint for add must be a String");

		// If we have a String constaint _other_than_ the component's getName(),
		//   save it as an explicit tab text and use getName() as the constraint
		else if (constraints != comp.getName()) {
			explicitTabText.put(comp, constraints);
			constraints = comp.getName();
		}	
	
		// Add the component to the panel	
		super.addImpl(comp, constraints, index);
	
		// if the bean is being displayed for a designer, force a repaint()
		if (java.beans.Beans.isDesignTime())
			repaint();
	}

	/** Add a listener who cares about tab selections
	 *  @param newListener (TabSelectionListener) -- the component who cares...
	 */
	public void addTabSelectionListener(TabSelectionListener newListener) {
		// lazy instantiation of the listener list...
		if (aTabSelectionListener == null) {
			aTabSelectionListener = new java.util.Vector();
		};
		
		// add the sucker to the list of people to inform
		aTabSelectionListener.addElement(newListener);
	}

	/** Determines which text will be displayed in the tabs of the Tab panel.
	 *  The text for each tab is determined as follows:
	 *  <pre>
	 *     If an explicit text were passed to add(), that text is used
	 *       (note if the text happens to have the same value as that component's
	 *        getName() call, it is not considered an explicit text)
	 *     else if the component implements TabNamedComponent
	 *       call its getTabName() method to determine the text
	 *     else if the tabText[] propery was set and the
	 *       text for that component is non-null
	 *       use the tabText[tab number]
	 *     else
	 *       call getName() as a "last resort"
	 *  </pre>
	 *
	 * <p>Note that this method should be used to figure out the tab text, <em>not</em>
	 *    <tt>getTabText()</tt>, as <tt>getTabText()</tt> only gets the 
	 *    <tt>tabText[]</tt> property.
	 *
	 * @return An array of Strings that will appear on the tabs.
	 * @see #getTabText
	 * @see #setTabText
	 */
	public String[] determineTabText() {
		int numComponents = getComponentCount();
	
		// make a new array and assign values for undefined components
		String names[] = new String[numComponents];
		Component comps[] = getComponents();
		for(int i=0;i < numComponents; i++) {
			names[i] = (String)explicitTabText.get(comps[i]);
			if (names[i] == null && comps[i] instanceof TabNamedComponent)
				names[i] = ((TabNamedComponent)comps[i]).getTabName();
			if (names[i] == null && tabText != null) {
				int pos = getPosition(comps[i]);
				if (pos < tabText.length)
					names[i] = tabText[pos];
			}	
			if (names[i] == null)
				names[i] = comps[i].getName();
		}	
		return names;
	}

	protected void determineVisible() {
		if (fm == null) return; // hasn't been painted yet...
		int startOfBothRect = getSize().width-26;
		
		String tabText[] = determineTabText();

		Component comp[] = getComponents();
		int compCount = getComponentCount();

		lastVisible = compCount-1; // assume they all fit...
		tooManyTabs = (firstVisible != 0);

		int tempXOff = 8;
		for(int num=firstVisible; num<compCount; num++) {
			String text = tabText[num];		
			int textWidth = (num==selected)?
			                    boldfm.stringWidth(text) :
			                    fm.stringWidth(text);

			tempXOff += 28 + (hslop*2) + textWidth;		
		
			if (tempXOff > startOfBothRect) {
				rightEnabled = true;
				tooManyTabs = true;
				if (num > firstVisible)
					lastVisible = num-1;
				else
					lastVisible = num;
				break;
			}	
			tempXOff -= 14;
		}
	}
	
	/** Draws the tabs at the top of the tab panel
	 *  @param g (Graphics) -- the graphics context into which tabs are drawn
	 */
	protected void drawTabs(Graphics g) {
		// Determine the text to put in the tabs
		String tabText[] = determineTabText();
		
		rightEnabled = false;      // assume we don't need the right arrow
		Dimension dim = getSize(); // get the size of the panel
		setupTabPolygons();        // create the polygons for tab drawing/location test
		
		// get a list of all the panels contained in the TabSplitter	
		Component comp[] = getComponents();
		int compCount = getComponentCount();
		if (compCount == 0) return; // if none, just get out
//		lastVisible = compCount-1; // assume they all fit...
	
		// if the first visible tab is not the first tab,
		//   force us to use the left/right arrows
//		tooManyTabs = (firstVisible != 0);
	
		// for each tab that is visible on the screen	
		Color tabColors[] = getTabColors();
		for(int num=compCount-1;num>firstVisible-2;num--) {
			int tabNum;
			// draw the selected tab _last_ so it will appear on top
			if (num == firstVisible-1) {
				if (selected < firstVisible) break;
				tabNum = selected;
				g.setFont(boldFont);
				}
			else if (num == selected)	continue;
			else tabNum = num;

			// choose the color for the tab
			Color tabColor;
			if (tabColors == null || tabColors.length == 0 ||
			    tabColors[tabNum % tabColors.length]==null)
				tabColor = Color.lightGray;  // default tab color
			else
				tabColor = tabColors[tabNum % tabColors.length];
		
			g.setColor(tabColor);
			
			// draw the polygon
			Polygon p = (Polygon)tabs.elementAt(tabNum-firstVisible);
			g.fillPolygon(p);
		
			//  draw hilites around that polygon
			int x[] = p.xpoints;
			int y[] = p.ypoints;
			g.drawLine(x[0],y[0],x[11],y[11]);
				
			g.setColor(tabColor.darker());
			int i;
			for(i=10;i>7;i--)
				g.drawLine(x[i],y[i],x[i+1],y[i+1]);
			g.setColor(tabColor.brighter());
			for(i=0;i<5;i++) {
				g.drawLine(x[i],y[i],x[i+1],y[i+1]);
				if (tabNum == selected)
					g.drawLine(x[11-i]-1,y[11-i],x[11-i-1]-1,y[11-i-1]);
				}
			g.drawLine(x[5],y[5],x[6],y[6]);
			g.drawLine(x[6],y[6],x[7],y[7]);
			g.drawLine(x[7],y[7],x[8],y[8]);
			if (tabNum == selected) {
				g.drawLine(x[0],y[0],2,y[0]);
				Dimension d = getSize();
				g.drawLine(x[11]-((tabNum==selected)?1:0),y[0],d.width-4,y[0]);
				g.setColor(tabColor);
			}
	
			// write the text for the tab
			g.setColor(getForeground());
			String text;
			text = tabText[tabNum];
			g.drawString(text, x[5]+hslop, y[0] - (fm.getDescent() + fm.getLeading()/2));
			
			// if the tab extends past the last place we can draw
			//   set tooManyTabs
//			if (x[11] > bothRect.x) {
//				rightEnabled = true;
//				tooManyTabs = true;
//				if (tabNum > firstVisible)
//					lastVisible = tabNum-1;
//				else
//					lastVisible = tabNum;
//			}	
		}
		g.setFont(currentFont);

		if (tooManyTabs) {
			// blank out area used for right arrow
			g.setColor(getBorderColor());
			g.fillRect(bothRect.x-13, 0, dim.width-bothRect.x+13, h+8);
		
			// draw arrows
			g.setColor(getTabBackground());
			leftEnabled = (firstVisible > 0);
			if (leftEnabled)
				g.fillPolygon(leftArrow);
			if (rightEnabled)
				g.fillPolygon(rightArrow);
			g.setColor(getTabBackground().brighter());
			if (leftEnabled)
				g.drawPolygon(leftArrow);
			if (rightEnabled)
				g.drawPolygon(rightArrow);
		}	
		else {
			// blank out area used for right arrow
			g.setColor(getBorderColor());
			g.fillRect(bothRect.x-2, 0, dim.width-bothRect.x+2, h+8);
		}	
	
		// draw the +/- for moving through tabs
		g.setColor(getTabBackground());
		g.fill3DRect(bothRect.x, bothRect.y, bothRect.width-1, bothRect.height-1, true);
		
		g.setColor(getTabBackground().brighter());
		g.drawLine(bothRect.x + 3, bothRect.y + 3, bothRect.x + bothRect.width-6, bothRect.y + bothRect.width-6);
		
		g.setColor(getForeground());
		int w2  = bothRect.width/2;
		int w4  = bothRect.width/4;
		int w8  = bothRect.width/8;
		
		g.drawLine(bothRect.x + w2,    bothRect.y + w4,
				 bothRect.x + w2+w4, bothRect.y + w4);
		g.drawLine(bothRect.x + w2+w8, bothRect.y + w8,
		           bothRect.x + w2+w8, bothRect.y + w4+w8);
		g.drawLine(bothRect.x + w8,    bothRect.y + w2+w8,
		           bothRect.x + w4+w8, bothRect.y + w2+w8);
	}

	/** let those who care know when a tab was selected 
	 *  @param e (TabSelectionEvent) -- the event to pass to the listeners
	 *  @see #addTabSelectionListener
	 *  @see #removeTabSelectionListener
	 */
	protected void fireTabSelected(TabSelectionEvent e) {
		if (aTabSelectionListener == null) return;  // noone cares, so get out

		// walk through the list and call the tabSelected methods
		int currentSize = aTabSelectionListener.size();
		for (int index = 0; index < currentSize; index++){
			TabSelectionListener l =
				(TabSelectionListener)aTabSelectionListener.elementAt(index);
			if (l != null)
				l.tabSelected(e);
		};
	}

	/**
	 * Gets the borderColor property (java.awt.Color) value.
	 * The borderColor is the color to paint behind the tabs and around
	 *   the edge of the panel display area.
	 * @return The borderColor property value.
	 * @see #setBorderColor
	 */
	public Color getBorderColor() {
		/* Returns the borderColor property value. */
		if (borderColor == null)  // use gray as the default
			borderColor = Color.gray;
		return borderColor;
	}

	protected String getExplicitTabText(Component c) {
		return (String)explicitTabText.get(c);
	}	
	/** Get the number of the first-visible physical tab.
	 *  @return The number of the tab that is visible.
	 *  @see #setFirstVisible
	 */
	public int getFirstVisible() {
		return firstVisible;
	}	

	/** Returns the insets of the TabPanel. The insets indicate the size of
	 *  the border of the container.
	 *  @see java.awt.LayoutManager
	 */
	public Insets getInsets() {
		FontMetrics fm = getFontMetrics(getFont());
		return new Insets(fm.getHeight()+12,6,6,6);
	}

	/* Gets the popup menu.  This is provided so the subclass TabSplitter
	 *   can add its own menu items to the popup menu.
	 * @return The popup menu for the tab panel
	 */
	protected PopupMenu getPopupMenu() {
		return popupMenu;
	}		

	/** Return the position of the component in the tabsplitter
	 *  (its physical tab number)
	 *  @param comp (Component) -- the component to search for.
	 *  @return The position of the component (-1 means not found)
	 */
	protected int getPosition(Object comp) {
		int count = getComponentCount();
		Component comps[] = getComponents();
		int i = 0;
		while(i < count && comps[i] != comp) i++;
		if (i == count) i = -1;
		return i;
	}	

	/** Get the tab text of the currently-selected component 
	 *  @return A String containing the currently-selected tab's text
	 */
	public String getSelectedName() {
		return determineTabText()[selected];
	}	

	/** Get the number of the currently-selected tab 
	 *  @return an int for the currently-selected tab
	 *  @see #setSelectedTabNum
	 */
	public int getSelectedTabNum() {
		return selected;
	}	

	/** Gets the color that's behind the tabs
	 *  @return The color behind the tabs
	 *  @see #setTabBackground
	 */
	public Color getTabBackground() {
		if (tabBackground == null)
			tabBackground = Color.lightGray;
		return tabBackground;
	}	

	/** Get the color to use when drawing the tabs 
	 *  @deprecated the new tabColors property should be used instead.
	 *  @see #getTabColors
	 */
	public Color getTabColor() {
		return getTabColors(0);
	}	
	
	/** Get the colors used to draw the tabs.  These colors are cycled through all
	 *  tabs that are painted.
	 *  @return The tabColors property value.
	 *  @see #setTabColors
	 */
	public Color[] getTabColors() {
		// lazy instantiation of the tab colors property
		if (tabColors == null)
			return new Color[] {null};
		return tabColors;
	}

	/** get a specific tab color.
	 * @return The tabColor property value.
	 * @param index The index value into the property array.
	 * @see #getTabColors
	 * @see #setTabColors
	 */
	public Color getTabColors(int index) {
		/* Returns the tabColors index property value. */
		if (tabColors == null)
			return null;
	
		return tabColors[index % tabColors.length];
	}

	/** Gets the explicitly-set tab text array.
	 *  Note that this method should not be used to see the text
	 *    displayed on the tabs; instead, <tt>determineTabText()</tt> should be used.
	 *  @return The tabText property value.
	 *  @see #setTabText
	 *  @see #determineTabText
	 */
	public String[] getTabText() {
		/* Returns the tabText property value. */
		return tabText;
	}

	/** Gets a specific string from the explicitly-set tab text array.
	 *  Note that this method should not be used to see the text
	 *    displayed on the tabs; instead, <tt>determineTabText()</tt> should be used.
	 *  @return The tabText property value.
	 *  @see #setTabText
	 *  @see #determineTabText
	 */
	public String getTabText(int index) {
		/* Returns the tabText index property value. */
		return getTabText()[index];
	}

	/** Get the component that is currently visible
	 *  @return the component that is on the currently-selected tab (note that this
	 *  method returns an Object because its subclass returns an array of visible comps)
	 */
	public Object getVisibleComponent() {
		return getComponent(selected);
	}	

	/** Return an array containing the selected tab number.  (It's done this way
	 *  to provide a consistent interface for TabSplitter.)
	 *  If you are using TabPanel and just want the int, use getSelectedTabNum()
	 *  @return an array containing the selected tab number.
	 */
	public int[] getVisibleComponentNum() {
		return new int[] {selected};
	}	

	/** A convenience method so TabSplitter can add merge capability
	 *  @param n (int) -- the target tab or a merge or tab to show
	 */
	protected void mergeOrShow(int n) {
		showPhysicalTab(n);
	}	

	/** Dummy method for the MouseListener interface... */
	public void mouseClicked(MouseEvent e) {	}
	
	/** Dummy method for the MouseListener interface... */
	public void mouseEntered(MouseEvent e) {	}
	
	/** Dummy method for the MouseListener interface... */
	public void mouseExited(MouseEvent e) {	}
	
	/** handle the mouse button being pressed -- check for right mouse
	 *  click to bring up popup menu
	 */
	public void mousePressed(MouseEvent e) {
		if (((e.getSource() == this) && e.isMetaDown()) )
			showPopup(e);
	}
	
	/** When the mouse is released, check for tab selection
	 * @param e (MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		selectTab(e);
	}
	
	/** Move to next tab, wrapping around to first tab if necessary. */
	public void next() {
		selected = ++selected % getComponentCount();
		showPhysicalTab(selected);
	}
	
	/** Paints the tab panel image (tabs, border, background)
	 * @param g (Graphics) The graphics context into which the tab panel is painted
	 */
	public void paint(Graphics g) {
		if (currentFont != getFont()) {
			currentFont = getFont();
			boldFont = new Font(currentFont.getName(), Font.BOLD, currentFont.getSize());
			fm     = getFontMetrics(currentFont);
			boldfm = getFontMetrics(boldFont);
			h  = fm.getHeight();
		}	

		Dimension d = getSize();
		if ((d.width != lastW) || (d.height != lastH) || (image == null)) {
			// size has changed, must resize image
			image = createImage(d.width, d.height);
			if (g1 != null) g1.dispose();
			g1 = image.getGraphics();

			lastW = d.width;
			lastH = d.height;
			}

		// draw the borders
		g1.setColor(getBorderColor());
		g1.fillRect(0,0,d.width,d.height);
		g1.setColor(getBackground());
		g1.fill3DRect(2,h+8,d.width-4,d.height-h-10,true);
		g1.setColor(getTabBackground());
		g1.draw3DRect(4,h+10,d.width-8,d.height-h-14,false);

		drawTabs(g1);

		g.drawImage(image,0,0,this);
		super.paint(g);
	}
	
	/** Move to previous tab, wrapping around to last tab if necessary. */
	public void previous() {
		if (--selected < 0) selected = getComponentCount()-1;
		showPhysicalTab(selected);
	}
	
	/** Remove a component from the container.
	 * @param index (int) Which component to remove.
	 */
	public void remove(int index) {
		if (index >= getComponentCount())
			throw new IllegalArgumentException("Not that many tabs");
		removeBody(getComponent(index), index);
	}
	
	/** Remove a component from the container.
	 * @param comp (Component) Which component to remove.
	 */
	public void remove(Component comp) {
		// figure out which tab contains the text
		Component c[] = getComponents();
		int num;
		for(num = 0; num < c.length && c[num] != comp; num++);
		if (num < c.length)
			removeBody(comp, num);
	}
	
	/** Remove all components from the container.
	 *  This method is overridden to force a repaint in design mode.
	 */
	public void removeAll() {
		super.removeAll();
		explicitTabText = new Hashtable();
		invalidate();
		validate();
		repaint();
		selected = 0;
	}
	
	/** Remove a component from the container.
	 * @param index (int) Which component to remove.
	 */
	private void removeBody(Component c, int index) {
		explicitTabText.remove(c);
		super.remove(index);
		
		if (selected >= index && selected > 0)
			selected--;
			
		if (selected > -1 && getComponentCount() > 0)
			showPhysicalTab(selected);
		invalidate();
		validate();
		repaint();
	}
	
	/** Remove a TabSelectionListener.
	 *  @param newListener com.magelang.tabsplitter.TabSelectionListener
	 *  @see #addTabSelectionListener
	 */
	public void removeTabSelectionListener(TabSelectionListener newListener) {
		if (aTabSelectionListener != null) {
			aTabSelectionListener.removeElement(newListener);
		};
	}
	
	/**
	 * Check to see if the user clicked on a tab or one of the
	 * tab panel navigation buttons.
	 * @param e (java.awt.event.MouseEvent) The mouse click.
	 */
	protected void selectTab(MouseEvent e) {
		// walk through the tab polygons to see if we have a hit...
		// first check if it's the "plus" or "minus"
		int count = getComponentCount();
		if (count > 0 && bothRect.contains(e.getX(), e.getY())) {
			if (e.getX() - bothRect.x > e.getY() - bothRect.y) // plus is upper right corner
				next();
			else // minus is the rest of the box
				previous();
		}	

		// if the user clicked on the right arrow, shift the tabs to the right
		else if (rightEnabled && tooManyTabs && rightArrow.contains(e.getX(), e.getY()))
			shiftRight();
	
		// if the user clicked on the left arrow, shift the tabs to the left
		else if (leftEnabled && tooManyTabs && leftArrow.contains(e.getX(), e.getY()))
			shiftLeft();

		// now walk through the rest of the tabs and see if the user clicked in one of them
		// the extra check is to filter out tests for the currently-selected tab
		else
			// for each tab starting at the first visible tab, moving right
			if (count > 0)
				for(int i = ((selected>=firstVisible)?firstVisible-1:firstVisible); i < count; i++) {
					int n = (i==firstVisible-1?selected:i);
					if (tabContains(n-firstVisible, e.getX(), e.getY())) {
						// select the target tab
						mergeOrShow(n);
						break;
					}	
				}	
	}
	
	/**
	 * Sets the borderColor property (java.awt.Color) value.
	 * The borderColor is the color to paint behind the tabs and around
	 *   the edge of the panel display area. 
	 * @param borderColor The new value for the property.
	 * @see #getBorderColor
	 */
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		invalidate();
	}
	
	protected void setExplicitTabText(Component c, String text) {
		explicitTabText.put(c,text);
	}	
	/** Explicitly sets which tab is the first to be visible 
	 *  @param value (int) the number for the first tab
	 */
	public void setFirstVisible(int value) {
		if (value >= getComponentCount())
			throw new IllegalArgumentException("Not that many tabs");
		firstVisible = value;
	}	
	
	/** set the font to use on the tabs
	 *  @param f (Font) -- the font to use when writing tab text
	 */
	public void setFont(Font f) {
		super.setFont(f);
		invalidate();
		// the following two lines are a hack to get the box to redraw properly
		// I'll figure out a better way sometime...
		((CardLayout)getLayout()).next(this);
		((CardLayout)getLayout()).previous(this);
		repaint();
	}
	
	/** Explicitly pick the selected tab by number.  Note that the number refers to 
	 *  the current tabs visible on the screen 
	 *  @param num int -- the tab number to select
	 *  @see #getSelectedTabNum
	 */
	public void setSelectedTabNum(int num) {
		if (num > (getComponentCount()-1))
			throw new IllegalArgumentException("Tab number greater than number of components");
		showPhysicalTab(num);
	}	
	
	/** set the color to use behind the tabs
	 *  @param color (Color) the color to draw behind the tabs
	 */
	public void setTabBackground(Color color) {
		tabBackground = color;	
		invalidate();
	}	
	
	/** Set the color to use when drawing the tabs 
	 *  @deprecated the new tabColors property should be used instead.
	 *  @see #setTabColors
	 */
	public void setTabColor(Color color) {
		setTabColors(0,color);
	}	
	
	/** Sets the colors to use when drawing the tabs.  This an an array of
	 *  colors that will be cycled through, ie, if there are more tabs than colors,
	 *  the next tab will reuse the first color.
	 *  @param tabColors The new value for the property.
	 *  @see #getTabColors
	 */
	public void setTabColors(Color[] tabColors) {
		if (tabColors == null)
			tabColors = new Color[] {null};
		this.tabColors = tabColors;
		invalidate();
		if (java.beans.Beans.isDesignTime())
			repaint();
	}
	
	/** Sets the colors to use when drawing the tabs.  This an an array of
	 *  colors that will be cycled through, ie, if there are more tabs than colors,
	 *  the next tab will reuse the first color.
	 *  @param tabColors The new value for the property.
	 *  @see #getTabColors
	 */
	public void setTabColors(int index, java.awt.Color tabColor) {
		if (index > (tabColors.length-1)) {
			Color newColors[] = new Color[index+1];
			System.arraycopy(tabColors, 0, newColors, 0, tabColors.length);
			for(int i = tabColors.length; i < index; i++)
				newColors[i] = Color.lightGray;
			tabColors = newColors;
		}	
		tabColors[index] = tabColor;
		invalidate();
		if (java.beans.Beans.isDesignTime())
			repaint();
	}
	
	/** Sets the explicit tab text to use when drawing the tabs.
	 *  @param tabText The new value for the property.
	 *  @see #getTabText
	 *  @see #determineTabText
	 */
	public void setTabText(String[] tabText) {
		this.tabText = tabText;
		return;
	}
	
	/** Sets the explicit tab text to use when drawing the tabs.
	 *  @param index (int) the specific text to set
	 *  @param tabText The new value for the property.
	 *  @see #getTabText
	 *  @see #determineTabText
	 */
	public void setTabText(int index, String tabText) {
		if (this.tabText == null) // give a little extra room...
			this.tabText = new String[index+10];
		else if(index >= this.tabText.length) {
			// reallocate
			String newTabText[] = new String[index + 10];
			System.arraycopy(this.tabText, 0, newTabText, 0, this.tabText.length);
			this.tabText = newTabText;
		}	
		this.tabText[index] = tabText;
	}
	
	/** Creates a Polygon object for each tab.  This polygon object
	 *  is used to draw the tab and to test if the user has clicked
	 *  the mouse within that tab.   The navigation buttons are also
	 *  set up in this method.
	 * @param g (java.awt.Graphics) The graphics context into which the tabs will be drawn
	 */
	protected void setupTabPolygons() {
		Dimension dim = getSize();
		int x[] = new int[6];
		int y[] = new int[12];

		bothRect  = new Rectangle(dim.width-26, 2, 24, 6+h);
		x[0] = 2;  x[1] = 11; x[2] = 11;
		y[0] = h/2+4; y[1] = h/2;  y[2] = h/2+8;
		leftArrow = new Polygon(x,y, 3);
		x[0] = bothRect.x - x[0];
		x[1] = bothRect.x - x[1];
		x[2] = bothRect.x - x[2];
		rightArrow = new Polygon(x,y, 3);
	
		int i;
		x[0]=0;   x[1]=3;   x[2]=4;   x[3]=9; x[4]=11; x[5]=14;
		y[0]=8+h; y[1]=6+h; y[2]=5+h; y[3]=5; y[4]=4;  y[5]=2;

		for(i=0; i<6; i++) y[11-i] = y[i];

		int x1[] = new int[12];

		String tabText[] = determineTabText();
				
		int tempXOff = 8;
		Component comp[] = getComponents();
		int compCount = getComponentCount();

		lastVisible = compCount-1; // assume they all fit...
		tooManyTabs = (firstVisible != 0);

		tabs = new Vector();
		for(int num=firstVisible; num<compCount; num++) {
			String text = tabText[num];		
			int textWidth = (num==selected)?
			                    boldfm.stringWidth(text) :
			                    fm.stringWidth(text);
			
			for(i=0; i<6; i++)
				x1[i] = x[i] + tempXOff;
			
			tempXOff += ((x[5]-x[0])*2) + (hslop*2) + textWidth;
		
			for(i=0; i<6; i++)
				x1[11-i] = tempXOff - x[i];
			
			tempXOff -= x[5];
		
			Polygon p = new Polygon(x1,y,12);
			tabs.addElement(p);

			// if the tab extends past the last place we can draw
			//   set tooManyTabs
			if (x1[11] > bothRect.x) {
				rightEnabled = true;
				tooManyTabs = true;
				if (num > firstVisible)
					lastVisible = num-1;
				else
					lastVisible = num;
			}	
		}
	}
	
	/** Shifts the row of tabs one position to the left
	 */
	public void shiftLeft() {
		if (firstVisible > 0) firstVisible--;
		repaint();
	}	
	
	/** Shifts the row of tabs one position to the right
	 */
	public void shiftRight() {
		if (firstVisible < (getComponentCount()-1)) firstVisible++;
		repaint();
	}
	
	/** Select a tab by number
	 *  @param n (int) the number of the tab to select
	 */
	public void show(int n) {
		showPhysicalTab(n);
	}
	
	/** Select a tab by component
	 *  @param comp (Component) the main component of the tab to select
	 */
	public void show(Component comp) {
		Component c[] = getComponents();
		int i=0;
		while(c[i] != comp) i++;
		
		if (i < getComponentCount())
			showPhysicalTab(i);
	}	
	
	/** Select a tab by text
	 *  @param tabName (String) the text of the tab to be selected
	 */
	public void show(String tabName) {
		String tabText[] = determineTabText();
		for(int i=0; i<tabText.length; i++) {
			if (tabText[i].equals(tabName)) {
				showPhysicalTab(i);
				return;
			}	
		}
		throw new IllegalArgumentException("No tab found with text \""+tabName+"\"");
	}	
	
	/** Select a tab by number
	 *  @param n (int) the number of the tab to select
	 */
	public void showPhysicalTab(int n) {
		if (n >= getComponentCount())
			throw new IllegalArgumentException("Not that many components!");
		selected = n;
		determineVisible();
		if (selected < firstVisible || selected > lastVisible)
			firstVisible = selected;
		((CardLayout)getLayout()).show(this, getComponent(n).getName());
		fireTabSelected(new TabSelectionEvent(this, getVisibleComponent(), 
		                                      selected, getSelectedName(), getVisibleComponentNum()));
		invalidate();
		validate();
		repaint();
	}
	
	/** bring up the popup menu for the tabsplitter
	 *  @param e (MouseEvent) tells where to bring it up
	 */
	protected void showPopup(MouseEvent e) {
		if (e.isMetaDown()) { // check right click
			if (tabMenuItems != null)
				for(int i = tabMenuItems.length-1;i>-1;i--)
					popupMenu.remove(tabMenuItems[i]);
			if ((tabMenuItems == null) || (tabMenuItems.length != getComponentCount()))
				tabMenuItems = new MenuItem[getComponentCount()];

			// add tab Strings to the popup menu
			String tabNames[] = determineTabText();
			for(int i = 0; i < tabNames.length; i++) {
				popupMenu.add(tabMenuItems[i] = new MenuItem(tabNames[i]));
				tabMenuItems[i].addActionListener(this);
			}	
			popupMenu.show(this, e.getX(), e.getY());
		}	
	}
	
	/** Check to see if the numbered tab contains an x,y location.  Used
	 *  to determine if the mouse was clicked in a given tab 
	 *  @param num (int) number of the tab to check
	 *  @param x (int) x-coord to check
	 *  @param y (int) y-coord to check
	 *  @return true if the (x,y) point is in the tab; false otherwise
	 */
	protected boolean tabContains(int num, int x, int y) {
		return ((Polygon)tabs.elementAt(num)).contains(x, y);
	}	
	
	/** Overridden to get rid of screen erase between drawings */
	public void update(Graphics g) {
		paint(g);
	}
	
}