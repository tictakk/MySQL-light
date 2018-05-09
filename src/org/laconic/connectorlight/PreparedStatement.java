package org.laconic.connectorlight;
import java.io.IOException;

/*
 * Matthew Kersey 2017
 */

public interface PreparedStatement {

	abstract void prepare(String sql) throws IOException;
	abstract void changeStatement(String sql);
	abstract void execute(String sql) throws IOException;
	abstract void close();
	abstract void execute();
	abstract ResultSet executeQuery() throws IOException;

}
