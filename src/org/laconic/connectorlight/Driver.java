package org.laconic.connectorlight;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class Driver implements java.sql.Driver{
	
	private static final String PREFIX = "jdbc:mysql://"; //protocol : subprotocol
	
	private int port = 0;
	private String host = null;
	private String schema = null;
	private String username = null;
	private String password = null;

	static {
        try {
            java.sql.DriverManager.registerDriver(new Driver());
        } catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }

    public Driver() throws SQLException {
        // Required for Class.forName().newInstance()
    }

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		if(url==null) {
			return false;
		}
		try {
			String protocol = url.substring(0,(url.indexOf("//")+2));
//			System.out.println(protocol);
			if(protocol.equalsIgnoreCase(PREFIX)) {
				return true;
			}else {
				return false;
			}
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public java.sql.Connection connect(String url, Properties info) throws SQLException {
//		DataSource ds = Class.forName("Driver").newInstance();
		if(acceptsURL(url)) {
			parseURL(url);
			try {
//				return new ConnectionImpl("127.0.0.1",3306,"root","root",null);
				return new ConnectionImpl(this.host,this.port,this.username,this.password,this.schema);
			} catch (NoSuchAlgorithmException | IOException e) {
				e.printStackTrace();
				return null;
			}
		}else {
			return null;
		}
		
	}

	@Override
	public int getMajorVersion() {
		return 0;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return null;
	}

	@Override
	public boolean jdbcCompliant() {
		return false;
	}
	
	private void parseURL(String url) {
//		String u = "jdbc:mysql://127.0.0.1:3306/CFB?user=root&password=root";
		try {
			String hostport = url.substring(url.indexOf('/')+2,url.lastIndexOf('/'));
			String[] split = hostport.split(":");
			this.host = split[0].trim();
			this.port = Integer.parseInt((split[1].trim()));
			this.schema = url.substring(url.lastIndexOf('/')+1,url.indexOf('?')).trim();
			this.username = url.substring(url.indexOf('=')+1,url.indexOf('&')).trim();
			this.password = url.substring(url.lastIndexOf('=')+1,url.length()).trim();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
