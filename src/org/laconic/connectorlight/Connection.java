package org.laconic.connectorlight;
/*
 * Matthew Kersey 2017
 */

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface Connection extends java.sql.Connection{
		
	public abstract void ping();
	
	public abstract int getServerCharSet();
	
	public abstract void quit() throws IOException;
	
	public abstract int getID();

	public abstract ServerIO getServerIO();
	
	public abstract void changeDB(String database) throws IOException;
	
	public abstract void setServerStatus();
	
	ResultSet doTextQuery(String query) throws IOException;
	
//	Statement createStatement();
	
	public abstract boolean changeUser(String username, String password) throws IOException, NoSuchAlgorithmException;
	
	PreparedStatement createdPreparedStatement();
	
	public abstract void doCommand(int command, String sql) throws IOException;
	
	public ResultSet getResultSetBinaryProtocol(int numOfColumns) throws IOException;

	ResultSet getResultSetTextProtocol(int numOfColumns) throws IOException;
}
