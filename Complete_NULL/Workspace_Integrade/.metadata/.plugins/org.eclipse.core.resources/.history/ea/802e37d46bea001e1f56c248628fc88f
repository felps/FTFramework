package grm.executionManager.dataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dataTypes.ApplicationExecutionInformation;
import dataTypes.ApplicationType;
import dataTypes.ExecutionRequestId;
import dataTypes.ProcessExecutionInformation;

import dataTypes.ApplicationExecutionStateInformation;
import dataTypes.ProcessExecutionStateInformation;
import grm.executionManager.dataTypes.ApplicationExecutionStateTypes;
import grm.executionManager.dataTypes.ProcessExecutionStateTypes;


public class ExecutionDatabaseManager {

	private Connection connection = null;
	
	private String url = "jdbc:h2:file:../database/ExecutionManager";

		
	/**
	 * Responsible for the database access.
	 * 
	 */
	public ExecutionDatabaseManager() {

		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}
		
	}

	/**
	 * Resgister an application in the database.
	 * 
	 * @param appInfo
	 *            Application execution information
	 * @param processInfo
	 *            Process execution information
	 */
	public synchronized void registerApplicationExecution(
			ApplicationExecutionInformation appInfo,
			ProcessExecutionInformation[] processInfo) {

		String[] str = separateReplicaIdFromRequestId(processInfo[0].executionRequestId.requestId);
		String requestId = str[0];
		String replicaId = str[1];

		PreparedStatement statement = null;
		String sql = null;
		try {
			sql = "SELECT * FROM ApplicationExecutionInformation WHERE RequestID = ?;";

			statement = connection().prepareStatement(sql);
			statement.setString(1, requestId);

			ResultSet rs = statement.executeQuery();

			if (!rs.next()) {
				statement.close();

				// In case of multiple replicas, only the first one registers static information
				if (replicaId.compareToIgnoreCase("0") == 0) {
					sql = "INSERT INTO ApplicationExecutionInformation VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);";

					statement = connection().prepareStatement(sql);
					statement.setString(1, requestId);
					statement.setString(2, appInfo.requestingAsctIor);
					statement.setString(3, appInfo.source);
					statement.setString(4, appInfo.originalGrmIor);
					statement.setString(5, appInfo.previousGrmIor);
					statement.setString(6, appInfo.applicationRepositoryIor);
					statement.setString(7, appInfo.basePath);
					statement.setString(8, appInfo.applicationName);
					statement.setString(9, appInfo.applicationConstraints);
					statement.setString(10, appInfo.applicationPreferences);
					statement.setInt(11, appInfo.applicationType.value());
					statement.setBoolean(12, appInfo.forceDifferentNodes);
					statement.setString(13, appInfo.userId);

					statement.execute();
					statement.close();
					
					// Fills the table applicationExecutionInformation_AvailableBinaries
					for (int i = 0; i < appInfo.availableBinaries.length; i++) {
						sql = "INSERT INTO ApplicationExecutionInformation_AvailableBinaries VALUES(?,?);";
						statement = connection().prepareStatement(sql);
						statement.setString(1, requestId);
						statement.setString(2, appInfo.availableBinaries[i]);

						statement.execute();
						statement.close();
					}

				}
			}

			// Fills the table ProcessExecutionInformation
			for (int i = 0; i < processInfo.length; i++) {

				sql = "SELECT * FROM ProcessExecutionInformation WHERE RequestID = ? AND ProcessID = ?;";

				statement = connection().prepareStatement(sql);
				statement.setString(1, requestId);
				statement.setString(2,
						processInfo[i].executionRequestId.processId);

				rs = statement.executeQuery();

				if (!rs.next()) {
					statement.close();

					str = separateReplicaIdFromRequestId(processInfo[0].executionRequestId.requestId);
					requestId = str[0];
					replicaId = str[1];

					if (replicaId.compareToIgnoreCase("0") == 0) {
						sql = "INSERT INTO ProcessExecutionInformation VALUES( ?,?,?);";
						statement = connection().prepareStatement(sql);
						statement.setString(1, requestId);
						statement.setString(2,
								processInfo[i].executionRequestId.processId);
						statement.setString(3, processInfo[i].processArguments);
						statement.execute();
						statement.close();
					}
				}
				statement.close();
			}
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Change application execution state.
	 * 
	 * @param requestId
	 *            Unique application execution ID.
	 * @param executionState
	 *            Execution State, this value can be found in
	 *            ApplicationExecutionStateTypes class.
	 */
	public synchronized void changeApplicationExecutionState(String requestId,
			int executionState) {
		String[] str = separateReplicaIdFromRequestId(requestId);
		requestId = str[0];
		String replicaId = str[1];

		PreparedStatement statement = null;
		String sql = null;
		try {
			if (executionState == ApplicationExecutionStateTypes.EXECUTING) {
				sql = "UPDATE ApplicationExecutionStateInformation SET ExecutionState = ?"
						+ " WHERE RequestID = ? AND ReplicaID = ?;";

				statement = connection().prepareStatement(sql);
				statement.setString(2, requestId);
				statement.setString(3, replicaId);
				statement.setInt(1, executionState);

			} else if (executionState == ApplicationExecutionStateTypes.SCHEDULED
					|| executionState == ApplicationExecutionStateTypes.REFUSED) {

				sql = "SELECT * FROM ApplicationExecutionStateInformation WHERE RequestID = ? AND replicaID = ?;";

				statement = connection().prepareStatement(sql);
				statement.setString(1, requestId);
				statement.setString(2, replicaId);

				ResultSet rs = statement.executeQuery();

				if (!rs.next()) {
					statement.close();
					sql = "INSERT INTO ApplicationExecutionStateInformation VALUES(?,?,?,?,?);";

					statement = connection().prepareStatement(sql);
					statement.setString(1, requestId);
					statement.setString(2, replicaId);
					statement.setInt(3, executionState);
					statement.setLong(4, System.currentTimeMillis());
					statement.setLong(5, 0);
				}
			}
			// Other execution states
			else {
				sql = "UPDATE ApplicationExecutionStateInformation SET ExecutionState = ?, "
						+ "FinishTimeStamp = ? WHERE RequestID = ? AND ReplicaID = ?;";

				statement = connection().prepareStatement(sql);
				statement.setString(3, requestId);
				statement.setString(4, replicaId);
				statement.setInt(1, executionState);
				statement.setLong(2, System.currentTimeMillis());
			}

			statement.execute();
			statement.close();
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Change process execution state, it has to be used just when the process
	 * starts or when the LRM fails before the process execution.
	 * 
	 * @param requestId
	 *            Unique application execution ID.
	 * @param processId
	 *            Process ID.
	 * @param lrmIOR
	 *            LRM wherethe process execute.
	 * @param executionState
	 *            Process state.
	 */
	public synchronized void changeProcessExecutionStateToExecutingOrFailure(
			String requestId, String processId, String lrmIOR,
			int executionState) {

		String[] str = separateReplicaIdFromRequestId(requestId);
		requestId = str[0];
		String replicaId = str[1];

		PreparedStatement statement = null;
		String sql = null;
		try {
			int nextId = this.getLastProcessRestartId(requestId, processId) + 1;
			
			sql = "INSERT INTO ProcessExecutionStateInformation VALUES(?,?,?,?,?,?,?,?,?);";

			statement = connection().prepareStatement(sql);
			statement.setString(1, requestId);
			statement.setString(2, replicaId);
			statement.setString(3, processId);
			statement.setInt(4, nextId);
			statement.setString(5, lrmIOR);
			statement.setInt(6, executionState);
			statement.setInt(7, 0);
			statement.setLong(8, System.currentTimeMillis());
			statement.setInt(9, 0);

			statement.execute();
			statement.close();
			if (executionState == ProcessExecutionStateTypes.EXECUTING) {
				changeApplicationExecutionState(requestId,
						ApplicationExecutionStateTypes.EXECUTING);
			} else {
				int currentState = calculateApplicationExecutionState(requestId);
				if (currentState != ApplicationExecutionStateTypes.EXECUTING) {
					changeApplicationExecutionState(requestId, currentState);
				}
			}
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Change process execution state, it has to be used just when the process
	 * finishes, with abnormal ending or not.
	 * 
	 * @param requestId
	 *            Unique application execution ID.
	 * @param processId
	 *            Process ID.
	 * @param executionState
	 *            Execution state.this value can be found in
	 *            ProcessExecutionStateInformation class.
	 * @param executionCode
	 *            Value return from
	 */
	public synchronized void changeProcessExecutionStateToFinished(String requestId,
			String processId, int executionState, int executionCode) {

		// TODO: Change executionCode type in database from TINYINT to INTEGER
		executionCode = executionCode % 128;
		
		String[] str = separateReplicaIdFromRequestId(requestId);
		requestId = str[0];
		String replicaId = str[1];

		PreparedStatement statement = null;
		String sql = null;
		try {
			int lastId = this.getLastProcessRestartId(requestId, processId);

			sql = "UPDATE ProcessExecutionStateInformation SET ExecutionState = ?,"
					+ " ExecutionCode = ?, EndExecutionTimeStamp = ? WHERE RequestID = ? "
					+ "AND ReplicaID = ? AND ProcessID = ? AND RestartID = ?;";

			statement = connection().prepareStatement(sql);

			statement.setInt(1, executionState);
			statement.setInt(2, executionCode);
			statement.setLong(3, System.currentTimeMillis());
			statement.setString(4, requestId);
			statement.setString(5, replicaId);
			statement.setString(6, processId);
			statement.setInt(7, lastId);
			
			statement.execute();
			statement.close();
			
			int currentState = calculateApplicationExecutionState(requestId);
			changeApplicationExecutionState(requestId, currentState);
						
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Retrieve application execution static information.
	 * 
	 * @param requestId
	 *            Unique application execution ID.
	 * @return Application execution information.
	 */
	public synchronized ApplicationExecutionInformation getApplicationExecutionInformation(
			String requestId) {

		String[] str = separateReplicaIdFromRequestId(requestId);
		requestId = str[0];

		ApplicationExecutionInformation appInfo = null;
		PreparedStatement statement = null;
		String sql = null;
		try {
			sql = "SELECT * FROM ApplicationExecutionInformation WHERE RequestID = ?;";
			statement = connection().prepareStatement(sql);
			statement.setString(1, requestId);

			ResultSet rs = statement.executeQuery();

			if (rs.first()) {
				appInfo = new ApplicationExecutionInformation();
				appInfo.applicationConstraints = rs
						.getString("ApplicationConstraints");
				appInfo.applicationName = rs.getString("ApplicationName");
				appInfo.applicationPreferences = rs
						.getString("ApplicationPreferences");
				appInfo.applicationRepositoryIor = rs
						.getString("ApplicationRepositoryIOR");
				appInfo.applicationType = ApplicationType.from_int(rs
						.getInt("ApplicationType"));
				appInfo.basePath = rs.getString("BasePath");
				appInfo.forceDifferentNodes = rs
						.getBoolean("ForceDifferentNodes");
				appInfo.originalGrmIor = rs.getString("OriginalGrmIOR");
				appInfo.previousGrmIor = rs.getString("PreviousGrmIOR");
				appInfo.requestingAsctIor = rs.getString("RequestingAsctIOR");
				appInfo.source = rs.getString("Source");
				appInfo.userId = rs.getString("UserId");
				statement.close();
			} else {
				statement.close();
				return null;
			}
			
			ArrayList<String> binaries = new ArrayList<String>();

			sql = "SELECT * FROM ApplicationExecutionInformation_AvailableBinaries WHERE RequestID = ?;";
			statement = connection().prepareStatement(sql);
			statement.setString(1, requestId);

			rs = statement.executeQuery();

			while (rs.next()) {
				binaries.add(rs.getString("AvailableBinary"));
			}
			appInfo.availableBinaries = binaries.toArray(new String[binaries
					.size()]);
			
			statement.close();
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return appInfo;
	}

	/**
	 * Retrieve Process execution static information.
	 * 
	 * @param requestId
	 *            Unique application execution ID.
	 * @param processId
	 *            Process ID.
	 * @return Process execution information.
	 */
	public synchronized ProcessExecutionInformation getProcessExecutionInformation(
			String requestId, String processId) {

		if (!hasReplicas(requestId)) {
			String[] str = separateReplicaIdFromRequestId(requestId);
			requestId = str[0];

			ProcessExecutionInformation processInfo = null;
			PreparedStatement statement = null;
			String sql = null;
			try {

				sql = "SELECT * FROM ProcessExecutionInformation WHERE RequestID = ? AND ProcessID = ?;";
				statement = connection().prepareStatement(sql);
				statement.setString(1, requestId);
				statement.setString(2, processId);
				ResultSet rs = statement.executeQuery();

				if (rs.first()) {
					processInfo = new ProcessExecutionInformation();
					processInfo.processArguments = rs
							.getString("ProcessArguments");
					ExecutionRequestId execId = new ExecutionRequestId();
					execId.processId = processId;
					execId.requestId = requestId;
					statement.close();
				} else {
					statement.close();
					close();
					return null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			close();
			return processInfo;
		}
		close();
		
		// TODO: Add code for dealing with replicas		
		return null;

	}

	/**
	 * Retrieve application execution state information.
	 * 
	 * @param requestId
	 *            Unique application execution ID.
	 * @return Application execution state information.
	 */
	public synchronized ApplicationExecutionStateInformation getApplicationExecutionStateInformation(
			String requestId) {

		if (!hasReplicas(requestId)) {
			String[] str = separateReplicaIdFromRequestId(requestId);
			requestId = str[0];

			PreparedStatement statement = null;
			String sql = null;
			try {
				sql = "SELECT ApplicationName,ExecutionState,FinishTimeStamp,SubmissionTimeStamp FROM "
						+ "ApplicationExecutionStateInformation s, ApplicationExecutionInformation i "
						+ "WHERE s.RequestID = ? AND i.RequestID = s.RequestID;";
				statement = connection().prepareStatement(sql);
				statement.setString(1, requestId);

				ResultSet rs = statement.executeQuery();

				if (rs.first()) {
					ApplicationExecutionStateInformation applicationInfo = new ApplicationExecutionStateInformation();
					applicationInfo.requestId = requestId;
					applicationInfo.applicationName = rs
							.getString("ApplicationName");
					applicationInfo.executionState = rs
							.getInt("ExecutionState");
					applicationInfo.finishTimeStamp = String.valueOf(rs
							.getLong("FinishTimeStamp"));
					applicationInfo.submissionTimeStamp = String.valueOf(rs
							.getLong("SubmissionTimeStamp"));
					
					statement.close();
					close();
					return applicationInfo;
				} else
					return null;

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		// TODO: Add code for dealing with replicas
		return null;
	}

	/**
	 * Retrieve Process execution state information.
	 * 
	 * @param requestId
	 *            Unique application execution ID.
	 * @param processId
	 *            Process ID.
	 * @return Process execution state information.
	 */
	public synchronized ProcessExecutionStateInformation getProcessExecutionStateInformation(
			String requestId, String processId) {

		if (!hasReplicas(requestId)) {
			String[] str = separateReplicaIdFromRequestId(requestId);
			requestId = str[0];

			PreparedStatement statement = null;
			String sql = null;
			try {
				int lastId = this.getLastProcessRestartId(requestId, processId);
				sql = "SELECT * FROM ProcessExecutionStateInformation WHERE RequestID = ? AND ProcessID = ? AND RestartID = ? ;";
				statement = connection().prepareStatement(sql);
				statement.setString(1, requestId);
				statement.setString(2, processId);
				statement.setInt(3, lastId);
				//System.out.println("Executando: " + sql);
				ResultSet rs = statement.executeQuery();

				if (rs.first()) {
					ProcessExecutionStateInformation processInfo = new ProcessExecutionStateInformation();
					processInfo.endExecutionTimeStamp = String.valueOf(rs
							.getLong("EndExecutionTimeStamp"));
					processInfo.startExecutionTimeStamp = String.valueOf(rs
							.getLong("StartExecutionTimeStamp"));
					processInfo.executionState = rs.getInt("ExecutionState");
					processInfo.executionCode = rs.getString("ExecutionCode");

					ExecutionRequestId execId = new ExecutionRequestId();
					execId.requestId = requestId; // sem o replicaId
					execId.processId = processId;
					processInfo.executionRequestId = execId;
					return processInfo;
				} 
					close();
					return null;

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		
		// TODO: Add code for dealing with replicas
		close();
		return null;
	}

	private synchronized int calculateApplicationExecutionState(String requestId) {
		String[] str = separateReplicaIdFromRequestId(requestId);
		requestId = str[0];

		PreparedStatement statement = null;
		String sql = null;

		List<Integer> states = new ArrayList<Integer>();

		try {
			// checks if some process is running
			sql = "SELECT ExecutionState FROM ProcessExecutionStateInformation "
					+ "WHERE RequestID = ? ";
			statement = connection().prepareStatement(sql);
			statement.setString(1, requestId);

			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				int executionState = rs.getInt("ExecutionState");
				states.add(executionState);
			}
			
			if (states.contains(ProcessExecutionStateTypes.ABORTED))
				return ApplicationExecutionStateTypes.ABORTED;
			if (states.contains(ProcessExecutionStateTypes.EXECUTING))
				return ApplicationExecutionStateTypes.EXECUTING;
			if (states
					.contains(ProcessExecutionStateTypes.FINISHED_WITH_HANDLED_FAILURE)) {

				sql = "SELECT COUNT(*) FROM ProcessExecutionStateInformation r WHERE "
						+ "r.RequestID = ? AND r.RestartID = 0 AND r.ExecutionState = ? "
						+ "AND NOT EXISTS ( SELECT * FROM "
						+ "ProcessExecutionStateInformation s WHERE "
						+ "r.ProcessID = s.ProcessID AND "
						+ "r.RequestID = s.RequestID AND "
						+ "s.ExecutionState <> ? );";

				statement = connection().prepareStatement(sql);
				statement.setString(1, requestId);
				statement.setInt(2, ProcessExecutionStateTypes.FINISHED_WITH_HANDLED_FAILURE);				
				statement.setInt(3, ProcessExecutionStateTypes.FINISHED_WITH_HANDLED_FAILURE);

				rs = statement.executeQuery();
				rs.next();
				int countUnfinished = rs.getInt(1);
				
				statement.close();
				
				if (countUnfinished > 0)
					return ProcessExecutionStateTypes.EXECUTING;
			}
			if (states.contains(ProcessExecutionStateTypes.FINISHED_WITH_UNHANDLED_FAILURE)
					|| states.contains(ProcessExecutionStateTypes.PRE_STARTED_FAILURE))
				return ApplicationExecutionStateTypes.FINISHED_WITH_FAILURES;

			return ApplicationExecutionStateTypes.FINISHED;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ApplicationExecutionStateTypes.FINISHED;
	}

	/**
	 * Verify if the application has replicas
	 * 
	 * @param requestId
	 *            Unique application execution ID.
	 * @return true if the application has replicas, and false if not.
	 */
	private synchronized boolean hasReplicas(String requestId) {
		return (requestId.split(":").length > 3);
	}

	private synchronized int getLastProcessRestartId(String requestId, String processId) {
		String[] str = separateReplicaIdFromRequestId(requestId);
		requestId = str[0];
		String replicaId = str[1];

		PreparedStatement statement = null;
		String sql = null;
		try {

			sql = "SELECT COUNT(*) FROM ProcessExecutionStateInformation WHERE RequestID = ? "
					+ "AND ReplicaID = ? AND ProcessID = ? ;";

			statement = connection().prepareStatement(sql);

			statement.setString(1, requestId);
			statement.setString(2, replicaId);
			statement.setString(3, processId);

			ResultSet rs = statement.executeQuery();
			rs.next();
			if (rs.getInt(1) == 0)
				return -1;
			statement.close();

			sql = "SELECT MAX(RestartID) FROM ProcessExecutionStateInformation WHERE RequestID = ? "
					+ "AND ReplicaID = ? AND ProcessID = ? ;";

			statement = connection().prepareStatement(sql);

			statement.setString(1, requestId);
			statement.setString(2, replicaId);
			statement.setString(3, processId);

			rs = statement.executeQuery();
			rs.next();
			int lastId = rs.getInt(1);
			
			statement.close();
			return lastId;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Separate the replica ID from request ID.
	 * 
	 * @param requestId
	 *            Original request ID.
	 * @return An vector containing the requestID, and the replica ID.
	 */
	private synchronized String[] separateReplicaIdFromRequestId(String requestId) {
		String[] reqId = requestId.split(":");
		String[] result = new String[2];

		if (reqId.length == 3) {
			result[0] = requestId;
			result[1] = "0";
		} else {
			result[0] = reqId[0] + ":" + reqId[1] + ":" + reqId[2];
			result[1] = reqId[3];
		}
		return result;
	}

	
	private synchronized Connection connection(){
		if (this.connection == null){
			try {
				this.connection = DriverManager.getConnection(this.url);
				this.connection.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return this.connection;

	}
	private synchronized void close(){
		try {		
			connection().close();
			this.connection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
