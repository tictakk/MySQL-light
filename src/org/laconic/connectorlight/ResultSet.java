package org.laconic.connectorlight;
/*
 * Matthew Kersey 2017
 */

public interface ResultSet extends java.sql.ResultSet{

	public abstract String getString(String column);
	public abstract int getInt(int column);
	public abstract String getString(int column);
	public abstract int getInt(String column);
	//skiprow?
	public abstract boolean hasNext();
	public int getNumberOfRows();
}
