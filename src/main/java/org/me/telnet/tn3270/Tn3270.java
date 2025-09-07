package org.me.telnet.tn3270;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.me.io.PeekableInputStream;
import org.me.telnet.SimpleOptionHandler;
import org.me.telnet.TelnetClient;
import org.me.telnet.TelnetNotificationHandler;
import org.me.telnet.TelnetOption;
import org.me.telnet.TerminalTypeOptionHandler;

public class Tn3270 implements TelnetNotificationHandler {
    
    private String hostname;
    private int port;
    private TelnetClient telnetClient;
    private ScreenBuffer screenBuffer;
    private Screen screen;
    private DataStreamParser parser;
    private PeekableInputStream inputStream;
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
        
        telnetClient.addOptionHandler(new TerminalTypeOptionHandler(terminalType, true, true, true, true));
        telnetClient.addOptionHandler(new SimpleOptionHandler(TelnetOption.BINARY, true, true, true, true));
        telnetClient.addOptionHandler(new SimpleOptionHandler(TelnetOption.END_OF_RECORD, true, true, true, true));
        
        telnetClient.connect(hostname, port);
        
        InputStream rawInputStream = telnetClient.getInputStream();
        inputStream = (rawInputStream instanceof PeekableInputStream) 
            ? (PeekableInputStream) rawInputStream 
            : new PeekableInputStream(rawInputStream);
        outputStream = telnetClient.getOutputStream();
        
        screen.setOutputStream(outputStream);
        
        connected = true;
        
        startReaderThread();
    }
    
    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try {
                // Debug output - uncomment if needed
                // System.out.println("TN3270: Reader thread started");
                while (connected && inputStream != null) {
                    // Use PeekableInputStream to process data stream
                	System.out.println("TN3270: parser.parse(inputStream) called");
                	Thread.sleep(1000); // Slight delay to allow data accumulation
                    parser.parse(inputStream);
                    System.out.println("TN3270: parser.parse(inputStream) returned");
                }
                System.out.println("TN3270: Reader thread exiting loop");
            } catch (IOException e) {
            	e.printStackTrace();
                if (connected) {
                    // System.err.println("TN3270: IOException in reader thread");
                    e.printStackTrace();
                }
            } catch(Exception ee) {
            	ee.printStackTrace();
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
//        String negotiation = 
//		switch (negotiation_code) {
//            case RECEIVED_DO -> "DO";
//            case RECEIVED_DONT -> "DONT";
//            case RECEIVED_WILL -> "WILL";
//            case RECEIVED_WONT -> "WONT";
//            default -> "UNKNOWN";
//        };
//        
//        System.out.println("Telnet Negotiation: Received " + negotiation + " for option code " + option_code);
    }
    
    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }
    
    public String getTerminalType() {
        return terminalType;
    }
}
