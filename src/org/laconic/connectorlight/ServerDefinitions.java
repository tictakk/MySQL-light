package org.laconic.connectorlight;
/*
 * Matthew Kersey 2017
 */

public class ServerDefinitions {

	public final static int SERVER_STATUS_IN_TRANSIT = 1;
	public final static int SERVER_STATUS_AUTOCOMMIT = 2;
	public final static int SERVER_MORE_RESULTS_EXISTS = 8;
	public final static int SERVER_QUERY_NO_GOOD_INDEX_USED = 16;
	public final static int SERVER_QUERY_NO_INDEX_USED = 32;
	public final static int SERVER_STATUS_CURSOR_EXISTS = 64;
	public final static int SERVER_STATUS_LAST_ROW_SENT = 128;
	public final static int SERVER_STATUS_DB_DROPPED = 1<<8;
	public final static int SERVER_STATUS_NO_BACKSLASH_ESCAPES = 1<<9;
	public final static int SERVER_STATUS_METADATA_CHANGED = 1<<10;
	public final static int SERVER_QUERY_WAS_SLOW = 1<<11;
	public final static int SERVER_PS_OUT_PARAMS = 1<<12;
	public final static int SERVER_STATUS_IN_TRANS_READONLY = 1<<13;
	public final static int SERVER_SESSION_STATE_CHANGED = 1<<14;
	
	public static void printServerStatus(int status) {
		System.out.println(ServerDefinitions.SERVER_MORE_RESULTS_EXISTS & status);
		System.out.println(ServerDefinitions.SERVER_PS_OUT_PARAMS & status);
		System.out.println(ServerDefinitions.SERVER_QUERY_NO_GOOD_INDEX_USED & status);
		System.out.println(ServerDefinitions.SERVER_QUERY_NO_INDEX_USED & status);
		System.out.println(ServerDefinitions.SERVER_QUERY_WAS_SLOW & status);
		System.out.println(ServerDefinitions.SERVER_SESSION_STATE_CHANGED & status);
		System.out.println(ServerDefinitions.SERVER_STATUS_AUTOCOMMIT & status);
		System.out.println(ServerDefinitions.SERVER_STATUS_CURSOR_EXISTS & status);
		System.out.println(ServerDefinitions.SERVER_STATUS_DB_DROPPED & status);
		System.out.println(ServerDefinitions.SERVER_STATUS_IN_TRANS_READONLY & status);
		System.out.println(ServerDefinitions.SERVER_STATUS_IN_TRANSIT & status);
		System.out.println(ServerDefinitions.SERVER_STATUS_LAST_ROW_SENT & status);
		System.out.println(ServerDefinitions.SERVER_STATUS_METADATA_CHANGED & status);
		System.out.println(ServerDefinitions.SERVER_STATUS_NO_BACKSLASH_ESCAPES & status);
	}
}
