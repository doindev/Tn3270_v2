package org.me.telnet;

public final class TelnetCommand {
    public static final int IAC = 255;
    public static final int DONT = 254;
    public static final int DO = 253;
    public static final int WONT = 252;
    public static final int WILL = 251;
    public static final int SB = 250;
    public static final int GA = 249;
    public static final int EL = 248;
    public static final int EC = 247;
    public static final int AYT = 246;
    public static final int AO = 245;
    public static final int IP = 244;
    public static final int BREAK = 243;
    public static final int DM = 242;
    public static final int NOP = 241;
    public static final int SE = 240;
    public static final int EOR = 239;
    public static final int ABORT = 238;
    public static final int SUSP = 237;
    public static final int EOF = 236;
    public static final int SYNCH = 242;
    
    public static final int MAX_COMMAND_VALUE = 255;
    
//    private static final String[] commandNames = {
//        "IAC", "DONT", "DO", "WONT", "WILL", "SB", "GA", "EL", "EC", "AYT",
//        "AO", "IP", "BRK", "DMARK", "NOP", "SE", "EOR", "ABORT", "SUSP", "EOF"
//    };
//    
//    private static final int[] commandCodes = {
//        IAC, DONT, DO, WONT, WILL, SB, GA, EL, EC, AYT,
//        AO, IP, BREAK, DM, NOP, SE, EOR, ABORT, SUSP, EOF
//    };
    
    private TelnetCommand() {
    }
    
    public static String getCommand(int code) {
        return String.valueOf(code);
    }
    
    public static boolean isValidCommand(int code) {
        return code >= SE && code <= IAC;
    }
}