package org.laconic.connectorlight;
/*
 * Matthew Kersey 2017
 */


public class Buffer {

	static final int MAX_BYTES_TO_DUMP = 512;
	private int bufferLength = 0;
	private byte[] buffer;
	private int pos = 0;
	
	public static final short ERROR_MARKER = 0xFF;
	public static final short EOF = 0xFE;
	public static final short AUT_SWITCH = 0xFE;
	public static final short INFILE = 0xFB;
	public static final short OK = 0x00;
	
	public Buffer(byte[] buf) {
		this.buffer=buf;
		pos=buffer.length;
	}
	
	public Buffer(int size){
		this.buffer = new byte[size];
	}
	
	public byte[] getBuffer() {
		return this.buffer;
	}
	
	public void addBytes(byte[] bytes) {
		if(buffer.length < pos + bytes.length) {
			buffer = copyBuffer(buffer.length+bytes.length);
			for(byte b : bytes) {
				buffer[pos++]=b;
			}
		}else {
			for(int i=0; i<bytes.length; i++) {
				buffer[pos] = bytes[i];
				pos++;
			}
		}
	}
	
	public void addByte(byte b) {
		if(buffer.length >= pos + 1) {
			buffer[pos]=b;
			pos++;
		}else {
			buffer = copyBuffer(pos+1);
			buffer[pos]=b;
			pos++;
		}
	}
	
	private byte[] copyBuffer(int newSize) {
		byte[] temp = new byte[newSize];
		int i = 0;
		for(byte b : buffer) {
			temp[i]=b;
			i++;
			pos=i;
		}
		return temp;
	}
	
	public void printBuffer() {
		for(byte b : buffer) {
			System.out.println(b);
		}
	}
	
	public void clearBuffer() {
		buffer = new byte[buffer.length];
		this.pos=0;
	}
	
	public void writeInt(int i) {
		 buffer[this.pos++] = (byte) (i & 0xff);
		 buffer[this.pos++] = (byte) (i >>> 8);
		 buffer[this.pos++] = (byte) (i >>> 16);
	}
	
	public void setPosition(int i) {
		this.pos=i;
	}
	
	public void setByte(int pos, byte val) {
		this.buffer[pos]=val;
	}
	
	public String readLenecString() {
		//position is where the length byte is located
		int i = 0;
		int length = buffer[pos];
//		System.out.println(length);
		byte[] stringVal = new byte[length];
		while(length > i) {
//			System.out.println(buffer[pos]+ " ");
			stringVal[i] = buffer[++pos];
			i++;
		}
//		System.out.println();
		if(pos<this.buffer.length) {
			pos++;
		}
		return new String(stringVal);
	}
	
	public long readLenecInt() {
		//position is where the length byte is located
		if((buffer[pos] & 0xFF) < 0xFB) {
			return buffer[pos++] & 0xFF;
		}else if((buffer[pos] & 0xFF) == 0xFB) {
			return 0;
		}else if((buffer[pos] & 0xFF) == 0xFC) {
			return ((buffer[pos++] & 0xFF) + ((buffer[pos++] & 0xFF)<<8));
		}else if((buffer[pos] & 0xFF) == 0xFD) {
			return ((buffer[pos++] & 0xFF) + ((buffer[pos++] & 0xFF)<<8) + ((buffer[pos++] & 0xFF)<<16));
		}else if((buffer[pos] & 0xFF) == 0xFE) {
			return ((buffer[pos++] & 0xFF) + ((buffer[pos++] & 0xFF)<<8)+ ((buffer[pos++] & 0xFF)<<16) + ((buffer[pos++] & 0xFF)<<24) + ((buffer[pos++] & 0xFF)<<32) + ((buffer[pos++] & 0xFF)<<40) + ((buffer[pos++] & 0xFF)<<48) + ((buffer[pos++] & 0xFF)<<56) + ((buffer[pos++] & 0xFF)<<64));
		}else {
			return 0;
		}
	}
	
	public int readInt(int size, int shift) {
		int num = 0;
		if(size>0) {
			num = ((buffer[pos++] & 0xFF) << (shift*8)) + readInt(--size,++shift);
		}
		return num;
	}
	
	public int getCurrentPosition() {
		return this.pos;
	}
	
}