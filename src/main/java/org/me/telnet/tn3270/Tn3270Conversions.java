package org.me.telnet.tn3270;

import java.util.Arrays;

public abstract class Tn3270Conversions {
	private static final char[] EBCDIC_TO_ASCII_TABLE = new char[256];
	private static final int[] ASCII_TO_EBCDIC_TABLE = new int[256];
	
	private static int[] EBCDIC_ADDRESS_CONVERSION_TABLE = {
            0x40, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7,
            0xC8, 0xC9, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
            0x50, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7,
            0xD8, 0xD9, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
            0x60, 0x61, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7,
            0xE8, 0xE9, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
            0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7,
            0xF8, 0xF9, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F
        };

	static {
		// Initialize with spaces
        Arrays.fill(EBCDIC_TO_ASCII_TABLE, ' '); 
        Arrays.fill(ASCII_TO_EBCDIC_TABLE, 0x40); // Default to space for unsupported characters
        
        // Common EBCDIC to ASCII mappings
        EBCDIC_TO_ASCII_TABLE[0x40] = ' ';	ASCII_TO_EBCDIC_TABLE[' '] = 0x40;
        EBCDIC_TO_ASCII_TABLE[0x4B] = '.';	ASCII_TO_EBCDIC_TABLE['.'] = 0x4B;
        EBCDIC_TO_ASCII_TABLE[0x4C] = '<';	ASCII_TO_EBCDIC_TABLE['<'] = 0x4C;
        EBCDIC_TO_ASCII_TABLE[0x4D] = '(';	ASCII_TO_EBCDIC_TABLE['('] = 0x4D;
        EBCDIC_TO_ASCII_TABLE[0x4E] = '+';	ASCII_TO_EBCDIC_TABLE['+'] = 0x4E;
        EBCDIC_TO_ASCII_TABLE[0x4F] = '|';	ASCII_TO_EBCDIC_TABLE['|'] = 0x4F;
        EBCDIC_TO_ASCII_TABLE[0x50] = '&';	ASCII_TO_EBCDIC_TABLE['&'] = 0x50;
        EBCDIC_TO_ASCII_TABLE[0x5A] = '!';	ASCII_TO_EBCDIC_TABLE['!'] = 0x5A;
        EBCDIC_TO_ASCII_TABLE[0x5B] = '$';	ASCII_TO_EBCDIC_TABLE['$'] = 0x5B;
        EBCDIC_TO_ASCII_TABLE[0x5C] = '*';	ASCII_TO_EBCDIC_TABLE['*'] = 0x5C;
        EBCDIC_TO_ASCII_TABLE[0x5D] = ')';	ASCII_TO_EBCDIC_TABLE[')'] = 0x5D;
        EBCDIC_TO_ASCII_TABLE[0x5E] = ';';	ASCII_TO_EBCDIC_TABLE[';'] = 0x5E;
        EBCDIC_TO_ASCII_TABLE[0x5F] = '~';	ASCII_TO_EBCDIC_TABLE['~'] = 0x5F;
        EBCDIC_TO_ASCII_TABLE[0x60] = '-';	ASCII_TO_EBCDIC_TABLE['-'] = 0x60;
        EBCDIC_TO_ASCII_TABLE[0x61] = '/';	ASCII_TO_EBCDIC_TABLE['/'] = 0x61;
        EBCDIC_TO_ASCII_TABLE[0x6B] = ',';	ASCII_TO_EBCDIC_TABLE[','] = 0x6B;
        EBCDIC_TO_ASCII_TABLE[0x6C] = '%';	ASCII_TO_EBCDIC_TABLE['%'] = 0x6C;
        EBCDIC_TO_ASCII_TABLE[0x6D] = '_';	ASCII_TO_EBCDIC_TABLE['_'] = 0x6D;
        EBCDIC_TO_ASCII_TABLE[0x6E] = '>';	ASCII_TO_EBCDIC_TABLE['>'] = 0x6E;
        EBCDIC_TO_ASCII_TABLE[0x6F] = '?';	ASCII_TO_EBCDIC_TABLE['?'] = 0x6F;
        EBCDIC_TO_ASCII_TABLE[0x79] = '`';	ASCII_TO_EBCDIC_TABLE['`'] = 0x79;
        EBCDIC_TO_ASCII_TABLE[0x7A] = ':';	ASCII_TO_EBCDIC_TABLE[':'] = 0x7A;
        EBCDIC_TO_ASCII_TABLE[0x7B] = '#';	ASCII_TO_EBCDIC_TABLE['#'] = 0x7B;
        EBCDIC_TO_ASCII_TABLE[0x7C] = '@';	ASCII_TO_EBCDIC_TABLE['@'] = 0x7C;
        EBCDIC_TO_ASCII_TABLE[0x7D] = '\'';	ASCII_TO_EBCDIC_TABLE['\''] = 0x7D;
        EBCDIC_TO_ASCII_TABLE[0x7E] = '=';	ASCII_TO_EBCDIC_TABLE['='] = 0x7E;
        EBCDIC_TO_ASCII_TABLE[0x7F] = '"';	ASCII_TO_EBCDIC_TABLE['"'] = 0x7F;
        
        // Lowercase letters
        EBCDIC_TO_ASCII_TABLE[0x79] = '`';	ASCII_TO_EBCDIC_TABLE['`'] = 0x79;
        EBCDIC_TO_ASCII_TABLE[0x81] = 'a';	ASCII_TO_EBCDIC_TABLE['a'] = 0x81;
        EBCDIC_TO_ASCII_TABLE[0x82] = 'b';	ASCII_TO_EBCDIC_TABLE['b'] = 0x82;
        EBCDIC_TO_ASCII_TABLE[0x83] = 'c';	ASCII_TO_EBCDIC_TABLE['c'] = 0x83;
        EBCDIC_TO_ASCII_TABLE[0x84] = 'd';	ASCII_TO_EBCDIC_TABLE['d'] = 0x84;
        EBCDIC_TO_ASCII_TABLE[0x85] = 'e';	ASCII_TO_EBCDIC_TABLE['e'] = 0x85;
        EBCDIC_TO_ASCII_TABLE[0x86] = 'f';	ASCII_TO_EBCDIC_TABLE['f'] = 0x86;
        EBCDIC_TO_ASCII_TABLE[0x87] = 'g';	ASCII_TO_EBCDIC_TABLE['g'] = 0x87;
        EBCDIC_TO_ASCII_TABLE[0x88] = 'h';	ASCII_TO_EBCDIC_TABLE['h'] = 0x88;
        EBCDIC_TO_ASCII_TABLE[0x89] = 'i';	ASCII_TO_EBCDIC_TABLE['i'] = 0x89;
        EBCDIC_TO_ASCII_TABLE[0x91] = 'j';	ASCII_TO_EBCDIC_TABLE['j'] = 0x91;
        EBCDIC_TO_ASCII_TABLE[0x92] = 'k';	ASCII_TO_EBCDIC_TABLE['k'] = 0x92;
        EBCDIC_TO_ASCII_TABLE[0x93] = 'l';	ASCII_TO_EBCDIC_TABLE['l'] = 0x93;
        EBCDIC_TO_ASCII_TABLE[0x94] = 'm';	ASCII_TO_EBCDIC_TABLE['m'] = 0x94;
        EBCDIC_TO_ASCII_TABLE[0x95] = 'n';	ASCII_TO_EBCDIC_TABLE['n'] = 0x95;
        EBCDIC_TO_ASCII_TABLE[0x96] = 'o';	ASCII_TO_EBCDIC_TABLE['o'] = 0x96;	
        EBCDIC_TO_ASCII_TABLE[0x97] = 'p';	ASCII_TO_EBCDIC_TABLE['p'] = 0x97;
        EBCDIC_TO_ASCII_TABLE[0x98] = 'q';	ASCII_TO_EBCDIC_TABLE['q'] = 0x98;	
        EBCDIC_TO_ASCII_TABLE[0x99] = 'r';	ASCII_TO_EBCDIC_TABLE['r'] = 0x99;
        EBCDIC_TO_ASCII_TABLE[0xA2] = 's';	ASCII_TO_EBCDIC_TABLE['s'] = 0xA2;
        EBCDIC_TO_ASCII_TABLE[0xA3] = 't';	ASCII_TO_EBCDIC_TABLE['t'] = 0xA3;
        EBCDIC_TO_ASCII_TABLE[0xA4] = 'u';	ASCII_TO_EBCDIC_TABLE['u'] = 0xA4;
        EBCDIC_TO_ASCII_TABLE[0xA5] = 'v';	ASCII_TO_EBCDIC_TABLE['v'] = 0xA5;
        EBCDIC_TO_ASCII_TABLE[0xA6] = 'w';	ASCII_TO_EBCDIC_TABLE['w'] = 0xA6;
        EBCDIC_TO_ASCII_TABLE[0xA7] = 'x';	ASCII_TO_EBCDIC_TABLE['x'] = 0xA7;
        EBCDIC_TO_ASCII_TABLE[0xA8] = 'y';	ASCII_TO_EBCDIC_TABLE['y'] = 0xA8;
        EBCDIC_TO_ASCII_TABLE[0xA9] = 'z';	ASCII_TO_EBCDIC_TABLE['z'] = 0xA9;
        
        // Uppercase letters
        EBCDIC_TO_ASCII_TABLE[0xC1] = 'A';	ASCII_TO_EBCDIC_TABLE['A'] = 0xC1;
        EBCDIC_TO_ASCII_TABLE[0xC2] = 'B';	ASCII_TO_EBCDIC_TABLE['B'] = 0xC2;
        EBCDIC_TO_ASCII_TABLE[0xC3] = 'C';	ASCII_TO_EBCDIC_TABLE['C'] = 0xC3;
        EBCDIC_TO_ASCII_TABLE[0xC4] = 'D';	ASCII_TO_EBCDIC_TABLE['D'] = 0xC4;
        EBCDIC_TO_ASCII_TABLE[0xC5] = 'E';	ASCII_TO_EBCDIC_TABLE['E'] = 0xC5;
        EBCDIC_TO_ASCII_TABLE[0xC6] = 'F';	ASCII_TO_EBCDIC_TABLE['F'] = 0xC6;
        EBCDIC_TO_ASCII_TABLE[0xC7] = 'G';	ASCII_TO_EBCDIC_TABLE['G'] = 0xC7;
        EBCDIC_TO_ASCII_TABLE[0xC8] = 'H';	ASCII_TO_EBCDIC_TABLE['H'] = 0xC8;
        EBCDIC_TO_ASCII_TABLE[0xC9] = 'I';	ASCII_TO_EBCDIC_TABLE['I'] = 0xC9;
        EBCDIC_TO_ASCII_TABLE[0xD1] = 'J';	ASCII_TO_EBCDIC_TABLE['J'] = 0xD1;
        EBCDIC_TO_ASCII_TABLE[0xD2] = 'K';	ASCII_TO_EBCDIC_TABLE['K'] = 0xD2;
        EBCDIC_TO_ASCII_TABLE[0xD3] = 'L';	ASCII_TO_EBCDIC_TABLE['L'] = 0xD3;
        EBCDIC_TO_ASCII_TABLE[0xD4] = 'M';	ASCII_TO_EBCDIC_TABLE['M'] = 0xD4;
        EBCDIC_TO_ASCII_TABLE[0xD5] = 'N';	ASCII_TO_EBCDIC_TABLE['N'] = 0xD5;
        EBCDIC_TO_ASCII_TABLE[0xD6] = 'O';	ASCII_TO_EBCDIC_TABLE['O'] = 0xD6;
        EBCDIC_TO_ASCII_TABLE[0xD7] = 'P';	ASCII_TO_EBCDIC_TABLE['P'] = 0xD7;
        EBCDIC_TO_ASCII_TABLE[0xD8] = 'Q';	ASCII_TO_EBCDIC_TABLE['Q'] = 0xD8;
        EBCDIC_TO_ASCII_TABLE[0xD9] = 'R';	ASCII_TO_EBCDIC_TABLE['R'] = 0xD9;
        EBCDIC_TO_ASCII_TABLE[0xE2] = 'S';	ASCII_TO_EBCDIC_TABLE['S'] = 0xE2;
        EBCDIC_TO_ASCII_TABLE[0xE3] = 'T';	ASCII_TO_EBCDIC_TABLE['T'] = 0xE3;
        EBCDIC_TO_ASCII_TABLE[0xE4] = 'U';	ASCII_TO_EBCDIC_TABLE['U'] = 0xE4;
        EBCDIC_TO_ASCII_TABLE[0xE5] = 'V';	ASCII_TO_EBCDIC_TABLE['V'] = 0xE5;
        EBCDIC_TO_ASCII_TABLE[0xE6] = 'W';	ASCII_TO_EBCDIC_TABLE['W'] = 0xE6;
        EBCDIC_TO_ASCII_TABLE[0xE7] = 'X';	ASCII_TO_EBCDIC_TABLE['X'] = 0xE7;
        EBCDIC_TO_ASCII_TABLE[0xE8] = 'Y';	ASCII_TO_EBCDIC_TABLE['Y'] = 0xE8;
        EBCDIC_TO_ASCII_TABLE[0xE9] = 'Z';	ASCII_TO_EBCDIC_TABLE['Z'] = 0xE9;
        
        // Numbers
        EBCDIC_TO_ASCII_TABLE[0xF0] = '0';	ASCII_TO_EBCDIC_TABLE['0'] = 0xF0;
        EBCDIC_TO_ASCII_TABLE[0xF1] = '1';	ASCII_TO_EBCDIC_TABLE['1'] = 0xF1;
        EBCDIC_TO_ASCII_TABLE[0xF2] = '2';	ASCII_TO_EBCDIC_TABLE['2'] = 0xF2;
        EBCDIC_TO_ASCII_TABLE[0xF3] = '3';	ASCII_TO_EBCDIC_TABLE['3'] = 0xF3;
        EBCDIC_TO_ASCII_TABLE[0xF4] = '4';	ASCII_TO_EBCDIC_TABLE['4'] = 0xF4;
        EBCDIC_TO_ASCII_TABLE[0xF5] = '5';	ASCII_TO_EBCDIC_TABLE['5'] = 0xF5;
        EBCDIC_TO_ASCII_TABLE[0xF6] = '6';	ASCII_TO_EBCDIC_TABLE['6'] = 0xF6;
        EBCDIC_TO_ASCII_TABLE[0xF7] = '7';	ASCII_TO_EBCDIC_TABLE['7'] = 0xF7;
        EBCDIC_TO_ASCII_TABLE[0xF8] = '8';	ASCII_TO_EBCDIC_TABLE['8'] = 0xF8;
        EBCDIC_TO_ASCII_TABLE[0xF9] = '9';	ASCII_TO_EBCDIC_TABLE['9'] = 0xF9;
	}
	
	
	
	public static byte asciiToEbcdic(char ascii) {
		return (byte) ASCII_TO_EBCDIC_TABLE[ascii];
	}
	
	public static char ebcdicToAscii(byte ebcdic) {
		return EBCDIC_TO_ASCII_TABLE[ebcdic & 0xFF];
	}
	
	public static byte[] encodeAddress(int position) {
        byte[] result = new byte[2];
        
        int high = (position >> 6) & 0x3F;
        int low = position & 0x3F;
        
        high = translateToBufferAddress(high);
        low = translateToBufferAddress(low);
        
        result[0] = (byte) high;
        result[1] = (byte) low;
        
        return result;
    }
    
    private static int translateToBufferAddress(int value) {
        if (value < 0 || value > 0x3F) {
            return 0;
        }
        
        return EBCDIC_ADDRESS_CONVERSION_TABLE[value];
    }
}
