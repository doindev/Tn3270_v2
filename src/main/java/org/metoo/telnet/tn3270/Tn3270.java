package org.metoo.telnet.tn3270;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.metoo.telnet.SimpleOptionHandler;
import org.metoo.telnet.TelnetClient;
import org.metoo.telnet.TelnetNotificationHandler;
import org.metoo.telnet.TelnetOption;
import org.metoo.telnet.TerminalTypeOptionHandler;

public class Tn3270 implements TelnetNotificationHandler {
    
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
        
        telnetClient.addOptionHandler(new TerminalTypeOptionHandler(terminalType, false, false, true, false));
        telnetClient.addOptionHandler(new SimpleOptionHandler(TelnetOption.BINARY, true, true, true, false));
        telnetClient.addOptionHandler(new SimpleOptionHandler(TelnetOption.END_OF_RECORD, true, true, true, false));
        
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
                // Debug output - uncomment if needed
                // System.out.println("TN3270: Reader thread started");
                while (connected && inputStream != null) {
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead > 0) {
                        // System.out.println("TN3270: Read " + bytesRead + " bytes from input stream");
                        byte[] data = new byte[bytesRead];
                        System.arraycopy(buffer, 0, data, 0, bytesRead);
                        parser.parse(data);
                    } else if (bytesRead < 0) {
                        // System.out.println("TN3270: End of stream reached");
                        break;
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    // System.err.println("TN3270: IOException in reader thread");
                    e.printStackTrace();
                }
            }
            // System.out.println("TN3270: Reader thread ended");
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
        
        // Debug output - uncomment if needed
        // if (TelnetOption.isValidOption(option_code)) {
        //     System.out.println("TN3270 Negotiation: " + negotiation + " " + 
        //                      TelnetOption.getOption(option_code));
        // }
    }
    
    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }
    
    public String getTerminalType() {
        return terminalType;
    }
}