package org.laconic.connectorlight;
/*
 * Matthew Kersey 2017
 */

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionImpl implements Connection{
	
	private ServerIO io = null;
	private int port;
	private String host;
	private String schema=null;
	private String user;
	private String pass;
	private int connectionID;
	private boolean autoCommit=false;
	private int warnings;
	private int status;
	
	public ConnectionImpl(String hostConnection, int portConnection, String username, String password, String databaseName) throws UnknownHostException, IOException, NoSuchAlgorithmException {
		
		this.port = portConnection;
		this.host = hostConnection;
		this.user = username;
		this.pass = password;
		this.schema = databaseName;
		
		coreConnect();
		setServerStatus();
		if(this.schema!=null) {
			changeDB(this.schema); //need to fix the client protocol to allow connecting to DB in handshake
		}
	}

	@Override
	public void ping() {		
		
	}

	@Override
	public int getServerCharSet() {
		return io.getServerCharSet();
	}

	@Override
	public void quit() throws IOException {
		io.sendCommand(1);
		io.close();
	}

	private void coreConnect() throws UnknownHostException, IOException, NoSuchAlgorithmException {
		io = new ServerIO(this.host,this.port);
		io.sendHandshake(this.user, this.pass);
		setID();
	}
	
	private void setID() {
		this.connectionID = this.io.getID();
	}

	@Override
	public int getID() {
		return this.connectionID;
	}
	
	public ServerIO getServerIO() {
		return this.io;
	}

	@Override
	public void changeDB(String database) throws IOException {
		this.io.sendCommand(2, database);
		Buffer buffer = this.io.receivePacket();
		byte[] packet = buffer.getBuffer();
		if(packet[0] == 0 ) { //OK packet
			this.warnings = (packet[1] & 0xFF) + ((packet[2] & 0xFF) << 8);
			this.status = (packet[3] & 0xFF) + ((packet[4] & 0xFF) << 8);
		}
		
	}

	@Override
	public void setServerStatus() {
		int status = this.io.getSeverStatus();
		if((ServerDefinitions.SERVER_STATUS_AUTOCOMMIT & status ) != 0) {
			this.autoCommit=true;
		}else {
			this.autoCommit=false;
		}
	}
	
	private boolean getAutocommit() {
		return this.autoCommit;
	}
	
	private Buffer getResponsePacket() {
		return null;
	}
	
	@Override
	public ResultSet doTextQuery(String query) throws IOException {
		/*
		 * possible resultset responses
		 * 0xFF Error packet
		 * 0x00 OK packet --- statement didn't require resultset
		 * 0xFB Local infile... dunno yet
		 * Resultset beginning with column number
		 */
		
		if(this.io.databaseIsAssigned()) {
			this.io.doSelect(3, query); //select
			Buffer buffer = this.io.receivePacket(); //select results, col number
			int result = (buffer.getBuffer()[0] & 0xFF);
			int numOfColumns;
			if(result == 0xFF) {
				System.out.println("Error packet");
				return null;
			}else if(result == 0x00) {
				System.out.println("OK packet");
				return null;
			}else if(result == 0xFB) {
				System.out.println("Infile?");
				return null;
			}else {
				numOfColumns = result;
				return getResultSetTextProtocol(numOfColumns);
			}
		}else {
			System.out.println("Database not assigned");
			return null;
		}
	}
	
	@Override
	public ResultSet getResultSetTextProtocol(int numOfColumns) throws IOException {
		String[] columns = new String[numOfColumns];
		Buffer buffer = new Buffer(0);
		LinkedHashMap<String,ArrayList<String>> table = new LinkedHashMap<String,ArrayList<String>>();

		int i = 0;
		while(numOfColumns > i) {
			buffer = this.io.receivePacket(); //select results, column data
			buffer.setPosition(0);
			buffer.readLenecString();
			buffer.readLenecString();
			buffer.readLenecString();
			buffer.readLenecString();
			columns[i] = buffer.readLenecString();
			table.put(columns[i], new ArrayList<String>());
			buffer.readLenecString();
			buffer.readLenecInt();
			buffer.readInt(2, 0);
			buffer.readInt(4, 0);
			buffer.readInt(1, 0);
			buffer.readInt(2, 0);
			buffer.readInt(1, 0);
			buffer.readInt(2, 0);
			i++;
		}
		//get EOF info
		buffer = this.io.receivePacket(); //select results EOF
		buffer.setPosition(0);
		
		int warnings = buffer.readInt(2,0);
		int serverStatus = buffer.readInt(2, 0);
		
		//now start reading the packets
		int z = 0;
		while(this.io.hasNext()) { //now every row
			buffer = this.io.receivePacket();
			if(((buffer.getBuffer()[0] & 0xFF)==0xFE) || ((buffer.getBuffer()[0] & 0xFF)== 0xFF) || ((buffer.getBuffer()[0] & 0xFF)== 0xFF)) { //this is the last packet
				this.io.setServerStatus((buffer.getBuffer()[3] & 0xFF) + ((buffer.getBuffer()[4] & 0xFF) << 8));
				break;
			}
			int j = 0;
			buffer.setPosition(0);
			while(numOfColumns > j) { //every column in the row
				if((buffer.getBuffer()[buffer.getCurrentPosition()]&0xFF)==251) { //check if a value is null
					table.get(columns[j]).add("null");
					buffer.setPosition(buffer.getCurrentPosition()+1);
				}else {
					table.get(columns[j]).add(buffer.readLenecString());
				}
				j++;
			}
			z++;
		}

		ResultSet rs = ResultSetImpl.createResultSet(table, columns, z);
		return rs;
	}
	
	@Override
	public ResultSet getResultSetBinaryProtocol(int numOfColumns) throws IOException {
		String[] columns = new String[numOfColumns];
		Buffer buffer = new Buffer(0);
		LinkedHashMap<String,ArrayList<String>> table = new LinkedHashMap<String,ArrayList<String>>();
		
		int i = 0;
		int types[] = new int[numOfColumns];
		while(numOfColumns > i) {
			buffer = this.io.receivePacket(); //select results, column definition data
			buffer.setPosition(0);
			buffer.readLenecString();
			buffer.readLenecString();
			buffer.readLenecString();
			buffer.readLenecString();
			columns[i] = buffer.readLenecString();
			table.put(columns[i], new ArrayList<String>());
			buffer.readLenecString();
			buffer.readLenecInt();
			buffer.readInt(2, 0);
			buffer.readInt(4, 0);
			types[i] = buffer.readInt(1, 0);//field types
			buffer.readInt(2, 0);
			buffer.readInt(1, 0);
			buffer.readInt(2, 0);
			i++;
		}
		//get EOF info
		buffer = this.io.receivePacket(); //select results EOF
		buffer.setPosition(0);
		
		int warnings = buffer.readInt(2,0);
		int serverStatus = buffer.readInt(2, 0);
		
		//Binary protocol row
		int z = 0;
		while(this.io.hasNext()) {
			buffer = this.io.receivePacket();
			if(((buffer.getBuffer()[0] & 0xFF)==0xFE) || ((buffer.getBuffer()[0] & 0xFF)== 0xFF) || ((buffer.getBuffer()[0] & 0xFF)== 0xFF)) { //this is the last packet
				this.io.setServerStatus((buffer.getBuffer()[3] & 0xFF) + ((buffer.getBuffer()[4] & 0xFF) << 8));
				break;
			}
			
			int j = 0;
			int maxNulls = 0; //will implement this late
			int nulls = 0;
			if(buffer.getBuffer()[1] != 0) { //we have at least on null parameter
				maxNulls = (numOfColumns+9)/8;
				nulls = (buffer.getBuffer()[1] & 0xFF) >> 2;
			}
			buffer.setPosition(2);
			
			while(numOfColumns > j) { //every column in the row
				
				if((nulls & 1<<j)==0) {
					switch(types[j]) { //obviously need to account for all type possiblities
					
						case Types.LONG: table.get(columns[j]).add(String.valueOf(buffer.readInt(4, 0))); break;// buffer.readInt(4, 0); break;
						
						case Types.VARSTRING: table.get(columns[j]).add(buffer.readLenecString()); break;
						
						default: System.out.println("no type found"); break;
					}
				}else {
					table.get(columns[j]).add("null");
				}
				j++;
			}
			z++;
		}
		
		ResultSet rs = ResultSetImpl.createResultSet(table, columns, z);
		return rs;
	}

	@Override
	public boolean changeUser(String username, String password) throws IOException, NoSuchAlgorithmException {
		this.io.changeUser(username, password);
		return false;
	}

	@Override
	public PreparedStatement createdPreparedStatement() {
		return PreparedStatementImpl.createPreparedStatement(this);
	}

	@Override
	public void doCommand(int command, String sql) throws IOException {
		this.io.sendCommand(command, sql);
	}

	@Override
	public void abort(Executor arg0) throws SQLException {
		
	}

	@Override
	public void clearWarnings() throws SQLException {
		
	}

	@Override
	public void close() throws SQLException {
		try {
			quit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void commit() throws SQLException {
		
	}

	@Override
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException {
		return null;
	}

	@Override
	public Clob createClob() throws SQLException {
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return null;
	}

	@Override
	public Statement createStatement() throws SQLException {
		StatementImpl st = new StatementImpl(this);
		return st;
	}

	@Override
	public Statement createStatement(int arg0, int arg1) throws SQLException {
		return null;
	}

	@Override
	public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
		return null;
	}

	@Override
	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return null;
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return false;
	}

	@Override
	public String getCatalog() throws SQLException {
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return null;
	}

	@Override
	public String getClientInfo(String arg0) throws SQLException {
		return null;
	}

	@Override
	public int getHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return null;
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return 0;
	}

	@Override
	public String getSchema() throws SQLException {
		return null;
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return 0;
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return null;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return false;
	}

	@Override
	public boolean isValid(int arg0) throws SQLException {
		return false;
	}

	@Override
	public String nativeSQL(String arg0) throws SQLException {
		return null;
	}

	@Override
	public CallableStatement prepareCall(String arg0) throws SQLException {
		return null;
	}

	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2) throws SQLException {
		return null;
	}

	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return null;
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(String arg0) throws SQLException {
		return null;
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
		return null;
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
		return null;
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
		return null;
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(String arg0, int arg1, int arg2) throws SQLException {
		return null;
	}

	@Override
	public java.sql.PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return null;
	}

	@Override
	public void releaseSavepoint(Savepoint arg0) throws SQLException {
		
	}

	@Override
	public void rollback() throws SQLException {
		
	}

	@Override
	public void rollback(Savepoint arg0) throws SQLException {
		
	}

	@Override
	public void setAutoCommit(boolean arg0) throws SQLException {
		
	}

	@Override
	public void setCatalog(String arg0) throws SQLException {
		
	}

	@Override
	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		
	}

	@Override
	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		
	}

	@Override
	public void setHoldability(int arg0) throws SQLException {
		
	}

	@Override
	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
	
	}

	@Override
	public void setReadOnly(boolean arg0) throws SQLException {
		
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return null;
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return null;
	}

	@Override
	public void setSchema(String schema) throws SQLException {
	
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
	
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}
	
}
