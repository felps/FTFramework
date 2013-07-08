package gridproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import messages.ExecutionResultsRequestMessage;
import messages.ExecutionResultsResponseMessage;
import messages.ExecutionStatusNotificationMessage;
import messages.ExecutionStatusRequestMessage;
import messages.ExecutionStatusResponseMessage;
import messages.KillApplicationRequestMessage;
import messages.KillApplicationResponseMessage;
import messages.OutputFileRequestMessage;
import messages.OutputFileResponseMessage;
import messages.RepositoryListRequestMessage;
import messages.RepositoryListResponseMessage;
import messages.SpecificExecutionStatusRequestMessage;
import messages.SpecificExecutionStatusResponseMessage;
import messages.SubmitApplicationRequestMessage;
import messages.SubmitApplicationResponseMessage;
import moca.core.proxy.util.mime.MimeUtils;
import asct.core.ApplicationControlFacade;
import asct.shared.AbstractGridApplication;
import asct.shared.ApplicationState;
import asct.shared.BspGridApplication;
import asct.shared.ExecutionRequestData;
import asct.shared.ExecutionRequestStatus;
import asct.shared.IExecutionListener;
import asct.shared.ParametricCopyHolder;
import asct.shared.ParametricGridApplication;
import asct.shared.SequencialGridApplication;
import clusterManagement.ApplicationNotFoundException;
import clusterManagement.DirectoryNotFoundException;
import clusterManagement.InvalidPathNameException;
import clusterManagement.SecurityException;
import dataTypes.ApplicationDescription;
import dataTypes.ApplicationType;
import dataTypes.ContentDescription;
import dataTypes.ExecutionRequestId;
import dataTypes.kindOfItens;


/**
 * @author Diego Gomes
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GridProxy implements IExecutionListener, GridProxyInterface {
	
	// -------------------------------------------------------------------------------
	// ATTRIBUTES AND CONSTRUCTOR
	// -------------------------------------------------------------------------------
		
	private GridProxyProperties properties;
	private ApplicationControlFacade grid;
	private GridProxyListenerThread listener;
	private String localDirectory;
	
	/**
	 * Constructor
	 * */
	public GridProxy(){
		
		System.out.println( "Initializing GridProxy." );
		properties = DBManager.getInstance().getGridProxyProperties();
		localDirectory = "output/";
		grid = new ApplicationControlFacade(localDirectory, false, false, null);
		grid.registerExecutionStateListener( this );
		listener = new GridProxyListenerThread( this );
		listener.start();
		
	}
	
	
	// -------------------------------------------------------------------------------
	// GETTERS AND SETTERS METHODS
	// -------------------------------------------------------------------------------
	
	/**
	 * @return Returns the properties.
	 */
	public GridProxyProperties getProperties() {
		return properties;
	}
	/**
	 * @param properties The properties to set.
	 */
	public void setProperties(GridProxyProperties properties) {
		this.properties = properties;
	}
	
	
	// -------------------------------------------------------------------------------
	// GRID PROXY INTERFACE 
	// -------------------------------------------------------------------------------
	
	
	/**
	 * Lista as aplicações do repositório.
	 * 
	 * @param message Mensagem com a requisição para listar as aplicações do repositório.
	 * @return response Mensagem que contém a lista de aplicações registradas no repositório.
	 * @see gridproxy.GridProxyInterface#getRepositoryList(java.lang.String)
	 */
	public synchronized RepositoryListResponseMessage getRepositoryList( 
			RepositoryListRequestMessage request) {
		
		
		RepositoryListResponseMessage response = new RepositoryListResponseMessage( 
				request.getAddressee(), request.getSender(), request.getMsgType(), 
				request.getDataType(), null);
		
		String str = getAllApplicationsFromRepository();
		String files[] = str.split("\n");
		
		str = "";
		for(int i=0; i<files.length; i++){
			if( ! files[i].equals("") ){
				str += files[i] + "\n";
				System.out.println("REPOSITORY: " + files[i]);
				
			}
		}
		System.out.println( str );
		response.setApplicationPaths( str.split("\n") );
		System.out.println( "response:" + response.getApplicationPaths() );
		response.setClientAddress( request.getClientAddress() );
		return response;
		
	}
	
	/**
	 * Formata os dados para fazer uma requisição de execução na grade.
	 * 
	 * @param message Mensagem que contém a requisição do cliente.
	 * @return response Mensagem que contém a lista de aplicações registradas no repositório.
	 * @see gridproxy.GridProxyInterface#submitApplication(java.lang.String)
	 */
	public synchronized SubmitApplicationResponseMessage submitApplication( 
			SubmitApplicationRequestMessage request){
		
		// Prepare response
		SubmitApplicationResponseMessage response = new SubmitApplicationResponseMessage(
				request.getAddressee(), request.getSender(), request.getMsgType(), 
				request.getDataType(), null);
		
		// Prepare status
		ExecutionRequestStatus requestStatus = null;
		
		StringTokenizer tokens = new StringTokenizer(request.getBinaryNames());
		int numberOfBinaries= tokens.countTokens();
		ExecutionRequestData data = new ExecutionRequestData();
		
		String binaryIds[] = new String[numberOfBinaries];
		for (int count = 0; count < numberOfBinaries; count++){		
			binaryIds[count] = tokens.nextToken();			
			
		}
		
		AbstractGridApplication application;
		ApplicationDescription applicationDescription = new ApplicationDescription( request.getBasePath(), 
				request.getApplicationName(), (new Integer(numberOfBinaries)).toString(), binaryIds );
		if( request.getApplicationType() == ApplicationType._bsp){
			application = new BspGridApplication( request.getApplicationName(), request.getBasePath() );
		}
		else if( request.getApplicationType() == ApplicationType._parametric){
			application = new ParametricGridApplication( request.getApplicationName(), request.getBasePath() );
		}
		else {
			application = new SequencialGridApplication( request.getApplicationName(), request.getBasePath() );
		}
		application.setDescription(applicationDescription);
		
		data.setApplication(application);
		data.setArguments( request.getApplicationArguments() );
		data.setNumberOfTasks( request.getNumberOfTasks() );
		
		System.out.println("=====>>>");
		System.out.println("app name " + request.getApplicationName());		
		System.out.println("base path " + request.getBasePath());				
		System.out.println("binaryNames " + request.getBinaryNames());

		data.setNumberOfTasks(request.getNumberOfTasks());
		System.out.println("numOfTask " + data.getNumberOfTasks());
		data.setNumberOfCopies(request.getParametricCopies().length);
		System.out.println("numOfparamCopies " + data.getNumberOfCopies());		
		data.setParametricCopies(data.getParametricCopies());		
		data.setForceDifferentMachines(request.isForceDifferentMachines());
		System.out.println("forceDifferentMachines " + request.isForceDifferentMachines());
		data.setConstraints(request.getApplicationContraints());
		System.out.println("appCons "+ request.getApplicationContraints());
		data.setPreferences(request.getApplicationPreferences());
		System.out.println("app Pref " + request.getApplicationPreferences());
		System.out.println("<<<=======");
		
		ParametricCopyHolder parametricCopies[] = null;
		
		if( request.getParametricCopies().length > 0 ){
			
			data.setNumberOfCopies( request.getParametricCopies().length );
			parametricCopies = new ParametricCopyHolder[ request.getParametricCopies().length ];
			for(int i=0; i<request.getParametricCopies().length; i++){
				parametricCopies[i] = new ParametricCopyHolder();
				parametricCopies[i].setArguments( request.getParametricCopies()[i].getArguments() );
				parametricCopies[i].setInputFiles( request.getParametricCopies()[i].getInputFiles() );
				parametricCopies[i].setOutputFiles( request.getParametricCopies()[i].getOutputFiles() );
			}
			data.setParametricCopies(parametricCopies);
		}else{
			data.setArguments(request.getApplicationArguments());
			System.out.println("apparg " + request.getApplicationArguments());
			data.setInputFiles(request.getInputFiles());
			System.out.println("input " + request.getInputFiles().length);
			data.setOutputFileNames(request.getOutputFiles());
			System.out.println("output " + request.getOutputFiles().length);			
			
		}
		
		// Gera o Id da Submissao
		long submitionId = DBManager.getInstance().insertSubmition(null, request.getApplicationName(), null, null);		
		
		
		// Armazena os arquivos de entrada
		if (application instanceof ParametricGridApplication) {
			
			System.out.println("Executing Parametric Application");
			
			for(int j=0; j < parametricCopies.length; j++){
				
				String[] inputFiles = parametricCopies[j].getInputFiles();
				for(int i=0; i < inputFiles.length; i++){
					
					// Se eh uma URL da web
					if( inputFiles[i].startsWith("http://") ){
						
						try {
							
							URL url = new URL( inputFiles[i] );
							InputStream is = url.openStream();
							byte b[] = new byte[1];
							File f1 = new File(localDirectory);
							f1.mkdir();
							File f2 = new File(localDirectory + String.valueOf(submitionId - 1));							
							f2.mkdir();	
							File f3 = new File(localDirectory + String.valueOf(submitionId - 1) + "/" + j  );							
							f3.mkdir();							
							
							
							FileOutputStream fos = new FileOutputStream( new File( 
									localDirectory + String.valueOf(submitionId - 1) + "/" + j + "/" + inputFiles[i].substring( 
											inputFiles[i].lastIndexOf("/") ) ) );
								
							
							is.read( b );
							fos.write( b );
							while( is.available() > 0 ){
								is.read( b );
								fos.write( b );
							}
							
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
					//inputFiles[i] = localDirectory + submitionId + inputFiles[i].substring(inputFiles[i].lastIndexOf("/") );
					inputFiles[i] = localDirectory + String.valueOf(submitionId - 1) + "/" + j + "/" + inputFiles[i].substring(inputFiles[i].lastIndexOf("/") );
					
				}
				parametricCopies[j].setInputFiles( inputFiles );
				
			}
			data.setParametricCopies( parametricCopies );
			
		}else if ( application instanceof SequencialGridApplication){	// Regular Application
			
			System.out.println("Executing Regular Application ");
			
			String[] inputFiles = data.getInputFiles();
			
			for(int i = 0; i < inputFiles.length; i++) {
				
				// Se eh uma URL da web
				if( inputFiles[i].startsWith("http://") ){
					
					try {
						
						URL url = new URL( inputFiles[i] );
						InputStream is = url.openStream();
						byte b[] = new byte[1];
						File f1 = new File(localDirectory);
						f1.mkdir();
						File f2 = new File(localDirectory + String.valueOf(submitionId - 1));						
						f2.mkdir();
						File f3 = new File(localDirectory + String.valueOf(submitionId - 1) + "/" + request.getNumberOfTasks()  );						
						f3.mkdir();							
						
						
						FileOutputStream fos = new FileOutputStream( new File( 
								localDirectory + String.valueOf(submitionId - 1) + "/" + request.getNumberOfTasks() + "/" + inputFiles[i].substring( 
										inputFiles[i].lastIndexOf("/") ) ) );
						
						is.read( b );
						fos.write( b );
						while( is.available() > 0 ){
							is.read( b );
							fos.write( b );
						}
						
					} catch (MalformedURLException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					inputFiles[i] = localDirectory +
						String.valueOf(submitionId - 1) + "/" +
						request.getNumberOfTasks() + "/" +
						inputFiles[i].substring(inputFiles[i].lastIndexOf("/") ); 
				}
			
			}
			data.setInputFiles( inputFiles );
			
		}else{
			System.out.println("Executing BSP Application ");
			
			String[] inputFiles = data.getInputFiles();
			
			String[] inFiles = new String[inputFiles.length];			
			
			for (int j =0; j < request.getNumberOfTasks(); j++){
			
				for(int i = 0; i < inputFiles.length; i++) {
					
					// Se eh uma URL da web
					if( inputFiles[i].startsWith("http://") ){
						
						try {
							
							URL url = new URL( inputFiles[i] );
							InputStream is = url.openStream();
							byte b[] = new byte[1];
							File f1 = new File(localDirectory);
							f1.mkdir();
							File f2 = new File(localDirectory + String.valueOf(submitionId - 1));
							System.out.println( "localDirectory + submitionId >" +  localDirectory + (submitionId -1 ));
							f2.mkdir();
	
							File f3 = new File(localDirectory + String.valueOf(submitionId - 1) + "/" + j  );
							System.out.println( "localDirectory + submitionId >" +  localDirectory + (submitionId - 1) + "/" + j );
							f3.mkdir();							
							
							
							FileOutputStream fos = new FileOutputStream( new File( 
									localDirectory + String.valueOf(submitionId - 1) + "/" + j + "/" + inputFiles[i].substring( 
											inputFiles[i].lastIndexOf("/") ) ) );
							
							is.read( b );
							fos.write( b );
							while( is.available() > 0 ){
								is.read( b );
								fos.write( b );
							}
							
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					inFiles[i] = localDirectory + String.valueOf(submitionId - 1) + "/" + j + "/" + inputFiles[i].substring(inputFiles[i].lastIndexOf("/") ); 
				}
			
			}
			data.setInputFiles( inFiles );
		}
			
		
		try {
			
			// Faz a requisicao para a grade
			requestStatus = grid.executeApplication( data );
			
		} catch (ApplicationNotFoundException e) {
			e.printStackTrace();
		} catch (DirectoryNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidPathNameException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		DBManager.getInstance().updateSubmition(submitionId, null, requestStatus.getRequestId(), requestStatus, 0);
		
		response.setSubmitionId( submitionId );
		 
		storeSubmitionClient( response.getSubmitionId(), request.getSender() );
		
		return response;
	}
	
	
	/**
	 * Requisita os estado de execução das aplicações submetidas à grade.
	 * 
	 * @param message Mensagem que contém a requisição do cliente.	  
	 * @return response Mensagem de resposta contendo o estado de execução das aplicações.	  
	 * @see gridproxy.GridProxyInterface#getExecutionStatus(java.lang.String, java.lang.String)
	 */
	public synchronized ExecutionStatusResponseMessage getExecutionStatus(ExecutionStatusRequestMessage request) {
		
		
		ExecutionStatusResponseMessage response = new ExecutionStatusResponseMessage( 
				request.getAddressee(), request.getSender(), request.getMsgType(), 
				request.getDataType(), null );
		
		String message = "";
		Vector status = DBManager.getInstance().selectSubmitionStatus( request.getSender(), 
				request.getFromDate(), request.getToDate() );
		for(int i=0; i< status.size(); i++){
			message += (String)status.get(i) + "\n";
		}
		
		response.setExecutionStates( message.split("\n") );
		
		return response;
	}
	
	
	/**
	 * Consulta o status de execução.
	 * 
	 * @param message Mensagem que contem as informacoes do cliente 
	 * e da requisicao de execucao a ser monitorada. 
	 * @return String que contem as informacoes de status 
	 * da execucao.
	 * @see gridproxy.GridProxyInterface#getSpecificExecutionStatus(java.lang.String)
	 */
	public synchronized SpecificExecutionStatusResponseMessage getSpecificExecutionStatus( 
			SpecificExecutionStatusRequestMessage request) {
		
		SpecificExecutionStatusResponseMessage response = new SpecificExecutionStatusResponseMessage(); 
		String status = DBManager.getInstance().selectSubmitionStatus( request.getSubmitionId() );
		response.setStatus( status );
		return response;
	}
	
	/**
	 *  
	 * @param message Mensagem que contém a requisição do cliente.
	 * @return response
	 * @see gridproxy.GridProxyInterface#getExecutionResults(java.lang.String)
	 */
	public synchronized ExecutionResultsResponseMessage getExecutionResults( 
			ExecutionResultsRequestMessage request) {
		
		
		ExecutionResultsResponseMessage response = new ExecutionResultsResponseMessage( 
				request.getAddressee(), request.getSender(), request.getMsgType(), 
				request.getDataType(), null );
		
		String executionId = DBManager.getInstance().selectSubmitionExecutionId( request.getSubmitionId() );
		
		grid.getApplicationResults( executionId, null );
		
		String results = getResults( new File( localDirectory + executionId ) );
		
		response.setResults( results.split("\n") );
		
		return response;
	}
	
	/**
	 * @param file
	 * @return
	 */
	private String getResults(File root) {
		
		String results = "";
		if( root != null ){
			if( root.exists() ){
				if( root.isDirectory() ){
					
					File file[] = root.listFiles();
					if( file != null ){
						
						for( int i=0; i < file.length; i++ ){
							
							if( file[i].isDirectory() ){
								

								results += getResults( file[i] );

							}
							else{
								results += "/" + file[i].getPath() + "\n";

							}
							
						}
					}
				}
			}
		}
		return results;
	}


	/**
	 * Requisita os arquivos de saída gerados pelas aplicações submetidas à grade. 
	 * @param message Mensagem que contém a requisição do cliente.
	 * 
	 * @see gridproxy.GridProxyInterface#getOutputFile(java.lang.String)
	 */
	public synchronized OutputFileResponseMessage getOutputFile(OutputFileRequestMessage request) {
		
		
		OutputFileResponseMessage response = new OutputFileResponseMessage( request.getAddressee(), 
				request.getSender(), request.getMsgType(), request.getDataType(), null);
		
				
		File file = new File( request.getFileName() );
		FileInputStream fis;
		byte content[] = null;
		
		try {
			
			content = new byte[ (int) file.length() ];
			fis = new FileInputStream( file );
			fis.read( content );
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String mime =	MimeUtils.guessContentTypeFromName(request.getFileName());

		
		if(mime!=null && mime.startsWith("image")){			
			response.setDataType(mime);
			response.setData(content);
		}		


		response.setFileContent( content );
		response.setFilePath( request.getFileName() );		
		
		return response;
	}
	
	/**
	 * Requisita o encerramento de uma aplicação em execução.
	 * 
	 * @param message Mensagem que contém a requisição do cliente.
	 * 
	 * @see gridproxy.GridProxyInterface#killApplication(java.lang.String)
	 */
	public synchronized KillApplicationResponseMessage killApplication( 
			KillApplicationRequestMessage request){
		
		KillApplicationResponseMessage response = new KillApplicationResponseMessage( 
				request.getAddressee(), request.getSender(), request.getMsgType(), 
				request.getDataType(), null );
		
		String executionId = DBManager.getInstance().selectSubmitionExecutionId( request.getSubmitionId() );
		
		grid.killApplication( executionId );
		
		return response;
	}
	
	
	
	
	// -------------------------------------------------------------------------------
	// EXECUTION LISTNER INTERFACE (IExecutionListener)
	// -------------------------------------------------------------------------------
	
	/**
	 * 
	 * @param execRequestId
	 * @param applicationStatus
	 * @see asct.shared.IExecutionListener#updateStatus(dataTypes.ExecutionRequestId, asct.shared.ApplicationState)
	 */
	public void updateStatus(ExecutionRequestId execRequestId, ApplicationState applicationStatus) {
		
		
		ExecutionStatusNotificationMessage notification = null;		
		ExecutionRequestStatus status = new ExecutionRequestStatus();
		status.setApplicationState( applicationStatus );
		DBManager.getInstance().updateSubmition( 0, null, execRequestId.requestId, status, 0 );
		
		String msgStatus = "";
		if( applicationStatus.equals(ApplicationState.EXECUTING) ){
			
			msgStatus = "EXECUTING";
			
		}
		else if( applicationStatus.equals(ApplicationState.FINISHED) ){
			
			msgStatus = "FINISHED";
			
		}
		else if( applicationStatus.equals(ApplicationState.REFUSED) ){
			
			msgStatus = "REFUSED";
			
		}
		else if( applicationStatus.equals(ApplicationState.TERMINATED) ){
			
			msgStatus = "TERMINATED";
			
		}
		
		String client = DBManager.getInstance().selectSubmitionClient( execRequestId.requestId );
		long submitionId = DBManager.getInstance().selectSubmitionId( execRequestId.requestId );
		String appName = DBManager.getInstance().selectSubmitionAppName( execRequestId.requestId );
		
		notification = new ExecutionStatusNotificationMessage(
				"Grid Proxy", client, 0, ExecutionStatusNotificationMessage.OBJECT, null);
		notification.setStatusNotification( msgStatus );
		notification.setAppName( appName );
		notification.setSubmitionId( submitionId );
		
		listener.sendNotification( notification );
	}
	
	
	// -------------------------------------------------------------------------------
	// DATA BASE MANAGER 
	// -------------------------------------------------------------------------------
	
	/**
	 * @param dm
	 * @param status
	 */
	public void storeSubmitionClient(long submitionId, String clientId) {
		
		DBManager db = DBManager.getInstance();
		db.updateSubmition( submitionId, clientId, null, null, 0 );
		
	}	
	
	
	// -------------------------------------------------------------------------------
	// PRIVATE METHODS -- MISCELANEOUS
	// -------------------------------------------------------------------------------
	
	/* (Non Javadoc)
	 * 
	 * @return
	 */
	private String getAllApplicationsFromRepository(){
		
		return getAllApplicationsFromDirectoryRecursively("/");
	}
	
	
	/* (Non Javadoc)
	 * 
	 * 
	 * @param dir
	 * @return
	 */
	private String getAllApplicationsFromDirectoryRecursively(String dir){
		
		boolean allowsChildren;
		String files = "";
		
		ContentDescription[] contents = null;
		try {
			if( dir.equalsIgnoreCase("/") )
				contents = grid.listRootDirectoryContents();
			else
				contents = grid.listDirectoryContents(dir);
			
		} catch (DirectoryNotFoundException e) {
			
			e.printStackTrace();
		} catch (InvalidPathNameException e) {
			
			e.printStackTrace();
		} catch (SecurityException e) {
			
			e.printStackTrace();
		}
		
		for (int i = 0; i < contents.length; i++) {
			
			allowsChildren = 	contents[i].kind.equals(kindOfItens.applicationDirectory)
								|| contents[i].kind.equals(kindOfItens.commonDirectory)
								|| contents[i].kind.equals(kindOfItens.rootDirectory);
			contents[i].fileName = "/" + contents[i].fileName;
			
			if( !allowsChildren ){
				
				if( ! contents[i].fileName.endsWith("AppDescription") ){
					files += contents[i].fileName+"\n";
					// files += contents[i].basePath + contents[i].applicationName+"\n";
				}
				
			}else{
				
				if( ! contents[i].fileName.endsWith("AppDescription") ){
					files += getAllApplicationsFromDirectoryRecursively( contents[i].fileName );
					// files += getAllApplicationsFromDirectoryRecursively(contents[i].applicationName);
				}
			}
		}
		return files;
	}
	
	
	
	// -------------------------------------------------------------------------------
	// MAIN METHOD -- LAUNCHER 
	// -------------------------------------------------------------------------------
	
	/**
	 * @param args
	 */
	public static void main(String args[]){		
		
		GridProxy gp = new GridProxy();
		
		
	}
}