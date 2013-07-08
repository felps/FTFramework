package grm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosTrading.IllegalConstraint;
import org.omg.CosTrading.IllegalServiceType;
import org.omg.CosTrading.Lookup;
import org.omg.CosTrading.LookupHelper;
import org.omg.CosTrading.OfferIteratorHolder;
import org.omg.CosTrading.OfferSeqHolder;
import org.omg.CosTrading.Policy;
import org.omg.CosTrading.PolicyNameSeqHolder;
import org.omg.CosTrading.Property;
import org.omg.CosTrading.Register;
import org.omg.CosTrading.RegisterHelper;
import org.omg.CosTrading.UnknownServiceType;
import org.omg.CosTrading.LookupPackage.HowManyProps;
import org.omg.CosTrading.LookupPackage.SpecifiedProps;
import org.omg.CosTrading.RegisterPackage.NoMatchingOffers;
import org.omg.CosTradingRepos.ServiceTypeRepository;
import org.omg.CosTradingRepos.ServiceTypeRepositoryHelper;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.PropStruct;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.PropertyMode;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.TypeStruct;

import dataTypes.ApplicationExecutionInformation;
import dataTypes.Histogram;
import dataTypes.NodeDynamicHistograms;
import dataTypes.NodeDynamicInformation;
import dataTypes.NodeStaticHistograms;
import dataTypes.NodeStaticInformation;
import dataTypes.SubtreeInformation;
import dataTypes.WrongHistogramTypeException;

// Class TraderManager
//
// Main functions:
// Repository for resource availability information, using CORBA Trader
//
// @author Hammurabi Mendes

public class TraderManager {
	// Number of LRM static features (e.g. OS name, CPU class)
	private static final int NUM_STATIC_FEATURES = 7;

	// Number of LRM dynamic features (e.g. free CPU load)
	private static final int NUM_DYNAMIC_FEATURES = 4;

	// Number of LRM measurable features (e.g. processor MHz, free RAM)
	private static final int NUM_MEASURABLE_FEATURES = 7;

	public static final int NUM_HISTOGRAM_INTERVALS = 5;

	private static final int NUM_FEATURES = NUM_STATIC_FEATURES + NUM_DYNAMIC_FEATURES;

	// This is the ORB used to access CORBA services
	private ORB orb;

	// These HashMaps keep track of registered LRMs and GRMs
	private HashMap<String, String> registeredLrms;
	private HashMap<String, String> registeredGrms;

	// This is a reference to the Trader Lookup interface
	private Lookup lookup;

	// This is a reference to the Trader Registry interface
	private Register register;

	// This is a reference to the Lookup's ServiceTypeRepository interface
	private ServiceTypeRepository serviceTypeRepository;

	// Creates a new TraderManager object
	//
	// @param orb - A reference to an ORB, used to access CORBA services

	public TraderManager(ORB orb) {
		this.orb = orb;

		registeredLrms = new HashMap<String, String>();
		registeredGrms = new HashMap<String, String>();

		try {
			lookup = LookupHelper.narrow(orb.resolve_initial_references("TradingService"));

			register = RegisterHelper.narrow(lookup.register_if());
			serviceTypeRepository = ServiceTypeRepositoryHelper.narrow(lookup.type_repos());
		} catch (InvalidName invalidNameException) {
			invalidNameException.printStackTrace();
		}

		if (!existsServiceType("NodeInformation")) {
			createNodeInformationServiceType();
		}

		if (!existsServiceType("SubtreeInformation")) {
			createSubtreeInformationServiceType();
		}

		removeAllOffers("NodeInformation");
		removeAllOffers("SubtreeInformation");
	}

	// ----------------------------------
	// Trader service type initialization
	// ----------------------------------

	// Checks if a given service type exists in the Trader
	//
	// @param serviceType - The name of the service type
	//
	// @returns - True if the service type exists, false otherwise

	private boolean existsServiceType(String serviceType) {
		TypeStruct typeDescription;

		try {
			typeDescription = serviceTypeRepository.describe_type(serviceType);
		} catch (UnknownServiceType e) {
			// Means that the service is not registered
			return false;
		} catch (IllegalServiceType e) {
			System.err.println("ERROR: TraderManager.existsServiceType -> Illegal service type \"" + serviceType + "\".");
			return false;
		}

		return true;
	}

	// Removes all offers of a given service type from the Trader
	//
	// @param serviceType - The name of the service type

	private void removeAllOffers(String serviceType) {
		try {
			register.withdraw_using_constraint(serviceType, "TRUE");
		} catch (NoMatchingOffers noMatchingOffersException) {
			// If the method is called when there are no offers in the trader.
			// This happens when there is no registered LRMs. Should do nothing
		} catch (UnknownServiceType unknownServiceTypeException) {
			// If the method is called when the specified service type does not
			// exist,
			// this exception is caught.
			System.err.println("Unknown service type " + serviceType + ".");
		} catch (IllegalServiceType illegalServiceTypeException) {
			System.err.println("FATAL: Error removing all offers for " + serviceType + " service type.");
			System.exit(1);
		} catch (IllegalConstraint illegalConstraintException) {
			System.err.println("FATAL: Error removing all offers for " + serviceType + " service type.");
			System.exit(1);
		}
	}

	// Creates a NodeInformation service within the trader, related to the
	// NodeInformation type

	private void createNodeInformationServiceType() {
		PropStruct[] properties = new PropStruct[NUM_FEATURES + 3];

		TypeCode stringTypeCode = orb.get_primitive_tc(TCKind.tk_string);
		TypeCode longTypeCode = orb.get_primitive_tc(TCKind.tk_long);
		TypeCode booleanTypeCode = orb.get_primitive_tc(TCKind.tk_boolean);
		TypeCode floatTypeCode = orb.get_primitive_tc(TCKind.tk_float);

		PropertyMode mandatoryProperty = PropertyMode.PROP_MANDATORY;
		PropertyMode normalProperty = PropertyMode.PROP_NORMAL;

		properties[0] = new PropStruct("lrmIor", stringTypeCode, mandatoryProperty);
		properties[1] = new PropStruct("hostName", stringTypeCode, mandatoryProperty);
		properties[2] = new PropStruct("osName", stringTypeCode, mandatoryProperty);
		properties[3] = new PropStruct("osVersion", stringTypeCode, mandatoryProperty);
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
			serviceTypeRepository.add_type("NodeInformation", "IDL:Lrm:1.0", properties, new String[] {});
		} catch (Exception exception) {
			System.err.println("FATAL: Unable to create NodeInformation service type on trader.");
			System.exit(1);
		}
	}

	// Creates a SubtreeInformation service within the trader, related to the
	// SubtreeInformation type

	private void createSubtreeInformationServiceType() {
		PropStruct[] properties = new PropStruct[NUM_MEASURABLE_FEATURES * NUM_HISTOGRAM_INTERVALS + 3];
		int index = 0;
		
		TypeCode stringTypeCode = orb.get_primitive_tc(TCKind.tk_string);
		TypeCode longTypeCode = orb.get_primitive_tc(TCKind.tk_long);
		TypeCode booleanTypeCode = orb.get_primitive_tc(TCKind.tk_boolean);		

		PropertyMode mandatoryProperty = PropertyMode.PROP_MANDATORY;
		PropertyMode normalProperty = PropertyMode.PROP_NORMAL;		

		properties[index++] = new PropStruct("childGrmIor", stringTypeCode, mandatoryProperty);		
		
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			properties[index++] = new PropStruct("processorMhz_" + i, longTypeCode, mandatoryProperty);
		}
		
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			properties[index++] = new PropStruct("totalRam_" + i, longTypeCode, mandatoryProperty);
		}
		
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			properties[index++] = new PropStruct("totalSwap_" + i, longTypeCode, mandatoryProperty);
		}
		
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			properties[index++] = new PropStruct("freeRam_" + i, longTypeCode, normalProperty);
		}
		
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			properties[index++] = new PropStruct("freeSwap_" + i, longTypeCode, normalProperty);
		}
		
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			properties[index++] = new PropStruct("freeDiskSpace_" + i, longTypeCode, normalProperty);
		}
		
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			properties[index++] = new PropStruct("cpuUsage_" + i, longTypeCode, normalProperty);
		}
				
		properties[index++] = new PropStruct("recentlyPicked", booleanTypeCode, mandatoryProperty);
		properties[index++] = new PropStruct("lastUpdated", longTypeCode, mandatoryProperty);

		try {
			serviceTypeRepository.add_type("SubtreeInformation", "IDL:Grm:1.0", properties, new String[] {});
		} catch (Exception exception) {
			System.err.println("FATAL: Unable to create SubtreeInformation service type on trader.");
			System.exit(1);
		}
	}

	// ---------------------
	// Property manipulators
	// ---------------------

	// Creates properties used to insert NodeStaticInformation on the Trader
	//
	// @param lrmIor - LRM's IOR
	// @param nodeStaticInformation - Node static information
	//
	// @returns - Properties used to put corresponding information on the Trader

	private Property[] createNodeStaticInformationProperties(String lrmIor, NodeStaticInformation nodeStaticInformation) {
		Property[] properties = new Property[NUM_STATIC_FEATURES + 3];

		properties[0] = new Property("lrmIor", orb.create_any());
		properties[0].value.insert_string(lrmIor);

		properties[1] = new Property("hostName", orb.create_any());
		properties[1].value.insert_string(nodeStaticInformation.hostName);

		properties[2] = new Property("osName", orb.create_any());
		properties[2].value.insert_string(nodeStaticInformation.osName);

		properties[3] = new Property("osVersion", orb.create_any());
		properties[3].value.insert_string(nodeStaticInformation.osVersion);

		properties[4] = new Property("processorName", orb.create_any());
		properties[4].value.insert_string(nodeStaticInformation.processorName);

		properties[5] = new Property("processorMhz", orb.create_any());
		properties[5].value.insert_long(nodeStaticInformation.processorMhz);

		properties[6] = new Property("totalRam", orb.create_any());
		properties[6].value.insert_long(nodeStaticInformation.totalRam);

		properties[7] = new Property("totalSwap", orb.create_any());
		properties[7].value.insert_long(nodeStaticInformation.totalSwap);

		properties[8] = new Property("recentlyPicked", orb.create_any());
		properties[8].value.insert_boolean(false);

		properties[9] = new Property("lastUpdated", orb.create_any());
		properties[9].value.insert_long((int) (new Date()).getTime() / 1000);

		return properties;
	}

	// Creates properties used to insert NodeDynamicInformation on the Trader
	//
	// @param lrmIor - LRM's IOR
	// @param nodeDynamicInformation - Node dynamic information
	//
	// @returns - Properties used to put corresponding information on the Trader

	private Property[] createNodeDynamicInformationProperties(NodeDynamicInformation nodeDynamicInformation) {
		Property[] properties = new Property[NUM_DYNAMIC_FEATURES + 2];

		properties[0] = new Property("freeRam", orb.create_any());
		properties[0].value.insert_long(nodeDynamicInformation.freeRam);

		properties[1] = new Property("freeSwap", orb.create_any());
		properties[1].value.insert_long(nodeDynamicInformation.freeSwap);

		properties[2] = new Property("freeDiskSpace", orb.create_any());
		properties[2].value.insert_long(nodeDynamicInformation.freeDiskSpace);

		properties[3] = new Property("cpuUsage", orb.create_any());
		properties[3].value.insert_float(nodeDynamicInformation.cpuUsage);

		properties[4] = new Property("recentlyPicked", orb.create_any());
		properties[4].value.insert_boolean(false);

		properties[5] = new Property("lastUpdated", orb.create_any());
		properties[5].value.insert_long((int) (new Date()).getTime() / 1000);

		return properties;
	}

	// Creates properties used to insert SubtreeInformation on the Trader
	//
	// @param childGrmIor - GRM's IOR
	// @param subtreeInformation - Subtree information
	//
	// @returns - Properties used to put corresponding information on the Trader

	private Property[] createSubtreeInformationProperties(String childGrmId, SubtreeInformation subtreeInformation) {

		Property[] properties = new Property[NUM_MEASURABLE_FEATURES * NUM_HISTOGRAM_INTERVALS + 3];
		int[] tempIntervalQuantities;
		int index = 0;

		properties[index] = new Property("childGrmIor", orb.create_any());
		properties[index].value.insert_string(childGrmId);
		index++;

		tempIntervalQuantities = subtreeInformation.staticHistograms.processorMhz.intervalQuantities;
		for (int i = 0; i < tempIntervalQuantities.length; i++) {
			properties[index] = new Property("processorMhz_" + i, orb.create_any());
			properties[index].value.insert_long(tempIntervalQuantities[i]);
			index++;
		}

		tempIntervalQuantities = subtreeInformation.staticHistograms.totalRam.intervalQuantities;
		for (int i = 0; i < tempIntervalQuantities.length; i++) {
			properties[index] = new Property("totalRam_" + i, orb.create_any());
			properties[index].value.insert_long(tempIntervalQuantities[i]);
			index++;
		}

		tempIntervalQuantities = subtreeInformation.staticHistograms.totalSwap.intervalQuantities;
		for (int i = 0; i < tempIntervalQuantities.length; i++) {
			properties[index] = new Property("totalSwap_" + i, orb.create_any());
			properties[index].value.insert_long(tempIntervalQuantities[i]);
			index++;
		}

		tempIntervalQuantities = subtreeInformation.dynamicHistograms.freeRam.intervalQuantities;
		for (int i = 0; i < tempIntervalQuantities.length; i++) {
			properties[index] = new Property("freeRam_" + i, orb.create_any());
			properties[index].value.insert_long(tempIntervalQuantities[i]);
			index++;
		}

		tempIntervalQuantities = subtreeInformation.dynamicHistograms.freeSwap.intervalQuantities;
		for (int i = 0; i < tempIntervalQuantities.length; i++) {
			properties[index] = new Property("freeSwap_" + i, orb.create_any());
			properties[index].value.insert_long(tempIntervalQuantities[i]);
			index++;
		}

		tempIntervalQuantities = subtreeInformation.dynamicHistograms.freeDiskSpace.intervalQuantities;
		for (int i = 0; i < tempIntervalQuantities.length; i++) {
			properties[index] = new Property("freeDiskSpace_" + i, orb.create_any());
			properties[index].value.insert_long(tempIntervalQuantities[i]);
			index++;
		}

		tempIntervalQuantities = subtreeInformation.dynamicHistograms.cpuUsage.intervalQuantities;
		for (int i = 0; i < tempIntervalQuantities.length; i++) {
			properties[index] = new Property("cpuUsage_" + i, orb.create_any());
			properties[index].value.insert_long(tempIntervalQuantities[i]);
			index++;
		}

		properties[index] = new Property("recentlyPicked", orb.create_any());
		properties[index].value.insert_boolean(false);
		index++;

		properties[index] = new Property("lastUpdated", orb.create_any());
		properties[index].value.insert_long((int) (new Date()).getTime() / 1000);

		return properties;
	}

	// Convert properties fetched from the trader in a SubtreeInformation
	// structure
	//
	// @param properties - Properties fetched from the trader
	//
	// @returns - A SubtreeInformation structure

	private SubtreeInformation createSubtreeInformation(Property[] properties) {

		SubtreeInformation subtreeInformation = initSubTreeInformation();

		for (int counter = 0; counter < properties.length; counter++) {

			for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
				if (properties[counter].name.equals("processorMhz_" + i)) {
					subtreeInformation.staticHistograms.processorMhz.intervalQuantities[i] = properties[counter].value.extract_long();
				}
			}

			for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
				if (properties[counter].name.equals("totalRam_" + i)) {
					subtreeInformation.staticHistograms.totalRam.intervalQuantities[i] = properties[counter].value.extract_long();
				}
			}

			for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
				if (properties[counter].name.equals("totalSwap_" + i)) {
					subtreeInformation.staticHistograms.totalSwap.intervalQuantities[i] = properties[counter].value.extract_long();
				}
			}

			for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
				if (properties[counter].name.equals("freeRam_" + i)) {
					subtreeInformation.dynamicHistograms.freeRam.intervalQuantities[i] = properties[counter].value.extract_long();
				}
			}

			for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
				if (properties[counter].name.equals("freeSwap_" + i)) {
					subtreeInformation.dynamicHistograms.freeSwap.intervalQuantities[i] = properties[counter].value.extract_long();
				}
			}

			for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
				if (properties[counter].name.equals("freeDiskSpace_" + i)) {
					subtreeInformation.dynamicHistograms.freeDiskSpace.intervalQuantities[i] = properties[counter].value.extract_long();
				}
			}

			for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
				if (properties[counter].name.equals("cpuUsage_" + i)) {
					subtreeInformation.dynamicHistograms.cpuUsage.intervalQuantities[i] = properties[counter].value.extract_long();
				}
			}

		}

		calcStandardDeviation(subtreeInformation.staticHistograms.processorMhz);
		calcStandardDeviation(subtreeInformation.staticHistograms.totalRam);
		calcStandardDeviation(subtreeInformation.staticHistograms.totalSwap);
		calcStandardDeviation(subtreeInformation.dynamicHistograms.freeRam);
		calcStandardDeviation(subtreeInformation.dynamicHistograms.freeSwap);
		calcStandardDeviation(subtreeInformation.dynamicHistograms.freeDiskSpace);
		calcStandardDeviation(subtreeInformation.dynamicHistograms.cpuUsage);

		return subtreeInformation;
	}

	// ------------------------------------
	// LRM registration, update and removal
	// ------------------------------------

	public void registerLrm(String lrmIor, NodeStaticInformation nodeStaticInformation) {

		// FIXME: Ignore the information is not the best choice in this case
		synchronized (registeredLrms) {
			if (registeredLrms.get(lrmIor) != null) {
				System.out.println("Attempt to double register a LRM.");
				// dumpNodeStaticInformation(nodeStaticInformation);
				return;
			}
		}

		Property[] properties = createNodeStaticInformationProperties(lrmIor, nodeStaticInformation);

		try {
			String offerId;

			synchronized (registeredLrms) {
				offerId = register.export(orb.string_to_object(lrmIor), "NodeInformation", properties);
				registeredLrms.put(lrmIor, offerId);
			}

			System.out.println("Registered LRM from '" + nodeStaticInformation.hostName + "' with id '" + offerId + "'.");
			// dumpNodeStaticInformation(nodeStaticInformation);
		} catch (Exception exception) {
			System.err.println("Unable to register LRM.");
			// dumpNodeStaticInformation(nodeStaticInformation);
		}
	}

	// Receive an information update from an LRM
	//
	// @param lrmIor - LRM's IOR
	// @param nodeDynamicInformation - Node dynamic information

	public void updateLrmInformation(String lrmIor, NodeDynamicInformation nodeDynamicInformation) {
		// Verifies if the LRM is already registered

		synchronized (registeredLrms) {
			if (registeredLrms.get(lrmIor) == null) {
				System.out.println("Attempt to update a LRM which is not registered.");
				// dumpNodeDynamicInformation(nodeDynamicInformation);
				return;
			}
		}

		Property[] properties = createNodeDynamicInformationProperties(nodeDynamicInformation);

		String offerId;

		try {
			synchronized (registeredLrms) {
				offerId = registeredLrms.get(lrmIor);
				register.modify(offerId, new String[] {}, properties);
				System.out.println("Updated LRM with id '" + offerId + "'.");
				// dumpNodeDynamicInformation(nodeDynamicInformation);
			}
		} catch (Exception exception) {
			System.out.println("Unable to update LRM.");
			// dumpNodeDynamicInformation(nodeDynamicInformation);
		}
	}

	// Creates properties used to insert NodeStaticInformation on the Trader
	//
	// @param lrmIor - LRM's IOR
	// @param nodeStaticInformation - Node static information
	//
	// @returns - Properties used to put corresponding information on the Trader

	public void removeLrmInformation(String lrmIor) {
		try {
			synchronized (registeredLrms) {
				register.withdraw(registeredLrms.get(lrmIor));
				registeredLrms.remove(lrmIor);
			}
		} catch (Exception exception) {
			System.err.println("Unable to remove information about LRM " + lrmIor + ".");
		}
	}

	// Returns an array containing the desired properties of NodeInformation
	// entries on the Trader
	//
	// @param constraints - Contraints that the returned information should
	// comply to
	// @param preferences - Order imposed on the information returned
	// @param propertiesSpecification - Specification of property names that
	// should be returned.
	// If it is null, all properties are returned

	public ArrayList<Property[]> getLrmInformation(String constraints, String preferences, String[] propertiesSpecification) {
		String generalPreferences;

		if (preferences != null && preferences.compareTo("") != 0) {
			generalPreferences = preferences;
		} else {
			generalPreferences = "";
		}

		Set registeredLrmsIorSet = registeredLrms.keySet();

		ArrayList<Property[]> fetchedPropertiesList = new ArrayList<Property[]>();

		Iterator registeredLrmsIorIterator = registeredLrmsIorSet.iterator();

		while (registeredLrmsIorIterator.hasNext()) {
			String lrmIor = (String) registeredLrmsIorIterator.next();

			String individualConstraints;

			if (constraints != null && constraints.compareTo("") != 0) {
				individualConstraints = constraints + " and ";
			} else {
				individualConstraints = "";
			}

			individualConstraints += "lrmIor == '" + lrmIor + "'";

			OfferSeqHolder offerSeqHolder = new OfferSeqHolder();

			try {
				SpecifiedProps specifiedProps = new SpecifiedProps();

				// FIXME: Is this really necessary?

				if (propertiesSpecification == null || propertiesSpecification.length == 0) {
					specifiedProps.__default(HowManyProps.all);
				} else {
					specifiedProps.__default(HowManyProps.some);

					specifiedProps.prop_names(propertiesSpecification);
				}

				// Queries for an IOR-identified LRM.
				lookup.query("NodeInformation", individualConstraints, generalPreferences, new Policy[] {}, specifiedProps, 1,
						offerSeqHolder, new OfferIteratorHolder(), new PolicyNameSeqHolder());

				fetchedPropertiesList.add(offerSeqHolder.value[0].properties);
			} catch (Exception exception) {
				System.err.println("Error fetching offer and properties for local LRM " + lrmIor + ".");
			}
		}

		return fetchedPropertiesList;
	}

	// ------------------------------------
	// GRM registration, update and removal
	// ------------------------------------

	// Registers a child GRM within this GRM
	//
	// @param childGrmIor - GRM's IOR
	// @param subtreeInformation - Subtree information

	public void registerGrm(String childGrmIor, SubtreeInformation subtreeInformation) {
		// FIXME: Ignore the information is not the best choice in this case

		// Verifies if the GRM is already registered

		synchronized (registeredGrms) {
			if (registeredGrms.get(childGrmIor) != null) {
				System.out.println("Attempt to double register a GRM:");
				dumpSubtreeHistogram(subtreeInformation);
				return;
			}
		}

		Property[] properties = createSubtreeInformationProperties(childGrmIor, subtreeInformation);
		System.out.println("Child GRM amount of properties: " + properties.length);

		try {
			String offerId;
						
			synchronized (registeredGrms) {
				offerId = register.export(orb.string_to_object(childGrmIor), "SubtreeInformation", properties);
				registeredGrms.put(childGrmIor, offerId);
			}

			System.out.println("Registered GRM " + childGrmIor + " with offer ID of " + offerId + ":");
			dumpSubtreeHistogram(subtreeInformation);
		} catch (Exception exception) {
			System.err.println("Unable to register GRM:");			
			dumpSubtreeHistogram(subtreeInformation);			
		}
	}

	// Receive an information update from a Subtree
	//
	// @param childGrmIor - GRM's IOR
	// @param subtreeInformation - Subtree information

	public void updateGrmInformation(String childGrmIor, SubtreeInformation subtreeInformation) {
		// Verifies if the GRM is already registered

		synchronized (registeredGrms) {
			if (registeredGrms.get(childGrmIor) == null) {
				System.out.println("Attempt to update a not registered GRM:");
				dumpSubtreeHistogram(subtreeInformation);

				return;
			}
		}

		Property[] properties = createSubtreeInformationProperties(childGrmIor, subtreeInformation);

		try {
			String offerId = registeredGrms.get(childGrmIor);

			synchronized (registeredGrms) {
				register.modify(offerId, new String[] {}, properties);
			}

			System.out.println("Updated GRM " + childGrmIor + " offer ID number " + offerId + ".");
			dumpSubtreeHistogram(subtreeInformation);
		}

		catch (Exception exception) {
			System.out.println("Unable to update GRM:");
			dumpSubtreeHistogram(subtreeInformation);
		}
	}

	// Creates properties used to insert SubtreeInformation on the Trader
	//
	// @param childGrmIor - GRM's IOR
	// @param subtreeInformation - Subtree information
	//
	// @returns - Properties used to put corresponding information on the Trader

	public void removeGrmInformation(String childGrmIor) {
		try {
			synchronized (registeredGrms) {
				register.withdraw(registeredGrms.get(childGrmIor));
				registeredGrms.remove(childGrmIor);
			}
		} catch (Exception exception) {
			System.err.println("Unable to remove information about child GRM " + childGrmIor + ".");
		}
	}

	// Returns an array containing the desired properties of SubtreeInformation
	// entries on the Trader
	//
	// @param constraints - Contraints that the returned information should
	// comply to
	// @param preferences - Order imposed on the information returned
	// @param propertiesSpecification - Specification of property names that
	// should be returned.
	// If it is null, all properties are returned

	public ArrayList<Property[]> getGrmInformation(String constraints, String preferences, String[] propertiesSpecification) {
		String generalPreferences;

		if (preferences != null && preferences.compareTo("") != 0) {
			generalPreferences = preferences;
		} else {
			generalPreferences = "";
		}

		Set registeredGrmsIorSet = registeredGrms.keySet();

		ArrayList<Property[]> fetchedPropertiesList = new ArrayList<Property[]>();

		Iterator registeredGrmsIorIterator = registeredGrmsIorSet.iterator();

		while (registeredGrmsIorIterator.hasNext()) {
			String childGrmIor = (String) registeredGrmsIorIterator.next();

			System.out.println("Trying to get information from GRM: " + childGrmIor);
			String individualConstraints;

			if (constraints != null && constraints.compareTo("") != 0) {
				individualConstraints = constraints + " and ";
			} else {
				individualConstraints = "";
			}

			individualConstraints += "childGrmIor == '" + childGrmIor + "'";

			OfferSeqHolder offerSeqHolder = new OfferSeqHolder();

			try {
				SpecifiedProps specifiedProps = new SpecifiedProps();

				// FIXME: Is this really necessary?
				if (propertiesSpecification == null || propertiesSpecification.length == 0) {
					specifiedProps.__default(HowManyProps.all);
				} else {
					specifiedProps.__default(HowManyProps.some);
					specifiedProps.prop_names(propertiesSpecification);
				}

				// Queries for an IOR-identified GRM.
				lookup.query("SubtreeInformation", individualConstraints, generalPreferences, new Policy[] {}, specifiedProps, 1,
						offerSeqHolder, new OfferIteratorHolder(), new PolicyNameSeqHolder());

				System.out.println("Obtained " + offerSeqHolder.value.length + " arrays of properties");
				if (offerSeqHolder.value[0].properties.length > 0)
					fetchedPropertiesList.add(offerSeqHolder.value[0].properties);
			} catch (Exception exception) {
				System.err.println("Error fetching offer and properties for child GRM " + childGrmIor + ".");
			}
		}

		return fetchedPropertiesList;
	}

	// -------------------
	// Convenience methods
	// -------------------

	// Modify the offer associated to a given LRM in order to indicate that the
	// LRM
	// was recently chosen to service an execution request
	//
	// @param lrmIor - The IOR of the given LRM

	public void setRecentlyPicked(String lrmIor) {
		Property[] properties = new Property[1];

		properties[0] = new Property("recentlyPicked", orb.create_any());
		properties[0].value.insert_boolean(true);

		try {
			synchronized (registeredLrms) {
				register.modify(registeredLrms.get(lrmIor), new String[] {}, properties);
			}
		} catch (Exception exception) {
			System.err.println("Error modifying \"recentlyPicked\" properties for LRM " + lrmIor + ".");
		}
	}

	// Get all static and dynamic node information entries in the trader
	// and packs them into an SubtreeInformation ArrayList, suitable for
	// the GRM averages calculation
	//
	// @returns - A list of SubtreeInformations related to node information

	public SubtreeInformation getLrmInformationTotals() {
		ArrayList<Property[]> fetchedPropertiesList = getLrmInformation(null, null, null);

		Property[] properties;

		SubtreeInformation subtreeInformation = initSubTreeInformation();

		for (int counter = 0; counter < fetchedPropertiesList.size(); counter++) {
			properties = fetchedPropertiesList.get(counter);
			for (int i = 0; i < properties.length; i++) {
				
				if (properties[i].name.equals("processorMhz")) {
					insertHistogramValue(subtreeInformation.staticHistograms.processorMhz, properties[i].value.extract_long());
				} else if (properties[i].name.equals("totalRam")) {
					insertHistogramValue(subtreeInformation.staticHistograms.totalRam, properties[i].value.extract_long());
				} else if (properties[i].name.equals("totalSwap")) {
					insertHistogramValue(subtreeInformation.staticHistograms.totalSwap, properties[i].value.extract_long());
				} else if (properties[i].name.equals("freeRam")) {
					insertHistogramValue(subtreeInformation.dynamicHistograms.freeRam, properties[i].value.extract_long());
				} else if (properties[i].name.equals("freeSwap")) {
					insertHistogramValue(subtreeInformation.dynamicHistograms.freeSwap, properties[i].value.extract_long());
				} else if (properties[i].name.equals("freeDiskSpace")) {
					insertHistogramValue(subtreeInformation.dynamicHistograms.freeDiskSpace, properties[i].value.extract_long());
				} else if (properties[i].name.equals("cpuUsage")) {
					insertHistogramValue(subtreeInformation.dynamicHistograms.cpuUsage, properties[i].value.extract_float());
				}

			}
		}

		return subtreeInformation;
	}

	/**
	 * @param processorMhz
	 * @param extract_long
	 */
	private void insertHistogramValue(Histogram histogram, float value) {
		
		System.out.println("====> inserindo " + value + " em " + histogram.type);
		
		histogram.average = (histogram.numberOfElements * histogram.average + value) / histogram.numberOfElements + 1;

		histogram.numberOfElements++;

		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			if (value < histogram.intervalBoundaries[i]) {
				histogram.intervalQuantities[i]++;
				break;
			}
		}
	}

	// Get all subtree information entries in the trader and packs them
	// into an SubtreeInformation ArrayList, suitable for the GRM
	// averages calculation
	//
	// @returns - A list of SubtreeInformations related to subtreeInformation

	public ArrayList<SubtreeInformation> getGrmInformationTotals() {
		ArrayList<Property[]> fetchedPropertiesList = new ArrayList<Property[]>();
		ArrayList<SubtreeInformation> subtreeInformationList = new ArrayList<SubtreeInformation>();

		// Apply no constraints or preferences, and do not restrict information
		// returned
		fetchedPropertiesList = getGrmInformation(null, null, null);

		for (int counter = 0; counter < fetchedPropertiesList.size(); counter++) {
			subtreeInformationList.add(createSubtreeInformation(fetchedPropertiesList.get(counter)));
		}

		return subtreeInformationList;
	}

	// -------------
	// TCL modifiers
	// -------------

	// Modifies a TCL query in order match only machines that conform to
	// the platforms of the available binaries
	//
	// @param applicationExecutionInformation - Information about the
	// application execution request
	//
	// @returns - The modified TCL query

	public String generateConstraints(ApplicationExecutionInformation applicationExecutionInformation) {
		String temporaryConstraint;
		String constraint;
      String originalConstraints = applicationExecutionInformation.applicationConstraints;

      originalConstraints = removeHoursFieldFromConstraints(originalConstraints);

      constraint = "";

		String osName;
		String processorName;

		for (int counter = 0; counter < applicationExecutionInformation.availableBinaries.length; counter++) {

         System.out.println("Entrei aqui");

			int separatorPosition = applicationExecutionInformation.availableBinaries[counter].indexOf("_");

			osName = applicationExecutionInformation.availableBinaries[counter].substring(0, separatorPosition);
			processorName = applicationExecutionInformation.availableBinaries[counter].substring(separatorPosition + 1);

			temporaryConstraint = "(osName == '" + osName + "' and " + "processorName == '" + processorName + "')";

			constraint = constraint + temporaryConstraint;

			if (counter < applicationExecutionInformation.availableBinaries.length - 1) {
				constraint = constraint + " or ";
			}
		}
      
      if (originalConstraints.trim().length() > 0)
         constraint = originalConstraints + " and (" + constraint + ")";

      return constraint;
   }

   // Removes the field 'hours' from the constraints, if it exists. This is done because
   // the Trader doesn't know this property, since it is only for LUPA
   //
   // @param originalConstraints - The original TCL constraints string
   //
   // @returns - The modified TCL query

   private String removeHoursFieldFromConstraints(String originalConstraints) {
      String modifiedConstraints = originalConstraints;

      /* First we look for the pattern 'hours OP _VALUE' in the constraints, where
       * OP may be <, >, <=, >=, == or != (although LUPA interprets always like >=) and
       * _VALUE must be an integer */
      Pattern pattern = Pattern.compile("hours\\s*([><!=]=|[><])\\s*\\d*");

      /* Try finding this pattern on the constraints */
      Matcher matcher = pattern.matcher(originalConstraints);
      if (matcher.find() == true) {
         /* The hours constraint is not actually removed, but instead it's replaced with the TRUE
          * constraint, which means that this field will never be the cause for some LRM not matching
          * this constraint */
         modifiedConstraints = matcher.replaceAll("TRUE");
      }

      return modifiedConstraints;
   }

	// Modifies a TCL query in order to prefer machines which were not
	// recently selected
	//
	// @param preferences - The original TCL preferences string
	//
	// @returns - The modified TCL query

	public String generatePreferences(String preferences) {
		if (preferences.length() > 0) {
			preferences = "with " + preferences + " and recentlyPicked != TRUE";
		} else {
			preferences = "with recentlyPicked != TRUE";
		}

		return preferences;
	}

	// -------------
	// Print methods
	// -------------

	// Prints node static information on standart output
	//
	// @param nodeStaticInfomation - Node static information

	private void dumpNodeStaticInformation(NodeStaticInformation nodeStaticInformation) {
		System.out.println("Hostname: " + nodeStaticInformation.hostName);
		System.out.println("OS Name: " + nodeStaticInformation.osName);
		System.out.println("OS Version: " + nodeStaticInformation.osVersion);
		System.out.println("Processor Name: " + nodeStaticInformation.processorName);
		System.out.println("Processor Mhz: " + nodeStaticInformation.processorMhz);
		System.out.println("Total RAM: " + nodeStaticInformation.totalRam);
		System.out.println("Total Swap: " + nodeStaticInformation.totalSwap);
	}

	// Prints node dynamic information on standart output
	//
	// @param nodeDynamicInfomation - Node dynamic information

	private void dumpNodeDynamicInformation(NodeDynamicInformation nodeDynamicInformation) {
		System.out.println("Free RAM: " + nodeDynamicInformation.freeRam);
		System.out.println("Free Swap: " + nodeDynamicInformation.freeSwap);
		System.out.println("Free Disk Space: " + nodeDynamicInformation.freeDiskSpace);
		System.out.println("CPU Usage: " + nodeDynamicInformation.cpuUsage);
	}

	// Prints subtree information on standart output
	//
	// @param subtreecInfomation - Subtree information

	public void dumpSubtreeHistogram(SubtreeInformation subtreeInformation) {

		Histogram tempHistogram;
		
		System.out.print("Processor Mhz:");
		tempHistogram = subtreeInformation.staticHistograms.processorMhz;
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalBoundaries[i]);
		}
		System.out.print(" :");
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalQuantities[i]);
		}
		System.out.println();

		System.out.print("Total RAM:");
		tempHistogram = subtreeInformation.staticHistograms.totalRam;
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalBoundaries[i]);
		}
		System.out.print(" :");
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalQuantities[i]);
		}
		System.out.println();

		System.out.print("Total Swap:");
		tempHistogram = subtreeInformation.staticHistograms.totalSwap;
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalBoundaries[i]);
		}
		System.out.print(" :");
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalQuantities[i]);
		}
		System.out.println();

		System.out.print("Free RAM:");
		tempHistogram = subtreeInformation.dynamicHistograms.freeRam;
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalBoundaries[i]);
		}
		System.out.print(" :");
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalQuantities[i]);
		}
		System.out.println();

		System.out.print("Free Swap:");
		tempHistogram = subtreeInformation.dynamicHistograms.freeSwap;
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalBoundaries[i]);
		}
		System.out.print(" :");
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalQuantities[i]);
		}
		System.out.println();

		System.out.print("Free Disk Space:");
		tempHistogram = subtreeInformation.dynamicHistograms.freeDiskSpace;
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalBoundaries[i]);
		}
		System.out.print(" :");
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalQuantities[i]);
		}
		System.out.println();

		System.out.print("CPU Usage:");
		tempHistogram = subtreeInformation.dynamicHistograms.cpuUsage;
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalBoundaries[i]);
		}
		System.out.print(" :");
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			System.out.print(" " + tempHistogram.intervalQuantities[i]);
		}
		System.out.println();

	}

	/**
	 * @param histogram1
	 * @param histogram2
	 * @return
	 * @throws WrongHistogramTypeException
	 */
	public Histogram addHistograms(Histogram histogram1, Histogram histogram2) throws WrongHistogramTypeException {

		if (histogram1 == null || histogram2 == null) {
			throw new dataTypes.WrongHistogramTypeException();
		}
		if (!histogram1.type.equals(histogram2.type)) {
			throw new dataTypes.WrongHistogramTypeException();
		}

		Histogram result = new Histogram(new int[NUM_HISTOGRAM_INTERVALS], new double[NUM_HISTOGRAM_INTERVALS], 0.0, 0.0, 0,
				histogram1.type);
		
		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS; i++) {
			result.intervalBoundaries[i] = histogram1.intervalBoundaries[i];
			result.intervalQuantities[i] = histogram1.intervalQuantities[i] + histogram2.intervalQuantities[i];			
		}

		result.numberOfElements = histogram1.numberOfElements + histogram2.numberOfElements;

		result.average = (histogram1.average * histogram1.numberOfElements + histogram2.average * histogram2.numberOfElements)
				/ (result.numberOfElements);

		result.standardDeviation = calcStandardDeviation(result);

		return result;
	}

	private double calcStandardDeviation(Histogram histogram) {
		double variance = Math.pow((histogram.intervalBoundaries[0] - histogram.average), 2) * histogram.intervalQuantities[0];
		double averagePoint;

		for (int i = 1; i < NUM_HISTOGRAM_INTERVALS - 2; i++) {
			averagePoint = (histogram.intervalBoundaries[i] + histogram.intervalBoundaries[i - 1]) / 2;
			variance += Math.pow(averagePoint - histogram.average, 2) * histogram.intervalQuantities[i];
		}

		variance += Math.pow((histogram.intervalBoundaries[NUM_HISTOGRAM_INTERVALS - 1] - histogram.average), 2)
				* histogram.intervalQuantities[NUM_HISTOGRAM_INTERVALS - 1];

		variance = variance / histogram.numberOfElements;

		return Math.sqrt(variance);
	}

	private void populateHistogramBounds(Histogram histogram, int start, int end) {
		double ratio = (end - start) / (NUM_HISTOGRAM_INTERVALS - 2);

		for (int i = 0; i < NUM_HISTOGRAM_INTERVALS - 1; i++) {
			histogram.intervalBoundaries[i] = start + ratio * i;
		}

		histogram.intervalBoundaries[NUM_HISTOGRAM_INTERVALS - 1] = Double.MAX_VALUE;
	}

	public SubtreeInformation initSubTreeInformation() {
		Histogram processorMhz = new Histogram(new int[NUM_HISTOGRAM_INTERVALS], new double[NUM_HISTOGRAM_INTERVALS], 0.0, 0.0, 0,
				"processorMhz");
		Histogram totalRam = new Histogram(new int[NUM_HISTOGRAM_INTERVALS], new double[NUM_HISTOGRAM_INTERVALS], 0.0, 0.0, 0, "totalRam");
		Histogram totalSwap = new Histogram(new int[NUM_HISTOGRAM_INTERVALS], new double[NUM_HISTOGRAM_INTERVALS], 0.0, 0.0, 0, "totalSwap");
		Histogram freeRam = new Histogram(new int[NUM_HISTOGRAM_INTERVALS], new double[NUM_HISTOGRAM_INTERVALS], 0.0, 0.0, 0, "freeRam");
		Histogram freeSwap = new Histogram(new int[NUM_HISTOGRAM_INTERVALS], new double[NUM_HISTOGRAM_INTERVALS], 0.0, 0.0, 0, "freeSwap");
		Histogram freeDiskSpace = new Histogram(new int[NUM_HISTOGRAM_INTERVALS], new double[NUM_HISTOGRAM_INTERVALS], 0.0, 0.0, 0,
				"freeDiskSpace");
		Histogram cpuUsage = new Histogram(new int[NUM_HISTOGRAM_INTERVALS], new double[NUM_HISTOGRAM_INTERVALS], 0.0, 0.0, 0, "cpuUsage");

		NodeStaticHistograms nodeStaticHistograms = new NodeStaticHistograms(processorMhz, totalRam, totalSwap);
		NodeDynamicHistograms nodeDynamicHistograms = new NodeDynamicHistograms(freeRam, freeSwap, freeDiskSpace, cpuUsage);

		SubtreeInformation subtreeInformation = new SubtreeInformation(nodeStaticHistograms, nodeDynamicHistograms);

		populateHistogramBounds(processorMhz, 300, 3000);
		populateHistogramBounds(totalRam, 256000, 4096000);
		populateHistogramBounds(totalSwap, 256000, 4096000);
		populateHistogramBounds(freeRam, 256, 4096);
		populateHistogramBounds(freeSwap, 256, 4096);
		populateHistogramBounds(freeDiskSpace, 100, 10240);
		populateHistogramBounds(cpuUsage, 0, 100);

		return subtreeInformation;
	}
}
