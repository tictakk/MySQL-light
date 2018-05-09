package org.laconic.connectorlight;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class Client {
	
	private static Map<Byte, Character> ASCII = new HashMap<Byte, Character>();
	private static Map<Character, Byte> ASC = new HashMap<Character, Byte>();
	
	public static void main(String args[]) throws UnknownHostException, IOException, NoSuchAlgorithmException, SQLException {
		createASCIItable();
		createASCtable();
		
//		Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/CFB?user=root&password=root");
//		
//		Statement st = con.createStatement();
//		ResultSet rs = st.executeQuery("SELECT * FROM TEAMS");
//		if(rs.next()) {
//			System.out.println(rs.getString(1));
//		}
//		con.close();
		
		System.out.println(ASCII.get((byte)65));
	}
	
	private static void createASCIItable() {
		ASCII.put((byte)32, ' ');ASCII.put((byte)33, '!');ASCII.put((byte)34, '"');
		ASCII.put((byte)35, '#');ASCII.put((byte)36, '$');ASCII.put((byte)37, '%');
		ASCII.put((byte)38, '&');ASCII.put((byte)39, '\'');ASCII.put((byte)40, '(');
		ASCII.put((byte)41, ')');ASCII.put((byte)42, '*');ASCII.put((byte)43, '+');
		ASCII.put((byte)44, ',');ASCII.put((byte)45, '-');ASCII.put((byte)46, '.');
		ASCII.put((byte)47, '/');ASCII.put((byte)48, '0');ASCII.put((byte)49, '1');
		ASCII.put((byte)50, '2');ASCII.put((byte)51, '3');ASCII.put((byte)52, '4');
		ASCII.put((byte)53, '5');ASCII.put((byte)54, '6');ASCII.put((byte)55, '7');
		ASCII.put((byte)56, '8');ASCII.put((byte)57, '9');ASCII.put((byte)58, ':');
		ASCII.put((byte)59, ';');ASCII.put((byte)60, '<');ASCII.put((byte)61, '=');
		ASCII.put((byte)62, '>');ASCII.put((byte)63, '?');ASCII.put((byte)64, '@');
		ASCII.put((byte)65, 'A');ASCII.put((byte)66, 'B');ASCII.put((byte)67, 'C');
		ASCII.put((byte)68, 'D');ASCII.put((byte)69, 'E');ASCII.put((byte)70, 'F');
		ASCII.put((byte)71, 'G');ASCII.put((byte)72, 'H');ASCII.put((byte)73, 'I');
		ASCII.put((byte)74, 'J');ASCII.put((byte)75, 'K');ASCII.put((byte)76, 'L');
		ASCII.put((byte)77, 'M');ASCII.put((byte)78, 'N');ASCII.put((byte)79, 'O');
		ASCII.put((byte)80, 'P');ASCII.put((byte)81, 'Q');ASCII.put((byte)82, 'R');
		ASCII.put((byte)83, 'S');ASCII.put((byte)84, 'T');ASCII.put((byte)85, 'U');
		ASCII.put((byte)86, 'V');ASCII.put((byte)87, 'W');ASCII.put((byte)88, 'X');
		ASCII.put((byte)89, 'Y');ASCII.put((byte)90, 'Z');ASCII.put((byte)91, '[');
		ASCII.put((byte)92, '\\');ASCII.put((byte)93, ']');ASCII.put((byte)94, '^');
		ASCII.put((byte)95, '_');ASCII.put((byte)96, '`');ASCII.put((byte)97, 'a');
		ASCII.put((byte)98, 'b');ASCII.put((byte)99, 'c');ASCII.put((byte)100, 'd');
		ASCII.put((byte)101, 'e');ASCII.put((byte)102, 'f');ASCII.put((byte)103, 'g');
		ASCII.put((byte)104, 'h');ASCII.put((byte)105, 'i');ASCII.put((byte)106, 'j');
		ASCII.put((byte)107, 'k');ASCII.put((byte)108, 'l');ASCII.put((byte)109, 'm');
		ASCII.put((byte)110, 'n');ASCII.put((byte)111, 'o');ASCII.put((byte)112, 'p');
		ASCII.put((byte)113, 'q');ASCII.put((byte)114, 'r');ASCII.put((byte)115, 's');
		ASCII.put((byte)116, 't');ASCII.put((byte)117, 'u');ASCII.put((byte)118, 'v');
		ASCII.put((byte)119, 'w');ASCII.put((byte)120, 'x');ASCII.put((byte) 121, 'y');
		ASCII.put((byte)122, 'z');ASCII.put((byte)123, '{');ASCII.put((byte)124, '|');
		ASCII.put((byte)125, '}');ASCII.put((byte)126, '~');
	}
	
	private static void createASCtable() {
		ASC.put(' ',(byte)32);ASC.put('!',(byte)33);ASC.put('"',(byte)34);
		ASC.put('#',(byte)35);ASC.put('$',(byte)36);ASC.put('%',(byte)37);
		ASC.put('&',(byte)38);ASC.put('\'',(byte)39);ASC.put('(',(byte)40);
		ASC.put(')',(byte)41);ASC.put('*',(byte)42);ASC.put('+',(byte)43);
		ASC.put(',',(byte)44);ASC.put('-',(byte)45);ASC.put('.',(byte)46);
		ASC.put('/',(byte)47);ASC.put('0',(byte)48);ASC.put('1',(byte)48);
		ASC.put('2',(byte)50);ASC.put('3',(byte)51);ASC.put('4',(byte)52);
		ASC.put('5',(byte)53);ASC.put('6',(byte)54);ASC.put('7',(byte)55);
		ASC.put('8',(byte)56);ASC.put('9',(byte)57);ASC.put(':',(byte)58);
		ASC.put(';',(byte)59);ASC.put('<',(byte)60);ASC.put('=',(byte)61);
		ASC.put('>',(byte)62);ASC.put('?',(byte)63);ASC.put('@',(byte)64);
		ASC.put('A',(byte)65);ASC.put('B',(byte)66);ASC.put('C',(byte)67);
		ASC.put('D',(byte)68);ASC.put('E',(byte)69);ASC.put('F',(byte)70);
		ASC.put('G',(byte)71);ASC.put('H',(byte)72);ASC.put('I',(byte)73);
		ASC.put('J',(byte)74);ASC.put('K',(byte)75);ASC.put('L',(byte)76);
		ASC.put('M',(byte)77);ASC.put('N',(byte)78);ASC.put('O',(byte)79);
		ASC.put('P',(byte)80);ASC.put('Q',(byte)81);ASC.put('R',(byte)82);
		ASC.put('S',(byte)83);ASC.put('T',(byte)84);ASC.put('U',(byte)85);
		ASC.put('V',(byte)86);ASC.put('W',(byte)87);ASC.put('X',(byte)88);
		ASC.put('Y',(byte)89);ASC.put('Z',(byte)90);ASC.put('[',(byte)91);
		ASC.put('\\',(byte)92);ASC.put(']',(byte)93);ASC.put('^',(byte)94);
		ASC.put('_',(byte)95);ASC.put('`',(byte)96);ASC.put('a',(byte)97);
		ASC.put('b',(byte)98);ASC.put('c',(byte)99);ASC.put('d',(byte)100);
		ASC.put('e',(byte)101);ASC.put('f',(byte)102);ASC.put('g',(byte)103);
		ASC.put('h',(byte)104);ASC.put('i',(byte)105);ASC.put('j',(byte)106);
		ASC.put('k',(byte)107);ASC.put('l',(byte)108);ASC.put('m',(byte)109);
		ASC.put('n',(byte)110);ASC.put('o',(byte)111);ASC.put('p',(byte)112);
		ASC.put('q',(byte)113);ASC.put('r',(byte)114);ASC.put('s',(byte)115);
		ASC.put('t',(byte)116);ASC.put('u',(byte)117);ASC.put('v',(byte)118);
		ASC.put('w',(byte)119);ASC.put('x',(byte)120);ASC.put('y',(byte)121);
		ASC.put('z',(byte)122);ASC.put('{',(byte)123);ASC.put('|',(byte)124);
		ASC.put('}',(byte)125);ASC.put('~',(byte)126);
	}
}
