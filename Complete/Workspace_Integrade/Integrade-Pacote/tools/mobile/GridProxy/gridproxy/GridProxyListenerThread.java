package gridproxy;

import java.io.IOException;
import java.net.InetSocketAddress;

import messages.ExecutionResultsRequestMessage;
import messages.ExecutionStatusRequestMessage;
import messages.KillApplicationRequestMessage;
import messages.OutputFileRequestMessage;
import messages.RepositoryListRequestMessage;
import messages.SpecificExecutionStatusRequestMessage;
import messages.SubmitApplicationRequestMessage;
import messages.SubmitApplicationResponseMessage;
import moca.core.proxy.message.Message;
import moca.protocol.net.TCPConnection;
import moca.protocol.net.async.DataListener;
import moca.protocol.net.async.ReceivedData;
import moca.protocol.net.async.tcp.AsyncTCPServer;


/**
 * GridProxyListenerThread escuta pelas mensagens dos clientes moveis
 * enviadas atraves do ProxyFramework. As mensagens que chegam sao tratadas
 * pelo GridProxy antes de serem enviadas à Grade. O GridProxyListenerThread 
 * tambem envia mensagens-resposta para cada cliente que a requisitou.
 * 
 * @author Diego Gomes
 */
public class GridProxyListenerThread extends Thread implements DataListener {
	
	GridProxy grid;
	AsyncTCPServer connection = null;
	
	
	/**
	 * Construtor.
	 * Inicia a Thread servidora que aguarda as mensagens com as requisicoes dos 
	 * clientes.
	 * 
	 * @param g referencia para o GridProxy.
	 */
	public GridProxyListenerThread( GridProxy g ) {
		super();
		grid = g;
		
		InetSocketAddress localAddress = new InetSocketAddress("0.0.0.0", 
				grid.getProperties().GetGridProxyPort() );
		try {
			connection = new AsyncTCPServer(localAddress);
			connection.addListener( (DataListener) this );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Chama metodos especificos do GridProxy para cada tipo de 
	 * requisicao do cliente.
	 * 
	 * @param msg mensagem recebida
	 * 
	 * @see moca.protocol.net.async.DataListener#onReceiveData(moca.protocol.net.async.ReceivedData)
	 */
	public void onReceiveData(ReceivedData msg) {
		
		System.out.println("New requisition");
		
		if (msg.getData() instanceof RepositoryListRequestMessage) {
			RepositoryListRequestMessage rlm = (RepositoryListRequestMessage) msg.getData();
			
			System.out.println("GetRepositoryList");
			sendMessage( grid.getRepositoryList( rlm ) );
			
		}
		else if (msg.getData() instanceof SubmitApplicationRequestMessage) {
			SubmitApplicationRequestMessage sam = (SubmitApplicationRequestMessage) msg.getData();
			
			System.out.println("SubmitApplication");
			SubmitApplicationResponseMessage response = grid.submitApplication( sam );
			
			
		}
		else if (msg.getData() instanceof ExecutionStatusRequestMessage) {
			
			ExecutionStatusRequestMessage esm = (ExecutionStatusRequestMessage) msg.getData();
			
			System.out.println("GetExecutionStatus");
			sendMessage( grid.getExecutionStatus( esm ) );
			
		}
		else if (msg.getData() instanceof SpecificExecutionStatusRequestMessage) {
			SpecificExecutionStatusRequestMessage sesm = (SpecificExecutionStatusRequestMessage) msg.getData();
			
			System.out.println("GetSpecificExecutionStatus");
			sendMessage( grid.getSpecificExecutionStatus( sesm ) );
		}
		else if (msg.getData() instanceof ExecutionResultsRequestMessage) {
			ExecutionResultsRequestMessage erm = (ExecutionResultsRequestMessage) msg.getData();
			
			System.out.println("GetExecutionResults");
			sendMessage( grid.getExecutionResults( erm ) );
		}
		else if (msg.getData() instanceof OutputFileRequestMessage) {
			OutputFileRequestMessage ofm = (OutputFileRequestMessage) msg.getData();
			
			System.out.println("GetOutputFile");
			sendMessage( grid.getOutputFile( ofm ) );
			
		}
		else if (msg.getData() instanceof KillApplicationRequestMessage) {
			KillApplicationRequestMessage kam = (KillApplicationRequestMessage) msg.getData();
			
			System.out.println("KillApplication");
			grid.killApplication( kam );
		}

		
		
	}
	
	
	/**
	 * Envia mensagens texto para os clientes.
	 * 
	 * @param message mensagem texto.
	 */
	public void sendMessage(Message msg){
		
		TCPConnection me = null;
		
		try{
			InetSocketAddress ServerSocketAddress = new InetSocketAddress( 
					grid.getProperties().GetProxyAdapterHost(), 
					grid.getProperties().GetProxyAdapterPort());
			System.out.println("proxy host: "+grid.getProperties().GetProxyAdapterHost() );
			System.out.println("proxy port: "+grid.getProperties().GetProxyAdapterPort() );
			me = new TCPConnection(ServerSocketAddress);		
			me.send( msg );		
			me.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param msgStatus
	 * @param client
	 */
	public void sendNotification(Message msg) {
		
		// TODO Auto-generated method stub
		TCPConnection me = null;
		
		try{
			InetSocketAddress ServerSocketAddress = new InetSocketAddress(grid.getProperties().GetProxyAdapterHost(),grid.getProperties().GetProxyAdapterPort());
			me = new TCPConnection(ServerSocketAddress); 
			me.send(msg);
			me.close() ;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}
