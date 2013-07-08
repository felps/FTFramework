package br.usp.ime.oppstore.cdrm;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import rice.environment.Environment;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreCapacityProtocol;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreIdProtocol;

public class CdrmLauncher {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
                
		if (args.length > 0)
			configureCdrmProtocol(args[0]);
		else
			configureCdrmProtocol(null);
                
        Environment env = new Environment();
        env.getParameters().setInt("pastry_lSetSize", 8);                
                
		try {

	        int localBindPort = 9001;
	        int bootstrapHostPort = 9001;
	        String bootstrapHost = null;
	        String localAddress = null;
	        try {
	        	//BufferedReader cdrmProperties = new BufferedReader(new InputStreamReader(new FileInputStream("cdrm.properties")));
	        	Properties cdrmProperties = new Properties();
	        	cdrmProperties.load(new FileInputStream("cdrm.properties"));
	        	bootstrapHost = cdrmProperties.getProperty("bootstrapHost");
	        	localAddress  = cdrmProperties.getProperty("localAddress");
	        	
	        	String localPortStr = cdrmProperties.getProperty("localPort");
	        	if (localPortStr != null) localBindPort = Integer.parseInt(localPortStr);
	        	String bootstrapPortStr = cdrmProperties.getProperty("bootstrapPort");
	        	if (bootstrapPortStr != null) bootstrapHostPort = Integer.parseInt(bootstrapPortStr);
	        	
	        	System.out.println("booststrapHost = " + bootstrapHost + " bootstrapPort = " + bootstrapPortStr + 
	        					   " localAddress = " + localAddress + " localPort = " + localPortStr);
	        }
	        catch(Exception e) {};
	        	        
	        InetSocketAddress bootstrapNodeAddress = null;
	        if (bootstrapHost == null)
	        	bootstrapNodeAddress = new InetSocketAddress(InetAddress.getLocalHost(), bootstrapHostPort);
	        else
	        	bootstrapNodeAddress = new InetSocketAddress(bootstrapHost, bootstrapHostPort);
	        
	        SocketPastryNodeFactory nodeFactory = null;
	        if (localAddress == null)
	        	nodeFactory = new SocketPastryNodeFactory(new RandomNodeIdFactory(env), localBindPort, env);
	        else
	        	nodeFactory = new SocketPastryNodeFactory(new RandomNodeIdFactory(env), InetAddress.getByName(localAddress), localBindPort, env);
	        
	        NodeHandle bootstrapNodeHandle = nodeFactory.getNodeHandle(bootstrapNodeAddress);
	        CdrmApp cdrmApp = new CdrmApp(nodeFactory, null, bootstrapNodeHandle, null);
	        
	    	Logger logger = cdrmApp.getLogger();
	        logger.info("Starting CDRM at " + InetAddress.getLocalHost() + ".");
	        logger.info("Using " + CdrmApp.idProtocol + " " + CdrmApp.capacityProtocol);
	        logger.debug("Registering CDRM at CORBA Name Service.");
	        
			// CORBA runtime initialization			
			ORB orb = ORB.init(new String[] {}, null);
			POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			poa.the_POAManager().activate();

			CdrmRequestsImpl cdrmRequestsImpl = new CdrmRequestsImpl(cdrmApp.getFileStorageRetrievalManager(), env, orb);

			NamingContextExt nameService = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

			// Registration of GRM and ExecutionManager on name server
			Object cdrmRequestsObject = poa.servant_to_reference(cdrmRequestsImpl);
			nameService.rebind(nameService.to_name("CdrmRequests"), cdrmRequestsObject);
			Object adrManagerObj = poa.servant_to_reference( cdrmApp.getAdrManager() );
			nameService.rebind(nameService.to_name("AdrManager"), adrManagerObj);

			logger.debug ("Waiting the CDRM to become ready.");
			System.out.println("Waiting the CDRM to become ready.");
			
	    	while ( cdrmApp.getVirtualNode().getNode().isReady() == false ) {
	        	try { Thread.sleep( 100 ); }
	        	catch (InterruptedException e) {}
	    	}
			
			logger.info("CDRM launched successfully!");
			System.out.println("CDRM launched successfully!");

			orb.run();
		}
		catch(Exception exception) {
			exception.printStackTrace();			
			System.err.println("Error launching CDRM.");
		}

	}

	private static void configureCdrmProtocol(String protocol) {
		
		if ( protocol != null ) {
        	if ( protocol.compareTo("pi") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.PASTRY;
        	}
        	else if ( protocol.compareTo("ai-linear") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
        		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.LINEAR;
        	}
        	else if ( protocol.compareTo("ai-quad") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
        		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.QUADRATIC;
        	}
        	else if ( protocol.compareTo("ai-hyper") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
        		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.HIPERBOLIC;
        	}
        	else if ( protocol.compareTo("ai-sqrt") == 0 ) {
        		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
        		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.SQUARED;
        	}        		
        	else {
        		System.out.println("ERROR!!!!");
        		System.exit(-1);
        	}
        }
        else {
    		CdrmApp.idProtocol = OpStoreIdProtocol.ADAPTIVE;
    		CdrmApp.capacityProtocol = OpStoreCapacityProtocol.LINEAR;
        }
	}

}
