package org.laconic.connectorlight;
/*
 * Matthew Kersey 2017
 */

import java.io.IOException;

public interface Statement extends java.sql.Statement, java.sql.Wrapper{

//	public abstract boolean doUpdate(String update);
//	public abstract ResultSet doQuery(String query) throws IOException;
	public abstract boolean execute(String sql);
	public abstract void close();
	
}
