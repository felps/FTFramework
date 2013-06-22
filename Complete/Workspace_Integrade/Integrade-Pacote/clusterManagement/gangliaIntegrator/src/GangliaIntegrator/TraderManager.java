package GangliaIntegrator;

import java.util.HashMap;

import org.omg.CORBA.ORB;
import org.omg.CosTrading.Property;


import org.omg.CosTrading.RegisterPackage.NoMatchingOffers;
import org.omg.CosTrading.UnknownServiceType;
import org.omg.CosTrading.IllegalConstraint;
import org.omg.CosTrading.IllegalServiceType;

import org.omg.CosTradingRepos.ServiceTypeRepository;
import org.omg.CosTradingRepos.ServiceTypeRepositoryHelper;

import org.omg.CosTrading.Lookup;
import org.omg.CosTrading.LookupHelper;

import org.omg.CosTrading.Register;
import org.omg.CosTrading.RegisterHelper;

import org.omg.CORBA.TypeCode;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.PropertyMode;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.PropStruct;
import org.omg.CORBA.TCKind;


public class TraderManager {
	
	private ORB _orb;
	
	private static final int PROPERTY_COUNT = 14;
	private static final String HOST_INFORMATION_SERVICE_NAME = "NodeInformation";
	
	private HashMap<String, String> _lrmIORToOfferIDs;
	
	private Register _register;
	private Lookup _lookup;
	private ServiceTypeRepository _serviceTypeRepository;
	
	
	
	public TraderManager(ORB orb) throws Exception{
	
		_orb = orb;
		
		
		_lookup = LookupHelper.narrow(_orb.resolve_initial_references("TradingService"));
    	_register = RegisterHelper.narrow(_lookup.register_if());
    	_serviceTypeRepository = ServiceTypeRepositoryHelper.narrow(_lookup.type_repos());
		
		_lrmIORToOfferIDs = new HashMap<String, String>();
		
		if(doesServiceTypeExist(HOST_INFORMATION_SERVICE_NAME))
			removeAllOffers(HOST_INFORMATION_SERVICE_NAME);
		//else
						
	}
	
	public void removeAllOffers(String serviceType) {
		try {
			_register.withdraw_using_constraint(serviceType, "TRUE");
		}
		catch(NoMatchingOffers noMatchingOffersException) {
			// If the method is called when there are no offers in the trader.
			// This happens when there is no registered LRMs. Should do nothing
		}
		catch(UnknownServiceType unknownServiceTypeException) {
			// If the method is called when the specified service type does not exist,
			// this exception is caught.
			System.err.println("Unknown service type " + serviceType + ".");
		}
		catch(IllegalServiceType illegalServiceTypeException) {
			System.err.println("FATAL: Error removing all offers for " + serviceType + " service type.");
			System.exit(1);
		}
		catch(IllegalConstraint illegalConstraintException) {
			System.err.println("FATAL: Error removing all offers for " + serviceType + " service type.");
			System.exit(1);
		}
		
		
	}
	
	public boolean doesServiceTypeExist(String serviceType) {
		
		try {
		    _serviceTypeRepository.describe_type(serviceType);
		} catch (UnknownServiceType e) {
		    // Means that the service is not registered
		    return false;
		} catch (IllegalServiceType e) {
		    System.err.println("ERROR: TraderManager.existsServiceType -> Illegal service type \"" + serviceType + "\".");
            return false;
		}
            
		return true;
	}

	public void createNodeInformationServiceType() {
		PropStruct [] properties = new PropStruct[PROPERTY_COUNT];
		
		TypeCode stringTypeCode = _orb.get_primitive_tc(TCKind.tk_string);
		TypeCode longTypeCode = _orb.get_primitive_tc(TCKind.tk_long);
		TypeCode booleanTypeCode = _orb.get_primitive_tc(TCKind.tk_boolean);
		TypeCode floatTypeCode = _orb.get_primitive_tc(TCKind.tk_float);
		
		PropertyMode mandatoryProperty = PropertyMode.PROP_MANDATORY;
		PropertyMode normalProperty = PropertyMode.PROP_NORMAL;
		
		properties[0] = new PropStruct("lrmIor", stringTypeCode, mandatoryProperty);
		properties[1] = new PropStruct("hostName", stringTypeCode, mandatoryProperty);
		properties[2] = new PropStruct("osName", stringTypeCode, mandatoryProperty);
		properties[3] = new PropStruct("osVersion",stringTypeCode, mandatoryProperty);
		properties[4] = new PropStruct("processorName", stringTypeCode, mandatoryProperty);
		properties[5] = new PropStruct("processorMhz", longTypeCode, mandatoryProperty);
		properties[6] = new PropStruct("totalRam", longTypeCode, mandatoryProperty);
		properties[7] = new PropStruct("totalSwap", longTypeCode, mandatoryProperty);
		properties[8] = new PropStruct("freeRam", longTypeCode, normalProperty);
		properties[9] = new PropStruct("freeSwap", longTypeCode, normalProperty);
		properties[10] = new PropStruct("freeDiskSpace", longTypeCode, normalProperty);
		properties[11] = new PropStruct("cpuUsage", floatTypeCode, normalProperty);
		properties[12] = new PropStruct("recentlyPicked", booleanTypeCode, mandatoryProperty);
		properties[13] = new PropStruct("lastUpdated", longTypeCode, mandatoryProperty);

		try {
			_serviceTypeRepository.add_type("NodeInformation", "IDL:Lrm:1.0", properties, new String []{});
		}
		catch(Exception exception) {
			System.err.println("FATAL: Unable to create NodeInformation service type on trader.");
			System.exit(1);
		}
		
		
	}

	

	public void insertNodeInformation(String lrmIOR, HostInformation hostInformation){
		
		Property [] hostProperties = new Property[PROPERTY_COUNT];
		
		hostProperties[0] = new Property("lrmIor", _orb.create_any());
		hostProperties[0].value.insert_string(lrmIOR);
		
		hostProperties[1] = new Property("hostName", _orb.create_any());
		hostProperties[1].value.insert_string(hostInformation.hostName());
		
		hostProperties[2] = new Property("osName", _orb.create_any());
		hostProperties[2].value.insert_string(hostInformation.osName());
		
		hostProperties[3] = new Property("osVersion", _orb.create_any());
		hostProperties[3].value.insert_string(hostInformation.osVersion());
		
		hostProperties[4] = new Property("processorName", _orb.create_any());
		hostProperties[4].value.insert_string(hostInformation.processorName());
		
		hostProperties[5] = new Property("processorMhz", _orb.create_any());
		hostProperties[5].value.insert_long(hostInformation.processorMhz());
		
		hostProperties[6] = new Property("totalRam", _orb.create_any());
		hostProperties[6].value.insert_long(hostInformation.totalRam());
		
		hostProperties[7] = new Property("totalSwap", _orb.create_any());
		hostProperties[7].value.insert_long(hostInformation.totalSwap());
		
		hostProperties[8] = new Property("lastUpdated", _orb.create_any());
		hostProperties[8].value.insert_long(hostInformation.lastUpdated());
		
		hostProperties[9] = new Property("freeRam", _orb.create_any());
		hostProperties[9].value.insert_long(hostInformation.freeRam());
		
		hostProperties[10] = new Property("freeSwap", _orb.create_any());
		hostProperties[10].value.insert_long(hostInformation.freeSwap());
		
		hostProperties[11] = new Property("freeDiskSpace", _orb.create_any());
		hostProperties[11].value.insert_long(hostInformation.freeDiskSpace());
		
		hostProperties[12] = new Property("cpuUsage", _orb.create_any());
		hostProperties[12].value.insert_float(hostInformation.cpuUsage());
		
		hostProperties[13] = new Property("recentlyPicked", _orb.create_any());
		hostProperties[13].value.insert_boolean(false);

		
		synchronized (_lrmIORToOfferIDs) {

			if(_lrmIORToOfferIDs.containsKey(lrmIOR)){
				try{
					
					System.out.println("TraderManager::insertNodeInformation: updating lrm: "+ lrmIOR);
					_register.modify(_lrmIORToOfferIDs.get(lrmIOR), new String [] {}, hostProperties);
				}
				catch(Exception e){
					System.out.println("TraderManager::insertNodeInformation: could not update offer in trader. Exception message: " + e.getMessage());
				}
			}
			else{
				try{
					System.out.println("TraderManager::insertNodeInformation: adding lrm: " + lrmIOR);
					String offerId = _register.export(_orb.string_to_object(lrmIOR), "NodeInformation", hostProperties);
					_lrmIORToOfferIDs.put(lrmIOR, offerId);
				}
				catch(Exception e){
					System.out.println("TraderManager::insertNodeInformation: could not insert offer in trader. Exception message: " + e.getMessage());
				}
			}
		}
	}
	
	public void removeNodeInformation(String lrmIOR){
		
		System.out.println("TraderManager::removeNodeInformation: removing lrm: " + lrmIOR);
		
		synchronized (_lrmIORToOfferIDs) {
		
			if(!_lrmIORToOfferIDs.containsKey(lrmIOR))
				return;
			
			try{
				_register.withdraw(_lrmIORToOfferIDs.get(lrmIOR));
				_lrmIORToOfferIDs.remove(lrmIOR);
			}
			
			catch(Exception e){
				System.out.println("TraderManager::removeNodeInformation: could not remove offer from trader. Exception message: " + e.getMessage());
			}
		}
	}
}
