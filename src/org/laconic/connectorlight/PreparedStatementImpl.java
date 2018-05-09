package org.laconic.connectorlight;
import java.io.IOException;

/*
 * Matthew Kersey 2017
 */

public class PreparedStatementImpl implements PreparedStatement{
	
	private String statement=null;
	private Connection conn=null;
	private int warnings=0;
	private int status=0;
	private int statementId=0;
	
	private PreparedStatementImpl(Connection connection) {
		this.conn=connection;
	}
	
	@Override
	public void prepare(String sql) throws IOException {
		
		int command = 0x16;
		this.statement = sql;
		this.conn.doCommand(command, sql); //send command to server

		Buffer prepareResponse = this.conn.getServerIO().receivePacket();

		int statementId = 0;
		int numOfColumns = 0;
		int numOfParams = 0;
		int numOfWarnings = 0;
		//command response		
		byte[] packet = prepareResponse.getBuffer();
		if(packet[0]==0) {
			//okay packet
			statementId = (packet[1] & 0xFF) + ((packet[2] & 0xFF) << 8) + ((packet[3] & 0xFF) << 16) + ((packet[4] & 0xFF) << 24);
			numOfColumns = (packet[5] & 0xFF) + ((packet[6] & 0xFF) << 8);
			numOfParams = (packet[7] & 0xFF) + ((packet[8] & 0xFF) << 8);
			numOfWarnings = (packet[10] & 0xFF) + ((packet[11] & 0xFF) << 8);
			this.statementId=statementId;
		}
		
		int i = 0;
		byte[] buff; //uesless?
		while(numOfColumns > i) { //get number of columns
//			System.out.println(i);
			buff = this.conn.getServerIO().receivePacket().getBuffer();
			i++;
		}
		
		//get EOF
		prepareResponse = this.conn.getServerIO().receivePacket();
		packet = prepareResponse.getBuffer();
		if(packet[0] == (0xFE)) {
			this.warnings = (packet[1] & 0xFF) + ((packet[2] & 0xFF) << 8);
			this.status = (packet[3] & 0xFF) + ((packet[4] & 0xFF) << 8);
		}
	}

	@Override
	public void changeStatement(String sql) {
		
	}

	@Override
	public ResultSet executeQuery() throws IOException {
		/*
		 * execute header 0x17
		 * flags
		 * 0 - no cursor
		 * 1 - read only
		 * 2 - cursor for update
		 * 3 - scrollable cursor
		 */
		
		if(this.conn.getServerIO().databaseIsAssigned()) {
			byte header = (byte) 0x17;
			byte[] id = {(byte)(this.statementId),(byte)(this.statementId >>>8),(byte)(this.statementId>>>16),(byte)(this.statementId>>>24)};
			byte flag = 0;
			byte[] iterator = {1, 0, 0, 0};
			Buffer packet = new Buffer(0);
			packet.addBytes(new byte[4]);
			packet.addByte(header);
			packet.addBytes(id);
			packet.addByte(flag);
			packet.addBytes(iterator);
			packet.setByte(0, (byte)(packet.getBuffer().length - 4));
			this.conn.getServerIO().sendPacket(packet); //needs to be fixed
			int response = this.conn.getServerIO().receivePacket().getBuffer()[0];

			if(response == 0xFF) {
				System.out.println("Error packet");
				return null;
			}else if(response == 0x00) {
				System.out.println("OK packet");
				return null;
			}else if(response == 0xFB) {
				System.out.println("Infile?");
				return null;
			}else {
				return this.conn.getResultSetBinaryProtocol(response);
			}
		}else {
			System.out.println("Database not assigned");
			return null;
		}
	}

	@Override
	public void close() {
		
	}

	protected static PreparedStatement createPreparedStatement(Connection connection) {
		return new PreparedStatementImpl(connection);
	}

	@Override
	public void execute(String sql) throws IOException {
		
	}

	@Override
	public void execute() {
		
	}
}
