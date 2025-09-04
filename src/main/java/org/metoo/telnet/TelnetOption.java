package org.metoo.telnet;

public class TelnetOption {
    public static final int BINARY = 0;
    public static final int ECHO = 1;
    public static final int RCP = 2;
    public static final int SUPPRESS_GO_AHEAD = 3;
    public static final int NAMS = 4;
    public static final int STATUS = 5;
    public static final int TIMING_MARK = 6;
    public static final int RCTE = 7;
    public static final int NAOL = 8;
    public static final int NAOP = 9;
    public static final int NAOCRD = 10;
    public static final int NAOHTS = 11;
    public static final int NAOHTD = 12;
    public static final int NAOFFD = 13;
    public static final int NAOVTS = 14;
    public static final int NAOVTD = 15;
    public static final int NAOLFD = 16;
    public static final int XASCII = 17;
    public static final int LOGOUT = 18;
    public static final int BM = 19;
    public static final int DET = 20;
    public static final int SUPDUP = 21;
    public static final int SUPDUP_OUTPUT = 22;
    public static final int SEND_LOCATION = 23;
    public static final int TERMINAL_TYPE = 24;
    public static final int END_OF_RECORD = 25;
    public static final int TACACS_USER_IDENTIFICATION = 26;
    public static final int OUTPUT_MARKING = 27;
    public static final int TERMINAL_LOCATION_NUMBER = 28;
    public static final int TELNET_3270_REGIME = 29;
    public static final int X3PAD = 30;
    public static final int NEGOTIATE_ABOUT_WINDOW_SIZE = 31;
    public static final int TERMINAL_SPEED = 32;
    public static final int REMOTE_FLOW_CONTROL = 33;
    public static final int LINEMODE = 34;
    public static final int X_DISPLAY_LOCATION = 35;
    public static final int ENVIRONMENT = 36;
    public static final int AUTHENTICATION = 37;
    public static final int ENCRYPTION = 38;
    public static final int NEW_ENVIRONMENT = 39;
    public static final int TN3270E = 40;
    public static final int XAUTH = 41;
    public static final int CHARSET = 42;
    public static final int RSP = 43;
    public static final int COM_PORT_CONTROL = 44;
    public static final int SUPPRESS_LOCAL_ECHO = 45;
    public static final int START_TLS = 46;
    public static final int KERMIT = 47;
    public static final int SEND_URL = 48;
    public static final int FORWARD_X = 49;
    public static final int EXTENDED_ASCII = 255;
    
    public static final int MAX_OPTION_VALUE = 255;
    
    private TelnetOption() {
    }
    
    public static String getOption(int code) {
        switch(code) {
            case BINARY: return "BINARY";
            case ECHO: return "ECHO";
            case SUPPRESS_GO_AHEAD: return "SUPPRESS_GO_AHEAD";
            case STATUS: return "STATUS";
            case TIMING_MARK: return "TIMING_MARK";
            case TERMINAL_TYPE: return "TERMINAL_TYPE";
            case END_OF_RECORD: return "END_OF_RECORD";
            case NEGOTIATE_ABOUT_WINDOW_SIZE: return "NEGOTIATE_ABOUT_WINDOW_SIZE";
            case TERMINAL_SPEED: return "TERMINAL_SPEED";
            case REMOTE_FLOW_CONTROL: return "REMOTE_FLOW_CONTROL";
            case LINEMODE: return "LINEMODE";
            case X_DISPLAY_LOCATION: return "X_DISPLAY_LOCATION";
            case ENVIRONMENT: return "ENVIRONMENT";
            case AUTHENTICATION: return "AUTHENTICATION";
            case ENCRYPTION: return "ENCRYPTION";
            case NEW_ENVIRONMENT: return "NEW_ENVIRONMENT";
            default: return "UNKNOWN_" + code;
        }
    }
    
    public static boolean isValidOption(int code) {
        return code >= 0 && code <= MAX_OPTION_VALUE;
    }
}