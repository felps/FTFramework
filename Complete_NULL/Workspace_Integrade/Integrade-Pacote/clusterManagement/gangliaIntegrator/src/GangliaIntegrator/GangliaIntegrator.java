package GangliaIntegrator;


import java.util.ArrayList;
import java.util.HashMap;



import clusterManagement.GangliaIntegratorPOA;

import org.apache.avalon.framework.logger.Logger;

import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.CORBA.ORB;
import org.omg.ETF.Profile;

public class GangliaIntegrator extends GangliaIntegratorPOA implements Runnable{
	
	
	//TODO: read from config file
	private static final int MAXIMUM_ZOMBIE_PERIOD = 120000;
	
	private HashMap<String, ArrayList<String>> _hostToLRMIOR;
	private HashMap<String, Long> _lrmLastUpdate;
	
	private ORB _orb;
	
	private NetworkXmlProvider _xmlProvider;
	private TraderManager _traderManager;
	
	public GangliaIntegrator(ORB orb) throws Exception{

		_orb = orb;
		
		_hostToLRMIOR = new HashMap<String, ArrayList<String>>();
		_lrmLastUpdate = new  HashMap<String, Long>();
		
		_xmlProvider = new NetworkXmlProvider();
		_traderManager = new TraderManager(_orb);

		//TODO: Decidir se apagaremos as ofertas existentes
		
		
		if(!_traderManager.doesServiceTypeExist("NodeInformation"))
			_traderManager.createNodeInformationServiceType();
		
		new Thread(this).start();
	}
		
	private String getHostFromIOR(String IOR){
		
		Logger logger = 
			((org.jacorb.orb.ORB)_orb).getConfiguration().getNamedLogger("jacorb.print_ior");
		
		            
		
		org.jacorb.orb.ParsedIOR parsedIOR = 
			new org.jacorb.orb.ParsedIOR(IOR,
					_orb, logger);
		
		
		Profile profile = parsedIOR.getEffectiveProfile();
		
		IIOPProfile iiopProfile = (IIOPProfile) profile;
		
		String host = ((IIOPAddress)iiopProfile.getAddress()).getOriginalHost();
		
		return host;
	}
	
	public void run(){
		
		try{
		while (true) {
			
			byte [] data = _xmlProvider.GetXml();
			HostInformation [] hostInformationarray = 
				InformationCollector.Collect(data);
			
			for(HostInformation hostInformation : hostInformationarray){
			
				System.out.println("Host ip: " + hostInformation.ipAddress() + " name: " + hostInformation.hostName()); 
					
					synchronized (_hostToLRMIOR) {
						
						if(_hostToLRMIOR.containsKey(hostInformation.ipAddress())){
							for(String lrmIOR: _hostToLRMIOR.get(hostInformation.ipAddress()))
								_traderManager.insertNodeInformation(lrmIOR, hostInformation);
						}
						else if(_hostToLRMIOR.containsKey(hostInformation.hostName())){
							for(String lrmIOR: _hostToLRMIOR.get(hostInformation.hostName()))
								_traderManager.insertNodeInformation(lrmIOR, hostInformation);
						}
						
					}
			}

			//Remove old LRMs
			long currentTime = System.currentTimeMillis();
			
			ArrayList<String> removals = new ArrayList<String>();
			
			synchronized (_lrmLastUpdate) {
				
				for (String lrmIOR : _lrmLastUpdate.keySet()) {
					if (currentTime - _lrmLastUpdate.get(lrmIOR) > MAXIMUM_ZOMBIE_PERIOD) {

						synchronized (_hostToLRMIOR) {
							_hostToLRMIOR.get(getHostFromIOR(lrmIOR)).remove(
									lrmIOR);
						}
							removals.add(lrmIOR);
						_traderManager.removeNodeInformation(lrmIOR);
					}
				}
				
				for(String lrmIOR : removals)
					_lrmLastUpdate.remove(lrmIOR);
			}
			
			
			//Tasks:
			
			//DONE: Register new hosts
			//DONE: Update existing hosts
			//DONE: Update LRM offers on existing hosts

			java.lang.Thread.sleep(
					Integer.parseInt(GangliaIntegratorLauncher.GetProperty(
							GangliaIntegratorLauncher.LOOKUP_INTERVAL)) * 1000);
		}
		}
		catch(Exception e){
			System.err.println("GangliaIntegrator::run: fatal error");
			e.printStackTrace();
		}
	}
	
	public void registerLrm(String LRMIOR){
		
		
		//TODO: If The IOR contains a hostname, convert to an IP address 
		
		String host = getHostFromIOR(LRMIOR);
		System.out.println("GangliaIntegrator::registerLrm: host: <" +
		host + "> ior: <" + LRMIOR + ">"); 
		
		synchronized (_hostToLRMIOR) {
			
			if(!_hostToLRMIOR.containsKey(host))
				_hostToLRMIOR.put(host, new ArrayList<String>());
			
				
			
			_hostToLRMIOR.get(host).add(LRMIOR);
		}
		
		synchronized (_lrmLastUpdate) {
			_lrmLastUpdate.put(LRMIOR, System.currentTimeMillis());	
		}
	}

	public void lrmKeepAlive(String LRMIOR){
		
				System.out.println("GangliaIntegrator::lrmKeepAlive: ior: <" +
				LRMIOR + ">"); 

		
		synchronized (_lrmLastUpdate) {
			_lrmLastUpdate.put(LRMIOR, System.currentTimeMillis());	
		}
	}
	
	
}
