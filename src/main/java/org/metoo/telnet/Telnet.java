package org.metoo.telnet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Telnet {
    static final boolean debug = false;
    
    private boolean[] doResponse;
    private boolean[] willResponse;
    private boolean[] options;
    
    private static final int TERMINAL_TYPE_OPTION = TelnetOption.TERMINAL_TYPE;
    private static final int NEGOTIATE_WINDOW_SIZE_OPTION = TelnetOption.NEGOTIATE_ABOUT_WINDOW_SIZE;
    private static final int SUBNEGOTIATION_MAX_LENGTH = 512;
    
    private int[] terminalType = null;
    protected int[] subnegotiationBuffer = new int[SUBNEGOTIATION_MAX_LENGTH];
    protected int subnegotiationCount = 0;
    
    private boolean localEcho = true;
    private boolean receivedDo[] = new boolean[256];
    private boolean receivedWill[] = new boolean[256];
    private boolean sentDo[] = new boolean[256];
    private boolean sentWill[] = new boolean[256];
    
    private TelnetNotificationHandler notifHandler = null;
    private List<TelnetOptionHandler> optionHandlers = new ArrayList<TelnetOptionHandler>();
    
    protected Socket socket = null;
    protected BufferedInputStream input = null;
    protected BufferedOutputStream output = null;
    
    public Telnet() {
        doResponse = new boolean[256];
        willResponse = new boolean[256];
        options = new boolean[256];
        
        for (int i = 0; i < 256; i++) {
            doResponse[i] = false;
            willResponse[i] = false;
            options[i] = false;
            receivedDo[i] = false;
            receivedWill[i] = false;
            sentDo[i] = false;
            sentWill[i] = false;
        }
    }
    
    protected void _connectAction_() throws IOException {
        for (int i = 0; i < 256; i++) {
            receivedDo[i] = false;
            receivedWill[i] = false;
            sentDo[i] = false;
            sentWill[i] = false;
        }
        
        _negotiateOptions();
    }
    
    private void _negotiateOptions() throws IOException {
        for (TelnetOptionHandler handler : optionHandlers) {
            if (handler.getInitLocal()) {
                _requestWill(handler.getOptionCode());
            }
            if (handler.getInitRemote()) {
                _requestDo(handler.getOptionCode());
            }
        }
    }
    
    final synchronized void _sendByte(int b) throws IOException {
        if (output != null) {
            output.write(b);
            if (debug) {
                System.err.println("SENT: " + b);
            }
        }
    }
    
    final synchronized void _sendCommand(int command) throws IOException {
        _sendByte(TelnetCommand.IAC);
        _sendByte(command);
    }
    
    final synchronized void _sendWill(int option) throws IOException {
        _sendByte(TelnetCommand.IAC);
        _sendByte(TelnetCommand.WILL);
        _sendByte(option);
        if (output != null) {
            output.flush();
        }
    }
    
    final synchronized void _sendWont(int option) throws IOException {
        _sendByte(TelnetCommand.IAC);
        _sendByte(TelnetCommand.WONT);
        _sendByte(option);
        if (output != null) {
            output.flush();
        }
    }
    
    final synchronized void _sendDo(int option) throws IOException {
        _sendByte(TelnetCommand.IAC);
        _sendByte(TelnetCommand.DO);
        _sendByte(option);
        if (output != null) {
            output.flush();
        }
    }
    
    final synchronized void _sendDont(int option) throws IOException {
        _sendByte(TelnetCommand.IAC);
        _sendByte(TelnetCommand.DONT);
        _sendByte(option);
        if (output != null) {
            output.flush();
        }
    }
    
    final synchronized void _requestWill(int option) throws IOException {
        if (!sentWill[option] || !receivedDo[option]) {
            _sendWill(option);
            sentWill[option] = true;
        }
    }
    
    final synchronized void _requestWont(int option) throws IOException {
        if (sentWill[option] || receivedDo[option]) {
            _sendWont(option);
            sentWill[option] = false;
        }
    }
    
    final synchronized void _requestDo(int option) throws IOException {
        if (!sentDo[option] || !receivedWill[option]) {
            _sendDo(option);
            sentDo[option] = true;
        }
    }
    
    final synchronized void _requestDont(int option) throws IOException {
        if (sentDo[option] || receivedWill[option]) {
            _sendDont(option);
            sentDo[option] = false;
        }
    }
    
    void _processDo(int option) throws IOException {
        if (debug) {
            System.err.println("RECEIVED DO: " + TelnetOption.getOption(option));
        }
        
        boolean acceptOption = false;
        
        if (optionHandlers.size() > 0) {
            for (TelnetOptionHandler handler : optionHandlers) {
                if (handler.getOptionCode() == option) {
                    acceptOption = handler.getAcceptLocal();
                    break;
                }
            }
        }
        
        if (!receivedDo[option]) {
            receivedDo[option] = true;
            if (acceptOption) {
                _sendWill(option);
                sentWill[option] = true;
            } else {
                _sendWont(option);
                sentWill[option] = false;
            }
        }
        
        if (notifHandler != null) {
            notifHandler.receivedNegotiation(TelnetNotificationHandler.RECEIVED_DO, option);
        }
    }
    
    void _processDont(int option) throws IOException {
        if (debug) {
            System.err.println("RECEIVED DONT: " + TelnetOption.getOption(option));
        }
        
        if (receivedDo[option]) {
            receivedDo[option] = false;
            _sendWont(option);
            sentWill[option] = false;
        }
        
        if (notifHandler != null) {
            notifHandler.receivedNegotiation(TelnetNotificationHandler.RECEIVED_DONT, option);
        }
    }
    
    void _processWill(int option) throws IOException {
        if (debug) {
            System.err.println("RECEIVED WILL: " + TelnetOption.getOption(option));
        }
        
        boolean acceptOption = false;
        
        if (optionHandlers.size() > 0) {
            for (TelnetOptionHandler handler : optionHandlers) {
                if (handler.getOptionCode() == option) {
                    acceptOption = handler.getAcceptRemote();
                    break;
                }
            }
        }
        
        if (!receivedWill[option]) {
            receivedWill[option] = true;
            if (acceptOption) {
                _sendDo(option);
                sentDo[option] = true;
            } else {
                _sendDont(option);
                sentDo[option] = false;
            }
        }
        
        if (notifHandler != null) {
            notifHandler.receivedNegotiation(TelnetNotificationHandler.RECEIVED_WILL, option);
        }
    }
    
    void _processWont(int option) throws IOException {
        if (debug) {
            System.err.println("RECEIVED WONT: " + TelnetOption.getOption(option));
        }
        
        if (receivedWill[option]) {
            receivedWill[option] = false;
            _sendDont(option);
            sentDo[option] = false;
        }
        
        if (notifHandler != null) {
            notifHandler.receivedNegotiation(TelnetNotificationHandler.RECEIVED_WONT, option);
        }
    }
    
    void _processSuboption(int[] suboption, int suboptionLength) throws IOException {
        if (suboptionLength > 0) {
            for (TelnetOptionHandler handler : optionHandlers) {
                if (handler.getOptionCode() == suboption[0]) {
                    int[] response = handler.answerSubnegotiation(suboption, suboptionLength);
                    if (response != null) {
                        _sendSubnegotiation(response);
                    }
                    break;
                }
            }
        }
    }
    
    final synchronized void _sendSubnegotiation(int[] subnegotiation) throws IOException {
        if (subnegotiation != null) {
            _sendByte(TelnetCommand.IAC);
            _sendByte(TelnetCommand.SB);
            for (int i = 0; i < subnegotiation.length; i++) {
                int b = subnegotiation[i];
                _sendByte(b);
                if (b == TelnetCommand.IAC) {
                    _sendByte(b);
                }
            }
            _sendByte(TelnetCommand.IAC);
            _sendByte(TelnetCommand.SE);
            if (output != null) {
                output.flush();
            }
        }
    }
    
    void _processCommand(int command) {
        if (debug) {
            System.err.println("RECEIVED COMMAND: " + command);
        }
        
        if (notifHandler != null) {
            notifHandler.receivedNegotiation(TelnetNotificationHandler.RECEIVED_COMMAND, command);
        }
    }
    
    public void addOptionHandler(TelnetOptionHandler opthand) throws IOException {
        optionHandlers.add(opthand);
    }
    
    public void deleteOptionHandler(int optcode) {
        for (int i = 0; i < optionHandlers.size(); i++) {
            if (optionHandlers.get(i).getOptionCode() == optcode) {
                optionHandlers.remove(i);
                break;
            }
        }
    }
    
    public void registerNotifHandler(TelnetNotificationHandler notifhand) {
        notifHandler = notifhand;
    }
    
    public void unregisterNotifHandler() {
        notifHandler = null;
    }
}