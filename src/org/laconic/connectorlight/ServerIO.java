package org.laconic.connectorlight;
/*
 * Matthew Kersey 2017
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

public class ServerIO {

	public static final int PACKET_HEADER = 4;
	
	protected Socket sockectConnection;
	protected BufferedInputStream input;
	protected OutputStream output;
	private int packetSequence = 0;
	
	//PROTOCOL VERSION
	private int protocolVersion;
	//Server version
	Buffer serverVersion;
	//Thread ID
	private int mysqlID;
	//SEED
	private Buffer seed;
	//CAPABILITIES
	private int capabilities;
	//CHARSET
	private int serverCharSet;
	//STATUS

	private int serverStatus;
	//
	private Buffer stringBuffer;
	
	private boolean databaseIsAssign = false;
	private String databaseName = null;
	
	String host;
	int port = 3306;
	
	private byte[] header = new byte[4];
	
	public ServerIO(String host, int port) throws UnknownHostException, IOException {
		this.host=host;
		this.port=port;
		
		this.sockectConnection = new Socket(host,port);
		input = new BufferedInputStream(this.sockectConnection.getInputStream());
		output = new BufferedOutputStream(this.sockectConnection.getOutputStream());
		
		severInit();
		
	}
	
	public int getID() {
		return this.mysqlID;
	}
	
	protected Buffer receivePacket() throws IOException {
		/*
		 * Think I did a good job with a basic receive packet. Might need to run some checks
		 */
		this.input.read(header, 0, 4);
//		Buffer buffer = new Buffer(header);
		int packetLength = (header[0] & 0xFF) + ((header[1] & 0xFF) << 8) + ((header[2] & 0xFF) << 16);
		if(packetLength==0) {
			byte[] packet = new byte[30];
			this.input.read(packet);
			Buffer newBuff = new Buffer(packet); //fix these two return newBuff(packet)
			return newBuff;
		}else {
			byte[] packet = new byte[packetLength];
			this.input.read(packet);
			Buffer newBuff = new Buffer(packet); //fix these two return newBuff(packet)
			return newBuff;
		}
	}
	
	protected Buffer receivePacket(int expectedSeq) throws IOException{
		
		this.input.read(header, 0, 4);
		Buffer buffer = new Buffer(header);
		int packetLength = buffer.getBuffer()[0];
		int actualSeq = buffer.getBuffer()[3];
		if(expectedSeq != actualSeq) {
			System.out.println("Error, packets out of order");
			return new Buffer(30);
		}else {
			byte[] packet = new byte[packetLength];
			this.input.read(packet);
			
			Buffer newBuff = new Buffer(packet); //fix these two return newBuff(packet)
			return newBuff;
		}
	}
	
	private final void severInit() throws IOException {
		/*
		 * This function will gather all the info about the database and set up our handshake.
		 * Really need to fix setting the capabilities.
		 */
		
		int packetLength = 0;
		
		Buffer buffer = new Buffer(PACKET_HEADER + packetLength);
		buffer.setPosition(0);

		//After connecting via socket, server sends handshake
		int pos = 0;
		
		byte[] serverPacket = receivePacket(packetSequence).getBuffer();
		this.protocolVersion = serverPacket[0];
		pos++;
		
		//get serverVersion
		this.serverVersion = new Buffer(0);
		byte b = serverPacket[pos];
		while(b != 0) {
			serverVersion.addByte(b);
			b = serverPacket[pos++];
		}
		
		//mysql connection threadID
		this.mysqlID = (serverPacket[pos++] & 0xFF) + ((serverPacket[pos++] & 0xFF)<<8) + ((serverPacket[pos++] & 0xFF)<<16) + ((serverPacket[pos++] & 0xFF)<<24);
		
		//first 8 bytes of 20 byte seed
		this.seed = new Buffer(20);
		byte i = 1;
		while(i<9) {
			seed.addByte(serverPacket[pos++]);
			i++;
		}
		pos++;
		//server capabilities
		this.capabilities = (serverPacket[pos++] & 0xFF) + ((serverPacket[pos++] & 0xFF)<<8);
		this.serverCharSet = (serverPacket[pos++] & 0xFF);
		
		serverStatus = (serverPacket[pos++] & 0xFF) + ((serverPacket[pos++] & 0xFF)<<8);
		
		pos+=13;
		
		byte end = serverPacket[pos++];
		while(end != 0) {
			this.seed.addByte(end);
			end = serverPacket[pos++];
		}
		
		this.stringBuffer = new Buffer(0);
		end = serverPacket[pos++];
		while(end != 0) {
			this.stringBuffer.addByte(end);
			end = serverPacket[pos++];
		}
		
		this.header = new byte[4];
		packetSequence++;
	}
	
	public void sendHandshake(String username, String password) throws IOException, NoSuchAlgorithmException {
		//bit mask that is known to work is byte[0]51 byte[1]7
		byte[] pass = Security.scramble411(password, new String(this.seed.getBuffer()), null);
		Buffer packetToSend = new Buffer(0);
		byte[] header = new byte[4];
		byte[] bitmask = new byte[4];
		bitmask[0] = (byte) 0;
		bitmask[1] = (byte) 130;
		byte[] maxPacket = new byte[4];
		byte charSet = 8;
		byte[] reserve = new byte[23];
		byte[] un = username.getBytes("UTF-8");
		byte nul = 0;
		bitmask[1] = (byte) 2;
		
		packetToSend.addBytes(header);
		packetToSend.addBytes(bitmask);
		packetToSend.addBytes(maxPacket);
		packetToSend.addByte(charSet);
		packetToSend.addBytes(reserve);
		packetToSend.addBytes(un);
		packetToSend.addByte(nul);
		packetToSend.addByte((byte)pass.length);
		packetToSend.addBytes(pass);
		
		packetToSend.setByte(0, (byte)(packetToSend.getBuffer().length - PACKET_HEADER));
		packetToSend.setByte(3, (byte)packetSequence);
		packetSequence++;
		sendPacket(packetToSend);
		
		handshakeResponse();
	}
	
	public void sendHandshake(String username, String password, String database) throws IOException, NoSuchAlgorithmException {
		//bit mask that is known to work is byte[0]51 byte[1]7
		byte[] pass = Security.scramble411(password, new String(this.seed.getBuffer()), null);
		Buffer packetToSend = new Buffer(0);
		byte[] header = new byte[4];
		byte[] bitmask = new byte[4];
		bitmask[0] = (byte) 0;
		bitmask[1] = (byte) 130;
		byte[] maxPacket = new byte[4];
		byte charSet = 8;
		byte[] reserve = new byte[23];
		byte[] un = username.getBytes("UTF-8");
		byte nul = 0;
		bitmask[1] = (byte) 2;
		
		packetToSend.addBytes(header);
		packetToSend.addBytes(bitmask);
		packetToSend.addBytes(maxPacket);
		packetToSend.addByte(charSet);
		packetToSend.addBytes(reserve);
		packetToSend.addBytes(un);
		packetToSend.addByte(nul);
		packetToSend.addByte((byte)pass.length);
		packetToSend.addBytes(pass);
		
		packetToSend.setByte(0, (byte)(packetToSend.getBuffer().length - PACKET_HEADER));
		packetToSend.setByte(3, (byte)packetSequence);
		packetSequence++;
		sendPacket(packetToSend);
		
		handshakeResponse();
	}
	//This should be private, need to fix
	public final void sendPacket(Buffer packet) throws IOException {
		/*
		 * we should really do more with this fn, but for now, it just sends the packet via output.
		 */
		this.output.write(packet.getBuffer());
		this.output.flush();
	}
	
	private void handshakeResponse() throws IOException {
		/* OK
		 * int<1> Header
		 * int<len> affected rows
		 * int<len> last_insert_id
		 * if(CLIENT_41) ---- should always be yes
		 * int<2> status flags
		 * int<2> warnings END IF
		 * ERROR
		 * int<1> Header
		 * int<2> Error Code
		 * if(CLIENT_41) ----- always yes
		 * String<1> State Marker
		 * String<5> State END IF
		 * String<EOF>
		 */
		byte[] packet = receivePacket(packetSequence).getBuffer();
		if(packet[0]==0 || packet[0]==0xFE) {
			//OK packet
			int affectedRows = packet[1]; //Really need to be checking for length encoded integers
			int lastId = packet[2]; //Also need to fix
			int serverStatus = (packet[4] << 8) + packet[3];
			int warningCount = (packet[6] << 8) + packet[5];
		}else if(packet[0]==0xFF){
			int errno = (packet[2]<<8) + packet[1];
			StringBuilder state = new StringBuilder();
			state.append(packet[7]);
			state.append(packet[8]);
			state.append(packet[9]);
			state.append(packet[10]);
			state.append(packet[11]);
			state.append(packet[12]);
			System.out.println("Error: erro code -"+errno+" Message: "+state.toString());
		}else {
			System.out.println("Don't recognize packet header -- handshake response");
		}
//		packetSequence=0; //worry about this later
	}
	
	public void sendCommand(int command) throws IOException {
		byte[] com = new byte[5];
		com[0] = 1;
		com[1] = 0;
		com[2] = 0;
		com[3] = 0; //seq number
		com[4] = (byte)command;
		Buffer buffer = new Buffer(0);
		buffer.addBytes(com);
		this.header = new byte[4];
		sendPacket(buffer);
	}
	
	public void sendCommand(int command, String val) throws IOException {
		if(command==2) {
			this.databaseName=val;
			this.databaseIsAssign=true;
		}
		int size = val.length();
		Buffer buffer = new Buffer(size+1+PACKET_HEADER+1);
		buffer.addByte((byte)(size+1+1));
		buffer.addByte((byte)0);
		buffer.addByte((byte)0);
		buffer.addByte((byte)0); //seq number
		buffer.addByte((byte)command);
		buffer.addBytes(val.getBytes());
		buffer.addByte((byte)0);
		this.header = new byte[4];
		sendPacket(buffer);
	}
	
	public void changeUser(String username, String password) throws IOException, NoSuchAlgorithmException {
		this.packetSequence = 0;
		byte pass[] = Security.scramble411(password, new String(this.seed.getBuffer()), null);

		Buffer buffer = new Buffer(0);
		this.header = new byte[4];
		buffer.addBytes(this.header);
		buffer.addByte((byte)17);
		buffer.addBytes(username.getBytes());
		buffer.addByte((byte)0);
		buffer.addBytes(pass);
		buffer.addByte((byte)0);
		buffer.setByte(0, (byte)(buffer.getBuffer().length-4));

		sendPacket(buffer);
		
		this.header = new byte[4];
	}
	
	public void doSelect(int command, String select) throws IOException {
		//SELECT * FROM MATCHUP WHERE GAME_ID=01161;
		int size = select.length();
		Buffer buffer = new Buffer(size+1+PACKET_HEADER);
		buffer.addByte((byte)(size+1));
		buffer.addByte((byte)0);
		buffer.addByte((byte)0);
		buffer.addByte((byte)0); //seq number
		buffer.addByte((byte)command);
		buffer.addBytes(select.getBytes());
		this.header = new byte[4];
		sendPacket(buffer);
	}
	
	public void printProperties() {
		System.out.println("protocol version: "+protocolVersion);
		System.out.println("mysql thread id : "+mysqlID);
		System.out.println("capabilities : "+this.capabilities);
		System.out.println("status : "+this.serverStatus);
		System.out.println("charset :"+this.serverCharSet);
	}
	
	public int getServerCharSet() {
		return this.serverCharSet;
	}
	public Buffer getSeed() {
		return this.seed;
	}
	
	public Buffer getStringBuffer() {
		return this.stringBuffer;
	}
	
	public void close() throws IOException {
		this.input.close();
		this.output.close();
		this.sockectConnection.close();
	}
	
	protected int getSeverStatus() {
		return this.serverStatus;
	}
	
	protected void setServerStatus(int status) {
		this.serverStatus = status;
	}
	
	public void capabilities() {
		System.out.println(Definitions.CLIENT_LONG_PASSWORD & this.capabilities);
		System.out.println(Definitions.CLIENT_FOUND_ROWS & this.capabilities);
		System.out.println(Definitions.CLIENT_LONG_FLAG  & this.capabilities);
		System.out.println(Definitions.CLIENT_CONNECT_WITH_DB & this.capabilities);
		System.out.println(Definitions.CLIENT_COMPRESS & this.capabilities);
		System.out.println(Definitions.CLIENT_ODBC & this.capabilities);
		System.out.println(Definitions.CLIENT_LOCAL_FILES  & this.capabilities);
		System.out.println(Definitions.CLIENT_IGNORE_SPACE & this.capabilities);
		System.out.println(Definitions.CLIENT_PROTOCOL & this.capabilities);
		System.out.println(Definitions.CLIENT_INTERACTIVE & this.capabilities);
		System.out.println(Definitions.CLIENT_SSL  & this.capabilities);
		System.out.println(Definitions.CLIENT_IGNORE_SIGPIPE & this.capabilities);
		System.out.println(Definitions.CLIENT_TRANSACTIONS  & this.capabilities);
		System.out.println(Definitions.CLIENT_SECURE_CONNECTION & this.capabilities);
		System.out.println(Definitions.CLIENT_MULTI_RESULTS & this.capabilities);
		System.out.println(Definitions.CLIENT_REMEMBER_OPTIONS & this.capabilities);
	}
	
	public boolean hasNext() throws IOException {
		if(this.input.available() > 0) {
			return true;
		}else {
			return false;
		}
	}
	
	public boolean databaseIsAssigned() {
		return this.databaseIsAssign;
	}
	
	public String getDatabaseName() {
		return this.databaseName;
	}
}
