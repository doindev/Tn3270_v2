package org.metoo.telnet;

public interface TelnetNotificationHandler {
    public static final int RECEIVED_DO = 1;
    public static final int RECEIVED_DONT = 2;
    public static final int RECEIVED_WILL = 3;
    public static final int RECEIVED_WONT = 4;
    public static final int RECEIVED_COMMAND = 5;
    
    void receivedNegotiation(int negotiation_code, int option_code);
}