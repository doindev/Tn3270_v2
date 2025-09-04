package org.metoo.telnet.tn3270;

import org.metoo.telnet.TelnetClient;
import org.metoo.telnet.TelnetNotificationHandler;
import org.metoo.telnet.TelnetOption;
import org.metoo.telnet.TelnetOptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Tn3270 implements TelnetNotificationHandler {
    private static final byte TN3270E_OPTION = 40;
    private static final byte TERMINAL_TYPE_OPTION = 24;
    private static final byte BINARY_OPTION = 0;
    private static final byte END_OF_RECORD_OPTION = 25;
    
    private String hostname;
    private int port;
    private TelnetClient telnetClient;
    private ScreenBuffer screenBuffer;
    private Screen screen;
    private DataStreamParser parser;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Thread readerThread;
    private boolean connected;
    private String terminalType = "IBM-3278-2-E";
    
    public Tn3270(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.screenBuffer = new ScreenBuffer(24, 80);
        this.screen = new Screen(screenBuffer);
        this.parser = new DataStreamParser(screenBuffer);
        this.telnetClient = new TelnetClient();
        this.connected = false;
    }
    
    public void connect() throws IOException {
        telnetClient.registerNotifHandler(this);
        
        telnetClient.addOptionHandler(new Tn3270OptionHandler());
        telnetClient.addOptionHandler(new TerminalTypeOptionHandler());
        telnetClient.addOptionHandler(new BinaryOptionHandler());
        telnetClient.addOptionHandler(new EndOfRecordOptionHandler());
        
        telnetClient.connect(hostname, port);
        
        inputStream = telnetClient.getInputStream();
        outputStream = telnetClient.getOutputStream();
        
        screen.setOutputStream(outputStream);
        
        connected = true;
        
        startReaderThread();
    }
    
    private void startReaderThread() {
        readerThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            try {
                while (connected && inputStream != null) {
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead > 0) {
                        byte[] data = new byte[bytesRead];
                        System.arraycopy(buffer, 0, data, 0, bytesRead);
                        parser.parse(data);
                    } else if (bytesRead < 0) {
                        break;
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    e.printStackTrace();
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }
    
    public void disconnect() {
        connected = false;
        
        if (readerThread != null) {
            readerThread.interrupt();
        }
        
        try {
            if (telnetClient != null) {
                telnetClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        inputStream = null;
        outputStream = null;
    }
    
    public boolean isConnected() {
        return connected && telnetClient != null && telnetClient.isConnected();
    }
    
    public Screen screen() {
        return screen;
    }
    
    @Override
    public void receivedNegotiation(int negotiation_code, int option_code) {
        String negotiation = "";
        switch (negotiation_code) {
            case RECEIVED_DO:
                negotiation = "DO";
                break;
            case RECEIVED_DONT:
                negotiation = "DONT";
                break;
            case RECEIVED_WILL:
                negotiation = "WILL";
                break;
            case RECEIVED_WONT:
                negotiation = "WONT";
                break;
            case RECEIVED_COMMAND:
                negotiation = "COMMAND";
                break;
        }
        
        if (TelnetOption.isValidOption(option_code)) {
            System.out.println("TN3270 Negotiation: " + negotiation + " " + 
                             TelnetOption.getOption(option_code));
        }
    }
    
    private class Tn3270OptionHandler extends TelnetOptionHandler {
        public Tn3270OptionHandler() {
            super(TN3270E_OPTION, false, true, false, true);
        }
        
        @Override
        public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
            if (suboptionLength > 0 && suboptionData[0] == TN3270E_OPTION) {
                return new int[] { TN3270E_OPTION, 0x02, 0x07, 0x00, 0x00 };
            }
            return null;
        }
    }
    
    private class TerminalTypeOptionHandler extends TelnetOptionHandler {
        private static final int TERMINAL_TYPE_IS = 0;
        private static final int TERMINAL_TYPE_SEND = 1;
        
        public TerminalTypeOptionHandler() {
            super(TERMINAL_TYPE_OPTION, false, true, false, true);
        }
        
        @Override
        public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
            if (suboptionLength > 1 && 
                suboptionData[0] == TERMINAL_TYPE_OPTION &&
                suboptionData[1] == TERMINAL_TYPE_SEND) {
                
                int[] response = new int[terminalType.length() + 2];
                response[0] = TERMINAL_TYPE_OPTION;
                response[1] = TERMINAL_TYPE_IS;
                
                for (int i = 0; i < terminalType.length(); i++) {
                    response[i + 2] = terminalType.charAt(i);
                }
                
                return response;
            }
            return null;
        }
    }
    
    private class BinaryOptionHandler extends TelnetOptionHandler {
        public BinaryOptionHandler() {
            super(BINARY_OPTION, true, true, true, true);
        }
        
        @Override
        public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
            return null;
        }
    }
    
    private class EndOfRecordOptionHandler extends TelnetOptionHandler {
        public EndOfRecordOptionHandler() {
            super(END_OF_RECORD_OPTION, true, true, true, true);
        }
        
        @Override
        public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
            return null;
        }
    }
    
    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }
    
    public String getTerminalType() {
        return terminalType;
    }
}