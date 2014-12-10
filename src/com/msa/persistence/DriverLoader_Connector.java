package com.msa.persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// Notice, do not import com.mysql.jdbc.*
// or you will have problems!


public class DriverLoader_Connector {

	private boolean driverLoaded=false;
	private Connection conn = null;

	private String dbUri="jdbc:mysql://localhost/WekaAnalysis";
	private String username="root";
	private String password="giantsquid";

	private void start() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			driverLoaded=true;
		} catch (Exception ex) {
			driverLoaded=false;
			ex.printStackTrace();
		}
	}

	
	public Connection getConnection() throws SQLException{
		if(!driverLoaded)
			start();
		
		if(conn == null || conn.isClosed()){
			try {
				conn = DriverManager.getConnection(dbUri, username, password);
				return conn;

			} catch (SQLException ex) {
				// handle any errors
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
			}
		}
		return conn;
	}
		


}

