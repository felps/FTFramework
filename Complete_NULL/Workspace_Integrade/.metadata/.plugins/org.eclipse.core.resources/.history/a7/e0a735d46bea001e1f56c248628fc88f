package grm;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.omg.CORBA.ORB;
import org.omg.CosTrading.Property;

import resourceProviders.Lrm;
import resourceProviders.LrmHelper;
import tools.Asct;
import tools.AsctHelper;
import clusterManagement.ExecutionManager;
import clusterManagement.Grm;
import clusterManagement.GrmHelper;
import clusterManagement.GrmPOA;
import dataTypes.ApplicationExecutionInformation;
import dataTypes.Histogram;
import dataTypes.NodeDynamicInformation;
import dataTypes.NodeStaticInformation;
import dataTypes.ProcessExecutionInformation;
import dataTypes.SubtreeInformation;
import dataTypes.WrongHistogramTypeException;

//Class GrmImpl - Servant implementation of Grm interface described on ResourceManagement.idl
//
//Main functions:
//
//Execution request broker
//LRM availability manager
//
//@author Hammurabi Mendes

public class GrmImpl extends GrmPOA implements Runnable {
	// This is the ORB used to access CORBA services
	private ORB orb;

	// The TraderManager is used for insertion, querying and removal operations
	private TraderManager traderManager;

	// This is a reference to the parent GRM
	private Grm parentGrm;

	// This is a reference to the Execution Manager
	private ExecutionManager executionManager;

	// This is the last time information from this GRM was updated on the parent
	// GRM
	private int lastUpdateTime;

	// This is the last SubtreeInformation updated on the parent GRM
	private SubtreeInformation lastUpdateSubtreeInformation;

	// This is the interval between checks for significant resource availability
	// changes in the children GRMs
	private int sampleInterval;

	// This is the maximum interval between updates sent to the parent GRM
	private int keepAliveInterval;

	// This counter is used to generate a unique ID for each execution request
	private int executionNumber = 0;

	// Creates a new GrmImpl object
	//
	// @param orb - A reference to an ORB, used to access CORBA services

	public GrmImpl(ORB orb, TraderManager trader) {
		this.orb = orb;

		traderManager = trader;

		// Default setting for 5 minutes
		this.sampleInterval = 300;

		// Default setting for 5 minutes (in seconds)
		this.keepAliveInterval = 300;

		(new Thread(this)).start();
	}

	// -----------------------------------
	// LRM and GRM registration and update
	// -----------------------------------

	// See comments for following methods in TraderManager class

	public void registerLrm(String lrmIor,
			NodeStaticInformation nodeStaticInformation) {
		traderManager.registerLrm(lrmIor, nodeStaticInformation);
	}

	public void updateLrmInformation(String lrmIor,
			NodeDynamicInformation nodeDynamicInformation) {
		traderManager.updateLrmInformation(lrmIor, nodeDynamicInformation);
	}

	public void registerGrm(String childGrmIor,
			SubtreeInformation subtreeInformation) {
		traderManager.registerGrm(childGrmIor, subtreeInformation);
	}

	public void updateGrmInformation(String childGrmIor,
			SubtreeInformation subtreeInformation) {
		traderManager.updateGrmInformation(childGrmIor, subtreeInformation);
	}

	// --------------------------
	// Execution request handling
	// --------------------------

	// Handle an ASCT-submitted application
	//
	// @param applicationExecutionInformation - Application-wide information
	// @param processExecutionInformation - Process-specific information
	//
	// @returns - True if the GRM accepted the request or it was sucessfully
	// forwared

	public boolean requestRemoteExecution(
			ApplicationExecutionInformation applicationExecutionInformation,
			ProcessExecutionInformation[] processExecutionInformation) {

		// SubtreeInformation sub = calculateGrmInformation();

		String constraints = traderManager
				.generateConstraints(applicationExecutionInformation);
		String preferences = traderManager
				.generatePreferences(applicationExecutionInformation.applicationPreferences);

		System.out.println("Execution Request:");

		System.out.print("Request for " + processExecutionInformation.length
				+ " processes");
		if (applicationExecutionInformation.forceDifferentNodes) {
			System.out.println(" that should run on different machines.");
		} else {
			System.out.println(".");
		}

		System.out.println("Constraints: " + constraints);
		System.out.println("Preferences: " + preferences);

		// constraints, preferences );

		String uniqueRequestId;

		for (int i = 0; i < processExecutionInformation.length; i++) {
			if ((uniqueRequestId = generateUniqueId(processExecutionInformation)) == null) {
				System.err
						.println("Aborting request due to unique application ID generation failure.");

				return false;
			}

			processExecutionInformation[i].executionRequestId.requestId = uniqueRequestId;
		}

		ArrayList<Property[]> fetchedLrmInformation = traderManager
				.getLrmInformation(constraints, preferences,
						new String[] { "lrmIor" });

		// When there is not enough hosts suitable to execute the application...

		if (((fetchedLrmInformation.size() < processExecutionInformation.length) && applicationExecutionInformation.forceDifferentNodes)
				|| (fetchedLrmInformation.size() == 0)) {

			System.out.println("Needed: " + processExecutionInformation.length
					+ " hosts. Got: " + fetchedLrmInformation.size()
					+ " hosts.");
			executionManager.setExecutionRefused(
					applicationExecutionInformation,
					processExecutionInformation);

			System.out.println("Trying other GRMs for execution request.");

			return forwardExecutionRequest(applicationExecutionInformation,
					processExecutionInformation);
		}

		// Send a message to the ExecutionManager containing the request
		// properties.

		if (executionManager != null) {
			executionManager.setExecutionScheduled(
					applicationExecutionInformation,
					processExecutionInformation);
		}

		int currentExecutionTrial = 0;

		while (currentExecutionTrial < processExecutionInformation.length) {
			if (fetchedLrmInformation.size() == 0) {
				break;
			}

			int currentPositionTrial = currentExecutionTrial
					% fetchedLrmInformation.size();

			Property[] lrmProperties = fetchedLrmInformation
					.get(currentPositionTrial);

			String lrmIor = lrmProperties[0].value.extract_string();

			try {
				Lrm lrm = LrmHelper.narrow(orb.string_to_object(lrmIor));

				/* ## BEGIN -- THIAGO ## */
				/*
				 * LUPA can tell the LRM to refuse the execution, so we make
				 * sure it was accepted
				 */
				if (lrm.requestExecution(applicationExecutionInformation,
						processExecutionInformation[currentExecutionTrial]) == 1) {

					traderManager.setRecentlyPicked(lrmIor);

					currentExecutionTrial++;

					// If the application must run on different nodes, discard
					// the current LRM

					if (applicationExecutionInformation.forceDifferentNodes) {
						fetchedLrmInformation.remove(currentPositionTrial);
					}
				}
				/* If LUPA dennied the execution */
				else {
					fetchedLrmInformation.remove(currentPositionTrial);
				}
				/* ## END -- THIAGO ## */
			} catch (org.omg.CORBA.TRANSIENT transientException) {
				System.err
						.println("Unable to reach LRM. Removing it from database");

				traderManager.removeLrmInformation(lrmIor);
				fetchedLrmInformation.remove(currentPositionTrial);
			}
		}

		if (currentExecutionTrial < processExecutionInformation.length) {
			System.out.println("Trying other GRMs for execution request.");

			return forwardExecutionRequest(applicationExecutionInformation,
					processExecutionInformation);
		}

		return true;
	}

	// Forward request to child GRMs and parent GRM
	//
	// @param applicationExecutionInformation - Application-wide information
	// @param processExecutionInformation - Process-specific information
	//
	// @returns - True if some GRM accepted the request

	private boolean forwardExecutionRequest(
			ApplicationExecutionInformation applicationExecutionInformation,
			ProcessExecutionInformation[] processExecutionInformation) {
		GrmTracker grmTracker = new GrmTracker(orb,
				applicationExecutionInformation.previousGrmIor,
				getParentGrmIor());

		ArrayList<Property[]> fetchedGrmInformation = traderManager
				.getGrmInformation(null, null, null);

		// FIXME: Apply some ASCT originated constraints/preferences for GRMs

		String childGrmId;

		for (int counter = 0; counter < fetchedGrmInformation.size(); counter++) {
			System.out.println("Looking for a child GRM " + (counter + 1)
					+ " of " + (fetchedGrmInformation.size()));
			System.out.println(fetchedGrmInformation.get(counter)[0].value
					.extract_string());
			childGrmId = fetchedGrmInformation.get(counter)[0].value
					.extract_string();
			grmTracker.insertUntraversedGrmId(childGrmId);
		}

		applicationExecutionInformation.previousGrmIor = getGrmIor();

		Grm nextGrm;

		while ((nextGrm = grmTracker.getNextGrm()) != null) {
			try {
				System.out.println("Found another GRM");
				System.out.println("Trying GRM" + nextGrm);
				if (nextGrm.requestRemoteExecution(
						applicationExecutionInformation,
						processExecutionInformation)) {
					return true;
				}
			} catch (org.omg.CORBA.TRANSIENT transientException) {
				String nextGrmId = orb.object_to_string(nextGrm);

				if (nextGrmId.equals(getParentGrmIor())) {
					System.err.println("Unable to reach parent GRM.");
					System.err
							.println("Contact your local system administrator.");
				} else {
					System.err
							.println("Unable to reach GRM " + nextGrmId + ".");
					System.err.println("Removing it from database.");

					traderManager.removeGrmInformation(nextGrmId);
				}
			}
		}

		try {
			Asct asct = AsctHelper
					.narrow(orb
							.string_to_object(applicationExecutionInformation.requestingAsctIor));

			for (int counter = 0; counter < processExecutionInformation.length; counter++) {
				String[] reqId = processExecutionInformation[counter].executionRequestId.requestId
						.split(":");
				String asctApplicationId = reqId[2];
				processExecutionInformation[counter].executionRequestId.requestId = asctApplicationId;
				asct
						.setExecutionRefused(processExecutionInformation[counter].executionRequestId);
			}
		} catch (org.omg.CORBA.TRANSIENT transientException) {
			System.err
					.println("Unable to reach ASCT. Failure notification is incomplete.");
		}

		return false;
	}

	// Generates a unique ID for the application using host address, current
	// time and original ASCT ID
	//
	// @param processExecutionInformation - Information about each process that
	// should be executed
	//
	// @returns - A string that is unique

	private String generateUniqueId(
			ProcessExecutionInformation[] processExecutionInformation) {
		StringBuffer newId = new StringBuffer();

		if (processExecutionInformation[0].executionRequestId.requestId
				.indexOf(":") < 0) {
			try {
				newId.append(java.net.InetAddress.getLocalHost()
						.getHostAddress().replaceAll("[.]", ""));
				newId.append(Calendar.getInstance().getTimeInMillis());
				newId.append(":");
				newId.append(++executionNumber);
				newId.append(":");
			} catch (UnknownHostException unknownHostException) {
				System.err.println("Could not get host information.");

				return null;
			}
		}

		newId
				.append(processExecutionInformation[0].executionRequestId.requestId);

		return newId.toString();
	}

	// ---------------------
	// GRM main funcionality
	// ---------------------

	// This thread has 2 responsabilities:
	//
	// 1 - Periodically checks if the registered LRMs are online. If their last
	// information
	// update is older than a threshold value, check if they are available
	//
	// 2 - Periodically updates the parent GRM with information about the
	// subtree
	// represented by this GRM (the local LRMs and the child GRM subtrees)

	public void run() {
		while (true) {
			// This is responsability 1

			int currentTime = (int) (new Date()).getTime() / 1000;

			if (parentGrm != null) {
				try {
					SubtreeInformation subtreeInformation = calculateGrmInformation();

					if (currentTime - lastUpdateTime >= keepAliveInterval
							|| hasSignificantChange(subtreeInformation,
									lastUpdateSubtreeInformation)) {
						parentGrm.updateGrmInformation(getGrmIor(),
								subtreeInformation);

						lastUpdateSubtreeInformation = subtreeInformation;
						lastUpdateTime = currentTime;
					}
				} catch (org.omg.CORBA.TRANSIENT transientException) {
					System.err.println("Unable to reach parent GRM.");
					System.err
							.println("Contact your local system administrator.");
				}
			}

			// This is responsability 2.

			checkLrmAvailability();

			// Now, determine the time to pause the thread, and do it.

			try {
				int sleepTime = lastUpdateTime + keepAliveInterval
						- currentTime;

				if (sampleInterval < sleepTime || sleepTime < 0) {
					sleepTime = sampleInterval;
				}

				// System.out.println("GRM sleeping for " + sleepTime + "
				// seconds.");

				// Convert seconds into miliseconds.
				sleepTime = sleepTime * 1000;

				Thread.sleep(sleepTime);
			} catch (InterruptedException interruptedException) {
				interruptedException.printStackTrace();
			}
		}
	}

	// If some LRM's last information update is older than keepAliveInterval,
	// check if it is available. If it is not the case, remove it from the
	// trader
	//
	// This implements responsability 2 of the thread's run() method
	// responsabilities,
	// as described there

	private void checkLrmAvailability() {
		ArrayList<Property[]> fetchedLrmInformation = new ArrayList<Property[]>();

		fetchedLrmInformation = traderManager.getLrmInformation(null, null,
				new String[] { "lrmIor", "lastUpdated" });

		for (int counter = 0; counter < fetchedLrmInformation.size(); counter++) {
			String lrmIor = fetchedLrmInformation.get(counter)[0].value
					.extract_string();

			int lastUpdateTime = fetchedLrmInformation.get(counter)[1].value
					.extract_long();
			int currentTime = (int) (new Date()).getTime() / 1000;

			// This threshold value used to be 200 (hardcoded).
			// Now it is set to be the same as keepAliveInterval.

			if ((currentTime - lastUpdateTime) > keepAliveInterval) {
				Lrm lrm = LrmHelper.narrow(orb.string_to_object(lrmIor));

				try {
					lrm.isAvailable();
				} catch (Exception exception) {
					System.err.println("Unable to reach LRM " + lrmIor + ".");
					System.err.println("Removing it from database.");

					traderManager.removeLrmInformation(lrmIor);
				}
			}
		}
	}

	// --------------------------
	// Setters for timeout values
	// --------------------------

	// Sets the interval between checks for significant resource availability
	// changes in the children GRMs. In case they exist, the parent GRM will
	// be notified
	//
	// @param seconds - New interval in seconds

	public void setSampleInterval(int seconds) {
		this.sampleInterval = seconds;
	}

	// Sets the maximum interval between updates sent to the parent GRM.
	//
	// @param seconds - New interval in seconds

	public void setKeepAliveInterval(int seconds) {
		this.keepAliveInterval = seconds;
	}

	// --------------------------------
	// Setter for the Execution Manager
	// --------------------------------

	// Sets the execution manager that is nofified when an execution starts
	//
	// @param executionManager - The reference to the new execution manager

	public void setExecutionManager(ExecutionManager executionManager) {
		this.executionManager = executionManager;
	}

	// ------------------------------------------
	// Accounting of subtree resources by the GRM
	// ------------------------------------------

	// Calculates information averages for the subtree determined by this GRM.
	// It accounts for direct connected LRMs and its child subtrees
	//
	// @returns - A SubtreeInformation structure

	private SubtreeInformation calculateGrmInformation() {
		SubtreeInformation fetchedLrmInformation = traderManager
				.getLrmInformationTotals();
		ArrayList<SubtreeInformation> fetchedGrmInformationList = traderManager
				.getGrmInformationTotals();

		ArrayList<SubtreeInformation> fetchedInformationList = new ArrayList<SubtreeInformation>();

		fetchedInformationList.add(fetchedLrmInformation);
		fetchedInformationList.addAll(fetchedGrmInformationList);

		int numberEntities = fetchedInformationList.size();

		SubtreeInformation subtreeInformation = traderManager
				.initSubTreeInformation();
		SubtreeInformation temporarySubtreeInformation;

		for (int subtreeInfoCounter = 0; subtreeInfoCounter < numberEntities; subtreeInfoCounter++) {
			temporarySubtreeInformation = fetchedInformationList
					.get(subtreeInfoCounter);

			try {
				subtreeInformation.staticHistograms.processorMhz = traderManager
						.addHistograms(
								subtreeInformation.staticHistograms.processorMhz,
								temporarySubtreeInformation.staticHistograms.processorMhz);
				subtreeInformation.staticHistograms.totalRam = traderManager
						.addHistograms(
								subtreeInformation.staticHistograms.totalRam,
								temporarySubtreeInformation.staticHistograms.totalRam);
				subtreeInformation.staticHistograms.totalSwap = traderManager
						.addHistograms(
								subtreeInformation.staticHistograms.totalSwap,
								temporarySubtreeInformation.staticHistograms.totalSwap);
				subtreeInformation.dynamicHistograms.freeRam = traderManager
						.addHistograms(
								subtreeInformation.dynamicHistograms.freeRam,
								temporarySubtreeInformation.dynamicHistograms.freeRam);
				subtreeInformation.dynamicHistograms.freeSwap = traderManager
						.addHistograms(
								subtreeInformation.dynamicHistograms.freeSwap,
								temporarySubtreeInformation.dynamicHistograms.freeSwap);
				subtreeInformation.dynamicHistograms.freeDiskSpace = traderManager
						.addHistograms(
								subtreeInformation.dynamicHistograms.freeDiskSpace,
								temporarySubtreeInformation.dynamicHistograms.freeDiskSpace);
				subtreeInformation.dynamicHistograms.cpuUsage = traderManager
						.addHistograms(
								subtreeInformation.dynamicHistograms.cpuUsage,
								temporarySubtreeInformation.dynamicHistograms.cpuUsage);
			} catch (WrongHistogramTypeException e) {
				// Should not catch this exception
				e.printStackTrace();
			}
		}

		return subtreeInformation;
	}

	// Checks if two SubtreeInformation objects differ in one field in more than
	// 10 percent
	//
	// @param subtreeInformation1 - First subtree information
	// @param subtreeInformation2 - Second subtree information
	//
	// @returns - True if at least one corresponding field of the parameters
	// differ more than 10 percent

	private boolean hasSignificantChange(
			SubtreeInformation subtreeInformation1,
			SubtreeInformation subtreeInformation2) {
		if (subtreeInformation1 == null || subtreeInformation2 == null) {
			return true;
		}

		if (areTenPercentDifferent(
				subtreeInformation1.staticHistograms.processorMhz,
				subtreeInformation2.staticHistograms.processorMhz)) {
			return true;
		}

		if (areTenPercentDifferent(
				subtreeInformation1.staticHistograms.totalRam,
				subtreeInformation2.staticHistograms.totalRam)) {
			return true;
		}

		if (areTenPercentDifferent(
				subtreeInformation1.staticHistograms.totalSwap,
				subtreeInformation2.staticHistograms.totalSwap)) {
			return true;
		}

		if (areTenPercentDifferent(
				subtreeInformation1.dynamicHistograms.freeRam,
				subtreeInformation2.dynamicHistograms.freeRam)) {
			return true;
		}

		if (areTenPercentDifferent(
				subtreeInformation1.dynamicHistograms.freeSwap,
				subtreeInformation2.dynamicHistograms.freeSwap)) {
			return true;
		}

		if (areTenPercentDifferent(
				subtreeInformation1.dynamicHistograms.freeDiskSpace,
				subtreeInformation2.dynamicHistograms.freeDiskSpace)) {
			return true;
		}

		if (areTenPercentDifferent(
				subtreeInformation1.dynamicHistograms.cpuUsage,
				subtreeInformation2.dynamicHistograms.cpuUsage)) {
			return true;
		}

		return false;
	}

	// Checks if a two float division ratio is more than 10 percent
	//
	// @param float1 - First floating point data
	// @param float2 - Second floating point data
	//
	// @returns - True if the float division ratio (any order of operands) is
	// more than 10 percent.
	// False, otherwise

	private boolean areTenPercentDifferent(Histogram histogram1,
			Histogram histogram2) {
		double ratio = histogram1.average / histogram2.average;

		if (ratio >= 1.1 || ratio <= 0.9) {
			return true;
		}

		return false;
	}

	// --------------------------------------------------
	// Parent setting, IOR fetching and availability test
	// --------------------------------------------------

	// Sets the parent GRM for this GRM
	//
	// @param parentGrmIor - IOR of the GRM that should be the parent

	public void setParentGrm(String parentGrmIor) {
		try {
			parentGrm = GrmHelper.narrow(orb.string_to_object(parentGrmIor));

			SubtreeInformation subtreeInformation = calculateGrmInformation();
			parentGrm.registerGrm(getGrmIor(), subtreeInformation);
		} catch (org.omg.CORBA.TRANSIENT transientException) {
			System.err.println("Unable to reach parent GRM.");
			System.err.println("Contact your local system administrator.");

			parentGrm = null;
		}
	}

	// Fetches the parent GRM IOR
	//
	// @returns - IOR of the parent GRM

	public String getParentGrmIor() {
		return orb.object_to_string(parentGrm);
	}

	// Fetches the GRM IOR
	//
	// @returns - IOR of the GRM

	public String getGrmIor() {
		return orb.object_to_string(this._this_object());
	}

	// Tests if this GRM is available.
	//
	// @returns - True if it is available

	public boolean isAvailable() {
		return true;
	}

}
