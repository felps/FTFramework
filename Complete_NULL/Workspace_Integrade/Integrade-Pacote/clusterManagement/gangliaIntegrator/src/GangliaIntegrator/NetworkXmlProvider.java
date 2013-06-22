package GangliaIntegrator;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;



public class NetworkXmlProvider {
	
	Socket _socket;
	
	public NetworkXmlProvider(){}
	
	
	
	public byte [] GetXml() throws IOException{
	
		_socket = 
			new Socket(GangliaIntegratorLauncher.GetProperty(GangliaIntegratorLauncher.GMETAD_HOST), 
					Integer.parseInt(GangliaIntegratorLauncher.GetProperty(GangliaIntegratorLauncher.GMETAD_PORT)));
		
		ArrayList<Byte> xmlBuffer = new ArrayList<Byte>();  
		byte [] tmpBuffer = new byte[1024];
		
		int bytesRead = 0; 

		try{
			while( (bytesRead =  _socket.getInputStream().read(tmpBuffer, 0, 1024)) != -1){
				for(int i = 0; i < bytesRead; i++)
					xmlBuffer.add(tmpBuffer[i]);
			}
		}
		catch(IOException ioe){
			System.out.println("Could not read from the socket. Error: " + ioe.getMessage());
			return getByteArray(xmlBuffer);
		}
		
		_socket.close();
		return getByteArray(xmlBuffer);
	}
	
	private byte [] getByteArray(ArrayList<Byte> arrayList){
		
		byte [] byteArray = new byte[arrayList.size()];
		
		for(int i= 0; i < arrayList.size(); i++)
			byteArray[i] = arrayList.get(i);
		
		return byteArray;
	}
	
}
