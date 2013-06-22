package masct.util;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import masct.gui.ImagePanel;
import masct.gui.Masct;
import messages.ExecutionResultsResponseMessage;
import messages.ExecutionStatusNotificationMessage;
import messages.ExecutionStatusResponseMessage;
import messages.OutputFileResponseMessage;
import messages.RepositoryListResponseMessage;
import messages.SubmitApplicationResponseMessage;
import moca.protocol.net.async.DataListener;
import moca.protocol.net.async.ReceivedData;
import moca.protocol.net.async.tcp.AsyncTCPServer;


public class MasctListener extends Thread implements DataListener {
	
	private AsyncTCPServer connection = null;
	private InetSocketAddress localAddress = null;
	
	public MasctListener(){
		
		try {
			localAddress = new InetSocketAddress( InetAddress.getLocalHost(), 5909);
			connection = new AsyncTCPServer(localAddress);
			connection.addListener(this);
			
		} catch (UnknownHostException e1) {

			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void onReceiveData(ReceivedData msg) {
		
		if (msg.getData() instanceof RepositoryListResponseMessage) {
			RepositoryListResponseMessage response = (RepositoryListResponseMessage) msg.getData();
			
			String[] applicationPathsArray = response.getApplicationPaths();
			
			for( int i=0; i < applicationPathsArray.length; i++){
				Masct.getInstance().getRepositoryListPanel().getJList().add( applicationPathsArray[i] );
				//Masct.getInstance().repositoryListPanel.getJList().setListData( response.getApplicationPaths() );
			}
			
			
			Masct.getInstance().getRemoteExecutionScroll().setVisible(false);
			Masct.getInstance().setMenuBar( Masct.getInstance().getRLMenuBar() );
			Masct.getInstance().getRepositoryListScroll().setVisible(true);
			
		}
		
		if (msg.getData() instanceof SubmitApplicationResponseMessage) {
			SubmitApplicationResponseMessage response = (SubmitApplicationResponseMessage) msg.getData();
			
			
			
		}
		
		if (msg.getData() instanceof ExecutionStatusResponseMessage) {
			
			ExecutionStatusResponseMessage response = (ExecutionStatusResponseMessage) msg.getData();
			
			String apps[] = response.getExecutionStates();
			
			for (int i=0 ; i < apps.length ; i++){
				Masct.getInstance().getExecutionStatePanel().getExecStatusLst().add(apps[i]);
			}
			
			
			//Masct.getInstance().executionStatePanel.getExecStatusLst().setListData( apps );
			
			Masct.getInstance().getPanel().setVisible(false);
			Masct.getInstance().getExecutionStatePanel().setParentPanel(Masct.getInstance().getPanel());
			Masct.getInstance().getExecutionStatePanel().setMainFrame( Masct.getInstance() );
			Masct.getInstance().getExecutionStatePanel().setMenuBar( Masct.getInstance().getMainMenuBar() );
			Masct.getInstance().setMenuBar( Masct.getInstance().getESMenuBar() );
			Masct.getInstance().getExecutionStateScroll().setVisible(true);
			
		}
		if (msg.getData() instanceof ExecutionResultsResponseMessage) {
			ExecutionResultsResponseMessage response = (ExecutionResultsResponseMessage) msg.getData();

			
			if( response.getResults() != null ){
				
				Masct.getInstance().getExecutionResultsPanel().remoteDir = response.getRemoteDir();
				Masct.getInstance().getExecutionResultsPanel().executionDir = response.getExecutionDir();
				
				String list[] = new String[ response.getResults().length ];
				Masct.getInstance().getExecutionResultsPanel().nodeDir = response.getNodeDir();
				Masct.getInstance().getExecutionResultsPanel().outputFileName = response.getOutputFileName();
				
				for( int i=0; i<response.getResults().length; i++ ){
					list[i] = response.getResults()[i].substring( response.getResults()[i].indexOf(response.getExecutionDir())+1 );
					Masct.getInstance().getExecutionResultsPanel().getResultsLst().add(list[i]);
				}

				Masct.getInstance().getExecutionStateScroll().setVisible( false );
				Masct.getInstance().setMenuBar( Masct.getInstance().getERMenuBar() );
				Masct.getInstance().getExecutionResultsScroll().setVisible(true);
			}
			
		}
		if (msg.getData() instanceof OutputFileResponseMessage) {
			
			OutputFileResponseMessage response = (OutputFileResponseMessage) msg.getData();
			
			if( response.getFilePath().endsWith(".jpg") || response.getFilePath().endsWith(".png") || 
					response.getFilePath().endsWith(".bmp") || response.getFilePath().endsWith(".gif" ) ) {
				new ImagePanel(response.getData()).setVisible(true);				
				
			}
			else{
				Masct.getInstance().getExecutionResultsScroll().setVisible( false );
				Masct.getInstance().setMenuBar( Masct.getInstance().getOutputMenuBar() );
				Masct.getInstance().getOutputFilesPanel().setFile( new String( response.getFileContent() ) );
				Masct.getInstance().getOutputFilesScroll().setVisible( true );
				
			}

			
		}
		
		
		
		
		
		/* ------------------------------------------------------------------------------ */
		/* NOTIFICATIONS */
		/* ------------------------------------------------------------------------------ */
		if ( msg.getData() instanceof ExecutionStatusNotificationMessage) {
			ExecutionStatusNotificationMessage esnm = (ExecutionStatusNotificationMessage)  msg.getData();
			
		/*	Dialog dialog = new Dialog(Masct.getInstance(),esnm.getStatusNotification(),true);
			dialog.setVisible(true);*/
		}		
		
		
	}
	
}
