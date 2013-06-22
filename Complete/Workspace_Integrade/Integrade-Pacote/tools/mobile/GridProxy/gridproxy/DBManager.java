package gridproxy;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;

import asct.shared.ApplicationState;
import asct.shared.ExecutionRequestStatus;


/**
 * Cria a conexão com o banco de dados e gerencia todas 
 * as consultas, inserções, atualizações e deleção de dados
 * no banco. 
 * 
 * @author Eduardo Viana
 */
public class DBManager {
	
	
	private static DBManager instance = null; // instancia unica
	
	Connection conn;				// conecta ao banco
	Statement statement = null;		// executa os comandos sql
	GridProxyProperties properties;	// carrega a propriedade de criação do banco e do sistema 
	
	private DBManager(){
		
		try {
			
			Class.forName("org.h2.Driver");
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String url = "jdbc:h2:file:db/mobile";
        String user = "user";
                
        Properties prop = new Properties();
        prop.setProperty("user", user);
        prop.put("password", "h2");
        
        try {
			conn = DriverManager.getConnection(url, prop);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		try {
			statement = conn.createStatement();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		properties = new GridProxyProperties();
		
		if( ! properties.isDatabaseCreated() ){
		
			String sql0 = 	"CREATE USER user PASSWORD 'h2' ADMIN;";
			
			String sql1 = 	"CREATE SCHEMA SUBMITION_SCHEMA AUTHORIZATION user;";
			
			String sql2 = 	"CREATE TABLE SUBMITION"							+
							"("													+
								"ID_SUBMITION numeric NOT NULL AUTO_INCREMENT PRIMARY KEY,"	+
								"ID_CLIENT varchar(80),"						+
								"APP_NAME varchar(80),"							+
								"ID_EXECUTION varchar(80),"						+
								"STATUS varchar(80),"							+
								"TIME timestamp NOT NULL"						+
			  				");"												+
							"GRANT ALL ON SUBMITION TO user;"					+
							"GRANT SELECT, UPDATE, INSERT, DELETE ON SUBMITION TO user;";
			
			String sql3 = 	"CREATE TABLE INPUT_FILES"							+
							"("													+
							"ID_SUBMITION numeric NOT NULL,"					+
							"FILE_NAME varchar(1000) NOT NULL,"					+
							"FILE_CONTENT blob,"						+
							"PRIMARY KEY(ID_SUBMITION, FILE_NAME),"				+
							"FOREIGN KEY(ID_SUBMITION) REFERENCES SUBMITION(ID_SUBMITION) " +
							"ON DELETE CASCADE " 								+
							"ON UPDATE CASCADE" 								+
							");"												+
							"GRANT ALL ON INPUT_FILES TO user;"					+
							"GRANT SELECT, UPDATE, INSERT, DELETE ON INPUT_FILES TO user;";
			
			try {
				
				statement.execute(sql1);
				statement.execute(sql2);
				statement.execute(sql3);
				properties.setDatabaseCreated( true );
					
			} catch (SQLException e) {
				
				properties.setDatabaseCreated( false );
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	public static DBManager getInstance(){
		
		if( instance == null ){
			
			instance = new DBManager();
		}
		return instance;
	}
	
	
	GridProxyProperties getGridProxyProperties(){
		return properties;
	}
	
	
	/* ----------------------------------------------------------------------- */
	/* MANAGEMENT OPERATIONS OF SUBMITION TABLE */
	/* ----------------------------------------------------------------------- */
	
	
	/**
	 * @param clientId
	 * @param executionId
	 * @param status
	 * @return
	 */
	public synchronized long insertSubmition( String clientId, String appName, String executionId, 
			ExecutionRequestStatus status ){
		
		String statusExec = "";
		long submitionId = 0;
		
		
		
		if( status != null ){
			if( status.getApplicationState().equals(ApplicationState.EXECUTING) ){
				statusExec = "EXECUTING";
			}
			else if( status.getApplicationState().equals(ApplicationState.FINISHED) ){
				statusExec = "FINISHED";
			}
			else if( status.getApplicationState().equals(ApplicationState.REFUSED) ){
				statusExec = "REFUSED";
			}
			else if( status.getApplicationState().equals(ApplicationState.TERMINATED) ){
				statusExec = "TERMINATED";
			}
		}
		
		String 											sql  = "INSERT INTO SUBMITION ( ";
		if( clientId != null )							sql +=							 "ID_CLIENT,";
		if( appName != null )							sql +=							 "APP_NAME,";
		if( executionId != null ) 						sql += 							 "ID_EXECUTION,";
		if( status != null ) 							sql += 							 "STATUS,";
														sql += 							 "TIME )";
														sql += " VALUES"+			" ( ";
		if( clientId != null )							sql +=							 "'"+clientId+"',";
		if( appName != null )							sql +=							 "'"+appName+"',";
		if( executionId != null ) 						sql += 							 "'"+executionId+"',";
		if( status != null ) 							sql += 							 "'"+statusExec	+"',";
														sql += 							 "CURRENT_TIMESTAMP() )";
		
		
		System.out.println("Sql: " + sql);
														
														
		try {
			
			statement.execute(sql);
			
			sql = "SELECT ID_SUBMITION, MAX( SUBMITION.TIME ) FROM SUBMITION GROUP BY ID_SUBMITION";
			
			ResultSet rs = statement.executeQuery(sql);
			rs.next();
			submitionId = rs.getLong("ID_SUBMITION");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return submitionId;
	}
	
	

	/**
	 * @param clientId
	 * @param executionId
	 * @param status
	 * @return
	 */
	public synchronized void updateSubmition( long submitionId, String clientId, String executionId, 
			ExecutionRequestStatus status, long time ){
		
		String statusExec = "";		
		
		
		if( status != null ){
			
			if( status.getApplicationState().equals(ApplicationState.EXECUTING) ){
				statusExec = "EXECUTING";
			}
			else if( status.getApplicationState().equals(ApplicationState.FINISHED) ){
				statusExec = "FINISHED";
			}
			else if( status.getApplicationState().equals(ApplicationState.REFUSED) ){
				statusExec = "REFUSED";
			}
			else if( status.getApplicationState().equals(ApplicationState.TERMINATED) ){
				statusExec = "TERMINATED";
			}
			
		}
				
		String sql = "UPDATE SUBMITION SET ";
		if( clientId != null ) 							sql += "ID_CLIENT='"+clientId+"' ";
		if( clientId != null && executionId != null ) 	sql += ", ";
		if( executionId != null ) 						sql += "ID_EXECUTION='"+executionId+"' ";
		if( executionId != null && status != null ) 	sql += ", ";
		if( status != null ) 							sql += "STATUS='"+statusExec+"' ";
		if( status != null && time != 0 ) 				sql += ", ";
		if( time != 0 ) 								sql += "TIME="+time+" ";
		if( submitionId != 0 )							sql += "WHERE ID_SUBMITION="+submitionId;
		if( submitionId == 0 && executionId != null )	sql += "WHERE ID_EXECUTION='"+executionId+"'";
			
		try {
			
			statement.executeUpdate(sql);
			
			} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * @param execId
	 * @return
	 */
	public synchronized String selectSubmitionStatus(long submitionId) {
		
		
		String status = "";
		String sql = "SELECT STATUS FROM SUBMITION WHERE ID_SUBMITION = " + submitionId;
		try {
			
			ResultSet rs = statement.executeQuery(sql);
			if( rs.next() )
				status = rs.getString("STATUS");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return status;
	}
	
	
	/**
	 * @param user
	 * @param from
	 * @param to
	 * @return
	 */
	public synchronized Vector selectSubmitionStatus(String user, String from, String to){
		

		
		String fromDate[] = from.split("/");
		String toDate[] = to.split("/");
		
		for(int i=0; i < fromDate.length; i++){
			System.out.println("formDate: " + fromDate[i]);
		}
		
		for(int i=0; i < toDate.length; i++){
			System.out.println("toDate: " + toDate[i]);
		}		
		
		
		Vector submitions = null;
		String sql = 		" SELECT ID_SUBMITION, APP_NAME, STATUS FROM SUBMITION " +
							" WHERE (ID_CLIENT='"+user+"') " +
							"AND (TIME >= '"+fromDate[2]+"-"+fromDate[1]+"-"+fromDate[0]+" 00:00:00.000') " +
							"AND (TIME <= '"+toDate[2]+"-"+toDate[1]+"-"+toDate[0]+" 23:59:59.999' )" +
							"ORDER BY ID_SUBMITION";
		try {
			
			ResultSet rs = statement.executeQuery(sql);
			submitions = new Vector();
			while( rs.next() ){
				long s1 = rs.getLong("ID_SUBMITION");
				String s2 = rs.getString("APP_NAME");
				String s3 = rs.getString("STATUS");
				submitions.add( s1+"-"+s2+"-"+s3 );
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return submitions;
	}
	
	
	/**
	 * @param execId
	 * @return
	 */
	public synchronized String selectSubmitionClient(String execId) {
		
		String cliente = "Nao pode pegar o cliente";
		String sql = "SELECT ID_CLIENT FROM SUBMITION WHERE ID_EXECUTION = '"+execId+"'";
		try {
			
			ResultSet rs = statement.executeQuery(sql);
			if( rs.next() )
				cliente = rs.getString("ID_CLIENT");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return cliente;
	}
	
	/**
	 * @param execId
	 * @return
	 */
	public synchronized long selectSubmitionId(String execId) {
		
		long id = 0;
		String sql = "SELECT ID_SUBMITION FROM SUBMITION WHERE ID_EXECUTION = '"+execId+"'";
		try {
			
			ResultSet rs = statement.executeQuery(sql);
			if( rs.next() )
				id = rs.getLong("ID_SUBMITION");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return id;
	}
	
	/**
	 * @param execId
	 * @return
	 */
	public synchronized String selectSubmitionAppName(String execId) {
		
		String name = "Nao pode ser nome";
		String sql = "SELECT APP_NAME FROM SUBMITION WHERE ID_EXECUTION = '"+execId+"'";
		try {
			
			ResultSet rs = statement.executeQuery(sql);
			if( rs.next() )
				name = rs.getString("APP_NAME");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return name;
	}
	
	/**
	 * @param submitionId
	 */
	public synchronized String selectSubmitionExecutionId(long submitionId) {
		
		String executionID = "Nao pode pegar o id de execucao";
		String sql = "SELECT ID_EXECUTION FROM SUBMITION WHERE ID_SUBMITION = "+submitionId;
		try {
			
			ResultSet rs = statement.executeQuery(sql);
			if( rs.next() )
				executionID = rs.getString("ID_EXECUTION");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return executionID;
		
		
	}
	
	
	
	/**
	 * @return
	 */
	public long getNextSubmitionId() {
		
		long id = 0;
		String sql = "SELECT ID_SUBMITION FROM SUBMITION ORDER BY ID_SUBMITION LIMIT 1";
		
		try {
			
			ResultSet rs = statement.executeQuery(sql);
			if( rs.next() )
				id = rs.getLong("ID_SUBMITION");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	
	
	/* ----------------------------------------------------------------------- */
	/* MANAGEMENT OPERATIONS OF INPUT_FILE TABLE */
	/* ----------------------------------------------------------------------- */
	
	/**
	 * @param submitionId
	 * @param fileName
	 * @param fileContent
	 */
	public synchronized void insertInputFile(long submitionId, String fileName, InputStream fileContent ){
		
		
		String sql = 	"INSERT INTO INPUT_FILES ( ID_SUBMITION, FILE_NAME ) " +
						"VALUES"+				"( "+submitionId+", '"+fileName+"' )";
		
		try {		

			
			statement.execute(sql);
			//PreparedStatement prep = conn.prepareStatement(sql);
			//prep.setBinaryStream(1, fileContent, fileContent.available() );
			//prep.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		
		}
		
	}
	
	
	
	
	
	
	public static void main(String args[]){
		
		DBManager db = getInstance();
		
		String sql = "SELECT * FROM SUBMITION";
		try {
			ResultSet rs = db.statement.executeQuery(sql);
			while( rs.next() ){				

				
			}
			System.out.println(" -----------------------------------------------------------");
			sql = "SELECT * FROM INPUT_FILES";
			ResultSet rs2 = db.statement.executeQuery(sql);
			System.out.println(" | ID_SUBMITION | FILE_NAME | FILE_CONTENT |");
			System.out.println(" -------------------------------------------");
			while( rs2.next() ){
				
				Blob b = rs2.getBlob("FILE_CONTENT");			

				
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
