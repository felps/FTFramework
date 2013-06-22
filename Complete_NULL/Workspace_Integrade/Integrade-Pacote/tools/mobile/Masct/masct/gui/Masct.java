package masct.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.io.IOException;

import masct.util.MasctClientAPI;
import masct.util.MasctProperties;
import messages.ExecutionStatusRequestMessage;
import moca.core.proxy.message.DefaultMessage;

public class Masct extends Frame{
	private static Masct instance = null;
	
	Panel contentPane = null;
	
	/* main menu */
	private MenuBar mainMenuBar = null;
	private Menu masctMainMenu = null;
	private MenuItem reMenuItem = null;
	private MenuItem esMenuItem = null;
	private MenuItem cfgMenuItem = null;
	
	/* remote exec menu */
	private MenuBar reMenuBar = null;
	private Menu reREMenu = null;
	
	/* execution status menu */
	private MenuBar esMenuBar = null;
	private Menu esESMenu = null;
	
	/* configuration menu */
	private MenuBar cfgMenuBar = null;
	private Menu cfgCFGMenu = null;
	
	/* repository list menu */
	private MenuBar rlMenuBar = null;
	private Menu rlRLMenu = null;
	
	/* execution results menu */
	private MenuBar erMenuBar = null;
	private Menu erERMenu = null;
	
	/* output file menu */
	private MenuBar outputMenuBar = null;
	Menu outputOUTMenu = null;
	
	private Panel panel = null;	
	
	private RemoteExecutionPanel remoteExecutionPanel = null;
	private ExecutionStatePanel executionStatePanel = null;
	private ConfigurePanel configurePanel = null;
	private RepositoryListPanel repositoryListPanel = null;
	private ExecutionResultsPanel executionResultsPanel = null;
	private OutputFilesPanel outputFilesPanel = null;
	
	private ScrollPane remoteExecutionScroll = null;
	private ScrollPane executionStateScroll = null;
	private ScrollPane configureScroll = null;
	private ScrollPane repositoryListScroll = null;
	private ScrollPane executionResultsScroll = null;
	private ScrollPane outputFilesScroll = null;
	
	private String proxyIp;
	private int proxyport;
	private String user;
	private String pass;
	private String from;
	private String to;
	private int masctport;
	private int w,h;

	/**
	 * This is the default constructor
	 */
	private Masct() {
		super();		
		
		GraphicsEnvironment.getLocalGraphicsEnvironment().
		getDefaultScreenDevice().setFullScreenWindow(this);
		
		java.awt.Dimension size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		
		w = size.width;
		h = size.height;
		
		remoteExecutionPanel = new RemoteExecutionPanel();
		remoteExecutionScroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		remoteExecutionScroll.setSize(w, h - 55);
		remoteExecutionScroll.add(remoteExecutionPanel);
		
		
		executionStatePanel = new ExecutionStatePanel();
		executionStateScroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		executionStateScroll.setSize(w, h - 55);
		executionStateScroll.add(executionStatePanel);
		
		configurePanel = new ConfigurePanel();
		configureScroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		configureScroll.setSize(w, h - 55);
		configureScroll.add(configurePanel);
		
		
		repositoryListPanel = new RepositoryListPanel();
		repositoryListScroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		repositoryListScroll.setSize(w, h - 55);
		repositoryListScroll.add(repositoryListPanel);
		
		executionResultsPanel = new ExecutionResultsPanel();
		executionResultsScroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		executionResultsScroll.setSize(w, h - 55);
		executionResultsScroll.add(executionResultsPanel);
		
		outputFilesPanel = new OutputFilesPanel();
		outputFilesScroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		outputFilesScroll.setSize(w, h - 55);
		outputFilesScroll.add(outputFilesPanel);
		
		remoteExecutionScroll.setVisible(false);
		executionStateScroll.setVisible(false);
		configureScroll.setVisible(false);		
		repositoryListScroll.setVisible(false);
		executionResultsScroll.setVisible(false);
		outputFilesScroll.setVisible(false);

		initialize();	
		
	}

	public synchronized static Masct getInstance(){
		
		if( instance == null ){
			instance = new Masct();
		}
		return instance;
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {		

		this.setResizable(false);
		this.setMenuBar(getMainMenuBar());
		this.setTitle("MAsct");
		this.add(getContentPane());
		
		loadProperties();
		

	}	


	/**
	 * 
	 * @throws IOException
	 */
	public boolean loadProperties() {
		
		MasctProperties properties = new MasctProperties();
		if( properties.isEmpty() )
			return false;
		
		user = properties.getMasctUser();
		pass = properties.getMasctPassword();
		proxyIp = properties.getProxyHost();
		proxyport = properties.getProxyPort();
		masctport = properties.getMasctPort();
		from = properties.getMasctFromDate();
		to = properties.getMasctToDate();
		return true;
		
	}
	
	public void subscribeClient(){
		ExecutionStatusRequestMessage subscription = new ExecutionStatusRequestMessage(
				user, "Grid Proxy", DefaultMessage.CONTROL, DefaultMessage.OBJECT,  null);
		
		MasctClientAPI.getInstance().send( subscription );		
		
	}
	
	/**
	 * @param string
	 * @throws IOException
	 */
	public boolean saveProperties(int masctPort, String masctUser, String masctPass, 
			String masctFromDate, String masctToDate, int proxyPort, String proxyHost ){
		
		MasctProperties properties = new MasctProperties();
		properties.save(masctPort, masctUser, masctPass, masctFromDate, masctToDate, proxyPort, proxyHost);
		user = properties.getMasctUser();
		pass = properties.getMasctPassword();
		proxyIp = properties.getProxyHost();
		proxyport = properties.getProxyPort();
		masctport = properties.getMasctPort();
		from = properties.getMasctFromDate();
		to = properties.getMasctToDate();
		return true;
		
	}
	
	/**
	 * This method initializes ContentPane
	 * 
	 * @return java.awt.Panel
	 */
	public Panel getContentPane() {
		if (contentPane == null) {
			contentPane = new Panel();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(getPanel(), java.awt.BorderLayout.CENTER);
			contentPane.add(remoteExecutionScroll, java.awt.BorderLayout.CENTER);
			contentPane.add(executionStateScroll, java.awt.BorderLayout.CENTER);
			contentPane.add(configureScroll, java.awt.BorderLayout.CENTER);
			contentPane.add(repositoryListScroll, java.awt.BorderLayout.CENTER);
			contentPane.add(executionResultsScroll, java.awt.BorderLayout.CENTER);
			contentPane.add(outputFilesScroll, java.awt.BorderLayout.CENTER);
			contentPane.add(getPanel(), java.awt.BorderLayout.CENTER);
		}
		return contentPane;
	}
	
	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	public MenuBar getMainMenuBar() {
		if (mainMenuBar == null) {
			mainMenuBar = new MenuBar();
			mainMenuBar.add(getMasctMainMenu());			
		}
		return mainMenuBar;
	}
	
	
	/**
	 * This method initializes jMenuBar	
	 * 	
	 * @return javax.swing.MenuBar	
	 */
	public MenuBar getREMenuBar() {
		if (reMenuBar == null) {
			reMenuBar = new MenuBar();
			reMenuBar.add(getREREMenu());
		}
		return reMenuBar;
	}
	
	
	/**
	 * This method initializes jMenuBar	
	 * 	
	 * @return javax.swing.MenuBar	
	 */
	public MenuBar getESMenuBar() {
		if (esMenuBar == null) {
			esMenuBar = new MenuBar();
			esMenuBar.add(getESESMenu());
		}
		return esMenuBar;
	}
	
	/**
	 * This method initializes jMenuBar	
	 * 	
	 * @return javax.swing.MenuBar	
	 */
	public MenuBar getCFGMenuBar() {
		if (cfgMenuBar == null) {
			cfgMenuBar = new MenuBar();
			cfgMenuBar.add(getCFGCFGMenu());
		}
		return cfgMenuBar;
	}
	
	public MenuBar getRLMenuBar() {
		if (rlMenuBar == null) {
			rlMenuBar = new MenuBar();
			rlMenuBar.add(getRLRLMenu());
		}
		return rlMenuBar;
	}
	
	public MenuBar getERMenuBar() {
		if (erMenuBar == null) {
			erMenuBar = new MenuBar();
			erMenuBar.add(getERERMenu());
		}
		return erMenuBar;
	}
	
	public MenuBar getOutputMenuBar() {
		if (outputMenuBar == null) {
			outputMenuBar = new MenuBar();
			outputMenuBar.add(getOutputOUTMenu());
		}
		return outputMenuBar;
	}
	
	/**
	 * This method initializes Menu	
	 * 	
	 * @return java.awt.Menu	
	 */
	public Menu getMasctMainMenu() {
		if (masctMainMenu == null) {
			masctMainMenu = new Menu();
			masctMainMenu.setLabel("Masct");
			masctMainMenu.add(getReMenuItem());
			masctMainMenu.add(getEsMenuItem());
			masctMainMenu.add(getCfgMenuItem());
		}
		return masctMainMenu;
	}
	
	/**
	 * This method initializes Menu	
	 * 	
	 * @return java.awt.Menu	
	 */
	public Menu getREREMenu() {
		if (reREMenu == null) {
			reREMenu = new Menu();
			reREMenu.setLabel("Remote Execution");
		}
		return reREMenu;
	}
	
	
	/**
	 * This method initializes Menu	
	 * 	
	 * @return java.awt.Menu	
	 */
	public Menu getESESMenu() {
		if (esESMenu == null) {
			esESMenu = new Menu();
			esESMenu.setLabel("Execution State");
		}
		return esESMenu;
	}
	
	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	public Menu getCFGCFGMenu() {
		if (cfgCFGMenu == null) {
			cfgCFGMenu = new Menu();
			cfgCFGMenu.setLabel("Configuration");
		}
		return cfgCFGMenu;
	}
	
	public Menu getRLRLMenu() {
		if (rlRLMenu == null) {
			rlRLMenu = new Menu();
			rlRLMenu.setLabel("Repository List");
		}
		return rlRLMenu;
	}
	
	public Menu getERERMenu() {
		if (erERMenu == null) {
			erERMenu = new Menu();
			erERMenu.setLabel("Execution Results");
		}
		return erERMenu;
	}
	
	public Menu getOutputOUTMenu() {
		if (outputOUTMenu == null) {
			outputOUTMenu = new Menu();
			outputOUTMenu.setLabel("Output File");
		}
		return outputOUTMenu;
	}
	
	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	public MenuItem getEsMenuItem() {
		
		if (esMenuItem == null) {
			
			esMenuItem = new MenuItem();
			esMenuItem.setLabel("Execution State");
			esMenuItem.addActionListener(new java.awt.event.ActionListener() {
				
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					ExecutionStatusRequestMessage request = new ExecutionStatusRequestMessage( user, 
							"Grid Proxy", 0, DefaultMessage.OBJECT, null);
					
					request.setFromDate( from );
					request.setToDate( to );
					
					MasctClientAPI.getInstance().send( request );
				}
			});
		}
		return esMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	public MenuItem getReMenuItem() {
		if (reMenuItem == null) {
			reMenuItem = new MenuItem();
			reMenuItem.setLabel("Remote Execution");
			reMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					panel.setVisible(false);
					Masct.getInstance().setMenuBar( Masct.getInstance().getREMenuBar() );
					remoteExecutionScroll.setVisible(true);
				}
			});
		}
		return reMenuItem;
	}

	/**
	 * This method initializes cfgMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	public MenuItem getCfgMenuItem() {
		if (cfgMenuItem == null) {
			cfgMenuItem = new MenuItem();
			cfgMenuItem.setLabel("Configuration");
			cfgMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					panel.setVisible(false);
					Masct.getInstance().setMenuBar( Masct.getInstance().getCFGMenuBar() );
					
					configurePanel.setProperties( masctport, user, pass, from, to, proxyport, proxyIp  );
					configureScroll.setVisible(true);
					
					
				}
			});
		}
		return cfgMenuItem;
	}

	/**
	 * This method initializes panel	
	 * 	
	 * @return java.awt.Panel	
	 */
	public Panel getPanel() {
		if (panel == null) {			
			panel = new Panel();
			panel.setLayout(new BorderLayout());
			panel.setBackground(java.awt.Color.white);

		}
		return panel;
	}

	/**
	 * Launches this application
	 */
	public static void main(String[] args) {
		Masct.getInstance();
	}

	public RemoteExecutionPanel getRemoteExecutionPanel() {
		return remoteExecutionPanel;
	}

	public void setRemoteExecutionPanel(RemoteExecutionPanel remoteExecutionPanel) {
		this.remoteExecutionPanel = remoteExecutionPanel;
	}

	public ConfigurePanel getConfigurePanel() {
		return configurePanel;
	}

	public void setConfigurePanel(ConfigurePanel configurePanel) {
		this.configurePanel = configurePanel;
	}

	public ExecutionResultsPanel getExecutionResultsPanel() {
		return executionResultsPanel;
	}

	public void setExecutionResultsPanel(ExecutionResultsPanel executionResultsPanel) {
		this.executionResultsPanel = executionResultsPanel;
	}

	public ExecutionStatePanel getExecutionStatePanel() {
		return executionStatePanel;
	}

	public void setExecutionStatePanel(ExecutionStatePanel executionStatePanel) {
		this.executionStatePanel = executionStatePanel;
	}

	public OutputFilesPanel getOutputFilesPanel() {
		return outputFilesPanel;
	}

	public void setOutputFilesPanel(OutputFilesPanel outputFilesPanel) {
		this.outputFilesPanel = outputFilesPanel;
	}

	public RepositoryListPanel getRepositoryListPanel() {
		return repositoryListPanel;
	}

	public void setRepositoryListPanel(RepositoryListPanel repositoryListPanel) {
		this.repositoryListPanel = repositoryListPanel;
	}

	public int getWidth() {
		return w;
	}

	public void setWidth(int width) {
		this.w = width;
	}

	public int getHeight() {
		return h;
	}

	public void setHeight(int height) {
		this.h = height;
	}

	public ScrollPane getRemoteExecutionScroll() {
		return remoteExecutionScroll;
	}

	public void setRemoteExecutionScroll(ScrollPane remoteExecutionScroll) {
		this.remoteExecutionScroll = remoteExecutionScroll;
	}

	public ScrollPane getExecutionStateScroll() {
		return executionStateScroll;
	}

	public void setExecutionStateScroll(ScrollPane executionStateScroll) {
		this.executionStateScroll = executionStateScroll;
	}

	public ScrollPane getConfigureScroll() {
		return configureScroll;
	}

	public void setConfigureScroll(ScrollPane configureScroll) {
		this.configureScroll = configureScroll;
	}

	public ScrollPane getRepositoryListScroll() {
		return repositoryListScroll;
	}

	public void setRepositoryListScroll(ScrollPane repositoryListScroll) {
		this.repositoryListScroll = repositoryListScroll;
	}

	public ScrollPane getExecutionResultsScroll() {
		return executionResultsScroll;
	}

	public void setExecutionResultsScroll(ScrollPane executionResultsScroll) {
		this.executionResultsScroll = executionResultsScroll;
	}

	public ScrollPane getOutputFilesScroll() {
		return outputFilesScroll;
	}

	public void setOutputFilesScroll(ScrollPane outputFilesScroll) {
		this.outputFilesScroll = outputFilesScroll;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public int getMasctport() {
		return masctport;
	}

	public void setMasctport(int masctport) {
		this.masctport = masctport;
	}

	public String getProxyIp() {
		return proxyIp;
	}

	public void setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp;
	}

	public int getProxyport() {
		return proxyport;
	}

	public void setProxyport(int proxyport) {
		this.proxyport = proxyport;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	

}
