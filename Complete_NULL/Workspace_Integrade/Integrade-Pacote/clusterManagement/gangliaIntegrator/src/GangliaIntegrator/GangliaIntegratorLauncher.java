package GangliaIntegrator;
import java.io.FileInputStream;
import java.io.IOException;


import org.omg.CORBA.ORB;

import org.omg.CORBA.Object;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


public class GangliaIntegratorLauncher {
	                                              
	static final String PROPERTIES_FILENAME = "ClusterInformationCollector.properties";

	public static final String GMETAD_HOST = "gmetad.host";
	public static final String GMETAD_PORT = "gmetad.port";
	public static final String LOOKUP_INTERVAL = "lookup.interval";
	public static final String CLUSTER_NAME = "cluster.name";

	static java.util.Properties _properties;

	public static String GetProperty(String propertyName) {

		return _properties.getProperty(propertyName);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		// Setup configuration
		try {
			FileInputStream propertiesFile = new FileInputStream(
					PROPERTIES_FILENAME);
			_properties = new java.util.Properties();
			_properties.load(propertiesFile);
			propertiesFile.close();
		} catch (Exception e) {
			System.out
					.println("Launcher::main: could not load properties file: "
							+ PROPERTIES_FILENAME);
			System.exit(1);
		}

		String[] expectedProperties = new String[] { "gmetad.host",
				"gmetad.port", "lookup.interval", "cluster.name" };

		for (String expectedProperty : expectedProperties)
			if (!_properties.containsKey(expectedProperty)) {

				System.out
						.println("Launcher::main: property not found in configuration file: "
								+ expectedProperty);
				System.exit(1);
			}
		
		//Create the XML provider
		try {		 
			ORB orb = ORB.init(new String[] {}, null);
			POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			poa.the_POAManager().activate();

			GangliaIntegrator gangliaIntegrator = 
				new GangliaIntegrator(orb);

			Object gangliaIntegratorObject =
				poa.servant_to_reference(gangliaIntegrator);

			NamingContextExt nameService = 
				NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
			nameService.rebind(nameService.to_name("GangliaIntegrator"), gangliaIntegratorObject);
			
			orb.run();

		} catch (Exception e) {
			System.err.println("Error launching the Ganglia integrator.");
			e.printStackTrace();
			System.exit(-1);
		}	
	}
}
