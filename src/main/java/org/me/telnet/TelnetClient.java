package org.me.telnet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLHandshakeException;
import org.me.io.PeekableInputStream;

public class TelnetClient extends Telnet {
    private boolean connected = false;
    private int connectTimeout = 60000;
    private int defaultPort = 23;
    private String terminalType = "VT100";
    private boolean sslEnabled = true;
    
    private InputStream negotiatedInput = null;
    private OutputStream negotiatedOutput = null;
    
    public TelnetClient() {
        super();
    }
    
    public TelnetClient(String termtype) {
        super();
        terminalType = termtype;
    }
    
    public void connect(InetAddress host, int port) throws IOException {
        if (sslEnabled) {
            try {
                SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                super.socket = sslFactory.createSocket();
                super.socket.connect(new java.net.InetSocketAddress(host, port), connectTimeout);
                ((SSLSocket) super.socket).startHandshake();
            } catch (SSLHandshakeException e) {
                if (super.socket != null) {
                    try {
                        super.socket.close();
                    } catch (IOException ignored) {}
                }
                super.socket = new Socket();
                super.socket.connect(new java.net.InetSocketAddress(host, port), connectTimeout);
            }
        } else {
            super.socket = new Socket();
            super.socket.connect(new java.net.InetSocketAddress(host, port), connectTimeout);
        }
        _connectAction_();
    }
    
    public void connect(InetAddress host) throws IOException {
        connect(host, defaultPort);
    }
    
    public void connect(String hostname, int port) throws UnknownHostException, IOException {
        connect(InetAddress.getByName(hostname), port);
    }
    
    public void connect(String hostname) throws UnknownHostException, IOException {
        connect(hostname, defaultPort);
    }
    
    @Override
    protected void _connectAction_() throws IOException {
        // Set the parent class's input and output fields
        super.input = new BufferedInputStream(super.socket.getInputStream());
        super.output = new BufferedOutputStream(super.socket.getOutputStream());
        connected = true;
        
        // Create the negotiated streams using the parent's streams
        negotiatedInput = new TelnetInputStream(super.input, this);
        negotiatedOutput = new TelnetOutputStream(super.output, this);
        
        super._connectAction_();
    }
    
    public void disconnect() throws IOException {
        if (super.socket != null) {
            super.socket.close();
        }
        super.socket = null;
        super.input = null;
        super.output = null;
        connected = false;
        negotiatedInput = null;
        negotiatedOutput = null;
    }
    
    public InputStream getInputStream() {
        return negotiatedInput;
    }
    
    public OutputStream getOutputStream() {
        return negotiatedOutput;
    }
    
    public boolean isConnected() {
        return connected && super.socket != null && !super.socket.isClosed();
    }
    
    public void setDefaultPort(int port) {
        defaultPort = port;
    }
    
    public int getDefaultPort() {
        return defaultPort;
    }
    
    public void setConnectTimeout(int timeout) {
        connectTimeout = timeout;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public void setSoTimeout(int timeout) throws SocketException {
        if (super.socket != null) {
            super.socket.setSoTimeout(timeout);
        }
    }
    
    public int getSoTimeout() throws SocketException {
        if (super.socket != null) {
            return super.socket.getSoTimeout();
        }
        return 0;
    }
    
    public void setTcpNoDelay(boolean on) throws SocketException {
        if (super.socket != null) {
            super.socket.setTcpNoDelay(on);
        }
    }
    
    public boolean getTcpNoDelay() throws SocketException {
        if (super.socket != null) {
            return super.socket.getTcpNoDelay();
        }
        return false;
    }
    
    public void setKeepAlive(boolean on) throws SocketException {
        if (super.socket != null) {
            super.socket.setKeepAlive(on);
        }
    }
    
    public boolean getKeepAlive() throws SocketException {
        if (super.socket != null) {
            return super.socket.getKeepAlive();
        }
        return false;
    }
    
    public void setSoLinger(boolean on, int val) throws SocketException {
        if (super.socket != null) {
            super.socket.setSoLinger(on, val);
        }
    }
    
    public int getSoLinger() throws SocketException {
        if (super.socket != null) {
            return super.socket.getSoLinger();
        }
        return -1;
    }
    
    public int getLocalPort() {
        if (super.socket != null) {
            return super.socket.getLocalPort();
        }
        return -1;
    }
    
    public InetAddress getLocalAddress() {
        if (super.socket != null) {
            return super.socket.getLocalAddress();
        }
        return null;
    }
    
    public int getRemotePort() {
        if (super.socket != null) {
            return super.socket.getPort();
        }
        return -1;
    }
    
    public InetAddress getRemoteAddress() {
        if (super.socket != null) {
            return super.socket.getInetAddress();
        }
        return null;
    }
    
    public void sendAYT(int timeout) throws IOException {
        _sendCommand(TelnetCommand.AYT);
    }
    
    public void sendCommand(byte command) throws IOException {
        _sendCommand(command);
    }
    
    public void setTerminalType(String type) {
        terminalType = type;
    }
    
    public String getTerminalType() {
        return terminalType;
    }
    
    public boolean isSslEnabled() {
        return sslEnabled;
    }
    
    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }
    
    public void useSystemProperties(boolean b) {
        System.setProperty("java.net.useSystemProxies", String.valueOf(b));
    }
    
    private class TelnetInputStream extends InputStream {
        private PeekableInputStream wrapped;
        private Telnet telnet;
        private boolean iacMode = false;
        private boolean sbMode = false;
        private int command = 0;
        
        public TelnetInputStream(InputStream is, Telnet tn) {
            wrapped = new PeekableInputStream(is);
            telnet = tn;
        }
        
        @Override
        public int read() throws IOException {
        	byte[] peekByte = new byte[1];
            while (true) {
                if (!wrapped.peek(peekByte, 0, 1)) {
                    return -1;  // EOF
                }
                
                int ch = peekByte[0] & 0xFF;
                
                if (iacMode) {
                    wrapped.read();  // consume the peeked byte
                    if (command == 0) {
                        switch (ch) {
                            case TelnetCommand.WILL:
                            case TelnetCommand.WONT:
                            case TelnetCommand.DO:
                            case TelnetCommand.DONT:
                            case TelnetCommand.SB:
                                command = ch;
                                break;
                            case TelnetCommand.IAC:
                                iacMode = false;
                                return ch;
                            default:
                                telnet._processCommand(ch);
                                iacMode = false;
                                break;
                        }
                    } else {
                        switch (command) {
                            case TelnetCommand.WILL:
                                telnet._processWill(ch);
                                break;
                            case TelnetCommand.WONT:
                                telnet._processWont(ch);
                                break;
                            case TelnetCommand.DO:
                                telnet._processDo(ch);
                                break;
                            case TelnetCommand.DONT:
                                telnet._processDont(ch);
                                break;
                            case TelnetCommand.SB:
                                sbMode = true;
                                subnegotiationCount = 0;
                                subnegotiationBuffer[subnegotiationCount++] = ch;
                                break;
                        }
                        command = 0;
                        iacMode = false;
                    }
                } else if (sbMode) {
                    wrapped.read();  // consume the peeked byte
                    if (ch == TelnetCommand.IAC) {
                        byte[] nextByte = new byte[1];
                        if (wrapped.peek(nextByte, 0, 1)) {
                            int next = nextByte[0] & 0xFF;
                            if (next == TelnetCommand.SE) {
                                wrapped.read();  // consume SE
                                telnet._processSuboption(subnegotiationBuffer, subnegotiationCount);
                                sbMode = false;
                            } else if (next == TelnetCommand.IAC) {
                                wrapped.read();  // consume doubled IAC
                                if (subnegotiationCount < subnegotiationBuffer.length) {
                                    subnegotiationBuffer[subnegotiationCount++] = ch;
                                }
                            }
                        }
                    } else {
                        if (subnegotiationCount < subnegotiationBuffer.length) {
                            subnegotiationBuffer[subnegotiationCount++] = ch;
                        }
                    }
                } else if (ch == TelnetCommand.IAC) {
                    // Peek ahead to see if this is actually a telnet command
                    byte[] nextBytes = new byte[2];
                    if (wrapped.peek(nextBytes, 0, 2)) {  // Peek at current byte and next
                        int next = nextBytes[1] & 0xFF;  // Get the byte after IAC
                        
                        // Check if it's a valid telnet command or negotiation
                        if (next == TelnetCommand.WILL || next == TelnetCommand.WONT ||
                            next == TelnetCommand.DO || next == TelnetCommand.DONT ||
                            next == TelnetCommand.SB || next == TelnetCommand.IAC ||
                            next == TelnetCommand.GA || next == TelnetCommand.EL ||
                            next == TelnetCommand.EC || next == TelnetCommand.AYT ||
                            next == TelnetCommand.AO || next == TelnetCommand.IP ||
                            next == TelnetCommand.BREAK || next == TelnetCommand.DM ||
                            next == TelnetCommand.NOP || next == TelnetCommand.SE) {
                            // This is a telnet command, process it normally
                            wrapped.read();  // consume the IAC
                            iacMode = true;
                        } else {
                            // Not a telnet command (could be IAC,EOR for 3270)
                            // Pass the IAC through as data
                            wrapped.read();  // consume and return the IAC
                            return ch;
                        }
                    } else {
                        // Can't peek ahead, treat as telnet command to be safe
                        wrapped.read();  // consume the IAC
                        iacMode = true;
                    }
                } else {
                    // Normal data byte - consume and return it
                    wrapped.read();
                    return ch;
                }
            }
        }
        
        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            
            int totalBytesRead = 0;
            
            while (totalBytesRead < len) {
                int byteByte = read();
                if (byteByte == -1) {
                    return totalBytesRead == 0 ? -1 : totalBytesRead;
                }
                b[off + totalBytesRead] = (byte) byteByte;
                totalBytesRead++;
                
                // Check if more data is immediately available
                if (totalBytesRead < len && wrapped.available() == 0) {
                    break;
                }
            }
            
            return totalBytesRead;
        }
        
        @Override
        public int available() throws IOException {
            return wrapped.available();
        }
        
        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }
    
    private class TelnetOutputStream extends OutputStream {
        private OutputStream wrapped;
        @SuppressWarnings("unused")
		private Telnet telnet;
        
        public TelnetOutputStream(OutputStream os, Telnet tn) {
            wrapped = os;
            telnet = tn;
        }
        
        @Override
        public void write(int b) throws IOException {
            if (b == TelnetCommand.IAC) {
                wrapped.write(TelnetCommand.IAC);
                wrapped.write(TelnetCommand.IAC);
            } else {
                wrapped.write(b);
            }
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                write(b[off + i] & 0xFF);
            }
        }
        
        @Override
        public void flush() throws IOException {
            wrapped.flush();
        }
        
        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }
}